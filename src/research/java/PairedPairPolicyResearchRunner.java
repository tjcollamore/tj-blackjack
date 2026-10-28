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

public class PairedPairPolicyResearchRunner
{
    
  private static final int SESSIONS =
      1_000_000;

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
            "paired-session-social-pairs-1m-160hands-500bankroll-25bet.csv"
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

      PolicyAccumulator socialAccumulator =
          new PolicyAccumulator(
              Policy.SOCIAL_PAIRS
          );

      PairAccumulator socialVsBasic =
          new PairAccumulator(
              Policy.SOCIAL_PAIRS,
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

        SessionState social =
            playSession(
                settings,
                Policy.SOCIAL_PAIRS,
                seed
            );

        basicAccumulator.add(
            basic
        );

        socialAccumulator.add(
            social
        );

        socialVsBasic.add(
            social,
            basic
        );
        
      if (session % 100_000 == 0)
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

  PolicySummary socialSummary =
      socialAccumulator.finish();

  PairSummary socialPair =
      socialVsBasic.finish();

  printPolicySummary(
      basicSummary
  );

  printPolicySummary(
      socialSummary
  );

  printPairSummary(
      socialPair
  );

  writePairSummary(
      payout,
      socialPair
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

    long[] softDoubles =
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
            softDoubles[0],
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
          softDoubles,
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
        softDoubles[0],
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
      long[] softDoubles,
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
                if (state.getHand().isSoft())
                {
                  softDoubles[0]++;
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

        if (policy == Policy.BASIC_STRATEGY)
        {
          return basicAction;
        }

        /*
         * Important:
         *
         * Preserve Basic Strategy surrender decisions
         * for now. We are researching PAIR simplification,
         * not surrender simplification.
         *
         * For example, under our H17 rules 8,8 vs Ace
         * may be surrendered before pair strategy is
         * considered.
         */
        if (basicAction == HandAction.SURRENDER)
        {
          return basicAction;
        }

        Hand hand =
            context.getPlayerHand();

        if (hand.getNumCards() != 2)
        {
          return basicAction;
        }

        int firstValue =
            hand.getCard(0)
                .getPointValue();

        int secondValue =
            hand.getCard(1)
                .getPointValue();

        if (firstValue != secondValue)
        {
          return basicAction;
        }

        int pairValue =
            firstValue;

        int dealer =
            context
                .getDealerUpCard()
                .getPointValue();

        /*
         * If splitting is not actually available
         * because of bankroll / hand limits, let
         * Basic Strategy choose the proper fallback.
         */
        boolean canSplit =
            context.isAvailable(
                HandAction.SPLIT
            );

        /*
         * A,A
         *
         * Always split when available.
         */
        if (pairValue == 11)
        {
          if (canSplit)
          {
            return HandAction.SPLIT;
          }

          return basicAction;
        }

        /*
         * 10,10
         *
         * Never split.
         */
        if (pairValue == 10)
        {
          return HandAction.STAND;
        }

        /*
         * 9,9
         *
         * Social simplification:
         * split vs 2-9.
         *
         * This intentionally adds the
         * 9,9 vs 7 split.
         */
        if (pairValue == 9)
        {
          if (dealer >= 2
              && dealer <= 9
              && canSplit)
          {
            return HandAction.SPLIT;
          }

          return HandAction.STAND;
        }

        /*
         * 8,8
         *
         * Always split when available.
         *
         * Any surrender situation was already
         * preserved above.
         */
        if (pairValue == 8)
        {
          if (canSplit)
          {
            return HandAction.SPLIT;
          }

          return basicAction;
        }

        /*
         * 7,7 and 6,6
         *
         * Social rule:
         * split vs 2-7.
         *
         * For 6,6 this intentionally adds
         * the dealer-7 split.
         */
        if (pairValue == 7
            || pairValue == 6)
        {
          if (dealer >= 2
              && dealer <= 7
              && canSplit)
          {
            return HandAction.SPLIT;
          }

          return HandAction.HIT;
        }

        /*
         * 5,5
         *
         * Never split.
         * Treat it exactly like hard 10.
         *
         * Returning Basic Strategy here is ideal,
         * since our hard-10 rules already handle
         * DOUBLE/HIT and bankroll availability.
         */
        if (pairValue == 5)
        {
          return basicAction;
        }

        /*
         * 4,4
         *
         * Social simplification:
         * NEVER split.
         *
         * Hard 8 simply hits.
         */
        if (pairValue == 4)
        {
          return HandAction.HIT;
        }

        /*
         * 3,3 and 2,2
         *
         * Split vs 2-7.
         */
        if (pairValue == 3
            || pairValue == 2)
        {
          if (dealer >= 2
              && dealer <= 7
              && canSplit)
          {
            return HandAction.SPLIT;
          }

          return HandAction.HIT;
        }

        return basicAction;
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
        summary.referencePolicy()
            + " finishes with more money: %.3f%%%n",
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
          "Average split difference: %+.3f/session%n",
          summary.meanSplitDifference()
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
      SOCIAL_PAIRS
    }

  private record SessionState(
      double bankroll,
      int handsPlayed,
      boolean unableToContinue,
      long doubles,
      long softDoubles,
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
    private long totalSoftDoubles;
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

      totalSoftDoubles +=
          state.softDoubles();

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
          (double) totalSoftDoubles
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

      private double totalSplitDifference;
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

        totalSplitDifference +=
            test.splits()
            - reference.splits();
        
      totalWagerExposureDifference +=
          test.wagerExposure()
          - reference.wagerExposure();

      totalHard9DoubleDifference +=
          test.softDoubles()
          - reference.softDoubles();
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
          totalSplitDifference
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
      double averageSoftDoubles,
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
      double meanSplitDifference,
      double meanHard9DoubleDifference)
  {
  }
}


