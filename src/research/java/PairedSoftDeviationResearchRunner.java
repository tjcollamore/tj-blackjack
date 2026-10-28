import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PairedSoftDeviationResearchRunner
{
  private static final int TRIALS_PER_STATE =
      1_000_000;

  private static final int SESSION_HANDS =
      160;

  private static final double AVERAGE_BET =
      25.0;

  private static final double CI_95_Z =
      1.96;

  private static final Path OUTPUT_FILE =
      Path.of(
          "research-data",
          "scratch",
          "paired-soft-deviations-aggressive4to6-8d-h17-das-ls-6to5-1m.csv"
      );

  private static final Path FREQUENCY_FILE =
      Path.of(
          "research-data",
          "canonical",
          "frequency-basicstrategy-compositions-8d-h17-das-ls-6to5-10m-rounds.csv"
      );

  private final GameSettings settings;
  private final BasicStrategy basicStrategy;
  private final PlayerRoundEngine playerRoundEngine;
  private final DealerPlayEngine dealerPlayEngine;

  public PairedSoftDeviationResearchRunner(
      GameSettings settings)
  {
    this.settings =
        settings;

    basicStrategy =
        new BasicStrategy();

    playerRoundEngine =
        new PlayerRoundEngine(
            settings
        );

    dealerPlayEngine =
        new DealerPlayEngine(
            settings
        );
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

    GameSettings settings =
        createResearchSettings();

    PairedSoftDeviationResearchRunner runner =
        new PairedSoftDeviationResearchRunner(
            settings
        );

    List<TestCase> cases =
        createTestCases();

    List<StateResult> results =
        new ArrayList<>();

    for (TestCase testCase :
         cases)
    {
      StateResult result =
          runner.runState(
              testCase
          );

      results.add(
          result
      );

      printStateResult(
          result
      );

      writeResult(
          result
      );

      System.out.println();
    }

    printGroupSummaries(
        results
    );

    System.out.println();

    System.out.println(
        "Saved detailed results to "
        + OUTPUT_FILE.toAbsolutePath()
    );
  }

  private static List<TestCase>
      createTestCases()
  {
    List<TestCase> cases =
        new ArrayList<>();

    /*
     * Soft 19:
     *
     * Basic: DOUBLE A,8 vs 6
     * Social: STAND
     */
    cases.add(
        new TestCase(
            "SOFT_19_VS_6",
            Rank.ACE,
            Rank.EIGHT,
            Rank.SIX,
            HandAction.DOUBLE,
            HandAction.STAND
        )
    );

    /*
     * Soft 18:
     *
     * Basic: DOUBLE A,7 vs 2-6
     * Social: STAND
     */
    for (Rank dealer :
         new Rank[]
         {
             Rank.TWO,
             Rank.THREE,
             Rank.FOUR,
             Rank.FIVE,
             Rank.SIX
         })
    {
      cases.add(
          new TestCase(
              "SOFT_18_VS_2_TO_6",
              Rank.ACE,
              Rank.SEVEN,
              dealer,
              HandAction.DOUBLE,
              HandAction.STAND
          )
      );
    }

    /*
     * Soft 17:
     *
     * Basic: DOUBLE A,6 vs 3
     * Social: HIT
     */
    cases.add(
        new TestCase(
            "SOFT_17_VS_3",
            Rank.ACE,
            Rank.SIX,
            Rank.THREE,
            HandAction.DOUBLE,
            HandAction.HIT
        )
    );

    /*
     * Soft 13 / 14:
     *
     * Basic: HIT vs 4
     * Social: DOUBLE vs 4
     */
    cases.add(
        new TestCase(
            "SOFT_13_14_VS_4",
            Rank.ACE,
            Rank.TWO,
            Rank.FOUR,
            HandAction.HIT,
            HandAction.DOUBLE
        )
    );

    cases.add(
        new TestCase(
            "SOFT_13_14_VS_4",
            Rank.ACE,
            Rank.THREE,
            Rank.FOUR,
            HandAction.HIT,
            HandAction.DOUBLE
        )
    );

    return cases;
  }

  private StateResult runState(
      TestCase testCase)
  {
    Card firstCard =
        new Card(
            testCase.firstRank(),
            Suit.HEARTS
        );

    Card secondCard =
        new Card(
            testCase.secondRank(),
            Suit.CLUBS
        );

    Card dealerUpCard =
        new Card(
            testCase.dealerRank(),
            Suit.SPADES
        );

    Hand playerHand =
        new Hand();

    playerHand.addCard(
        firstCard
    );

    playerHand.addCard(
        secondCard
    );

    EnumSet<HandAction> allowedActions =
        PlayerActionRules.getAllowedActions(
            playerHand,
            false,
            settings
        );

    PlayerDecisionContext context =
        new PlayerDecisionContext(
            playerHand,
            dealerUpCard,
            settings,
            allowedActions,
            EnumSet.copyOf(
                allowedActions
            ),
            false,
            settings.getDecksInShoe(),
            1.0
        );

    HandAction actualBasicAction =
        basicStrategy
            .chooseRecommendedAction(
                context
            );

    if (actualBasicAction
        != testCase.basicAction())
    {
      throw new IllegalStateException(
          "Expected Basic Strategy "
          + testCase.basicAction()
          + " for "
          + describeTestCase(
              testCase
          )
          + ", but got "
          + actualBasicAction
      );
    }

    if (!allowedActions.contains(
        testCase.socialAction()))
    {
      throw new IllegalStateException(
          "Social action "
          + testCase.socialAction()
          + " is not allowed for "
          + describeTestCase(
              testCase
          )
      );
    }

    long basicBetter =
        0;

    long same =
        0;

    long socialBetter =
        0;

    double meanDifference =
        0.0;

    double differenceM2 =
        0.0;

    double differenceWhenBasicBetter =
        0.0;

    double differenceWhenSocialBetter =
        0.0;

    Map<OutcomePair, Long> outcomePairs =
        new LinkedHashMap<>();

    for (int trial = 1;
         trial <= TRIALS_PER_STATE;
         trial++)
    {
      PairedTrial paired =
          runPairedTrial(
              context,
              testCase.basicAction(),
              testCase.socialAction()
          );

      double basicResult =
          paired.basicResult();

      double socialResult =
          paired.socialResult();

      double difference =
          basicResult
          - socialResult;

      double delta =
          difference
          - meanDifference;

      meanDifference +=
          delta / trial;

      double delta2 =
          difference
          - meanDifference;

      differenceM2 +=
          delta
          * delta2;

      if (difference > 0)
      {
        basicBetter++;

        differenceWhenBasicBetter +=
            difference;
      }
      else if (difference < 0)
      {
        socialBetter++;

        differenceWhenSocialBetter +=
            -difference;
      }
      else
      {
        same++;
      }

      OutcomePair pair =
          new OutcomePair(
              describeResult(
                  basicResult
              ),
              describeResult(
                  socialResult
              )
          );

      outcomePairs.merge(
          pair,
          1L,
          Long::sum
      );

      if (trial % 100_000 == 0)
      {
        System.out.println(
            "  "
            + describeTestCase(
                testCase
            )
            + ": completed "
            + trial
            + " paired trials..."
        );
      }
    }

    double variance =
        differenceM2
        / (TRIALS_PER_STATE - 1);

    double standardDeviation =
        Math.sqrt(
            variance
        );

    double standardError =
        standardDeviation
        / Math.sqrt(
            TRIALS_PER_STATE
        );

    double ci95 =
        CI_95_Z
        * standardError;

    double averageDifferenceWhenBasicBetter =
        basicBetter == 0
        ? 0.0
        : differenceWhenBasicBetter
          / basicBetter;

    double averageDifferenceWhenSocialBetter =
        socialBetter == 0
        ? 0.0
        : differenceWhenSocialBetter
          / socialBetter;

    return new StateResult(
        testCase.group(),
        normalizeComposition(
            testCase.firstRank(),
            testCase.secondRank()
        ),
        playerHand.getScore(),
        testCase.dealerRank()
            .getValue(),
        testCase.basicAction(),
        testCase.socialAction(),
        TRIALS_PER_STATE,
        basicBetter,
        same,
        socialBetter,
        meanDifference,
        standardDeviation,
        standardError,
        ci95,
        averageDifferenceWhenBasicBetter,
        averageDifferenceWhenSocialBetter,
        outcomePairs
    );
  }

  private PairedTrial runPairedTrial(
      PlayerDecisionContext context,
      HandAction basicAction,
      HandAction socialAction)
  {
    Shoe sourceShoe =
        createFreshShoe(
            context
        );

    SharedDrawSequence sequence =
        new SharedDrawSequence(
            sourceShoe
        );

    ReplayShoe basicShoe =
        new ReplayShoe(
            context.getDeckCount(),
            sequence
        );

    ReplayShoe socialShoe =
        new ReplayShoe(
            context.getDeckCount(),
            sequence
        );

    double basicResult =
        playBranch(
            context,
            basicAction,
            basicShoe
        );

    double socialResult =
        playBranch(
            context,
            socialAction,
            socialShoe
        );

    return new PairedTrial(
        basicResult,
        socialResult
    );
  }

  private double playBranch(
      PlayerDecisionContext context,
      HandAction forcedFirstAction,
      Shoe shoe)
  {
    Hand playerHand =
        copyHand(
            context.getPlayerHand()
        );

    Hand dealerHand =
        new Hand();

    dealerHand.addCard(
        context.getDealerUpCard()
    );

    dealerHand.addCard(
        shoe.drawCard()
    );

    ForcedFirstActionStrategy strategy =
        new ForcedFirstActionStrategy(
            forcedFirstAction,
            basicStrategy
        );

    PlayerHandState initialState =
        new PlayerHandState(
            playerHand
        );

    List<PlayerHandState> playerStates =
        playerRoundEngine.play(
            initialState,
            context.getDealerUpCard(),
            strategy,
            shoe,
            new PlayerRoundListener()
            {
              @Override
              public boolean canDoubleOrSplit(
                  PlayerHandState state)
              {
                return true;
              }

              @Override
              public double getWagerAmount(
                  PlayerHandState state)
              {
                return context.getWagerAmount();
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

    double totalResult =
        0.0;

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

      double handResult;

      switch (outcome)
      {
        case WIN:
          handResult = 1.0;
          break;

        case LOSS:
          handResult = -1.0;
          break;

        case PUSH:
          handResult = 0.0;
          break;

        case SURRENDER:
          handResult = -0.5;
          break;

        case BLACKJACK:
          throw new IllegalStateException(
              "Natural blackjack should not "
              + "occur in soft-hand research"
          );

        default:
          throw new IllegalStateException(
              "Unexpected outcome: "
              + outcome
          );
      }

      totalResult +=
          state.getWagerMultiplier()
          * handResult;
    }

    return totalResult;
  }

  private Shoe createFreshShoe(
      PlayerDecisionContext context)
  {
    Shoe shoe =
        new Shoe(
            context.getDeckCount()
        );

    Hand playerHand =
        context.getPlayerHand();

    for (int i = 0;
         i < playerHand.getNumCards();
         i++)
    {
      shoe.removeCard(
          playerHand.getCard(i)
      );
    }

    shoe.removeCard(
        context.getDealerUpCard()
    );

    return shoe;
  }

  private Hand copyHand(
      Hand original)
  {
    Hand copy =
        new Hand();

    for (int i = 0;
         i < original.getNumCards();
         i++)
    {
      copy.addCard(
          original.getCard(i)
      );
    }

    return copy;
  }

  private static void printStateResult(
      StateResult result)
  {
    System.out.println();

    System.out.println(
        "Soft "
        + result.playerTotal()
        + " ("
        + result.composition()
        + ") vs dealer "
        + result.dealerValue()
    );

    System.out.println(
        "Basic: "
        + result.basicAction()
        + " | Social: "
        + result.socialAction()
    );

    System.out.println(
        "--------------------------------------"
    );

    System.out.printf(
        Locale.US,
        "Basic Strategy better: %.3f%%%n",
        result.basicBetterRate()
        * 100.0
    );

    System.out.printf(
        Locale.US,
        "Same monetary outcome: %.3f%%%n",
        result.sameRate()
        * 100.0
    );

    System.out.printf(
        Locale.US,
        "Social Strategy better: %.3f%%%n",
        result.socialBetterRate()
        * 100.0
    );

    System.out.printf(
        Locale.US,
        "Basic Strategy advantage: %+.3f percentage points%n",
        (
            result.basicBetterRate()
            - result.socialBetterRate()
        )
        * 100.0
    );

    System.out.println();

    System.out.printf(
        Locale.US,
        "Paired EV cost: %.6f wager units%n",
        result.meanEvDifference()
    );

    System.out.printf(
        Locale.US,
        "Standard error: %.6f%n",
        result.standardError()
    );

    System.out.printf(
        Locale.US,
        "95%% CI: %.6f to %.6f wager units%n",
        result.meanEvDifference()
            - result.ci95(),
        result.meanEvDifference()
            + result.ci95()
    );

    System.out.printf(
        Locale.US,
        "EV cost per $25 occurrence: $%.3f%n",
        result.meanEvDifference()
        * AVERAGE_BET
    );

    System.out.printf(
        Locale.US,
        "95%% CI per occurrence: $%.3f to $%.3f%n",
        (
            result.meanEvDifference()
            - result.ci95()
        )
        * AVERAGE_BET,
        (
            result.meanEvDifference()
            + result.ci95()
        )
        * AVERAGE_BET
    );

    System.out.printf(
        Locale.US,
        "Average difference when Basic is better: $%.2f%n",
        result.averageDifferenceWhenBasicBetter()
        * AVERAGE_BET
    );

    System.out.printf(
        Locale.US,
        "Average difference when Social is better: $%.2f%n",
        result.averageDifferenceWhenSocialBetter()
        * AVERAGE_BET
    );
  }

  private static void printGroupSummaries(
      List<StateResult> results)
      throws IOException
  {
    Map<StateKey, Double> frequencies =
        loadRelevantFrequencies();

    System.out.println();

    System.out.println(
        "FREQUENCY-WEIGHTED DEVIATION GROUPS"
    );

    System.out.println(
        "==================================="
    );

    String[] groups =
    {
        "SOFT_19_VS_6",
        "SOFT_18_VS_2_TO_6",
        "SOFT_17_VS_3",
        "SOFT_13_14_VS_4"
    };

    double allFrequency =
        0.0;

    double allEvNumerator =
        0.0;

    double allBasicBetter =
        0.0;

    double allSame =
        0.0;

    double allSocialBetter =
        0.0;

    double allVarianceNumerator =
        0.0;

    for (String group :
         groups)
    {
      double groupFrequency =
          0.0;

      double groupEvNumerator =
          0.0;

      double groupBasicBetter =
          0.0;

      double groupSame =
          0.0;

      double groupSocialBetter =
          0.0;

      double groupVarianceNumerator =
          0.0;

      for (StateResult result :
           results)
      {
        if (!result.group()
            .equals(group))
        {
          continue;
        }

        StateKey key =
            new StateKey(
                result.composition(),
                result.dealerValue()
            );

        Double frequency =
            frequencies.get(
                key
            );

        if (frequency == null)
        {
          throw new IllegalStateException(
              "Missing frequency for "
              + result.composition()
              + " vs "
              + result.dealerValue()
          );
        }

        groupFrequency +=
            frequency;

        groupEvNumerator +=
            frequency
            * result.meanEvDifference();

        groupBasicBetter +=
            frequency
            * result.basicBetterRate();

        groupSame +=
            frequency
            * result.sameRate();

        groupSocialBetter +=
            frequency
            * result.socialBetterRate();

        groupVarianceNumerator +=
            frequency
            * frequency
            * result.standardError()
            * result.standardError();
      }

      printWeightedGroup(
          group,
          groupFrequency,
          groupEvNumerator,
          groupBasicBetter,
          groupSame,
          groupSocialBetter,
          groupVarianceNumerator
      );

      allFrequency +=
          groupFrequency;

      allEvNumerator +=
          groupEvNumerator;

      allBasicBetter +=
          groupBasicBetter;

      allSame +=
          groupSame;

      allSocialBetter +=
          groupSocialBetter;

      allVarianceNumerator +=
          groupVarianceNumerator;
    }

    System.out.println();

    System.out.println(
        "ALL AGGRESSIVE_4_TO_6 DEVIATIONS"
    );

    System.out.println(
        "================================"
    );

    printWeightedNumbers(
        allFrequency,
        allEvNumerator,
        allBasicBetter,
        allSame,
        allSocialBetter,
        allVarianceNumerator
    );
  }

  private static void printWeightedGroup(
      String group,
      double frequency,
      double evNumerator,
      double basicBetter,
      double same,
      double socialBetter,
      double varianceNumerator)
  {
    System.out.println();

    System.out.println(
        group
    );

    System.out.println(
        "--------------------------------------"
    );

    printWeightedNumbers(
        frequency,
        evNumerator,
        basicBetter,
        same,
        socialBetter,
        varianceNumerator
    );
  }

  private static void printWeightedNumbers(
      double frequency,
      double evNumerator,
      double basicBetter,
      double same,
      double socialBetter,
      double varianceNumerator)
  {
    double evPerOccurrence =
        evNumerator
        / frequency;

    double basicBetterRate =
        basicBetter
        / frequency;

    double sameRate =
        same
        / frequency;

    double socialBetterRate =
        socialBetter
        / frequency;

    double standardError =
        Math.sqrt(
            varianceNumerator
        )
        / frequency;

    double ci95 =
        CI_95_Z
        * standardError;

    double occurrencesPerSession =
        frequency
        * SESSION_HANDS;

    double sessionCost =
        occurrencesPerSession
        * evPerOccurrence
        * AVERAGE_BET;

    double sessionStandardError =
        occurrencesPerSession
        * standardError
        * AVERAGE_BET;

    double sessionCI95 =
        CI_95_Z
        * sessionStandardError;

    System.out.printf(
        Locale.US,
        "Occurrences per 160-hand session: %.3f%n",
        occurrencesPerSession
    );

    System.out.printf(
        Locale.US,
        "Basic Strategy better: %.3f%%%n",
        basicBetterRate
        * 100.0
    );

    System.out.printf(
        Locale.US,
        "Same monetary outcome: %.3f%%%n",
        sameRate
        * 100.0
    );

    System.out.printf(
        Locale.US,
        "Social Strategy better: %.3f%%%n",
        socialBetterRate
        * 100.0
    );

    System.out.printf(
        Locale.US,
        "Basic Strategy advantage: %+.3f percentage points%n",
        (
            basicBetterRate
            - socialBetterRate
        )
        * 100.0
    );

    System.out.printf(
        Locale.US,
        "EV cost per occurrence: $%.3f%n",
        evPerOccurrence
        * AVERAGE_BET
    );

    System.out.printf(
        Locale.US,
        "Expected EV cost per session: $%.3f%n",
        sessionCost
    );

    System.out.printf(
        Locale.US,
        "95%% CI for session EV cost: $%.3f to $%.3f%n",
        sessionCost
            - sessionCI95,
        sessionCost
            + sessionCI95
    );
  }

  private static Map<StateKey, Double>
      loadRelevantFrequencies()
      throws IOException
  {
    Map<StateKey, Double> frequencies =
        new LinkedHashMap<>();

    List<String> lines =
        Files.readAllLines(
            FREQUENCY_FILE
        );

    for (int i = 1;
         i < lines.size();
         i++)
    {
      String[] columns =
          lines.get(i)
              .split(",", -1);

      String handType =
          columns[0];

      int playerTotal =
          Integer.parseInt(
              columns[1]
          );

      int dealerValue =
          Integer.parseInt(
              columns[3]
          );

      int cardCount =
          Integer.parseInt(
              columns[4]
          );

      boolean cameFromSplit =
          Boolean.parseBoolean(
              columns[5]
          );

      if (!handType.equals("SOFT")
          || cardCount != 2
          || cameFromSplit)
      {
        continue;
      }

      boolean relevant =
          (playerTotal == 19
              && dealerValue == 6)
          ||
          (playerTotal == 18
              && dealerValue >= 2
              && dealerValue <= 6)
          ||
          (playerTotal == 17
              && dealerValue == 3)
          ||
          (
              (playerTotal == 13
                  || playerTotal == 14)
              && dealerValue == 4
          );

      if (!relevant)
      {
        continue;
      }

      String composition =
          normalizeFrequencyComposition(
              columns[6]
          );

      double frequency =
          Double.parseDouble(
              columns[9]
          );

      StateKey key =
          new StateKey(
              composition,
              dealerValue
          );

      frequencies.merge(
          key,
          frequency,
          Double::sum
      );
    }

    return frequencies;
  }

  private static String
      normalizeFrequencyComposition(
          String composition)
  {
    String[] parts =
        composition.split("\\+");

    List<Integer> values =
        new ArrayList<>();

    for (String part :
         parts)
    {
      values.add(
          rankNameToValue(
              part
          )
      );
    }

    values.sort(
        Integer::compareTo
    );

    return values.get(0)
        + "+"
        + values.get(1);
  }

  private static String normalizeComposition(
      Rank first,
      Rank second)
  {
    int firstValue =
        first.getValue();

    int secondValue =
        second.getValue();

    if (firstValue <= secondValue)
    {
      return firstValue
          + "+"
          + secondValue;
    }

    return secondValue
        + "+"
        + firstValue;
  }

  private static int rankNameToValue(
      String rank)
  {
    return switch (rank)
    {
      case "ACE" ->
          11;

      case "TWO" ->
          2;

      case "THREE" ->
          3;

      case "FOUR" ->
          4;

      case "FIVE" ->
          5;

      case "SIX" ->
          6;

      case "SEVEN" ->
          7;

      case "EIGHT" ->
          8;

      case "NINE" ->
          9;

      case "TEN",
           "JACK",
           "QUEEN",
           "KING" ->
          10;

      default ->
          throw new IllegalArgumentException(
              "Unknown rank: "
              + rank
          );
    };
  }

  private static String describeResult(
      double result)
  {
    if (result == 2.0)
      return "+2 WIN";

    if (result == 1.0)
      return "+1 WIN";

    if (result == 0.0)
      return "PUSH";

    if (result == -0.5)
      return "-0.5";

    if (result == -1.0)
      return "-1 LOSS";

    if (result == -2.0)
      return "-2 LOSS";

    return String.format(
        Locale.US,
        "%+.1f",
        result
    );
  }

  private static String describeTestCase(
      TestCase testCase)
  {
    return "A,"
        + testCase.secondRank()
        + " vs "
        + testCase.dealerRank();
  }

  private static void writeResult(
      StateResult result)
      throws IOException
  {
    Files.createDirectories(
        OUTPUT_FILE.getParent()
    );

    boolean writeHeader =
        !Files.exists(
            OUTPUT_FILE
        );

    if (writeHeader)
    {
      Files.writeString(
          OUTPUT_FILE,
          String.join(
              ",",
              "group",
              "player_composition",
              "player_total",
              "dealer_value",
              "basic_action",
              "social_action",
              "trials",
              "basic_better",
              "same",
              "social_better",
              "basic_better_rate",
              "same_rate",
              "social_better_rate",
              "paired_ev_cost",
              "paired_standard_deviation",
              "paired_standard_error",
              "paired_ev_ci95",
              "avg_difference_when_basic_better",
              "avg_difference_when_social_better"
          )
          + System.lineSeparator(),
          StandardOpenOption.CREATE
      );
    }

    String row =
        String.format(
            Locale.US,
            "%s,%s,%d,%d,%s,%s,%d,%d,%d,%d,"
            + "%.10f,%.10f,%.10f,%.10f,%.10f,"
            + "%.10f,%.10f,%.10f,%.10f",
            result.group(),
            result.composition(),
            result.playerTotal(),
            result.dealerValue(),
            result.basicAction(),
            result.socialAction(),
            result.trials(),
            result.basicBetter(),
            result.same(),
            result.socialBetter(),
            result.basicBetterRate(),
            result.sameRate(),
            result.socialBetterRate(),
            result.meanEvDifference(),
            result.standardDeviation(),
            result.standardError(),
            result.ci95(),
            result.averageDifferenceWhenBasicBetter(),
            result.averageDifferenceWhenSocialBetter()
        );

    Files.writeString(
        OUTPUT_FILE,
        row + System.lineSeparator(),
        StandardOpenOption.APPEND
    );
  }

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
        GameSettings.BlackjackPayout.SIX_TO_FIVE
    );

    return settings;
  }

  private static class SharedDrawSequence
  {
    private final Shoe sourceShoe;

    private final List<Card> cards =
        new ArrayList<>();

    SharedDrawSequence(
        Shoe sourceShoe)
    {
      this.sourceShoe =
          sourceShoe;
    }

    Card getCard(
        int index)
    {
      while (cards.size()
             <= index)
      {
        cards.add(
            sourceShoe.drawCard()
        );
      }

      return cards.get(
          index
      );
    }
  }

  private static class ReplayShoe
      extends Shoe
  {
    private final SharedDrawSequence sequence;

    private int nextIndex =
        0;

    ReplayShoe(
        int deckCount,
        SharedDrawSequence sequence)
    {
      super(deckCount);

      this.sequence =
          sequence;
    }

    @Override
    public Card drawCard()
    {
      Card card =
          sequence.getCard(
              nextIndex
          );

      nextIndex++;

      return card;
    }
  }

  private record TestCase(
      String group,
      Rank firstRank,
      Rank secondRank,
      Rank dealerRank,
      HandAction basicAction,
      HandAction socialAction)
  {
  }

  private record PairedTrial(
      double basicResult,
      double socialResult)
  {
  }

  private record OutcomePair(
      String basicOutcome,
      String socialOutcome)
  {
  }

  private record StateKey(
      String composition,
      int dealerValue)
  {
  }

  private record StateResult(
      String group,
      String composition,
      int playerTotal,
      int dealerValue,
      HandAction basicAction,
      HandAction socialAction,
      int trials,
      long basicBetter,
      long same,
      long socialBetter,
      double meanEvDifference,
      double standardDeviation,
      double standardError,
      double ci95,
      double averageDifferenceWhenBasicBetter,
      double averageDifferenceWhenSocialBetter,
      Map<OutcomePair, Long> outcomePairs)
  {
    double basicBetterRate()
    {
      return (double) basicBetter
          / trials;
    }

    double sameRate()
    {
      return (double) same
          / trials;
    }

    double socialBetterRate()
    {
      return (double) socialBetter
          / trials;
    }
  }
}
