import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AceSuper4SessionRiskResearchRunner
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

  /*
   * Representative major jackpot for the session study.
   * Change this one constant to rerun at another meter value.
   */
  private static final double MAJOR_JACKPOT =
      650_000.0;

  private static final double OTHER_ROYAL_MULTIPLIER =
      0.10;

  private static final double ACCEPTABLE_BANKROLL =
      400.0;

  private static final long BASE_SEED =
      10_24_2026L;

  private static final double Z_95 =
      1.96;


  // ------------------------------------------------------------
  // Fixed Super 4 payouts used in the prior studies.
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


  /*
   * We avoid using the actually observed royal suit as the
   * progressive "major" trigger because that adds unnecessary
   * Monte Carlo noise.
   *
   * Conditional on a royal, one of four suits gets the full
   * jackpot and three of four get 10% of the jackpot.
   *
   * Therefore the expected royal payout is:
   *
   *   0.25 * J + 0.75 * 0.10 * J
   * = 0.325 * J
   *
   * For session-level bankroll simulation, we preserve variance
   * by randomly selecting major vs other royal at the correct
   * 1/4 vs 3/4 probabilities using a deterministic per-session RNG.
   */
  private static final double MAJOR_ROYAL_PROBABILITY =
      0.25;


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

    ACE_SWITCH_SUPER4_2_5(
        true,
        2.5
    ),

    ACE_SWITCH_SUPER4_3(
        true,
        3.0
    ),

    ACE_SWITCH_SUPER4_3_5(
        true,
        3.5
    ),

    ACE_SWITCH_SUPER4_4(
        true,
        4.0
    ),

    ACE_SWITCH_SUPER4_4_5(
        true,
        4.5
    ),

    ACE_SWITCH_SUPER4_5(
        true,
        5.0
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


    PairAccumulator[] vsFlat =
        new PairAccumulator[
            policies.length
        ];


    PairAccumulator[] vsSwitchOnly =
        new PairAccumulator[
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


      if (session % 25_000 == 0)
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
        "ACE SUPER 4 SESSION RISK STUDY"
    );

    System.out.println(
        "========================================"
    );

    System.out.printf(
        Locale.US,
        "Major jackpot: $%,.0f%n",
        MAJOR_JACKPOT
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


    int super4Opportunities =
        0;


    int super4Bets =
        0;


    int super4Wins =
        0;


    int royalHits =
        0;


    int majorRoyalHits =
        0;


    int otherRoyalHits =
        0;


    double super4Profit =
        0.0;


    double totalMainExposure =
        0.0;


    /*
     * Separate deterministic RNG stream used only to choose
     * whether a royal is the 1-in-4 major suit or one of the
     * 3-in-4 other suits.
     *
     * This preserves correct jackpot variance while avoiding
     * suit-composition noise in the card simulation.
     */
    java.util.Random royalRng =
        new java.util.Random(
            seed ^ 0x5A5A5A5A5A5A5A5AL
        );


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
         * Negative Ace surplus means more Aces have appeared
         * than expected, leaving an Ace-poor remaining shoe.
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
              playSuper4,
              royalRng
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


        if (result.majorRoyal())
        {
          majorRoyalHits++;
        }


        if (result.otherRoyal())
        {
          otherRoyalHits++;
        }
      }


      handsPlayed++;
    }


    double endingBankroll =
        bankroll[0];


    double totalLoss =
        STARTING_BANKROLL
        - endingBankroll;


    return new SessionState(
        policy,
        endingBankroll,
        handsPlayed,
        endingBankroll
            < MAIN_BET,
        endingBankroll
            >= ACCEPTABLE_BANKROLL,
        endingBankroll
            > STARTING_BANKROLL,
        endingBankroll
            <= STARTING_BANKROLL - 100.0,
        endingBankroll
            <= STARTING_BANKROLL - 200.0,
        endingBankroll
            <= STARTING_BANKROLL - 300.0,
        Math.max(
            0.0,
            totalLoss
        ),
        totalMainExposure,
        tableSwitches,
        midpointChecks,
        super4Opportunities,
        super4Bets,
        super4Wins,
        royalHits,
        majorRoyalHits,
        otherRoyalHits,
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
      boolean evaluateSuper4,
      java.util.Random royalRng)
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
                dealerBlackjack,
                royalRng
            )
            : new Super4Result(
                0.0,
                false,
                false,
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
          super4.royal(),
          super4.majorRoyal(),
          super4.otherRoyal()
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


      return new RoundResult(
          mainExposure[0],
          super4.payout(),
          super4.royal(),
          super4.majorRoyal(),
          super4.otherRoyal()
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
              "Natural blackjack should already be resolved"
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
        super4.royal(),
        super4.majorRoyal(),
        super4.otherRoyal()
    );
  }


  // ============================================================
  // SUPER 4
  // ============================================================

  private static Super4Result evaluateSuper4(
      Card player1,
      Card player2,
      Card dealerUp,
      Card dealerHole,
      boolean dealerBlackjack,
      java.util.Random royalRng)
  {
    if (!dealerBlackjack)
    {
      if (dealerUp.getRank()
          == Rank.ACE)
      {
        return new Super4Result(
            PAY_ACE_NO_BJ,
            false,
            false,
            false
        );
      }


      return new Super4Result(
          0.0,
          false,
          false,
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


    if (isRoyal(cards))
    {
      boolean majorRoyal =
          royalRng.nextDouble()
          < MAJOR_ROYAL_PROBABILITY;


      double payout =
          majorRoyal
              ? MAJOR_JACKPOT
              : MAJOR_JACKPOT
                * OTHER_ROYAL_MULTIPLIER;


      return new Super4Result(
          payout,
          true,
          majorRoyal,
          !majorRoyal
      );
    }


    int maxRankCount =
        maxRankCount(
            cards
        );


    if (maxRankCount >= 3)
    {
      return new Super4Result(
          PAY_3K,
          false,
          false,
          false
      );
    }


    if (isStraight(cards))
    {
      return new Super4Result(
          PAY_STRAIGHT,
          false,
          false,
          false
      );
    }


    if (isFlush(cards))
    {
      return new Super4Result(
          PAY_FLUSH,
          false,
          false,
          false
      );
    }


    if (isTwoPair(cards))
    {
      return new Super4Result(
          PAY_TWO_PAIR,
          false,
          false,
          false
      );
    }


    if (isSameColor(cards))
    {
      return new Super4Result(
          PAY_SAME_COLOR,
          false,
          false,
          false
      );
    }


    if (maxRankCount >= 2)
    {
      return new Super4Result(
          PAY_PAIR,
          false,
          false,
          false
      );
    }


    return new Super4Result(
        PAY_OTHER_BJ,
        false,
        false,
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
    java.util.EnumMap<Rank, Integer> counts =
        new java.util.EnumMap<>(
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
    java.util.EnumMap<Rank, Integer> counts =
        new java.util.EnumMap<>(
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


    Arrays.sort(
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


  private static String suitName(
      Card card)
  {
    try
    {
      java.lang.reflect.Method method =
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

  private static GameSettings createResearchSettings()
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


    private long loss100;


    private long loss200;


    private long loss300;


    private long totalHands;


    private double totalMainExposure;


    private long totalSwitches;


    private long totalMidpointChecks;


    private long totalOpportunities;


    private long totalSuper4Bets;


    private long totalSuper4Wins;


    private long totalRoyals;


    private long totalMajorRoyals;


    private long totalOtherRoyals;


    private double totalSuper4Profit;


    private final double[] endingBankrolls =
        new double[
            SESSIONS
        ];


    PolicyAccumulator(
        Policy policy)
    {
      this.policy =
          policy;
    }


    void add(
        SessionState state)
    {
      endingBankrolls[
          (int) sessions
      ] =
          state.bankroll();


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
      {
        unable++;
      }


      if (state.acceptable())
      {
        acceptable++;
      }


      if (state.profitable())
      {
        profitable++;
      }


      if (state.lossAtLeast100())
      {
        loss100++;
      }


      if (state.lossAtLeast200())
      {
        loss200++;
      }


      if (state.lossAtLeast300())
      {
        loss300++;
      }


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


      totalMajorRoyals +=
          state.majorRoyalHits();


      totalOtherRoyals +=
          state.otherRoyalHits();


      totalSuper4Profit +=
          state.super4Profit();
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


      double[] sorted =
          Arrays.copyOf(
              endingBankrolls,
              (int) sessions
          );


      Arrays.sort(
          sorted
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

          (double) loss100
              / sessions,

          (double) loss200
              / sessions,

          (double) loss300
              / sessions,

          percentile(
              sorted,
              0.10
          ),

          percentile(
              sorted,
              0.25
          ),

          percentile(
              sorted,
              0.50
          ),

          percentile(
              sorted,
              0.75
          ),

          percentile(
              sorted,
              0.90
          ),

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

          totalSuper4Bets == 0
              ? 0.0
              : (double) totalMajorRoyals
                / totalSuper4Bets,

          totalSuper4Bets == 0
              ? 0.0
              : (double) totalOtherRoyals
                / totalSuper4Bets
      );
    }
  }


  private static double percentile(
      double[] sorted,
      double p)
  {
    if (sorted.length == 0)
    {
      return 0.0;
    }


    double index =
        p
        * (sorted.length - 1);


    int low =
        (int) Math.floor(
            index
        );


    int high =
        (int) Math.ceil(
            index
        );


    if (low == high)
    {
      return sorted[
          low
      ];
    }


    double fraction =
        index
        - low;


    return sorted[low]
        + fraction
          * (
              sorted[high]
              - sorted[low]
          );
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


    private long testLoss100;


    private long referenceLoss100;


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
      {
        testRuin++;
      }


      if (referenceState.unableToContinue())
      {
        referenceRuin++;
      }


      if (testState.acceptable())
      {
        testAcceptable++;
      }


      if (referenceState.acceptable())
      {
        referenceAcceptable++;
      }


      if (testState.lossAtLeast100())
      {
        testLoss100++;
      }


      if (referenceState.lossAtLeast100())
      {
        referenceLoss100++;
      }
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
          ) / count,

          (
              (double) testLoss100
              - referenceLoss100
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
        s.unableRate()
            * 100.0
    );


    System.out.printf(
        Locale.US,
        "Ending bankroll >= $400: %.3f%%%n",
        s.acceptableRate()
            * 100.0
    );


    System.out.printf(
        Locale.US,
        "Profitable sessions: %.3f%%%n",
        s.profitableRate()
            * 100.0
    );


    System.out.printf(
        Locale.US,
        "Lose $100 or more: %.3f%%%n",
        s.loss100Rate()
            * 100.0
    );


    System.out.printf(
        Locale.US,
        "Lose $200 or more: %.3f%%%n",
        s.loss200Rate()
            * 100.0
    );


    System.out.printf(
        Locale.US,
        "Lose $300 or more: %.3f%%%n",
        s.loss300Rate()
            * 100.0
    );


    System.out.printf(
        Locale.US,
        "10th percentile bankroll: $%.2f%n",
        s.p10()
    );


    System.out.printf(
        Locale.US,
        "25th percentile bankroll: $%.2f%n",
        s.p25()
    );


    System.out.printf(
        Locale.US,
        "Median ending bankroll: $%.2f%n",
        s.p50()
    );


    System.out.printf(
        Locale.US,
        "75th percentile bankroll: $%.2f%n",
        s.p75()
    );


    System.out.printf(
        Locale.US,
        "90th percentile bankroll: $%.2f%n",
        s.p90()
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
          "Major royal occurrence per side bet: %.6f%%%n",
          s.majorRoyalRate()
              * 100.0
      );


      System.out.printf(
          Locale.US,
          "Other royal occurrence per side bet: %.6f%%%n",
          s.otherRoyalRate()
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


    System.out.printf(
        Locale.US,
        "Lose-$100+ difference: %+.3f pp%n",
        s.loss100Difference()
            * 100.0
    );
  }


  // ============================================================
  // RECORDS
  // ============================================================

  private record RoundResult(
      double mainExposure,
      double super4Payout,
      boolean royal,
      boolean majorRoyal,
      boolean otherRoyal)
  {
  }


  private record Super4Result(
      double payout,
      boolean royal,
      boolean majorRoyal,
      boolean otherRoyal)
  {
  }


  private record SessionState(
      Policy policy,
      double bankroll,
      int handsPlayed,
      boolean unableToContinue,
      boolean acceptable,
      boolean profitable,
      boolean lossAtLeast100,
      boolean lossAtLeast200,
      boolean lossAtLeast300,
      double realizedLoss,
      double mainExposure,
      int tableSwitches,
      int midpointChecks,
      int super4Opportunities,
      int super4Bets,
      int super4Wins,
      int royalHits,
      int majorRoyalHits,
      int otherRoyalHits,
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
      double loss100Rate,
      double loss200Rate,
      double loss300Rate,
      double p10,
      double p25,
      double p50,
      double p75,
      double p90,
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
      double majorRoyalRate,
      double otherRoyalRate)
  {
  }


  private record PairSummary(
      Policy test,
      Policy reference,
      double meanDifference,
      double ci95,
      double ruinDifference,
      double acceptableDifference,
      double loss100Difference)
  {
  }
}
