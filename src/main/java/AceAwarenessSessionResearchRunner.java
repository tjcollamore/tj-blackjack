import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class AceAwarenessSessionResearchRunner
{
  private static final int SESSIONS =
      500_000;

  private static final int HANDS_PER_SESSION =
      160;

  private static final double STARTING_BANKROLL =
      500.0;

  private static final double MAIN_BET =
      25.0;

  private static final double SUPER4_BET =
      5.0;

  private static final double ACCEPTABLE_BANKROLL =
      400.0;

  private static final long BASE_SEED =
      10_24_2026L;

  private static final double Z_95 =
      1.96;


  // ------------------------------------------------------------
  // Representative Encore-style practical Super 4 payouts.
  //
  // Royals deliberately receive NO giant progressive value.
  // They are treated as the underlying J-Q-K-A straight ($400)
  // and separately counted.
  // ------------------------------------------------------------

  private static final double PAY_3K =
      800.0;

  private static final double PAY_STRAIGHT =
      400.0;

  private static final double PAY_FLUSH =
      300.0;

  private static final double PAY_TWO_PAIR =
      200.0;

  private static final double PAY_SAME_COLOR =
      100.0;

  private static final double PAY_PAIR =
      50.0;

  private static final double PAY_OTHER_BJ =
      25.0;

  private static final double PAY_ACE_NO_BJ =
      10.0;


  private enum Policy
  {
    FLAT_BASIC(
        false,
        Double.POSITIVE_INFINITY
    ),

    ACE_SWITCH_ONLY(
        true,
        Double.POSITIVE_INFINITY
    ),

    ACE_SWITCH_SUPER4_5(
        true,
        5.0
    ),

    ACE_SWITCH_SUPER4_5_5(
        true,
        5.5
    ),

    ACE_SWITCH_SUPER4_6(
        true,
        6.0
    );


    private final boolean switchTables;

    private final double super4Threshold;


    Policy(
        boolean switchTables,
        double super4Threshold)
    {
      this.switchTables =
          switchTables;

      this.super4Threshold =
          super4Threshold;
    }


    boolean usesTableSwitching()
    {
      return switchTables;
    }


    boolean usesSuper4()
    {
      return Double.isFinite(
          super4Threshold
      );
    }


    double getSuper4Threshold()
    {
      return super4Threshold;
    }
  }


  public static void main(
      String[] args)
  {
    GameSettings settings =
        createResearchSettings();


    Policy[] policies =
        Policy.values();


    PolicyAccumulator[] accumulators =
        new PolicyAccumulator[
            policies.length
        ];


    for (int i = 0;
         i < policies.length;
         i++)
    {
      accumulators[i] =
          new PolicyAccumulator(
              policies[i]
          );
    }


    PairAccumulator[] vsFlat =
        new PairAccumulator[
            policies.length
        ];


    PairAccumulator[] vsSwitchOnly =
        new PairAccumulator[
            policies.length
        ];


    for (int i = 1;
         i < policies.length;
         i++)
    {
      vsFlat[i] =
          new PairAccumulator(
              policies[i],
              Policy.FLAT_BASIC
          );
    }


    for (int i = 2;
         i < policies.length;
         i++)
    {
      vsSwitchOnly[i] =
          new PairAccumulator(
              policies[i],
              Policy.ACE_SWITCH_ONLY
          );
    }


    for (int session = 1;
         session <= SESSIONS;
         session++)
    {
      long seed =
          BASE_SEED
          + session;


      SessionState[] states =
          new SessionState[
              policies.length
          ];


      for (int i = 0;
           i < policies.length;
           i++)
      {
        states[i] =
            playSession(
                settings,
                policies[i],
                seed
            );


        accumulators[i].add(
            states[i]
        );
      }


      SessionState flat =
          states[
              Policy.FLAT_BASIC.ordinal()
          ];


      SessionState switchOnly =
          states[
              Policy.ACE_SWITCH_ONLY.ordinal()
          ];


      for (int i = 1;
           i < policies.length;
           i++)
      {
        vsFlat[i].add(
            states[i],
            flat
        );
      }


      for (int i = 2;
           i < policies.length;
           i++)
      {
        vsSwitchOnly[i].add(
            states[i],
            switchOnly
        );
      }


      if (session % 25_000
          == 0)
      {
        System.out.println(
            "Completed "
            + String.format(
                Locale.US,
                "%,d",
                session
            )
            + " paired sessions..."
        );
      }
    }


    System.out.println();

    System.out.println(
        "========================================"
    );

    System.out.println(
        "ACE AWARENESS FULL SESSION STUDY"
    );

    System.out.println(
        "========================================"
    );


    for (PolicyAccumulator accumulator :
         accumulators)
    {
      printPolicySummary(
          accumulator.finish()
      );
    }


    System.out.println();

    System.out.println(
        "========================================"
    );

    System.out.println(
        "VS FLAT BASIC"
    );

    System.out.println(
        "========================================"
    );


    for (int i = 1;
         i < policies.length;
         i++)
    {
      printPairSummary(
          vsFlat[i].finish()
      );
    }


    System.out.println();

    System.out.println(
        "========================================"
    );

    System.out.println(
        "SUPER 4 VALUE ABOVE SWITCH-ONLY"
    );

    System.out.println(
        "========================================"
    );


    for (int i = 2;
         i < policies.length;
         i++)
    {
      printPairSummary(
          vsSwitchOnly[i].finish()
      );
    }
  }


  // ============================================================
  // SESSION
  // ============================================================

  private static SessionState playSession(
      GameSettings settings,
      Policy policy,
      long seed)
  {
    double[] bankroll =
    {
        STARTING_BANKROLL
    };


    PlayerRoundEngine playerRoundEngine =
        new PlayerRoundEngine(
            settings
        );


    DealerPlayEngine dealerPlayEngine =
        new DealerPlayEngine(
            settings
        );


    HandStrategy strategy =
        new BasicStrategy();


    AceTracker tracker =
        new AceTracker();


    Shoe shoe =
        new Shoe(
            settings.getDecksInShoe(),
            seed
        );


    boolean midpointChecked =
        false;


    int replacementShoeNumber =
        0;


    int handsPlayed =
        0;


    int tableSwitches =
        0;


    int midpointChecks =
        0;


    int super4Bets =
        0;


    int super4Wins =
        0;


    int royalHits =
        0;


    double super4Profit =
        0.0;


    double totalMainExposure =
        0.0;


    /*
     * Tracks what the night felt like:
     * number of times the +threshold
     * was actually available.
     */
    int super4Opportunities =
        0;


    for (int handNumber = 0;
         handNumber < HANDS_PER_SESSION;
         handNumber++)
    {
      if (bankroll[0]
          < MAIN_BET)
      {
        break;
      }


      // --------------------------------------------------------
      // NORMAL SHUFFLE
      // --------------------------------------------------------

      if (shoe.needsShuffle())
      {
        shoe.shuffle();

        tracker.reset();

        midpointChecked =
            false;
      }


      // --------------------------------------------------------
      // MIDPOINT TABLE-SWITCH DECISION
      // --------------------------------------------------------

      if (policy.usesTableSwitching()
          && !midpointChecked
          && shoe.getRemainingCardCount()
              <= 4 * 52)
      {
        midpointChecked =
            true;

        midpointChecks++;


        /*
         * Negative Ace surplus:
         *
         * more Aces have already appeared
         * than expected -> remaining shoe
         * relatively Ace-poor.
         */
        if (tracker.getAceSurplus()
            < 0.0)
        {
          tableSwitches++;


          replacementShoeNumber++;


          long replacementSeed =
              seed
              + 1_000_000_000L
              + replacementShoeNumber
                * 1_000_003L;


          shoe =
              new Shoe(
                  settings.getDecksInShoe(),
                  replacementSeed
              );


          tracker.reset();


          midpointChecked =
              false;
        }
      }


      // --------------------------------------------------------
      // PRE-DEAL SUPER 4 DECISION
      // --------------------------------------------------------

      boolean playSuper4 =
          false;


      if (policy.usesSuper4())
      {
        double surplus =
            tracker.getAceSurplus();


        if (surplus
            >= policy.getSuper4Threshold())
        {
          super4Opportunities++;


          /*
           * Need enough bankroll to place
           * both the $25 main wager and the
           * $5 side bet.
           */
          if (bankroll[0]
              >= MAIN_BET
                 + SUPER4_BET)
          {
            playSuper4 =
                true;


            bankroll[0] -=
                SUPER4_BET;


            super4Profit -=
                SUPER4_BET;


            super4Bets++;
          }
        }
      }


      // --------------------------------------------------------
      // MAIN ROUND + OPTIONAL SIDE BET
      // --------------------------------------------------------

      RoundResult result =
          playRound(
              settings,
              playerRoundEngine,
              dealerPlayEngine,
              shoe,
              strategy,
              tracker,
              bankroll,
              playSuper4
          );


      totalMainExposure +=
          result.mainExposure();


      if (playSuper4)
      {
        double payout =
            result.super4Payout();


        bankroll[0] +=
            payout;


        super4Profit +=
            payout;


        if (payout > 0.0)
        {
          super4Wins++;
        }


        if (result.royal())
        {
          royalHits++;
        }
      }


      handsPlayed++;
    }


    return new SessionState(
        policy,

        bankroll[0],

        handsPlayed,

        bankroll[0]
            < MAIN_BET,

        bankroll[0]
            >= ACCEPTABLE_BANKROLL,

        bankroll[0]
            > STARTING_BANKROLL,

        totalMainExposure,

        tableSwitches,

        midpointChecks,

        super4Opportunities,

        super4Bets,

        super4Wins,

        royalHits,

        super4Profit
    );
  }


  // ============================================================
  // ROUND
  // ============================================================

  private static RoundResult playRound(
      GameSettings settings,
      PlayerRoundEngine playerRoundEngine,
      DealerPlayEngine dealerPlayEngine,
      Shoe shoe,
      HandStrategy strategy,
      AceTracker tracker,
      double[] bankroll,
      boolean evaluateSuper4)
  {
    bankroll[0] -=
        MAIN_BET;


    double[] mainExposure =
    {
        MAIN_BET
    };


    Map<Card, Boolean> observed =
        new IdentityHashMap<>();


    Hand playerHand =
        new Hand();


    Hand dealerHand =
        new Hand();


    Card player1 =
        shoe.drawCard();


    playerHand.addCard(
        player1
    );


    observeVisible(
        tracker,
        observed,
        player1
    );


    Card dealerUp =
        shoe.drawCard();


    dealerHand.addCard(
        dealerUp
    );


    observeVisible(
        tracker,
        observed,
        dealerUp
    );


    Card player2 =
        shoe.drawCard();


    playerHand.addCard(
        player2
    );


    observeVisible(
        tracker,
        observed,
        player2
    );


    Card dealerHole =
        shoe.drawCard();


    dealerHand.addCard(
        dealerHole
    );


    boolean playerBlackjack =
        playerHand.isBlackjack();


    boolean dealerBlackjack =
        dealerHand.isBlackjack();


    Super4Result super4 =
        evaluateSuper4
            ? evaluateSuper4(
                player1,
                player2,
                dealerUp,
                dealerHole,
                dealerBlackjack
            )
            : new Super4Result(
                0.0,
                false
            );


    // ----------------------------------------------------------
    // DEALER BLACKJACK
    // ----------------------------------------------------------

    if (dealerBlackjack)
    {
      observeVisible(
          tracker,
          observed,
          dealerHole
      );


      if (playerBlackjack)
      {
        bankroll[0] +=
            MAIN_BET;
      }


      return new RoundResult(
          mainExposure[0],
          super4.payout(),
          super4.royal()
      );
    }


    // ----------------------------------------------------------
    // PLAYER BLACKJACK
    // ----------------------------------------------------------

    if (playerBlackjack)
    {
      bankroll[0] +=
          MAIN_BET
          * settings
              .getBlackjackPayout()
              .getTotalReturnMultiplier();


      /*
       * Dealer hole remains unseen.
       */
      return new RoundResult(
          mainExposure[0],
          super4.payout(),
          super4.royal()
      );
    }


    // ----------------------------------------------------------
    // PLAYER PLAY
    // ----------------------------------------------------------

    PlayerHandState initialState =
        new PlayerHandState(
            playerHand
        );


    List<PlayerHandState> states =
        playerRoundEngine.play(
            initialState,
            dealerUp,
            strategy,
            shoe,
            new PlayerRoundListener()
            {
              @Override
              public boolean canDoubleOrSplit(
                  PlayerHandState state)
              {
                return bankroll[0]
                    >= MAIN_BET;
              }


              @Override
              public double getWagerAmount(
                  PlayerHandState state)
              {
                return MAIN_BET;
              }


              @Override
              public void handStarted(
                  PlayerHandState state,
                  int index,
                  int handCount)
              {
              }


              @Override
              public void cardDrawn(
                  PlayerHandState state,
                  Card card)
              {
                observeVisible(
                    tracker,
                    observed,
                    card
                );
              }


              @Override
              public void handDoubled(
                  PlayerHandState state)
              {
                bankroll[0] -=
                    MAIN_BET;


                mainExposure[0] +=
                    MAIN_BET;
              }


              @Override
              public void handSurrendered(
                  PlayerHandState state)
              {
              }


              @Override
              public void handSplit(
                  PlayerHandState originalState,
                  PlayerHandState newState,
                  HandSplitResult splitResult)
              {
                bankroll[0] -=
                    MAIN_BET;


                mainExposure[0] +=
                    MAIN_BET;


                observeHand(
                    tracker,
                    observed,
                    originalState.getHand()
                );


                observeHand(
                    tracker,
                    observed,
                    newState.getHand()
                );
              }


              @Override
              public void handFinished(
                  PlayerHandState state,
                  int index,
                  int handCount,
                  HandFinishReason reason)
              {
              }
            }
        );


    // ----------------------------------------------------------
    // DEALER PLAY
    // ----------------------------------------------------------

    boolean dealerNeedsToPlay =
        false;


    for (PlayerHandState state :
         states)
    {
      if (!state.getHand().isBusted()
          && !state.didSurrender())
      {
        dealerNeedsToPlay =
            true;

        break;
      }
    }


    if (dealerNeedsToPlay)
    {
      observeVisible(
          tracker,
          observed,
          dealerHole
      );


      dealerPlayEngine.play(
          dealerHand,
          shoe,
          card ->
              observeVisible(
                  tracker,
                  observed,
                  card
              )
      );
    }


    // ----------------------------------------------------------
    // MAIN-GAME SETTLEMENT
    // ----------------------------------------------------------

    for (PlayerHandState state :
         states)
    {
      HandOutcome outcome =
          HandOutcomeAnalyzer.analyze(
              state.getHand(),
              dealerHand,
              false,
              state.didSurrender()
          );


      double wager =
          MAIN_BET
          * state.getWagerMultiplier();


      switch (outcome)
      {
        case WIN:
          bankroll[0] +=
              wager * 2.0;
          break;


        case LOSS:
          break;


        case PUSH:
          bankroll[0] +=
              wager;
          break;


        case SURRENDER:
          bankroll[0] +=
              MAIN_BET * 0.5;
          break;


        case BLACKJACK:
          throw new IllegalStateException(
              "Natural blackjack should "
              + "already be resolved"
          );


        default:
          throw new IllegalStateException(
              "Unexpected outcome: "
              + outcome
          );
      }
    }


    return new RoundResult(
        mainExposure[0],
        super4.payout(),
        super4.royal()
    );
  }


  // ============================================================
  // PRACTICAL SUPER 4 MODEL
  // ============================================================

  private static Super4Result evaluateSuper4(
      Card player1,
      Card player2,
      Card dealerUp,
      Card dealerHole,
      boolean dealerBlackjack)
  {
    if (!dealerBlackjack)
    {
      if (dealerUp.getRank()
          == Rank.ACE)
      {
        return new Super4Result(
            PAY_ACE_NO_BJ,
            false
        );
      }


      return new Super4Result(
          0.0,
          false
      );
    }


    Card[] cards =
    {
        player1,
        player2,
        dealerUp,
        dealerHole
    };


    /*
     * Royal is tracked, but intentionally
     * receives only the underlying straight
     * value for practical-EV research.
     */
    if (isRoyal(cards))
    {
      return new Super4Result(
          PAY_STRAIGHT,
          true
      );
    }


    int maxRankCount =
        maxRankCount(
            cards
        );


    /*
     * User confirms Encore does not show a
     * separate 4-of-a-kind award.
     *
     * Treat 4 of a kind as satisfying the
     * 3-of-a-kind category for this practical
     * model.
     */
    if (maxRankCount >= 3)
    {
      return new Super4Result(
          PAY_3K,
          false
      );
    }


    if (isStraight(cards))
    {
      return new Super4Result(
          PAY_STRAIGHT,
          false
      );
    }


    if (isFlush(cards))
    {
      return new Super4Result(
          PAY_FLUSH,
          false
      );
    }


    if (isTwoPair(cards))
    {
      return new Super4Result(
          PAY_TWO_PAIR,
          false
      );
    }


    if (isSameColor(cards))
    {
      return new Super4Result(
          PAY_SAME_COLOR,
          false
      );
    }


    if (maxRankCount >= 2)
    {
      return new Super4Result(
          PAY_PAIR,
          false
      );
    }


    return new Super4Result(
        PAY_OTHER_BJ,
        false
    );
  }


  private static boolean isRoyal(
      Card[] cards)
  {
    if (!isFlush(cards))
    {
      return false;
    }


    boolean ace =
        false;

    boolean king =
        false;

    boolean queen =
        false;

    boolean jack =
        false;


    for (Card card :
         cards)
    {
      switch (card.getRank())
      {
        case ACE:
          ace = true;
          break;

        case KING:
          king = true;
          break;

        case QUEEN:
          queen = true;
          break;

        case JACK:
          jack = true;
          break;

        default:
          return false;
      }
    }


    return ace
        && king
        && queen
        && jack;
  }


  private static int maxRankCount(
      Card[] cards)
  {
    Map<Rank, Integer> counts =
        new EnumMap<>(
            Rank.class
        );


    int max =
        0;


    for (Card card :
         cards)
    {
      Rank rank =
          card.getRank();


      int count =
          counts.getOrDefault(
              rank,
              0
          ) + 1;


      counts.put(
          rank,
          count
      );


      max =
          Math.max(
              max,
              count
          );
    }


    return max;
  }


  private static boolean isTwoPair(
      Card[] cards)
  {
    Map<Rank, Integer> counts =
        new EnumMap<>(
            Rank.class
        );


    for (Card card :
         cards)
    {
      Rank rank =
          card.getRank();


      counts.put(
          rank,
          counts.getOrDefault(
              rank,
              0
          ) + 1
      );
    }


    int pairs =
        0;


    for (int count :
         counts.values())
    {
      if (count == 2)
      {
        pairs++;
      }
    }


    return pairs == 2;
  }


  private static boolean isStraight(
      Card[] cards)
  {
    int[] values =
        new int[4];


    for (int i = 0;
         i < 4;
         i++)
    {
      values[i] =
          pokerValue(
              cards[i].getRank()
          );
    }


    java.util.Arrays.sort(
        values
    );


    for (int i = 1;
         i < values.length;
         i++)
    {
      if (values[i]
          == values[i - 1])
      {
        return false;
      }
    }


    if (values[1]
            == values[0] + 1
        && values[2]
            == values[1] + 1
        && values[3]
            == values[2] + 1)
    {
      return true;
    }


    /*
     * A-2-3-4
     */
    return values[0] == 2
        && values[1] == 3
        && values[2] == 4
        && values[3] == 14;
  }


  private static int pokerValue(
      Rank rank)
  {
    switch (rank)
    {
      case TWO:
        return 2;

      case THREE:
        return 3;

      case FOUR:
        return 4;

      case FIVE:
        return 5;

      case SIX:
        return 6;

      case SEVEN:
        return 7;

      case EIGHT:
        return 8;

      case NINE:
        return 9;

      case TEN:
        return 10;

      case JACK:
        return 11;

      case QUEEN:
        return 12;

      case KING:
        return 13;

      case ACE:
        return 14;

      default:
        throw new IllegalStateException(
            "Unexpected rank: "
            + rank
        );
    }
  }


  private static boolean isFlush(
      Card[] cards)
  {
    String suit =
        suitName(
            cards[0]
        );


    for (int i = 1;
         i < cards.length;
         i++)
    {
      if (!suit.equals(
          suitName(
              cards[i]
          )))
      {
        return false;
      }
    }


    return true;
  }


  private static boolean isSameColor(
      Card[] cards)
  {
    String first =
        colorName(
            cards[0]
        );


    for (int i = 1;
         i < cards.length;
         i++)
    {
      if (!first.equals(
          colorName(
              cards[i]
          )))
      {
        return false;
      }
    }


    return true;
  }


  /*
   * Keeps this runner independent of the
   * exact compile-time Suit enum API.
   */
  private static String suitName(
      Card card)
  {
    try
    {
      Method method =
          card.getClass()
              .getMethod(
                  "getSuit"
              );


      Object suit =
          method.invoke(
              card
          );


      return String.valueOf(
          suit
      )
      .toUpperCase(
          Locale.US
      );
    }
    catch (ReflectiveOperationException e)
    {
      throw new IllegalStateException(
          "Card.getSuit() required",
          e
      );
    }
  }


  private static String colorName(
      Card card)
  {
    String suit =
        suitName(
            card
        );


    if (suit.contains("HEART")
        || suit.contains("DIAMOND"))
    {
      return "RED";
    }


    if (suit.contains("SPADE")
        || suit.contains("CLUB"))
    {
      return "BLACK";
    }


    throw new IllegalStateException(
        "Unknown suit: "
        + suit
    );
  }


  // ============================================================
  // ACE TRACKING
  // ============================================================

  private static class AceTracker
  {
    private int visibleCards;

    private int acesSeen;


    void reset()
    {
      visibleCards =
          0;

      acesSeen =
          0;
    }


    void observe(
        Card card)
    {
      visibleCards++;


      if (card.getRank()
          == Rank.ACE)
      {
        acesSeen++;
      }
    }


    double getAceSurplus()
    {
      return visibleCards / 13.0
          - acesSeen;
    }
  }


  private static void observeVisible(
      AceTracker tracker,
      Map<Card, Boolean> observed,
      Card card)
  {
    if (observed.containsKey(
        card))
    {
      return;
    }


    observed.put(
        card,
        true
    );


    tracker.observe(
        card
    );
  }


  private static void observeHand(
      AceTracker tracker,
      Map<Card, Boolean> observed,
      Hand hand)
  {
    for (int i = 0;
         i < hand.getNumCards();
         i++)
    {
      observeVisible(
          tracker,
          observed,
          hand.getCard(i)
      );
    }
  }


  // ============================================================
  // SETTINGS
  // ============================================================

  private static GameSettings
      createResearchSettings()
  {
    GameSettings settings =
        new GameSettings();


    settings.setDecksInShoe(
        8
    );


    settings.setDealerHitsSoft17(
        true
    );


    settings.setDoubleAfterSplit(
        true
    );


    settings.setAllowSurrender(
        true
    );


    settings.setBlackjackPayout(
        GameSettings.BlackjackPayout
            .THREE_TO_TWO
    );


    return settings;
  }


  // ============================================================
  // ACCUMULATION
  // ============================================================

  private static class PolicyAccumulator
  {
    private final Policy policy;


    private long sessions;

    private double meanBankroll;

    private double bankrollM2;


    private long unable;

    private long acceptable;

    private long profitable;


    private long totalHands;

    private double totalMainExposure;


    private long totalSwitches;

    private long totalMidpointChecks;


    private long totalOpportunities;

    private long totalSuper4Bets;

    private long totalSuper4Wins;

    private long totalRoyals;


    private double totalSuper4Profit;


    private long zeroOpportunitySessions;

    private long oneOpportunitySessions;

    private long twoOpportunitySessions;

    private long threePlusOpportunitySessions;


    PolicyAccumulator(
        Policy policy)
    {
      this.policy =
          policy;
    }


    void add(
        SessionState state)
    {
      sessions++;


      double delta =
          state.bankroll()
          - meanBankroll;


      meanBankroll +=
          delta / sessions;


      bankrollM2 +=
          delta
          * (
              state.bankroll()
              - meanBankroll
          );


      if (state.unableToContinue())
        unable++;


      if (state.acceptable())
        acceptable++;


      if (state.profitable())
        profitable++;


      totalHands +=
          state.handsPlayed();


      totalMainExposure +=
          state.mainExposure();


      totalSwitches +=
          state.tableSwitches();


      totalMidpointChecks +=
          state.midpointChecks();


      totalOpportunities +=
          state.super4Opportunities();


      totalSuper4Bets +=
          state.super4Bets();


      totalSuper4Wins +=
          state.super4Wins();


      totalRoyals +=
          state.royalHits();


      totalSuper4Profit +=
          state.super4Profit();


      if (state.super4Opportunities()
          == 0)
      {
        zeroOpportunitySessions++;
      }
      else if (state.super4Opportunities()
          == 1)
      {
        oneOpportunitySessions++;
      }
      else if (state.super4Opportunities()
          == 2)
      {
        twoOpportunitySessions++;
      }
      else
      {
        threePlusOpportunitySessions++;
      }
    }


    PolicySummary finish()
    {
      double sd =
          sessions <= 1
              ? 0.0
              : Math.sqrt(
                  bankrollM2
                  / (sessions - 1)
              );


      return new PolicySummary(
          policy,

          meanBankroll,

          meanBankroll
              - STARTING_BANKROLL,

          sd,

          (double) unable
              / sessions,

          (double) acceptable
              / sessions,

          (double) profitable
              / sessions,

          (double) totalHands
              / sessions,

          totalMainExposure
              / sessions,

          (double) totalSwitches
              / sessions,

          (double) totalMidpointChecks
              / sessions,

          (double) totalOpportunities
              / sessions,

          (double) totalSuper4Bets
              / sessions,

          totalSuper4Profit
              / sessions,

          totalSuper4Bets == 0
              ? 0.0
              : totalSuper4Profit
                / totalSuper4Bets,

          totalSuper4Bets == 0
              ? 0.0
              : (double) totalSuper4Wins
                / totalSuper4Bets,

          totalSuper4Bets == 0
              ? 0.0
              : (double) totalRoyals
                / totalSuper4Bets,

          (double) zeroOpportunitySessions
              / sessions,

          (double) oneOpportunitySessions
              / sessions,

          (double) twoOpportunitySessions
              / sessions,

          (double) threePlusOpportunitySessions
              / sessions
      );
    }
  }


  private static class PairAccumulator
  {
    private final Policy test;

    private final Policy reference;


    private long count;

    private double mean;

    private double m2;


    private long testRuin;

    private long referenceRuin;


    private long testAcceptable;

    private long referenceAcceptable;


    PairAccumulator(
        Policy test,
        Policy reference)
    {
      this.test =
          test;

      this.reference =
          reference;
    }


    void add(
        SessionState testState,
        SessionState referenceState)
    {
      count++;


      double difference =
          testState.bankroll()
          - referenceState.bankroll();


      double delta =
          difference
          - mean;


      mean +=
          delta / count;


      m2 +=
          delta
          * (
              difference
              - mean
          );


      if (testState.unableToContinue())
        testRuin++;


      if (referenceState.unableToContinue())
        referenceRuin++;


      if (testState.acceptable())
        testAcceptable++;


      if (referenceState.acceptable())
        referenceAcceptable++;
    }


    PairSummary finish()
    {
      double sd =
          count <= 1
              ? 0.0
              : Math.sqrt(
                  m2
                  / (count - 1)
              );


      double se =
          sd
          / Math.sqrt(
              count
          );


      return new PairSummary(
          test,
          reference,

          mean,

          Z_95 * se,

          (
              (double) testRuin
              - referenceRuin
          ) / count,

          (
              (double) testAcceptable
              - referenceAcceptable
          ) / count
      );
    }
  }


  // ============================================================
  // PRINTING
  // ============================================================

  private static void printPolicySummary(
      PolicySummary s)
  {
    System.out.println();

    System.out.println(
        s.policy()
    );


    System.out.println(
        "----------------------------------------"
    );


    System.out.printf(
        Locale.US,
        "Average ending bankroll: $%.2f%n",
        s.meanBankroll()
    );


    System.out.printf(
        Locale.US,
        "Average total P/L: $%+.2f%n",
        s.meanProfit()
    );


    System.out.printf(
        Locale.US,
        "Ending bankroll SD: $%.2f%n",
        s.bankrollSD()
    );


    System.out.printf(
        Locale.US,
        "Unable to continue: %.3f%%%n",
        s.unableRate() * 100.0
    );


    System.out.printf(
        Locale.US,
        "Ending bankroll >= $400: %.3f%%%n",
        s.acceptableRate() * 100.0
    );


    System.out.printf(
        Locale.US,
        "Profitable sessions: %.3f%%%n",
        s.profitableRate() * 100.0
    );


    System.out.printf(
        Locale.US,
        "Average hands played: %.2f%n",
        s.averageHands()
    );


    System.out.printf(
        Locale.US,
        "Average table switches: %.3f%n",
        s.averageSwitches()
    );


    System.out.printf(
        Locale.US,
        "Average midpoint checks: %.3f%n",
        s.averageMidpointChecks()
    );


    if (s.policy().usesSuper4())
    {
      System.out.printf(
          Locale.US,
          "Super 4 opportunities/session: %.3f%n",
          s.averageOpportunities()
      );


      System.out.printf(
          Locale.US,
          "Super 4 bets/session: %.3f%n",
          s.averageSuper4Bets()
      );


      System.out.printf(
          Locale.US,
          "Average Super 4 amount wagered: $%.2f%n",
          s.averageSuper4Bets()
              * SUPER4_BET
      );


      System.out.printf(
          Locale.US,
          "Average Super 4 P/L: $%+.3f%n",
          s.averageSuper4Profit()
      );


      System.out.printf(
          Locale.US,
          "Super 4 P/L per bet: $%+.4f%n",
          s.super4ProfitPerBet()
      );


      System.out.printf(
          Locale.US,
          "Super 4 effective edge: %+.3f%%%n",
          s.super4ProfitPerBet()
              / SUPER4_BET
              * 100.0
      );


      System.out.printf(
          Locale.US,
          "Super 4 win rate: %.3f%%%n",
          s.super4WinRate()
              * 100.0
      );


      System.out.printf(
          Locale.US,
          "Royal occurrence per side bet: %.6f%%%n",
          s.royalRate()
              * 100.0
      );


      System.out.printf(
          Locale.US,
          "Sessions with 0 opportunities: %.3f%%%n",
          s.zeroOpportunityRate()
              * 100.0
      );


      System.out.printf(
          Locale.US,
          "Sessions with 1 opportunity: %.3f%%%n",
          s.oneOpportunityRate()
              * 100.0
      );


      System.out.printf(
          Locale.US,
          "Sessions with 2 opportunities: %.3f%%%n",
          s.twoOpportunityRate()
              * 100.0
      );


      System.out.printf(
          Locale.US,
          "Sessions with 3+ opportunities: %.3f%%%n",
          s.threePlusOpportunityRate()
              * 100.0
      );
    }
  }


  private static void printPairSummary(
      PairSummary s)
  {
    System.out.println();

    System.out.println(
        s.test()
        + " vs "
        + s.reference()
    );


    System.out.println(
        "----------------------------------------"
    );


    System.out.printf(
        Locale.US,
        "Average P/L difference: $%+.3f%n",
        s.meanDifference()
    );


    System.out.printf(
        Locale.US,
        "95%% CI: $%+.3f to $%+.3f%n",
        s.meanDifference()
            - s.ci95(),

        s.meanDifference()
            + s.ci95()
    );


    System.out.printf(
        Locale.US,
        "Unable-to-continue difference: %+.3f pp%n",
        s.ruinDifference()
            * 100.0
    );


    System.out.printf(
        Locale.US,
        "$400+ night difference: %+.3f pp%n",
        s.acceptableDifference()
            * 100.0
    );
  }


  // ============================================================
  // RECORDS
  // ============================================================

  private record RoundResult(
      double mainExposure,
      double super4Payout,
      boolean royal)
  {
  }


  private record Super4Result(
      double payout,
      boolean royal)
  {
  }


  private record SessionState(
      Policy policy,
      double bankroll,
      int handsPlayed,
      boolean unableToContinue,
      boolean acceptable,
      boolean profitable,
      double mainExposure,
      int tableSwitches,
      int midpointChecks,
      int super4Opportunities,
      int super4Bets,
      int super4Wins,
      int royalHits,
      double super4Profit)
  {
  }


  private record PolicySummary(
      Policy policy,
      double meanBankroll,
      double meanProfit,
      double bankrollSD,
      double unableRate,
      double acceptableRate,
      double profitableRate,
      double averageHands,
      double averageMainExposure,
      double averageSwitches,
      double averageMidpointChecks,
      double averageOpportunities,
      double averageSuper4Bets,
      double averageSuper4Profit,
      double super4ProfitPerBet,
      double super4WinRate,
      double royalRate,
      double zeroOpportunityRate,
      double oneOpportunityRate,
      double twoOpportunityRate,
      double threePlusOpportunityRate)
  {
  }


  private record PairSummary(
      Policy test,
      Policy reference,
      double meanDifference,
      double ci95,
      double ruinDifference,
      double acceptableDifference)
  {
  }
}