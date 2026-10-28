import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class AceFiveSpreadResearchRunner
{
  private static final int SESSIONS =
      500_000;


  private static final int HANDS_PER_SESSION =
      160;


  /*
   * Realistic social-night bankroll.
   */
  private static final double STARTING_BANKROLL =
      500.0;


  private static final double BASE_BET =
      25.0;


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
          "ace-five-spread-study-500k-160hands-500bankroll.csv"
      );


  private enum Policy
  {
    FLAT_BASIC(25.0),

    ACE_FIVE_1_TO_2(50.0),

    ACE_FIVE_1_TO_3(75.0),

    ACE_FIVE_1_TO_4(100.0),

    ACE_FIVE_1_TO_6(150.0),

    ACE_FIVE_1_TO_8(200.0);


    private final double maxBet;


    Policy(
        double maxBet)
    {
      this.maxBet =
          maxBet;
    }


    public double getMaxBet()
    {
      return maxBet;
    }
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

        Policy.ACE_FIVE_1_TO_3,

        Policy.ACE_FIVE_1_TO_4,

        Policy.ACE_FIVE_1_TO_6,

        Policy.ACE_FIVE_1_TO_8
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
        "ACE/FIVE SPREAD STUDY"
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


    long[] bet75Hands =
    {
        0
    };


    long[] bet100Hands =
    {
        0
    };


    long[] bet150Hands =
    {
        0
    };


    long[] bet200Hands =
    {
        0
    };


    long[] wagerChanges =
    {
        0
    };


    long[] wagerIncreases =
    {
        0
    };


    long[] wagerDecreases =
    {
        0
    };


    double[] maxInitialBet =
    {
        0.0
    };


    int handsPlayed =
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


    AceFiveCount count =
        null;


    if (policy
        != Policy.FLAT_BASIC)
    {
      count =
          new AceFiveCount();
    }


    double previousBet =
        BASE_BET;


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
            bet75Hands[0],
            bet100Hands[0],
            bet150Hands[0],
            bet200Hands[0],
            wagerChanges[0],
            wagerIncreases[0],
            wagerDecreases[0],
            maxInitialBet[0]
        );
      }


      if (shoe.needsShuffle())
      {
        shoe.shuffle();


        if (count != null)
        {
          count.reset();
        }


        previousBet =
            BASE_BET;
      }


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


      /*
       * Track visible wager movement.
       */
      if (initialBet != oldBet)
      {
        wagerChanges[0]++;


        if (initialBet > oldBet)
        {
          wagerIncreases[0]++;
        }
        else
        {
          wagerDecreases[0]++;
        }
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
      else if (initialBet == 75.0)
      {
        bet75Hands[0]++;
      }
      else if (initialBet == 100.0)
      {
        bet100Hands[0]++;
      }
      else if (initialBet == 150.0)
      {
        bet150Hands[0]++;
      }
      else if (initialBet == 200.0)
      {
        bet200Hands[0]++;
      }
      else
      {
        throw new IllegalStateException(
            "Unexpected initial bet: "
            + initialBet
        );
      }


      maxInitialBet[0] =
          Math.max(
              maxInitialBet[0],
              initialBet
          );


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
        bet75Hands[0],
        bet100Hands[0],
        bet150Hands[0],
        bet200Hands[0],
        wagerChanges[0],
        wagerIncreases[0],
        wagerDecreases[0],
        maxInitialBet[0]
    );
  }


  private static SessionState createSessionState(
      Policy policy,
      double bankroll,
      int handsPlayed,
      boolean unableToContinue,
      double totalInitialWager,
      double totalWagerExposure,
      long bet25Hands,
      long bet50Hands,
      long bet75Hands,
      long bet100Hands,
      long bet150Hands,
      long bet200Hands,
      long wagerChanges,
      long wagerIncreases,
      long wagerDecreases,
      double maxInitialBet)
  {
    return new SessionState(
        policy,

        bankroll,

        handsPlayed,

        unableToContinue,

        bankroll
            >= ACCEPTABLE_NIGHT_THRESHOLD,

        bankroll
            > STARTING_BANKROLL,

        totalInitialWager,

        totalWagerExposure,

        bet25Hands,

        bet50Hands,

        bet75Hands,

        bet100Hands,

        bet150Hands,

        bet200Hands,

        wagerChanges,

        wagerIncreases,

        wagerDecreases,

        maxInitialBet
    );
  }


  // ============================================================
  // BETTING POLICY
  // ============================================================

  private static double chooseBet(
      Policy policy,
      AceFiveCount count,
      Shoe shoe,
      double previousBet)
  {
    if (policy
        == Policy.FLAT_BASIC)
    {
      return BASE_BET;
    }


    double runningCount =
        count.getSignal(
            shoe.getRemainingCardCount()
        );


    /*
     * Same simple Ace/Five progression
     * for every tested spread:
     *
     * RC >= +2:
     *   double previous bet,
     *   capped at that policy's maximum.
     *
     * RC <= +1:
     *   immediately reset to $25.
     *
     * Examples:
     *
     * 1-2:
     *   25 -> 50
     *
     * 1-3:
     *   25 -> 50 -> 75
     *
     * 1-4:
     *   25 -> 50 -> 100
     *
     * 1-6:
     *   25 -> 50 -> 100 -> 150
     *
     * 1-8:
     *   25 -> 50 -> 100 -> 200
     */
    if (runningCount >= 2.0)
    {
      return Math.min(
          previousBet * 2.0,
          policy.getMaxBet()
      );
    }


    return BASE_BET;
  }


  /*
   * Use the largest normal wager level
   * supported by the player's bankroll.
   */
  private static double capBetToBankroll(
      double requestedBet,
      double bankroll)
  {
    if (requestedBet >= 200.0
        && bankroll >= 200.0)
    {
      return 200.0;
    }


    if (requestedBet >= 150.0
        && bankroll >= 150.0)
    {
      return 150.0;
    }


    if (requestedBet >= 100.0
        && bankroll >= 100.0)
    {
      return 100.0;
    }


    if (requestedBet >= 75.0
        && bankroll >= 75.0)
    {
      return 75.0;
    }


    if (requestedBet >= 50.0
        && bankroll >= 50.0)
    {
      return 50.0;
    }


    return BASE_BET;
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
      AceFiveCount count,
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


      return;
    }


    // ----------------------------------------------------------
    // PLAYER PLAY
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
    // DEALER PLAY
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
  // CARD VISIBILITY
  // ============================================================

  private static void observeHand(
      AceFiveCount count,
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
      AceFiveCount count,
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
        "$75 hands: %.3f%%%n",
        summary.bet75Rate()
            * 100.0
    );


    System.out.printf(
        Locale.US,
        "$100 hands: %.3f%%%n",
        summary.bet100Rate()
            * 100.0
    );


    System.out.printf(
        Locale.US,
        "$150 hands: %.3f%%%n",
        summary.bet150Rate()
            * 100.0
    );


    System.out.printf(
        Locale.US,
        "$200 hands: %.3f%%%n",
        summary.bet200Rate()
            * 100.0
    );


    System.out.printf(
        Locale.US,
        "Average session max initial bet: $%.2f%n",
        summary.averageMaxInitialBet()
    );


    System.out.printf(
        Locale.US,
        "Average wager changes/session: %.2f%n",
        summary.averageWagerChanges()
    );


    System.out.printf(
        Locale.US,
        "Average wager increases/session: %.2f%n",
        summary.averageWagerIncreases()
    );


    System.out.printf(
        Locale.US,
        "Average wager decreases/session: %.2f%n",
        summary.averageWagerDecreases()
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
            "max_bet",
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
            "bet_75_rate",
            "bet_100_rate",
            "bet_150_rate",
            "bet_200_rate",
            "average_max_initial_bet",
            "average_wager_changes",
            "average_wager_increases",
            "average_wager_decreases"
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

            "%s,%d,%d,%.2f,%.2f,%.2f,"
            + "%.10f,%.10f,%.10f,"
            + "%.10f,%.10f,%.10f,"
            + "%.10f,%.10f,%.10f,"
            + "%.10f,%.10f,"
            + "%.10f,"
            + "%.10f,%.10f,%.10f,"
            + "%.10f,%.10f,%.10f,"
            + "%.10f,"
            + "%.10f,%.10f,%.10f%n",

            summary.policy(),

            SESSIONS,

            HANDS_PER_SESSION,

            STARTING_BANKROLL,

            BASE_BET,

            summary.policy().getMaxBet(),

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

            summary.bet75Rate(),

            summary.bet100Rate(),

            summary.bet150Rate(),

            summary.bet200Rate(),

            summary.averageMaxInitialBet(),

            summary.averageWagerChanges(),

            summary.averageWagerIncreases(),

            summary.averageWagerDecreases()
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
      long bet75Hands,
      long bet100Hands,
      long bet150Hands,
      long bet200Hands,
      long wagerChanges,
      long wagerIncreases,
      long wagerDecreases,
      double maxInitialBet)
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
      double bet75Rate,
      double bet100Rate,
      double bet150Rate,
      double bet200Rate,
      double averageMaxInitialBet,
      double averageWagerChanges,
      double averageWagerIncreases,
      double averageWagerDecreases)
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

    private long bet75Hands;

    private long bet100Hands;

    private long bet150Hands;

    private long bet200Hands;


    private long totalWagerChanges;

    private long totalWagerIncreases;

    private long totalWagerDecreases;


    private double totalMaxInitialBet;


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
          delta
          / count;


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


      bet75Hands +=
          state.bet75Hands();


      bet100Hands +=
          state.bet100Hands();


      bet150Hands +=
          state.bet150Hands();


      bet200Hands +=
          state.bet200Hands();


      totalWagerChanges +=
          state.wagerChanges();


      totalWagerIncreases +=
          state.wagerIncreases();


      totalWagerDecreases +=
          state.wagerDecreases();


      totalMaxInitialBet +=
          state.maxInitialBet();
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
          bet50Hands
          + bet75Hands
          + bet100Hands
          + bet150Hands
          + bet200Hands;


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

          (double) bet75Hands
              / totalHands,

          (double) bet100Hands
              / totalHands,

          (double) bet150Hands
              / totalHands,

          (double) bet200Hands
              / totalHands,

          totalMaxInitialBet
              / count,

          (double) totalWagerChanges
              / count,

          (double) totalWagerIncreases
              / count,

          (double) totalWagerDecreases
              / count
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
          delta
          / count;


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
          count == 0
              ? 0.0
              : sd
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