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

public class PairedHard9Vs2ResearchRunner
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
          "paired-hard9-vs2-double-8d-h17-das-ls-6to5-1m.csv"
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

  public PairedHard9Vs2ResearchRunner(
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

    PairedHard9Vs2ResearchRunner runner =
        new PairedHard9Vs2ResearchRunner(
            settings
        );

    Rank[][] compositions =
    {
        { Rank.SEVEN, Rank.TWO },
        { Rank.SIX, Rank.THREE },
        { Rank.FIVE, Rank.FOUR }
    };

    List<StateResult> results =
        new ArrayList<>();

    for (Rank[] composition :
         compositions)
    {
      StateResult result =
          runner.runState(
              composition[0],
              composition[1]
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
      Rank secondRank)
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
            Rank.TWO,
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

    HandAction socialAction =
        HandAction.DOUBLE;

    if (basicAction != HandAction.HIT)
    {
      throw new IllegalStateException(
          "Expected Basic Strategy to HIT "
          + "Hard 9 vs 2, but got "
          + basicAction
      );
    }

    if (!allowedActions.contains(
        HandAction.DOUBLE))
    {
      throw new IllegalStateException(
          "DOUBLE is not allowed for "
          + "this Hard 9 state"
      );
    }

    long basicBetter =
        0;

    long same =
        0;

    long socialBetter =
        0;

    double differenceMean =
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
              basicAction,
              socialAction
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
            "  Completed "
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

    double avgBasicBetter =
        basicBetter == 0
        ? 0.0
        : differenceWhenBasicBetter
          / basicBetter;

    double avgSocialBetter =
        socialBetter == 0
        ? 0.0
        : differenceWhenSocialBetter
          / socialBetter;

    return new StateResult(
        normalizeComposition(
            firstRank,
            secondRank
        ),
        TRIALS_PER_STATE,
        basicBetter,
        same,
        socialBetter,
        differenceMean,
        standardDeviation,
        standardError,
        ci95,
        avgBasicBetter,
        avgSocialBetter,
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
              + "occur in Hard 9 research"
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
        + " vs dealer 2"
    );

    System.out.println(
        "--------------------"
    );

    System.out.printf(
        Locale.US,
        "Basic Strategy (HIT) better: %.3f%%%n",
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
        "Social Strategy (DOUBLE) better: %.3f%%%n",
        result.socialBetterRate()
        * 100.0
    );

    System.out.printf(
        Locale.US,
        "Basic Strategy advantage: %.3f percentage points%n",
        (
            result.basicBetterRate()
            - result.socialBetterRate()
        )
        * 100.0
    );

    System.out.println();

    System.out.printf(
        Locale.US,
        "Paired EV cost of doubling: %.6f wager units%n",
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
        "Average difference when Basic Strategy is better: $%.2f%n",
        result.averageDifferenceWhenBasicBetter()
        * AVERAGE_BET
    );

    System.out.printf(
        Locale.US,
        "Average difference when Social Strategy is better: $%.2f%n",
        result.averageDifferenceWhenSocialBetter()
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
          "  %-7s -> %-7s : %.3f%%%n",
          entry.getKey()
              .basicOutcome(),
          entry.getKey()
              .socialOutcome(),
          rate * 100.0
      );
    }
  }

  private static void printWeightedSummary(
      List<StateResult> results)
      throws IOException
  {
    Map<String, Double> frequencies =
        loadFrequencies();

    double totalFrequency =
        0.0;

    double weightedBasicBetter =
        0.0;

    double weightedSame =
        0.0;

    double weightedSocialBetter =
        0.0;

    double weightedEv =
        0.0;

    double varianceNumerator =
        0.0;

    for (StateResult result :
         results)
    {
      Double frequency =
          frequencies.get(
              result.composition()
          );

      if (frequency == null)
      {
        throw new IllegalStateException(
            "Missing frequency for "
            + result.composition()
        );
      }

      totalFrequency +=
          frequency;

      weightedBasicBetter +=
          frequency
          * result.basicBetterRate();

      weightedSame +=
          frequency
          * result.sameRate();

      weightedSocialBetter +=
          frequency
          * result.socialBetterRate();

      weightedEv +=
          frequency
          * result.meanEvDifference();

      varianceNumerator +=
          frequency
          * frequency
          * result.standardError()
          * result.standardError();
    }

    double basicBetterRate =
        weightedBasicBetter
        / totalFrequency;

    double sameRate =
        weightedSame
        / totalFrequency;

    double socialBetterRate =
        weightedSocialBetter
        / totalFrequency;

    double evCostPerOccurrence =
        weightedEv
        / totalFrequency;

    double standardError =
        Math.sqrt(
            varianceNumerator
        )
        / totalFrequency;

    double ci95 =
        CI_95_Z
        * standardError;

    double occurrencesPerSession =
        totalFrequency
        * SESSION_HANDS;

    double sessionCost =
        occurrencesPerSession
        * evCostPerOccurrence
        * AVERAGE_BET;

    double sessionStandardError =
        occurrencesPerSession
        * standardError
        * AVERAGE_BET;

    double sessionCI95 =
        CI_95_Z
        * sessionStandardError;

    System.out.println();

    System.out.println(
        "FREQUENCY-WEIGHTED HARD 9 VS 2"
    );

    System.out.println(
        "=============================="
    );

    System.out.printf(
        Locale.US,
        "Basic Strategy (HIT) better: %.3f%%%n",
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
        "Social Strategy (DOUBLE) better: %.3f%%%n",
        socialBetterRate
        * 100.0
    );

    System.out.printf(
        Locale.US,
        "Basic Strategy advantage: %.3f percentage points%n",
        (
            basicBetterRate
            - socialBetterRate
        )
        * 100.0
    );

    System.out.println();

    System.out.printf(
        Locale.US,
        "Paired EV cost: %.6f wager units%n",
        evCostPerOccurrence
    );

    System.out.printf(
        Locale.US,
        "Standard error: %.6f wager units%n",
        standardError
    );

    System.out.printf(
        Locale.US,
        "95%% CI: %.6f to %.6f wager units%n",
        evCostPerOccurrence
            - ci95,
        evCostPerOccurrence
            + ci95
    );

    System.out.printf(
        Locale.US,
        "EV cost per $25 occurrence: $%.3f%n",
        evCostPerOccurrence
        * AVERAGE_BET
    );

    System.out.printf(
        Locale.US,
        "95%% CI per occurrence: $%.3f to $%.3f%n",
        (
            evCostPerOccurrence
            - ci95
        )
        * AVERAGE_BET,
        (
            evCostPerOccurrence
            + ci95
        )
        * AVERAGE_BET
    );

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
        "95%% CI for session EV cost: $%.3f to $%.3f%n",
        sessionCost
            - sessionCI95,
        sessionCost
            + sessionCI95
    );

    System.out.println();

    System.out.println(
        "Candidate Social Strategy rule:"
    );

    System.out.println(
        "Double Hard 9 vs dealer 2-6."
    );
  }

  private static Map<String, Double>
      loadFrequencies()
      throws IOException
  {
    Map<String, Double> frequencies =
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

      if (!handType.equals("HARD")
          || playerTotal != 9
          || dealerValue != 2
          || cardCount != 2
          || cameFromSplit)
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

      frequencies.merge(
          composition,
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
            "%s,2,HIT,DOUBLE,%d,%d,%d,%d,%.10f,%.10f,%.10f,%.10f,%.10f,%.10f,%.10f,%.10f,%.10f",
            result.composition(),
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
      double socialResult)
  {
  }

  private record OutcomePair(
      String basicOutcome,
      String socialOutcome)
  {
  }

  private record StateResult(
      String composition,
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