import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class AceFiveBankrollSpreadResearchRunner
{
  private static final int SESSIONS =
      250_000;


  private static final int HANDS_PER_SESSION =
      160;


  private static final double BASE_BET =
      25.0;


  private static final double[] BANKROLLS =
  {
      500.0,
      1000.0,
      2000.0,
      5000.0
  };


  private static final long BASE_SEED =
      10_24_2026L;


  private static final double CI_95_Z =
      1.96;


  private static final Path OUTPUT_FILE =
      Path.of(
          "research-data",
          "scratch",
          "ace-five-bankroll-spread-study-250k.csv"
      );


  private enum Policy
  {
    FLAT_BASIC(25.0),

    ACE_FIVE_1_TO_2(50.0),

    ACE_FIVE_1_TO_3(75.0),

    ACE_FIVE_1_TO_4(100.0),

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
        Policy.ACE_FIVE_1_TO_8
    };


    for (double startingBankroll :
         BANKROLLS)
    {
      runBankrollStudy(
          settings,
          policies,
          startingBankroll
      );
    }


    System.out.println();

    System.out.println(
        "Saved results to "
        + OUTPUT_FILE.toAbsolutePath()
    );
  }


  // ============================================================
  // BANKROLL STUDY
  // ============================================================

  private static void runBankrollStudy(
      GameSettings settings,
      Policy[] policies,
      double startingBankroll)
      throws IOException
  {
    System.out.println();

    System.out.println(
        "========================================"
    );

    System.out.printf(
        Locale.US,
        "BANKROLL: $%.0f (%.0f units)%n",
        startingBankroll,
        startingBankroll / BASE_BET
    );

    System.out.println(
        "========================================"
    );


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
              policies[i],
              startingBankroll
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
      /*
       * Every policy within this bankroll
       * receives the same starting shoe seed.
       */
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
                startingBankroll,
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
            + " paired sessions for $"
            + String.format(
                Locale.US,
                "%.0f",
                startingBankroll
            )
            + " bankroll..."
        );
      }
    }


    System.out.println();

    System.out.println(
        "RESULTS"
    );

    System.out.println(
        "----------------------------------------"
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
        "VS FLAT BASIC"
    );

    System.out.println(
        "----------------------------------------"
    );


    for (int i = 1;
         i < policies.length;
         i++)
    {
      printPairSummary(
          startingBankroll,
          vsFlat[i].finish()
      );
    }
  }


  // ============================================================
  // SESSION
  // ============================================================

  private static SessionState playSession(
      GameSettings settings,
      Policy policy,
      double startingBankroll,
      long seed)
  {
    double[] bankroll =
    {
        startingBankroll
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


    long[] bet200Hands =
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
            startingBankroll,
            bankroll[0],
            handsPlayed,
            true,
            totalInitialWager[0],
            totalWagerExposure[0],
            bet25Hands[0],
            bet50Hands[0],
            bet75Hands[0],
            bet100Hands[0],
            bet200Hands[0],
            wagerChanges[0],
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
      else if (initialBet == 75.0)
      {
        bet75Hands[0]++;
      }
      else if (initialBet == 100.0)
      {
        bet100Hands[0]++;
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
        startingBankroll,
        bankroll[0],
        handsPlayed,
        false,
        totalInitialWager[0],
        totalWagerExposure[0],
        bet25Hands[0],
        bet50Hands[0],
        bet75Hands[0],
        bet100Hands[0],
        bet200Hands[0],
        wagerChanges[0],
        maxInitialBet[0]
    );
  }


  private static SessionState createSessionState(
      Policy policy,
      double startingBankroll,
      double endingBankroll,
      int handsPlayed,
      boolean unableToContinue,
      double totalInitialWager,
      double totalWagerExposure,
      long bet25Hands,
      long bet50Hands,
      long bet75Hands,
      long bet100Hands,
      long bet200Hands,
      long wagerChanges,
      double maxInitialBet)
  {
    /*
     * "Protected bankroll" means the session
     * finishes with at least 80% of the
     * starting bankroll.
     *
     * This generalizes the old $400 threshold
     * used for a $500 bankroll.
     */
    double protectedThreshold =
        startingBankroll
        * 0.80;


    return new SessionState(
        policy,

        startingBankroll,

        endingBankroll,

        handsPlayed,

        unableToContinue,

        endingBankroll
            >= protectedThreshold,

        endingBankroll
            > startingBankroll,

        totalInitialWager,

        totalWagerExposure,

        bet25Hands,

        bet50Hands,

        bet75Hands,

        bet100Hands,

        bet200Hands,

        wagerChanges,

        maxInitialBet
    );
  }


  // ============================================================
  // BETTING
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
     * Same standard/simple Ace-Five rule
     * across every spread.
     *
     * RC >= +2:
     * double previous wager up to max.
     *
     * RC <= +1:
     * reset immediately to one unit.
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


  private static double capBetToBankroll(
      double requestedBet,
      double bankroll)
  {
    if (requestedBet >= 200.0
        && bankroll >= 200.0)
    {
      return 200.0;
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
        summary.meanProfitLoss()
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
        "Finish with >=80%% bankroll: %.3f%%%n",
        summary.protectedBankrollRate()
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
        "Average session max bet: $%.2f%n",
        summary.averageMaxInitialBet()
    );


    System.out.printf(
        Locale.US,
        "Average wager changes/session: %.2f%n",
        summary.averageWagerChanges()
    );
  }


  private static void printPairSummary(
      double startingBankroll,
      PairSummary summary)
  {
    System.out.println();

    System.out.println(
        summary.testPolicy()
        + " vs "
        + summary.referencePolicy()
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
        "Ruin-rate difference: %+.3f pp%n",
        summary.ruinRateDifference()
            * 100.0
    );


    System.out.printf(
        Locale.US,
        ">=80%% bankroll difference: %+.3f pp%n",
        summary.protectedRateDifference()
            * 100.0
    );
  }


  private static void writeHeader()
      throws IOException
  {
    String header =
        String.join(
            ",",
            "starting_bankroll",
            "starting_units",
            "policy",
            "max_bet",
            "sessions",
            "hands_per_session",
            "mean_ending_bankroll",
            "mean_profit_loss",
            "ending_bankroll_sd",
            "ruin_rate",
            "protected_bankroll_rate",
            "profitable_session_rate",
            "average_hands_played",
            "average_initial_wager",
            "average_wager_exposure",
            "return_on_initial_wager",
            "return_on_total_exposure",
            "above_minimum_rate",
            "average_max_initial_bet",
            "average_wager_changes"
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
            "%.2f,%.2f,%s,%.2f,%d,%d,"
            + "%.10f,%.10f,%.10f,"
            + "%.10f,%.10f,%.10f,"
            + "%.10f,%.10f,%.10f,"
            + "%.10f,%.10f,%.10f,"
            + "%.10f,%.10f%n",

            summary.startingBankroll(),

            summary.startingBankroll()
                / BASE_BET,

            summary.policy(),

            summary.policy()
                .getMaxBet(),

            SESSIONS,

            HANDS_PER_SESSION,

            summary.meanEndingBankroll(),

            summary.meanProfitLoss(),

            summary.endingBankrollSD(),

            summary.ruinRate(),

            summary.protectedBankrollRate(),

            summary.profitableSessionRate(),

            summary.averageHandsPlayed(),

            summary.averageInitialWager(),

            summary.averageWagerExposure(),

            summary.returnOnInitialWager(),

            summary.returnOnExposure(),

            summary.aboveMinimumRate(),

            summary.averageMaxInitialBet(),

            summary.averageWagerChanges()
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
      double startingBankroll,
      double endingBankroll,
      int handsPlayed,
      boolean unableToContinue,
      boolean protectedBankroll,
      boolean profitableSession,
      double totalInitialWager,
      double wagerExposure,
      long bet25Hands,
      long bet50Hands,
      long bet75Hands,
      long bet100Hands,
      long bet200Hands,
      long wagerChanges,
      double maxInitialBet)
  {
  }


  private record PolicySummary(
      Policy policy,
      double startingBankroll,
      double meanEndingBankroll,
      double meanProfitLoss,
      double endingBankrollSD,
      double ruinRate,
      double protectedBankrollRate,
      double profitableSessionRate,
      double averageHandsPlayed,
      double averageInitialWager,
      double averageWagerExposure,
      double returnOnInitialWager,
      double returnOnExposure,
      double aboveMinimumRate,
      double averageMaxInitialBet,
      double averageWagerChanges)
  {
  }


  private static class PolicyAccumulator
  {
    private final Policy policy;

    private final double startingBankroll;


    private long count;


    private double meanBankroll;

    private double bankrollM2;


    private long ruinCount;

    private long protectedCount;

    private long profitableCount;


    private long totalHands;


    private double totalInitialWager;

    private double totalWagerExposure;


    private long bet25Hands;

    private long bet50Hands;

    private long bet75Hands;

    private long bet100Hands;

    private long bet200Hands;


    private long totalWagerChanges;


    private double totalMaxInitialBet;


    PolicyAccumulator(
        Policy policy,
        double startingBankroll)
    {
      this.policy =
          policy;


      this.startingBankroll =
          startingBankroll;
    }


    void add(
        SessionState state)
    {
      count++;


      double delta =
          state.endingBankroll()
          - meanBankroll;


      meanBankroll +=
          delta / count;


      double delta2 =
          state.endingBankroll()
          - meanBankroll;


      bankrollM2 +=
          delta
          * delta2;


      if (state.unableToContinue())
      {
        ruinCount++;
      }


      if (state.protectedBankroll())
      {
        protectedCount++;
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


      bet200Hands +=
          state.bet200Hands();


      totalWagerChanges +=
          state.wagerChanges();


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
          + bet200Hands;


      double meanProfit =
          meanBankroll
          - startingBankroll;


      double averageInitialWagerTotal =
          totalInitialWager
          / count;


      double averageExposure =
          totalWagerExposure
          / count;


      return new PolicySummary(
          policy,

          startingBankroll,

          meanBankroll,

          meanProfit,

          sd,

          (double) ruinCount
              / count,

          (double) protectedCount
              / count,

          (double) profitableCount
              / count,

          (double) totalHands
              / count,

          totalInitialWager
              / totalHands,

          averageExposure,

          meanProfit
              / averageInitialWagerTotal,

          meanProfit
              / averageExposure,

          (double) aboveMinimumHands
              / totalHands,

          totalMaxInitialBet
              / count,

          (double) totalWagerChanges
              / count
      );
    }
  }


  private record PairSummary(
      Policy testPolicy,
      Policy referencePolicy,
      double meanDifference,
      double ci95,
      double ruinRateDifference,
      double protectedRateDifference)
  {
  }


  private static class PairAccumulator
  {
    private final Policy testPolicy;

    private final Policy referencePolicy;


    private long count;


    private double meanDifference;

    private double differenceM2;


    private long testRuinCount;

    private long referenceRuinCount;


    private long testProtectedCount;

    private long referenceProtectedCount;


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
          test.endingBankroll()
          - reference.endingBankroll();


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


      if (test.unableToContinue())
      {
        testRuinCount++;
      }


      if (reference.unableToContinue())
      {
        referenceRuinCount++;
      }


      if (test.protectedBankroll())
      {
        testProtectedCount++;
      }


      if (reference.protectedBankroll())
      {
        referenceProtectedCount++;
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

          CI_95_Z
              * se,

          (
              (double) testRuinCount
              - referenceRuinCount
          )
          / count,

          (
              (double) testProtectedCount
              - referenceProtectedCount
          )
          / count
      );
    }
  }
}