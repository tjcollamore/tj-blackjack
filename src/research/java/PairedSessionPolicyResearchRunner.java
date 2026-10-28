import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PairedSessionPolicyResearchRunner
{
  private static final int SESSIONS =
      100_000;

  private static final int HANDS_PER_SESSION =
      160;

  private static final double STARTING_BANKROLL =
      500.0;

  private static final double BASE_BET =
      25.0;

  private static final double CI_95_Z =
      1.96;

  /*
   * Fixed base seed makes the entire research
   * experiment reproducible.
   */
  private static final long BASE_SEED =
      9_202_026L;

  private static final Path OUTPUT_FILE =
      Path.of(
          "research-data",
          "scratch",
          "paired-session-policy-hard9-100k-160hands-500bankroll-25bet.csv"
      );

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

    GameSettings.BlackjackPayout[] payouts =
    {
        GameSettings.BlackjackPayout.SIX_TO_FIVE,
        GameSettings.BlackjackPayout.THREE_TO_TWO
    };

    for (GameSettings.BlackjackPayout payout :
         payouts)
    {
      runExperiment(
          payout
      );
    }

    System.out.println();

    System.out.println(
        "Saved results to "
        + OUTPUT_FILE.toAbsolutePath()
    );
  }

  private static void runExperiment(
      GameSettings.BlackjackPayout payout)
      throws IOException
  {
    GameSettings settings =
        createResearchSettings(
            payout
        );

    System.out.println();

    System.out.println(
        "======================================"
    );

    System.out.println(
        "BLACKJACK PAYOUT: "
        + payout
    );

    System.out.println(
        "======================================"
    );

    PolicyAccumulator basicAccumulator =
        new PolicyAccumulator(
            Policy.BASIC_STRATEGY
        );

    PolicyAccumulator noHard9Accumulator =
        new PolicyAccumulator(
            Policy.NO_HARD_9_DOUBLES
        );

    PolicyAccumulator expandedAccumulator =
        new PolicyAccumulator(
            Policy.HARD_9_DOUBLE_2_TO_6
        );

    PairAccumulator noHard9VsBasic =
        new PairAccumulator(
            Policy.NO_HARD_9_DOUBLES,
            Policy.BASIC_STRATEGY
        );

    PairAccumulator expandedVsBasic =
        new PairAccumulator(
            Policy.HARD_9_DOUBLE_2_TO_6,
            Policy.BASIC_STRATEGY
        );

    for (int session = 1;
         session <= SESSIONS;
         session++)
    {
      long seed =
          BASE_SEED
          + session;

      SessionState basic =
          playSession(
              settings,
              Policy.BASIC_STRATEGY,
              seed
          );

      SessionState noHard9 =
          playSession(
              settings,
              Policy.NO_HARD_9_DOUBLES,
              seed
          );

      SessionState expanded =
          playSession(
              settings,
              Policy.HARD_9_DOUBLE_2_TO_6,
              seed
          );

      basicAccumulator.add(
          basic
      );

      noHard9Accumulator.add(
          noHard9
      );

      expandedAccumulator.add(
          expanded
      );

      noHard9VsBasic.add(
          noHard9,
          basic
      );

      expandedVsBasic.add(
          expanded,
          basic
      );

      if (session % 10_000 == 0)
      {
        System.out.println(
            "Completed "
            + session
            + " paired sessions..."
        );
      }
    }

    PolicySummary basicSummary =
        basicAccumulator.finish();

    PolicySummary noHard9Summary =
        noHard9Accumulator.finish();

    PolicySummary expandedSummary =
        expandedAccumulator.finish();

    PairSummary noHard9Pair =
        noHard9VsBasic.finish();

    PairSummary expandedPair =
        expandedVsBasic.finish();

    printPolicySummary(
        basicSummary
    );

    printPolicySummary(
        noHard9Summary
    );

    printPolicySummary(
        expandedSummary
    );

    printPairSummary(
        noHard9Pair
    );

    printPairSummary(
        expandedPair
    );

    writePairSummary(
        payout,
        noHard9Pair
    );

    writePairSummary(
        payout,
        expandedPair
    );
  }

  private static SessionState playSession(
      GameSettings settings,
      Policy policy,
      long seed)
  {
    double[] bankroll =
    {
        STARTING_BANKROLL
    };

    long[] doubles =
    {
        0
    };

    long[] hard9Doubles =
    {
        0
    };

    long[] splits =
    {
        0
    };

    double[] totalAmountWagered =
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

    BasicStrategy basicStrategy =
        new BasicStrategy();

    PlayerRoundEngine playerRoundEngine =
        new PlayerRoundEngine(
            settings
        );

    DealerPlayEngine dealerPlayEngine =
        new DealerPlayEngine(
            settings
        );

    HandStrategy strategy =
        createStrategy(
            policy,
            basicStrategy
        );

    for (int handNumber = 0;
         handNumber < HANDS_PER_SESSION;
         handNumber++)
    {
      if (bankroll[0] < BASE_BET)
      {
        return new SessionState(
            bankroll[0],
            handsPlayed,
            true,
            doubles[0],
            hard9Doubles[0],
            splits[0],
            totalAmountWagered[0]
        );
      }

      if (shoe.needsShuffle())
      {
        shoe.shuffle();
      }

      playRound(
          settings,
          playerRoundEngine,
          dealerPlayEngine,
          shoe,
          strategy,
          bankroll,
          doubles,
          hard9Doubles,
          splits,
          totalAmountWagered
      );

      handsPlayed++;
    }

    return new SessionState(
        bankroll[0],
        handsPlayed,
        false,
        doubles[0],
        hard9Doubles[0],
        splits[0],
        totalAmountWagered[0]
    );
  }

  private static void playRound(
      GameSettings settings,
      PlayerRoundEngine playerRoundEngine,
      DealerPlayEngine dealerPlayEngine,
      Shoe shoe,
      HandStrategy strategy,
      double[] bankroll,
      long[] doubles,
      long[] hard9Doubles,
      long[] splits,
      double[] totalAmountWagered)
  {
    bankroll[0] -=
        BASE_BET;

    totalAmountWagered[0] +=
        BASE_BET;

    Hand playerHand =
        new Hand();

    Hand dealerHand =
        new Hand();

    playerHand.addCard(
        shoe.drawCard()
    );

    Card dealerUpCard =
        shoe.drawCard();

    dealerHand.addCard(
        dealerUpCard
    );

    playerHand.addCard(
        shoe.drawCard()
    );

    dealerHand.addCard(
        shoe.drawCard()
    );

    boolean playerBlackjack =
        playerHand.isBlackjack();

    boolean dealerBlackjack =
        dealerHand.isBlackjack();

    if (dealerBlackjack)
    {
      if (playerBlackjack)
      {
        bankroll[0] +=
            BASE_BET;
      }

      return;
    }

    if (playerBlackjack)
    {
      bankroll[0] +=
          BASE_BET
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
                    >= BASE_BET;
              }

              @Override
              public double getWagerAmount(
                  PlayerHandState state)
              {
                return BASE_BET;
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
              }

              @Override
              public void handDoubled(
                  PlayerHandState state)
              {
                /*
                 * The state still contains the
                 * original two-card hand when
                 * this callback occurs.
                 */
                if (!state.getHand().isSoft()
                    && state.getHand().getScore()
                       == 9)
                {
                  hard9Doubles[0]++;
                }

                bankroll[0] -=
                    BASE_BET;

                totalAmountWagered[0] +=
                    BASE_BET;

                doubles[0]++;
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
                    BASE_BET;

                totalAmountWagered[0] +=
                    BASE_BET;

                splits[0]++;
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
      dealerPlayEngine.play(
          dealerHand,
          shoe,
          null
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
          BASE_BET
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
              BASE_BET * 0.5;
          break;

        case BLACKJACK:
          throw new IllegalStateException(
              "Natural blackjack should have "
              + "been resolved before player play"
          );

        default:
          throw new IllegalStateException(
              "Unexpected outcome: "
              + outcome
          );
      }
    }
  }

  private static HandStrategy createStrategy(
      Policy policy,
      BasicStrategy basicStrategy)
  {
    return context ->
    {
      HandAction basicAction =
          basicStrategy.chooseAction(
              context
          );

      Hand hand =
          context.getPlayerHand();

      boolean hardNine =
          !hand.isSoft()
          && hand.getScore() == 9;

      if (!hardNine)
      {
        return basicAction;
      }

      switch (policy)
      {
        case BASIC_STRATEGY:
          return basicAction;

        case NO_HARD_9_DOUBLES:
        {
          if (basicAction
              == HandAction.DOUBLE)
          {
            return HandAction.HIT;
          }

          return basicAction;
        }

        case HARD_9_DOUBLE_2_TO_6:
        {
          int dealer =
              context
                  .getDealerUpCard()
                  .getPointValue();

          if (dealer >= 2
              && dealer <= 6)
          {
            if (context.isAvailable(
                HandAction.DOUBLE))
            {
              return HandAction.DOUBLE;
            }

            return HandAction.HIT;
          }

          return basicAction;
        }

        default:
          throw new IllegalStateException(
              "Unexpected policy: "
              + policy
          );
      }
    };
  }

  private static void printPolicySummary(
      PolicySummary summary)
  {
    System.out.println();

    System.out.println(
        summary.policy()
    );

    System.out.println(
        "--------------------------------------"
    );

    System.out.printf(
        Locale.US,
        "Average ending bankroll: $%.2f%n",
        summary.meanEndingBankroll()
    );

    System.out.printf(
        Locale.US,
        "Average profit/loss: %+.2f%n",
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
        summary.unableRate()
        * 100.0
    );

    System.out.printf(
        Locale.US,
        "Average hands played: %.2f%n",
        summary.averageHandsPlayed()
    );

    System.out.printf(
        Locale.US,
        "Average total doubles: %.3f%n",
        summary.averageDoubles()
    );

    System.out.printf(
        Locale.US,
        "Average Hard-9 doubles: %.3f%n",
        summary.averageHard9Doubles()
    );

    System.out.printf(
        Locale.US,
        "Average splits: %.3f%n",
        summary.averageSplits()
    );

    System.out.printf(
        Locale.US,
        "Average wager exposure: $%.2f%n",
        summary.averageWagerExposure()
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
        "======================================"
    );

    System.out.printf(
        Locale.US,
        "Average ending-bankroll difference: %+.3f%n",
        summary.meanDifference()
    );

    System.out.printf(
        Locale.US,
        "Difference standard error: $%.3f%n",
        summary.differenceStandardError()
    );

    System.out.printf(
        Locale.US,
        "95%% CI for bankroll difference: $%+.3f to $%+.3f%n",
        summary.meanDifference()
            - summary.differenceCI95(),
        summary.meanDifference()
            + summary.differenceCI95()
    );

    System.out.println();

    System.out.printf(
        Locale.US,
        "Test policy finishes with more money: %.3f%%%n",
        summary.testBetterRate()
        * 100.0
    );

    System.out.printf(
        Locale.US,
        "Same ending bankroll: %.3f%%%n",
        summary.sameRate()
        * 100.0
    );

    System.out.printf(
        Locale.US,
        "Basic Strategy finishes with more money: %.3f%%%n",
        summary.referenceBetterRate()
        * 100.0
    );

    System.out.printf(
        Locale.US,
        "Head-to-head advantage: %+.3f percentage points%n",
        (
            summary.testBetterRate()
            - summary.referenceBetterRate()
        )
        * 100.0
    );

    System.out.println();

    System.out.printf(
        Locale.US,
        "Both complete 160 hands: %.3f%%%n",
        summary.bothSurviveRate()
        * 100.0
    );

    System.out.printf(
        Locale.US,
        "Both unable to continue: %.3f%%%n",
        summary.bothRuinRate()
        * 100.0
    );

    System.out.printf(
        Locale.US,
        "Test policy SAVES session from early ruin: %.3f%%%n",
        summary.testSavesRate()
        * 100.0
    );

    System.out.printf(
        Locale.US,
        "Test policy CAUSES early ruin instead: %.3f%%%n",
        summary.testCausesRate()
        * 100.0
    );

    System.out.printf(
        Locale.US,
        "Net survival advantage: %+.3f percentage points%n",
        (
            summary.testSavesRate()
            - summary.testCausesRate()
        )
        * 100.0
    );

    System.out.println();

    System.out.printf(
        Locale.US,
        "Average wager-exposure difference: %+.2f%n",
        summary.meanWagerExposureDifference()
    );

    System.out.printf(
        Locale.US,
        "Average Hard-9-double difference: %+.3f/session%n",
        summary.meanHard9DoubleDifference()
    );
  }

  private static void writeHeader()
      throws IOException
  {
    Files.writeString(
        OUTPUT_FILE,
        String.join(
            ",",
            "blackjack_payout",
            "test_policy",
            "reference_policy",
            "sessions",
            "hands_per_session",
            "starting_bankroll",
            "base_bet",
            "mean_bankroll_difference",
            "difference_standard_deviation",
            "difference_standard_error",
            "difference_ci95",
            "test_better_rate",
            "same_rate",
            "reference_better_rate",
            "both_survive_rate",
            "both_ruin_rate",
            "test_saves_from_ruin_rate",
            "test_causes_ruin_rate",
            "mean_wager_exposure_difference",
            "mean_hard9_double_difference"
        )
        + System.lineSeparator(),
        StandardOpenOption.CREATE
    );
  }

  private static void writePairSummary(
      GameSettings.BlackjackPayout payout,
      PairSummary summary)
      throws IOException
  {
    String row =
        String.format(
            Locale.US,
            "%s,%s,%s,%d,%d,%.2f,%.2f,"
            + "%.10f,%.10f,%.10f,%.10f,"
            + "%.10f,%.10f,%.10f,"
            + "%.10f,%.10f,%.10f,%.10f,"
            + "%.10f,%.10f",
            payout.name(),
            summary.testPolicy().name(),
            summary.referencePolicy().name(),
            SESSIONS,
            HANDS_PER_SESSION,
            STARTING_BANKROLL,
            BASE_BET,
            summary.meanDifference(),
            summary.differenceStandardDeviation(),
            summary.differenceStandardError(),
            summary.differenceCI95(),
            summary.testBetterRate(),
            summary.sameRate(),
            summary.referenceBetterRate(),
            summary.bothSurviveRate(),
            summary.bothRuinRate(),
            summary.testSavesRate(),
            summary.testCausesRate(),
            summary.meanWagerExposureDifference(),
            summary.meanHard9DoubleDifference()
        );

    Files.writeString(
        OUTPUT_FILE,
        row + System.lineSeparator(),
        StandardOpenOption.APPEND
    );
  }

  private static GameSettings
      createResearchSettings(
          GameSettings.BlackjackPayout payout)
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
        payout
    );

    return settings;
  }

  private static double standardDeviation(
      double m2,
      int count)
  {
    if (count <= 1)
      return 0.0;

    return Math.sqrt(
        m2 / (count - 1)
    );
  }

  private static double standardError(
      double standardDeviation,
      int count)
  {
    return standardDeviation
        / Math.sqrt(
            count
        );
  }

  private enum Policy
  {
    BASIC_STRATEGY,
    NO_HARD_9_DOUBLES,
    HARD_9_DOUBLE_2_TO_6
  }

  private record SessionState(
      double bankroll,
      int handsPlayed,
      boolean unableToContinue,
      long doubles,
      long hard9Doubles,
      long splits,
      double wagerExposure)
  {
  }

  private static class PolicyAccumulator
  {
    private final Policy policy;

    private int count;

    private double meanBankroll;
    private double bankrollM2;

    private long unableCount;
    private long totalHands;
    private long totalDoubles;
    private long totalHard9Doubles;
    private long totalSplits;

    private double totalWagerExposure;

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
        unableCount++;
      }

      totalHands +=
          state.handsPlayed();

      totalDoubles +=
          state.doubles();

      totalHard9Doubles +=
          state.hard9Doubles();

      totalSplits +=
          state.splits();

      totalWagerExposure +=
          state.wagerExposure();
    }

    PolicySummary finish()
    {
      return new PolicySummary(
          policy,
          meanBankroll,
          standardDeviation(
              bankrollM2,
              count
          ),
          (double) unableCount
              / count,
          (double) totalHands
              / count,
          (double) totalDoubles
              / count,
          (double) totalHard9Doubles
              / count,
          (double) totalSplits
              / count,
          totalWagerExposure
              / count
      );
    }
  }

  private static class PairAccumulator
  {
    private final Policy testPolicy;
    private final Policy referencePolicy;

    private int count;

    private double meanDifference;
    private double differenceM2;

    private long testBetter;
    private long same;
    private long referenceBetter;

    private long bothSurvive;
    private long bothRuin;
    private long testSaves;
    private long testCauses;

    private double totalWagerExposureDifference;
    private double totalHard9DoubleDifference;

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

      if (difference > 0)
      {
        testBetter++;
      }
      else if (difference < 0)
      {
        referenceBetter++;
      }
      else
      {
        same++;
      }

      boolean testRuin =
          test.unableToContinue();

      boolean referenceRuin =
          reference.unableToContinue();

      if (!testRuin
          && !referenceRuin)
      {
        bothSurvive++;
      }
      else if (testRuin
               && referenceRuin)
      {
        bothRuin++;
      }
      else if (!testRuin
               && referenceRuin)
      {
        /*
         * Test policy completes the night while
         * Basic Strategy cannot.
         */
        testSaves++;
      }
      else
      {
        /*
         * Basic Strategy completes the night while
         * the test policy cannot.
         */
        testCauses++;
      }

      totalWagerExposureDifference +=
          test.wagerExposure()
          - reference.wagerExposure();

      totalHard9DoubleDifference +=
          test.hard9Doubles()
          - reference.hard9Doubles();
    }

    PairSummary finish()
    {
      double sd =
          standardDeviation(
              differenceM2,
              count
          );

      double se =
          standardError(
              sd,
              count
          );

      return new PairSummary(
          testPolicy,
          referencePolicy,
          meanDifference,
          sd,
          se,
          CI_95_Z * se,
          (double) testBetter
              / count,
          (double) same
              / count,
          (double) referenceBetter
              / count,
          (double) bothSurvive
              / count,
          (double) bothRuin
              / count,
          (double) testSaves
              / count,
          (double) testCauses
              / count,
          totalWagerExposureDifference
              / count,
          totalHard9DoubleDifference
              / count
      );
    }
  }

  private record PolicySummary(
      Policy policy,
      double meanEndingBankroll,
      double endingBankrollSD,
      double unableRate,
      double averageHandsPlayed,
      double averageDoubles,
      double averageHard9Doubles,
      double averageSplits,
      double averageWagerExposure)
  {
  }

  private record PairSummary(
      Policy testPolicy,
      Policy referencePolicy,
      double meanDifference,
      double differenceStandardDeviation,
      double differenceStandardError,
      double differenceCI95,
      double testBetterRate,
      double sameRate,
      double referenceBetterRate,
      double bothSurviveRate,
      double bothRuinRate,
      double testSavesRate,
      double testCausesRate,
      double meanWagerExposureDifference,
      double meanHard9DoubleDifference)
  {
  }
}
