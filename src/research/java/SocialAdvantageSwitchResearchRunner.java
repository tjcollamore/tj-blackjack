import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class SocialAdvantageSwitchResearchRunner
{
  private static final int SESSIONS =
      500_000;


  private static final int HANDS_PER_SESSION =
      160;


  private static final double STARTING_BANKROLL =
      500.0;


  private static final double BASE_BET =
      25.0;


  private static final double ACE_FIVE_MAX_BET =
      50.0;


  private static final double ACCEPTABLE_NIGHT_THRESHOLD =
      400.0;


  private static final long BASE_SEED =
      10_24_2026L;


  private static final double CI_95_Z =
      1.96;


  private static final Path OUTPUT_FILE =
      Path.of(
          "research-data",
          "scratch",
          "social-advantage-switch-study-500k-160hands-500bankroll.csv"
      );


  private enum Policy
  {
    FLAT_BASIC,

    ACE_FIVE_1_TO_2,

    ACE_FIVE_1_TO_2_SWITCH,

    ACE_ONLY_SWITCH
  }


  public static void main(String[] args)
      throws IOException
  {
    if (Files.exists(OUTPUT_FILE))
    {
      throw new IllegalStateException(
          "Research output already exists: "
          + OUTPUT_FILE
      );
    }


    Files.createDirectories(
        OUTPUT_FILE.getParent()
    );


    writeHeader();


    GameSettings settings =
        createResearchSettings();


    Policy[] policies =
    {
        Policy.FLAT_BASIC,

        Policy.ACE_FIVE_1_TO_2,

        Policy.ACE_FIVE_1_TO_2_SWITCH,

        Policy.ACE_ONLY_SWITCH
    };


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


    PairAccumulator aceFiveSwitchVsNoSwitch =
        new PairAccumulator(
            Policy.ACE_FIVE_1_TO_2_SWITCH,
            Policy.ACE_FIVE_1_TO_2
        );


    PairAccumulator aceOnlyVsAceFiveSwitch =
        new PairAccumulator(
            Policy.ACE_ONLY_SWITCH,
            Policy.ACE_FIVE_1_TO_2_SWITCH
        );


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


      for (int i = 1;
           i < policies.length;
           i++)
      {
        vsFlat[i].add(
            states[i],
            flat
        );
      }


      aceFiveSwitchVsNoSwitch.add(
          states[
              Policy
                  .ACE_FIVE_1_TO_2_SWITCH
                  .ordinal()
          ],

          states[
              Policy
                  .ACE_FIVE_1_TO_2
                  .ordinal()
          ]
      );


      aceOnlyVsAceFiveSwitch.add(
          states[
              Policy
                  .ACE_ONLY_SWITCH
                  .ordinal()
          ],

          states[
              Policy
                  .ACE_FIVE_1_TO_2_SWITCH
                  .ordinal()
          ]
      );


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
        "SOCIAL ADVANTAGE SWITCH STUDY"
    );

    System.out.println(
        "========================================"
    );


    for (PolicyAccumulator accumulator :
         accumulators)
    {
      PolicySummary summary =
          accumulator.finish();


      printPolicySummary(
          summary
      );


      writePolicySummary(
          summary
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
        "VALUE OF ACE/FIVE TABLE SWITCHING"
    );

    System.out.println(
        "========================================"
    );


    printPairSummary(
        aceFiveSwitchVsNoSwitch.finish()
    );


    System.out.println();

    System.out.println(
        "========================================"
    );

    System.out.println(
        "ACE-ONLY VS ACE/FIVE SWITCHING"
    );

    System.out.println(
        "========================================"
    );


    printPairSummary(
        aceOnlyVsAceFiveSwitch.finish()
    );


    System.out.println();

    System.out.println(
        "Saved results to "
        + OUTPUT_FILE.toAbsolutePath()
    );
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


    double[] totalInitialWager =
    {
        0.0
    };


    double[] totalWagerExposure =
    {
        0.0
    };


    long[] bet25Hands =
    {
        0
    };


    long[] bet50Hands =
    {
        0
    };


    long[] wagerChanges =
    {
        0
    };


    double[] maxInitialBet =
    {
        0.0
    };


    int handsPlayed =
        0;


    int tableSwitches =
        0;


    int midpointDecisions =
        0;


    int midpointAbandons =
        0;


    Shoe shoe =
        new Shoe(
            settings.getDecksInShoe(),
            seed
        );


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


    CountingSystem count =
        createCount(
            policy
        );


    double previousBet =
        BASE_BET;


    boolean midpointDecisionMade =
        false;


    int replacementShoeNumber =
        0;


    for (int handNumber = 0;
         handNumber < HANDS_PER_SESSION;
         handNumber++)
    {
      if (bankroll[0] < BASE_BET)
      {
        return createSessionState(
            policy,
            bankroll[0],
            handsPlayed,
            true,
            totalInitialWager[0],
            totalWagerExposure[0],
            bet25Hands[0],
            bet50Hands[0],
            wagerChanges[0],
            maxInitialBet[0],
            tableSwitches,
            midpointDecisions,
            midpointAbandons
        );
      }


      // --------------------------------------------------------
      // NORMAL SHUFFLE
      // --------------------------------------------------------

      if (shoe.needsShuffle())
      {
        shoe.shuffle();


        if (count != null)
        {
          count.reset();
        }


        previousBet =
            BASE_BET;


        midpointDecisionMade =
            false;
      }


      // --------------------------------------------------------
      // MIDPOINT DECISION
      // --------------------------------------------------------

      if (usesTableSwitching(policy)
          && !midpointDecisionMade
          && reachedHalfwayPoint(shoe))
      {
        midpointDecisionMade =
            true;


        midpointDecisions++;


        if (shouldSwitch(
            policy,
            count,
            shoe))
        {
          midpointAbandons++;

          tableSwitches++;

          replacementShoeNumber++;


          /*
           * Idealized switch for now:
           *
           * Leave this shoe and immediately
           * begin a fresh independent shoe.
           *
           * No lost hands/time yet.
           */
          long replacementSeed =
              seed
              + 1_000_000_000L
              + (
                  replacementShoeNumber
                  * 1_000_003L
              );


          shoe =
              new Shoe(
                  settings.getDecksInShoe(),
                  replacementSeed
              );


          count.reset();


          previousBet =
              BASE_BET;


          midpointDecisionMade =
              false;
        }
      }


      // --------------------------------------------------------
      // BET
      // --------------------------------------------------------

      double oldBet =
          previousBet;


      double requestedBet =
          chooseBet(
              policy,
              count,
              shoe,
              previousBet
          );


      double initialBet =
          capBetToBankroll(
              requestedBet,
              bankroll[0]
          );


      if (initialBet != oldBet)
      {
        wagerChanges[0]++;
      }


      previousBet =
          initialBet;


      totalInitialWager[0] +=
          initialBet;


      if (initialBet == 25.0)
      {
        bet25Hands[0]++;
      }
      else if (initialBet == 50.0)
      {
        bet50Hands[0]++;
      }
      else
      {
        throw new IllegalStateException(
            "Unexpected initial wager: "
            + initialBet
        );
      }


      maxInitialBet[0] =
          Math.max(
              maxInitialBet[0],
              initialBet
          );


      // --------------------------------------------------------
      // PLAY ROUND
      // --------------------------------------------------------

      playRound(
          settings,
          playerRoundEngine,
          dealerPlayEngine,
          shoe,
          strategy,
          count,
          initialBet,
          bankroll,
          totalWagerExposure
      );


      handsPlayed++;
    }


    return createSessionState(
        policy,
        bankroll[0],
        handsPlayed,
        false,
        totalInitialWager[0],
        totalWagerExposure[0],
        bet25Hands[0],
        bet50Hands[0],
        wagerChanges[0],
        maxInitialBet[0],
        tableSwitches,
        midpointDecisions,
        midpointAbandons
    );
  }


  private static SessionState createSessionState(
      Policy policy,
      double endingBankroll,
      int handsPlayed,
      boolean unableToContinue,
      double totalInitialWager,
      double totalWagerExposure,
      long bet25Hands,
      long bet50Hands,
      long wagerChanges,
      double maxInitialBet,
      int tableSwitches,
      int midpointDecisions,
      int midpointAbandons)
  {
    return new SessionState(
        policy,

        endingBankroll,

        handsPlayed,

        unableToContinue,

        endingBankroll
            >= ACCEPTABLE_NIGHT_THRESHOLD,

        endingBankroll
            > STARTING_BANKROLL,

        totalInitialWager,

        totalWagerExposure,

        bet25Hands,

        bet50Hands,

        wagerChanges,

        maxInitialBet,

        tableSwitches,

        midpointDecisions,

        midpointAbandons
    );
  }


  // ============================================================
  // COUNTING SYSTEM
  // ============================================================

  private static CountingSystem createCount(
      Policy policy)
  {
    switch (policy)
    {
      case FLAT_BASIC:
        return null;


      case ACE_FIVE_1_TO_2:
      case ACE_FIVE_1_TO_2_SWITCH:
        return new AceFiveCount();


      case ACE_ONLY_SWITCH:
        return new AceOnlyCount();


      default:
        throw new IllegalStateException(
            "Unexpected policy: "
            + policy
        );
    }
  }


  // ============================================================
  // BETTING
  // ============================================================

  private static double chooseBet(
      Policy policy,
      CountingSystem count,
      Shoe shoe,
      double previousBet)
  {
    switch (policy)
    {
      case FLAT_BASIC:

      case ACE_ONLY_SWITCH:
        return BASE_BET;


      case ACE_FIVE_1_TO_2:

      case ACE_FIVE_1_TO_2_SWITCH:
        double runningCount =
            count.getSignal(
                shoe.getRemainingCardCount()
            );


        /*
         * Conservative 1-2 Ace/Five.
         *
         * Favorable:
         *   $25 -> $50
         *
         * Remain favorable:
         *   stay at $50
         *
         * RC <= +1:
         *   immediately return to $25
         */
        if (runningCount >= 2.0)
        {
          return Math.min(
              previousBet * 2.0,
              ACE_FIVE_MAX_BET
          );
        }


        return BASE_BET;


      default:
        throw new IllegalStateException(
            "Unexpected policy: "
            + policy
        );
    }
  }


  private static double capBetToBankroll(
      double requestedBet,
      double bankroll)
  {
    if (requestedBet >= 50.0
        && bankroll >= 50.0)
    {
      return 50.0;
    }


    return BASE_BET;
  }


  // ============================================================
  // TABLE SWITCHING
  // ============================================================

  private static boolean usesTableSwitching(
      Policy policy)
  {
    return policy
            == Policy
                .ACE_FIVE_1_TO_2_SWITCH
        || policy
            == Policy
                .ACE_ONLY_SWITCH;
  }


  private static boolean reachedHalfwayPoint(
      Shoe shoe)
  {
    return shoe.getRemainingCardCount()
        <= 4 * 52;
  }


  private static boolean shouldSwitch(
      Policy policy,
      CountingSystem count,
      Shoe shoe)
  {
    switch (policy)
    {
      case ACE_FIVE_1_TO_2_SWITCH:
        /*
         * Negative Ace/Five RC:
         *
         * relatively unfavorable
         * composition.
         */
        return count.getSignal(
            shoe.getRemainingCardCount()
        ) < 0.0;


      case ACE_ONLY_SWITCH:
        /*
         * Ace-only:
         *
         * running surplus =
         *
         * expected Aces seen
         * - actual Aces seen
         *
         * Negative means MORE Aces have
         * appeared than expected, leaving
         * the unknown shoe relatively
         * Ace-poor.
         *
         * No true-count division needed.
         */
        AceOnlyCount aceCount =
            (AceOnlyCount) count;


        return aceCount
            .getRunningSurplus()
            < 0.0;


      default:
        return false;
    }
  }


  // ============================================================
  // ROUND
  // ============================================================

  private static void playRound(
      GameSettings settings,
      PlayerRoundEngine playerRoundEngine,
      DealerPlayEngine dealerPlayEngine,
      Shoe shoe,
      HandStrategy strategy,
      CountingSystem count,
      double initialBet,
      double[] bankroll,
      double[] totalWagerExposure)
  {
    bankroll[0] -=
        initialBet;


    totalWagerExposure[0] +=
        initialBet;


    Map<Card, Boolean> observed =
        new IdentityHashMap<>();


    Hand playerHand =
        new Hand();


    Hand dealerHand =
        new Hand();


    // ----------------------------------------------------------
    // INITIAL DEAL
    // ----------------------------------------------------------

    Card playerCard1 =
        shoe.drawCard();


    playerHand.addCard(
        playerCard1
    );


    observeVisible(
        count,
        observed,
        playerCard1
    );


    Card dealerUpCard =
        shoe.drawCard();


    dealerHand.addCard(
        dealerUpCard
    );


    observeVisible(
        count,
        observed,
        dealerUpCard
    );


    Card playerCard2 =
        shoe.drawCard();


    playerHand.addCard(
        playerCard2
    );


    observeVisible(
        count,
        observed,
        playerCard2
    );


    /*
     * Hole card leaves shoe but is hidden.
     */
    Card dealerHoleCard =
        shoe.drawCard();


    dealerHand.addCard(
        dealerHoleCard
    );


    boolean playerBlackjack =
        playerHand.isBlackjack();


    boolean dealerBlackjack =
        dealerHand.isBlackjack();


    // ----------------------------------------------------------
    // NATURALS
    // ----------------------------------------------------------

    if (dealerBlackjack)
    {
      observeVisible(
          count,
          observed,
          dealerHoleCard
      );


      if (playerBlackjack)
      {
        bankroll[0] +=
            initialBet;
      }


      return;
    }


    if (playerBlackjack)
    {
      bankroll[0] +=
          initialBet
          * settings
              .getBlackjackPayout()
              .getTotalReturnMultiplier();


      /*
       * Dealer hole remains unseen and
       * therefore uncounted.
       */
      return;
    }


    // ----------------------------------------------------------
    // PLAYER
    // ----------------------------------------------------------

    PlayerHandState initialState =
        new PlayerHandState(
            playerHand
        );


    List<PlayerHandState> playerStates =
        playerRoundEngine.play(
            initialState,
            dealerUpCard,
            strategy,
            shoe,
            new PlayerRoundListener()
            {
              @Override
              public boolean canDoubleOrSplit(
                  PlayerHandState state)
              {
                return bankroll[0]
                    >= initialBet;
              }


              @Override
              public double getWagerAmount(
                  PlayerHandState state)
              {
                return initialBet;
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
                    count,
                    observed,
                    card
                );
              }


              @Override
              public void handDoubled(
                  PlayerHandState state)
              {
                bankroll[0] -=
                    initialBet;


                totalWagerExposure[0] +=
                    initialBet;
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
                    initialBet;


                totalWagerExposure[0] +=
                    initialBet;


                observeHand(
                    count,
                    observed,
                    originalState.getHand()
                );


                observeHand(
                    count,
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
    // DEALER
    // ----------------------------------------------------------

    boolean dealerNeedsToPlay =
        false;


    for (PlayerHandState state :
         playerStates)
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
          count,
          observed,
          dealerHoleCard
      );


      dealerPlayEngine.play(
          dealerHand,
          shoe,
          card ->
              observeVisible(
                  count,
                  observed,
                  card
              )
      );
    }


    // ----------------------------------------------------------
    // SETTLEMENT
    // ----------------------------------------------------------

    for (PlayerHandState state :
         playerStates)
    {
      HandOutcome outcome =
          HandOutcomeAnalyzer.analyze(
              state.getHand(),
              dealerHand,
              false,
              state.didSurrender()
          );


      double wager =
          initialBet
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
              initialBet * 0.5;
          break;


        case BLACKJACK:
          throw new IllegalStateException(
              "Natural blackjack should "
              + "have been resolved earlier"
          );


        default:
          throw new IllegalStateException(
              "Unexpected outcome: "
              + outcome
          );
      }
    }
  }


  // ============================================================
  // VISIBILITY
  // ============================================================

  private static void observeHand(
      CountingSystem count,
      Map<Card, Boolean> observed,
      Hand hand)
  {
    if (count == null)
    {
      return;
    }


    for (int i = 0;
         i < hand.getNumCards();
         i++)
    {
      observeVisible(
          count,
          observed,
          hand.getCard(i)
      );
    }
  }


  private static void observeVisible(
      CountingSystem count,
      Map<Card, Boolean> observed,
      Card card)
  {
    if (count == null)
    {
      return;
    }


    if (observed.containsKey(card))
    {
      return;
    }


    observed.put(
        card,
        true
    );


    count.observe(
        card
    );
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
  // OUTPUT
  // ============================================================

  private static void printPolicySummary(
      PolicySummary summary)
  {
    System.out.println();

    System.out.println(
        summary.policy()
    );


    System.out.println(
        "----------------------------------------"
    );


    System.out.printf(
        Locale.US,
        "Average ending bankroll: $%.2f%n",
        summary.meanEndingBankroll()
    );


    System.out.printf(
        Locale.US,
        "Average P/L: $%+.2f%n",
        summary.meanEndingBankroll()
            - STARTING_BANKROLL
    );


    System.out.printf(
        Locale.US,
        "Ending bankroll SD: $%.2f%n",
        summary.endingBankrollSD()
    );


    System.out.printf(
        Locale.US,
        "Unable to continue: %.3f%%%n",
        summary.ruinRate()
            * 100.0
    );


    System.out.printf(
        Locale.US,
        "Ending bankroll >= $400: %.3f%%%n",
        summary.acceptableNightRate()
            * 100.0
    );


    System.out.printf(
        Locale.US,
        "Profitable sessions: %.3f%%%n",
        summary.profitableSessionRate()
            * 100.0
    );


    System.out.printf(
        Locale.US,
        "Average hands played: %.2f%n",
        summary.averageHandsPlayed()
    );


    System.out.printf(
        Locale.US,
        "Average initial wager: $%.2f%n",
        summary.averageInitialWager()
    );


    System.out.printf(
        Locale.US,
        "Average wager exposure: $%.2f%n",
        summary.averageWagerExposure()
    );


    System.out.printf(
        Locale.US,
        "Return / initial wagered: %+.4f%%%n",
        summary.returnOnInitialWager()
            * 100.0
    );


    System.out.printf(
        Locale.US,
        "Return / total exposure: %+.4f%%%n",
        summary.returnOnExposure()
            * 100.0
    );


    System.out.printf(
        Locale.US,
        "Hands above minimum: %.3f%%%n",
        summary.aboveMinimumRate()
            * 100.0
    );


    System.out.printf(
        Locale.US,
        "$25 hands: %.3f%%%n",
        summary.bet25Rate()
            * 100.0
    );


    System.out.printf(
        Locale.US,
        "$50 hands: %.3f%%%n",
        summary.bet50Rate()
            * 100.0
    );


    System.out.printf(
        Locale.US,
        "Average session max bet: $%.2f%n",
        summary.averageMaxInitialBet()
    );


    System.out.printf(
        Locale.US,
        "Average wager changes/session: %.2f%n",
        summary.averageWagerChanges()
    );


    System.out.printf(
        Locale.US,
        "Average table switches/session: %.4f%n",
        summary.averageTableSwitches()
    );


    System.out.printf(
        Locale.US,
        "Midpoint decisions/session: %.4f%n",
        summary.averageMidpointDecisions()
    );


    System.out.printf(
        Locale.US,
        "Midpoint abandons/session: %.4f%n",
        summary.averageMidpointAbandons()
    );


    System.out.printf(
        Locale.US,
        "Midpoint abandonment rate: %.3f%%%n",
        summary.midpointAbandonmentRate()
            * 100.0
    );
  }


  private static void printPairSummary(
      PairSummary summary)
  {
    System.out.println();

    System.out.println(
        summary.testPolicy()
        + " vs "
        + summary.referencePolicy()
    );


    System.out.println(
        "----------------------------------------"
    );


    System.out.printf(
        Locale.US,
        "Average P/L difference: $%+.3f%n",
        summary.meanDifference()
    );


    System.out.printf(
        Locale.US,
        "95%% CI: $%+.3f to $%+.3f%n",
        summary.meanDifference()
            - summary.ci95(),
        summary.meanDifference()
            + summary.ci95()
    );


    System.out.printf(
        Locale.US,
        "Test finishes ahead: %.3f%%%n",
        summary.testBetterRate()
            * 100.0
    );


    System.out.printf(
        Locale.US,
        "Same bankroll: %.3f%%%n",
        summary.sameRate()
            * 100.0
    );


    System.out.printf(
        Locale.US,
        "Reference finishes ahead: %.3f%%%n",
        summary.referenceBetterRate()
            * 100.0
    );


    System.out.printf(
        Locale.US,
        "Ruin-rate difference: %+.3f pp%n",
        summary.ruinRateDifference()
            * 100.0
    );


    System.out.printf(
        Locale.US,
        "$400+ night difference: %+.3f pp%n",
        summary.acceptableRateDifference()
            * 100.0
    );
  }


  private static void writeHeader()
      throws IOException
  {
    String header =
        String.join(
            ",",
            "policy",
            "sessions",
            "hands_per_session",
            "starting_bankroll",
            "base_bet",
            "mean_ending_bankroll",
            "mean_profit_loss",
            "ending_bankroll_sd",
            "ruin_rate",
            "acceptable_night_rate",
            "profitable_session_rate",
            "average_hands_played",
            "average_initial_wager",
            "average_wager_exposure",
            "return_on_initial_wager",
            "return_on_total_exposure",
            "above_minimum_rate",
            "bet_25_rate",
            "bet_50_rate",
            "average_max_initial_bet",
            "average_wager_changes",
            "average_table_switches",
            "average_midpoint_decisions",
            "average_midpoint_abandons",
            "midpoint_abandonment_rate"
        )
        + System.lineSeparator();


    Files.writeString(
        OUTPUT_FILE,
        header,
        StandardOpenOption.CREATE
    );
  }


  private static void writePolicySummary(
      PolicySummary summary)
      throws IOException
  {
    String row =
        String.format(
            Locale.US,

            "%s,%d,%d,%.2f,%.2f,"
            + "%.10f,%.10f,%.10f,"
            + "%.10f,%.10f,%.10f,"
            + "%.10f,%.10f,%.10f,"
            + "%.10f,%.10f,"
            + "%.10f,%.10f,%.10f,"
            + "%.10f,%.10f,"
            + "%.10f,%.10f,%.10f,%.10f%n",

            summary.policy(),

            SESSIONS,

            HANDS_PER_SESSION,

            STARTING_BANKROLL,

            BASE_BET,

            summary.meanEndingBankroll(),

            summary.meanEndingBankroll()
                - STARTING_BANKROLL,

            summary.endingBankrollSD(),

            summary.ruinRate(),

            summary.acceptableNightRate(),

            summary.profitableSessionRate(),

            summary.averageHandsPlayed(),

            summary.averageInitialWager(),

            summary.averageWagerExposure(),

            summary.returnOnInitialWager(),

            summary.returnOnExposure(),

            summary.aboveMinimumRate(),

            summary.bet25Rate(),

            summary.bet50Rate(),

            summary.averageMaxInitialBet(),

            summary.averageWagerChanges(),

            summary.averageTableSwitches(),

            summary.averageMidpointDecisions(),

            summary.averageMidpointAbandons(),

            summary.midpointAbandonmentRate()
        );


    Files.writeString(
        OUTPUT_FILE,
        row,
        StandardOpenOption.APPEND
    );
  }


  // ============================================================
  // DATA
  // ============================================================

  private record SessionState(
      Policy policy,
      double bankroll,
      int handsPlayed,
      boolean unableToContinue,
      boolean acceptableNight,
      boolean profitableSession,
      double totalInitialWager,
      double wagerExposure,
      long bet25Hands,
      long bet50Hands,
      long wagerChanges,
      double maxInitialBet,
      int tableSwitches,
      int midpointDecisions,
      int midpointAbandons)
  {
  }


  private record PolicySummary(
      Policy policy,
      double meanEndingBankroll,
      double endingBankrollSD,
      double ruinRate,
      double acceptableNightRate,
      double profitableSessionRate,
      double averageHandsPlayed,
      double averageInitialWager,
      double averageWagerExposure,
      double returnOnInitialWager,
      double returnOnExposure,
      double aboveMinimumRate,
      double bet25Rate,
      double bet50Rate,
      double averageMaxInitialBet,
      double averageWagerChanges,
      double averageTableSwitches,
      double averageMidpointDecisions,
      double averageMidpointAbandons,
      double midpointAbandonmentRate)
  {
  }


  private static class PolicyAccumulator
  {
    private final Policy policy;


    private long count;


    private double meanBankroll;

    private double bankrollM2;


    private long ruinCount;

    private long acceptableCount;

    private long profitableCount;


    private long totalHands;


    private double totalInitialWager;

    private double totalWagerExposure;


    private long bet25Hands;

    private long bet50Hands;


    private long totalWagerChanges;


    private double totalMaxInitialBet;


    private long totalTableSwitches;

    private long totalMidpointDecisions;

    private long totalMidpointAbandons;


    PolicyAccumulator(
        Policy policy)
    {
      this.policy =
          policy;
    }


    void add(
        SessionState state)
    {
      count++;


      double delta =
          state.bankroll()
          - meanBankroll;


      meanBankroll +=
          delta / count;


      double delta2 =
          state.bankroll()
          - meanBankroll;


      bankrollM2 +=
          delta
          * delta2;


      if (state.unableToContinue())
      {
        ruinCount++;
      }


      if (state.acceptableNight())
      {
        acceptableCount++;
      }


      if (state.profitableSession())
      {
        profitableCount++;
      }


      totalHands +=
          state.handsPlayed();


      totalInitialWager +=
          state.totalInitialWager();


      totalWagerExposure +=
          state.wagerExposure();


      bet25Hands +=
          state.bet25Hands();


      bet50Hands +=
          state.bet50Hands();


      totalWagerChanges +=
          state.wagerChanges();


      totalMaxInitialBet +=
          state.maxInitialBet();


      totalTableSwitches +=
          state.tableSwitches();


      totalMidpointDecisions +=
          state.midpointDecisions();


      totalMidpointAbandons +=
          state.midpointAbandons();
    }


    PolicySummary finish()
    {
      double sd =
          count <= 1
              ? 0.0
              : Math.sqrt(
                  bankrollM2
                  / (count - 1)
              );


      long aboveMinimumHands =
          bet50Hands;


      double meanProfit =
          meanBankroll
          - STARTING_BANKROLL;


      double averageInitialWagerTotal =
          totalInitialWager
          / count;


      double averageExposure =
          totalWagerExposure
          / count;


      double returnOnInitialWager =
          averageInitialWagerTotal == 0.0
              ? 0.0
              : meanProfit
                / averageInitialWagerTotal;


      double returnOnExposure =
          averageExposure == 0.0
              ? 0.0
              : meanProfit
                / averageExposure;


      double midpointAbandonmentRate =
          totalMidpointDecisions == 0
              ? 0.0
              : (double) totalMidpointAbandons
                / totalMidpointDecisions;


      return new PolicySummary(
          policy,

          meanBankroll,

          sd,

          (double) ruinCount
              / count,

          (double) acceptableCount
              / count,

          (double) profitableCount
              / count,

          (double) totalHands
              / count,

          totalInitialWager
              / totalHands,

          averageExposure,

          returnOnInitialWager,

          returnOnExposure,

          (double) aboveMinimumHands
              / totalHands,

          (double) bet25Hands
              / totalHands,

          (double) bet50Hands
              / totalHands,

          totalMaxInitialBet
              / count,

          (double) totalWagerChanges
              / count,

          (double) totalTableSwitches
              / count,

          (double) totalMidpointDecisions
              / count,

          (double) totalMidpointAbandons
              / count,

          midpointAbandonmentRate
      );
    }
  }


  private record PairSummary(
      Policy testPolicy,
      Policy referencePolicy,
      double meanDifference,
      double differenceSD,
      double standardError,
      double ci95,
      double testBetterRate,
      double sameRate,
      double referenceBetterRate,
      double ruinRateDifference,
      double acceptableRateDifference)
  {
  }


  private static class PairAccumulator
  {
    private final Policy testPolicy;

    private final Policy referencePolicy;


    private long count;


    private double meanDifference;

    private double differenceM2;


    private long testBetterCount;

    private long sameCount;

    private long referenceBetterCount;


    private long testRuinCount;

    private long referenceRuinCount;


    private long testAcceptableCount;

    private long referenceAcceptableCount;


    PairAccumulator(
        Policy testPolicy,
        Policy referencePolicy)
    {
      this.testPolicy =
          testPolicy;


      this.referencePolicy =
          referencePolicy;
    }


    void add(
        SessionState test,
        SessionState reference)
    {
      count++;


      double difference =
          test.bankroll()
          - reference.bankroll();


      double delta =
          difference
          - meanDifference;


      meanDifference +=
          delta / count;


      double delta2 =
          difference
          - meanDifference;


      differenceM2 +=
          delta
          * delta2;


      if (difference > 0.0)
      {
        testBetterCount++;
      }
      else if (difference < 0.0)
      {
        referenceBetterCount++;
      }
      else
      {
        sameCount++;
      }


      if (test.unableToContinue())
      {
        testRuinCount++;
      }


      if (reference.unableToContinue())
      {
        referenceRuinCount++;
      }


      if (test.acceptableNight())
      {
        testAcceptableCount++;
      }


      if (reference.acceptableNight())
      {
        referenceAcceptableCount++;
      }
    }


    PairSummary finish()
    {
      double sd =
          count <= 1
              ? 0.0
              : Math.sqrt(
                  differenceM2
                  / (count - 1)
              );


      double se =
          sd
          / Math.sqrt(
              count
          );


      return new PairSummary(
          testPolicy,

          referencePolicy,

          meanDifference,

          sd,

          se,

          CI_95_Z
              * se,

          (double) testBetterCount
              / count,

          (double) sameCount
              / count,

          (double) referenceBetterCount
              / count,

          (
              (double) testRuinCount
              - referenceRuinCount
          )
          / count,

          (
              (double) testAcceptableCount
              - referenceAcceptableCount
          )
          / count
      );
    }
  }
}
