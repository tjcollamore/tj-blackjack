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

public class PairedOutcomeResearchRunner
{
  private static final int TRIALS_PER_STATE =
      1_000_000;

  private static final double AVERAGE_BET =
      25.0;

  private static final int SESSION_HANDS =
      160;

  private static final double CI_95_Z =
      1.96;

  private static final Path OUTPUT_FILE =
      Path.of(
          "research-data",
          "scratch",
          "paired-hard12-vs2to3-8d-h17-das-ls-6to5-1m.csv"
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

  public PairedOutcomeResearchRunner(
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

    PairedOutcomeResearchRunner runner =
        new PairedOutcomeResearchRunner(
            settings
        );

    Rank[][] compositions =
    {
        { Rank.TEN, Rank.TWO },
        { Rank.NINE, Rank.THREE },
        { Rank.EIGHT, Rank.FOUR },
        { Rank.SEVEN, Rank.FIVE }
    };

    Rank[] dealerRanks =
    {
        Rank.TWO,
        Rank.THREE
    };

    List<StateResult> results =
        new ArrayList<>();

    for (Rank dealerRank :
         dealerRanks)
    {
      for (Rank[] composition :
           compositions)
      {
        StateResult result =
            runner.runState(
                composition[0],
                composition[1],
                dealerRank
            );

        results.add(
            result
        );

        runner.printStateResult(
            result
        );

        writeResult(
            result
        );

        System.out.println();
      }
    }

    printWeightedSummary(
        results
    );

    System.out.println();

    System.out.println(
        "Saved detailed results to "
        + OUTPUT_FILE.toAbsolutePath()
    );
  }

  private StateResult runState(
      Rank firstRank,
      Rank secondRank,
      Rank dealerRank)
  {
    Card firstCard =
        new Card(
            firstRank,
            Suit.HEARTS
        );

    Card secondCard =
        new Card(
            secondRank,
            Suit.CLUBS
        );

    Card dealerUpCard =
        new Card(
            dealerRank,
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

    HandAction basicAction =
        basicStrategy
            .chooseRecommendedAction(
                context
            );

    HandAction simplifiedAction =
        HandAction.STAND;

    if (basicAction != HandAction.HIT)
    {
      throw new IllegalStateException(
          "Expected Basic Strategy to HIT "
          + "Hard 12 vs "
          + dealerRank
          + ", but got "
          + basicAction
      );
    }

    long basicBetter =
        0;

    long same =
        0;

    long simplifiedBetter =
        0;

    double differenceMean =
        0.0;

    double differenceM2 =
        0.0;

    double differenceWhenBasicBetter =
        0.0;

    double differenceWhenSimplifiedBetter =
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
              basicAction,
              simplifiedAction
          );

      double basicResult =
          paired.basicResult();

      double simplifiedResult =
          paired.simplifiedResult();

      double difference =
          basicResult
          - simplifiedResult;

      /*
       * Welford's algorithm on the paired
       * difference itself.
       *
       * This is the important random variable:
       *
       * Basic Strategy result
       * minus
       * Simplified Strategy result.
       */
      double delta =
          difference
          - differenceMean;

      differenceMean +=
          delta / trial;

      double delta2 =
          difference
          - differenceMean;

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
        simplifiedBetter++;

        differenceWhenSimplifiedBetter +=
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
                  simplifiedResult
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
            "  Completed "
            + trial
            + " paired trials..."
        );
      }
    }

    double differenceVariance =
        differenceM2
        / (TRIALS_PER_STATE - 1);

    double differenceStandardDeviation =
        Math.sqrt(
            differenceVariance
        );

    double differenceStandardError =
        differenceStandardDeviation
        / Math.sqrt(
            TRIALS_PER_STATE
        );

    double differenceCI95 =
        CI_95_Z
        * differenceStandardError;

    double averageDifferenceWhenBasicBetter =
        basicBetter == 0
        ? 0.0
        : differenceWhenBasicBetter
          / basicBetter;

    double averageDifferenceWhenSimplifiedBetter =
        simplifiedBetter == 0
        ? 0.0
        : differenceWhenSimplifiedBetter
          / simplifiedBetter;

    return new StateResult(
        normalizeComposition(
            firstRank,
            secondRank
        ),
        dealerRank.getValue(),
        TRIALS_PER_STATE,
        basicBetter,
        same,
        simplifiedBetter,
        differenceMean,
        differenceStandardDeviation,
        differenceStandardError,
        differenceCI95,
        averageDifferenceWhenBasicBetter,
        averageDifferenceWhenSimplifiedBetter,
        outcomePairs
    );
  }

  private PairedTrial runPairedTrial(
      PlayerDecisionContext context,
      HandAction basicAction,
      HandAction simplifiedAction)
  {
    while (true)
    {
      Shoe sourceShoe =
          createFreshShoe(
              context
          );

      SharedDrawSequence sequence =
          new SharedDrawSequence(
              sourceShoe
          );

      Card dealerHoleCard =
          sequence.getCard(
              0
          );

      Hand dealerCheck =
          new Hand();

      dealerCheck.addCard(
          context.getDealerUpCard()
      );

      dealerCheck.addCard(
          dealerHoleCard
      );

      /*
       * Same conditioning used by the existing
       * Decision Help research:
       *
       * the player decision exists only after
       * dealer blackjack has been ruled out.
       */
      if (dealerCheck.isBlackjack())
      {
        continue;
      }

      ReplayShoe basicShoe =
          new ReplayShoe(
              context.getDeckCount(),
              sequence
          );

      ReplayShoe simplifiedShoe =
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

      double simplifiedResult =
          playBranch(
              context,
              simplifiedAction,
              simplifiedShoe
          );

      return new PairedTrial(
          basicResult,
          simplifiedResult
      );
    }
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

    initialState.setCameFromSplit(
        context.cameFromSplit()
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
              + "occur in Hard 12 research"
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
        result.composition()
        + " vs dealer "
        + result.dealerValue()
    );

    System.out.println(
        "--------------------"
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
        "Simplified strategy better: %.3f%%%n",
        result.simplifiedBetterRate()
        * 100.0
    );

    System.out.println();

    System.out.printf(
        Locale.US,
        "Paired EV cost of simplifying: %.6f wager units%n",
        result.meanEvDifference()
    );

    System.out.printf(
        Locale.US,
        "Paired EV standard error: %.6f%n",
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
        "At $%.2f: $%.3f EV per occurrence%n",
        AVERAGE_BET,
        result.meanEvDifference()
        * AVERAGE_BET
    );

    System.out.printf(
        Locale.US,
        "95%% CI at $%.2f: $%.3f to $%.3f%n",
        AVERAGE_BET,
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
        "Average difference when Basic Strategy is better: $%.2f%n",
        result.averageDifferenceWhenBasicBetter()
        * AVERAGE_BET
    );

    System.out.printf(
        Locale.US,
        "Average difference when simplified strategy is better: $%.2f%n",
        result.averageDifferenceWhenSimplifiedBetter()
        * AVERAGE_BET
    );

    System.out.println();

    System.out.println(
        "Outcome transitions:"
    );

    for (Map.Entry<
        OutcomePair,
        Long
    > entry :
        result.outcomePairs()
            .entrySet())
    {
      double rate =
          (double) entry.getValue()
          / result.trials();

      System.out.printf(
          Locale.US,
          "  %-6s -> %-6s : %.3f%%%n",
          entry.getKey()
              .basicOutcome(),
          entry.getKey()
              .simplifiedOutcome(),
          rate * 100.0
      );
    }
  }

  private static void printWeightedSummary(
      List<StateResult> results)
      throws IOException
  {
    Map<StateKey, Double> frequencies =
        loadRelevantFrequencies();

    System.out.println();

    System.out.println(
        "FREQUENCY-WEIGHTED SUMMARY"
    );

    System.out.println(
        "=========================="
    );

    double combinedFrequency =
        0.0;

    double combinedBasicBetter =
        0.0;

    double combinedSame =
        0.0;

    double combinedSimplifiedBetter =
        0.0;

    double combinedEvNumerator =
        0.0;

    double combinedVarianceNumerator =
        0.0;

    for (int dealerValue :
         new int[] { 2, 3 })
    {
      double dealerFrequency =
          0.0;

      double dealerBasicBetter =
          0.0;

      double dealerSame =
          0.0;

      double dealerSimplifiedBetter =
          0.0;

      double dealerEvNumerator =
          0.0;

      double dealerVarianceNumerator =
          0.0;

      for (StateResult result :
           results)
      {
        if (result.dealerValue()
            != dealerValue)
        {
          continue;
        }

        StateKey key =
            new StateKey(
                result.composition(),
                dealerValue
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
              + dealerValue
          );
        }

        dealerFrequency +=
            frequency;

        dealerBasicBetter +=
            frequency
            * result.basicBetterRate();

        dealerSame +=
            frequency
            * result.sameRate();

        dealerSimplifiedBetter +=
            frequency
            * result.simplifiedBetterRate();

        dealerEvNumerator +=
            frequency
            * result.meanEvDifference();

        /*
         * Variance of a weighted sum of
         * independent Monte Carlo estimates:
         *
         * sum(
         *   frequency^2
         *   * standardError^2
         * )
         */
        dealerVarianceNumerator +=
            frequency
            * frequency
            * result.standardError()
            * result.standardError();
      }

      double conditionalBasicBetter =
          dealerBasicBetter
          / dealerFrequency;

      double conditionalSame =
          dealerSame
          / dealerFrequency;

      double conditionalSimplifiedBetter =
          dealerSimplifiedBetter
          / dealerFrequency;

      double conditionalEvCost =
          dealerEvNumerator
          / dealerFrequency;

      double conditionalStandardError =
          Math.sqrt(
              dealerVarianceNumerator
          )
          / dealerFrequency;

      double conditionalCI95 =
          CI_95_Z
          * conditionalStandardError;

      System.out.println();

      System.out.println(
          "Hard 12 vs "
          + dealerValue
      );

      System.out.printf(
          Locale.US,
          "Basic Strategy better: %.3f%%%n",
          conditionalBasicBetter
          * 100.0
      );

      System.out.printf(
          Locale.US,
          "Same monetary outcome: %.3f%%%n",
          conditionalSame
          * 100.0
      );

      System.out.printf(
          Locale.US,
          "Simplified strategy better: %.3f%%%n",
          conditionalSimplifiedBetter
          * 100.0
      );

      System.out.printf(
          Locale.US,
          "Basic Strategy advantage: %.3f percentage points%n",
          (
              conditionalBasicBetter
              - conditionalSimplifiedBetter
          )
          * 100.0
      );

      System.out.printf(
          Locale.US,
          "Paired EV cost: %.6f wager units%n",
          conditionalEvCost
      );

      System.out.printf(
          Locale.US,
          "Standard error: %.6f%n",
          conditionalStandardError
      );

      System.out.printf(
          Locale.US,
          "95%% CI: %.6f to %.6f wager units%n",
          conditionalEvCost
              - conditionalCI95,
          conditionalEvCost
              + conditionalCI95
      );

      System.out.printf(
          Locale.US,
          "EV cost per $25 occurrence: $%.3f%n",
          conditionalEvCost
          * AVERAGE_BET
      );

      System.out.printf(
          Locale.US,
          "95%% CI per $25 occurrence: $%.3f to $%.3f%n",
          (
              conditionalEvCost
              - conditionalCI95
          )
          * AVERAGE_BET,
          (
              conditionalEvCost
              + conditionalCI95
          )
          * AVERAGE_BET
      );

      combinedFrequency +=
          dealerFrequency;

      combinedBasicBetter +=
          dealerBasicBetter;

      combinedSame +=
          dealerSame;

      combinedSimplifiedBetter +=
          dealerSimplifiedBetter;

      combinedEvNumerator +=
          dealerEvNumerator;

      combinedVarianceNumerator +=
          dealerVarianceNumerator;
    }

    System.out.println();

    System.out.println(
        "Combined Hard 12 vs 2/3"
    );

    System.out.println(
        "-----------------------"
    );

    double combinedBasicBetterRate =
        combinedBasicBetter
        / combinedFrequency;

    double combinedSameRate =
        combinedSame
        / combinedFrequency;

    double combinedSimplifiedBetterRate =
        combinedSimplifiedBetter
        / combinedFrequency;

    double combinedAdvantage =
        combinedBasicBetterRate
        - combinedSimplifiedBetterRate;

    double combinedEvCostPerOccurrence =
        combinedEvNumerator
        / combinedFrequency;

    double combinedStandardError =
        Math.sqrt(
            combinedVarianceNumerator
        )
        / combinedFrequency;

    double combinedCI95 =
        CI_95_Z
        * combinedStandardError;

    System.out.printf(
        Locale.US,
        "Basic Strategy better: %.3f%%%n",
        combinedBasicBetterRate
        * 100.0
    );

    System.out.printf(
        Locale.US,
        "Same monetary outcome: %.3f%%%n",
        combinedSameRate
        * 100.0
    );

    System.out.printf(
        Locale.US,
        "Simplified strategy better: %.3f%%%n",
        combinedSimplifiedBetterRate
        * 100.0
    );

    System.out.printf(
        Locale.US,
        "Basic Strategy advantage: %.3f percentage points%n",
        combinedAdvantage
        * 100.0
    );

    System.out.println();

    System.out.printf(
        Locale.US,
        "Paired EV cost: %.6f wager units%n",
        combinedEvCostPerOccurrence
    );

    System.out.printf(
        Locale.US,
        "Standard error: %.6f wager units%n",
        combinedStandardError
    );

    System.out.printf(
        Locale.US,
        "95%% CI: %.6f to %.6f wager units%n",
        combinedEvCostPerOccurrence
            - combinedCI95,
        combinedEvCostPerOccurrence
            + combinedCI95
    );

    System.out.printf(
        Locale.US,
        "Paired EV cost per occurrence: $%.3f at $25%n",
        combinedEvCostPerOccurrence
        * AVERAGE_BET
    );

    System.out.printf(
        Locale.US,
        "95%% CI per occurrence: $%.3f to $%.3f%n",
        (
            combinedEvCostPerOccurrence
            - combinedCI95
        )
        * AVERAGE_BET,
        (
            combinedEvCostPerOccurrence
            + combinedCI95
        )
        * AVERAGE_BET
    );

    double occurrencesPerSession =
        combinedFrequency
        * SESSION_HANDS;

    double sessionCost =
        occurrencesPerSession
        * combinedEvCostPerOccurrence
        * AVERAGE_BET;

    double sessionStandardError =
        occurrencesPerSession
        * combinedStandardError
        * AVERAGE_BET;

    double sessionCI95 =
        CI_95_Z
        * sessionStandardError;

    System.out.printf(
        Locale.US,
        "Expected occurrences per %d-hand session: %.3f%n",
        SESSION_HANDS,
        occurrencesPerSession
    );

    System.out.printf(
        Locale.US,
        "Expected EV cost per %d-hand session: $%.3f%n",
        SESSION_HANDS,
        sessionCost
    );

    System.out.printf(
        Locale.US,
        "Session EV standard error: $%.3f%n",
        sessionStandardError
    );

    System.out.printf(
        Locale.US,
        "95%% CI for session EV cost: $%.3f to $%.3f%n",
        sessionCost
            - sessionCI95,
        sessionCost
            + sessionCI95
    );

    System.out.println();

    System.out.println(
        "NOTE: Confidence intervals quantify"
    );

    System.out.println(
        "Monte Carlo uncertainty in the paired"
    );

    System.out.println(
        "outcome simulations. They do not yet"
    );

    System.out.println(
        "include sampling uncertainty from the"
    );

    System.out.println(
        "10-million-round frequency dataset."
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

      String composition =
          columns[6];

      if (!handType.equals("HARD")
          || playerTotal != 12
          || cardCount != 2
          || cameFromSplit
          || (dealerValue != 2
              && dealerValue != 3))
      {
        continue;
      }

      String normalized =
          normalizeFrequencyComposition(
              composition
          );

      double frequency =
          Double.parseDouble(
              columns[9]
          );

      StateKey key =
          new StateKey(
              normalized,
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
    if (result > 0)
      return "WIN";

    if (result < 0)
      return "LOSS";

    return "PUSH";
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
              "player_composition",
              "dealer_value",
              "trials",
              "basic_better",
              "same",
              "simplified_better",
              "basic_better_rate",
              "same_rate",
              "simplified_better_rate",
              "paired_ev_cost",
              "paired_standard_deviation",
              "paired_standard_error",
              "paired_ev_ci95",
              "avg_difference_when_basic_better",
              "avg_difference_when_simplified_better"
          )
          + System.lineSeparator(),
          StandardOpenOption.CREATE
      );
    }

    String row =
        String.format(
            Locale.US,
            "%s,%d,%d,%d,%d,%d,%.10f,%.10f,%.10f,%.10f,%.10f,%.10f,%.10f,%.10f,%.10f",
            result.composition(),
            result.dealerValue(),
            result.trials(),
            result.basicBetter(),
            result.same(),
            result.simplifiedBetter(),
            result.basicBetterRate(),
            result.sameRate(),
            result.simplifiedBetterRate(),
            result.meanEvDifference(),
            result.standardDeviation(),
            result.standardError(),
            result.ci95(),
            result.averageDifferenceWhenBasicBetter(),
            result.averageDifferenceWhenSimplifiedBetter()
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

    settings.setDecksInShoe(8);
    settings.setDealerHitsSoft17(true);
    settings.setDoubleAfterSplit(true);
    settings.setAllowSurrender(true);

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

  private record PairedTrial(
      double basicResult,
      double simplifiedResult)
  {
  }

  private record OutcomePair(
      String basicOutcome,
      String simplifiedOutcome)
  {
  }

  private record StateKey(
      String composition,
      int dealerValue)
  {
  }

  private record StateResult(
      String composition,
      int dealerValue,
      int trials,
      long basicBetter,
      long same,
      long simplifiedBetter,
      double meanEvDifference,
      double standardDeviation,
      double standardError,
      double ci95,
      double averageDifferenceWhenBasicBetter,
      double averageDifferenceWhenSimplifiedBetter,
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

    double simplifiedBetterRate()
    {
      return (double) simplifiedBetter
          / trials;
    }
  }
}
