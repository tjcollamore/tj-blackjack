import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SocialStrategyAnalysisRunner
{
  private static final Path HARD_12_FILE =
      Path.of(
          "research-data",
          "canonical",
          "ev-hard12-vs2to6-compositions-8d-h17-das-ls-6to5-1m.csv"
      );

  private static final Path HARD_13_FILE =
      Path.of(
          "research-data",
          "canonical",
          "ev-hard13-vs2to3-compositions-8d-h17-das-ls-6to5-1m.csv"
      );

  /*
   * For now this points to scratch because
   * we are validating the new composition-aware
   * frequency experiment.
   *
   * Once validated, move the CSV to canonical
   * and change this path.
   */
  private static final Path FREQUENCY_FILE =
      Path.of(
          "research-data",
          "canonical",
          "frequency-basicstrategy-compositions-8d-h17-das-ls-6to5-10m-rounds.csv"
      );

  private static final int SESSION_HANDS = 160;

  private static final double AVERAGE_BET =
      25.0;

  public static void main(String[] args)
      throws IOException
  {
    System.out.println(
        "TJ'S SOCIAL STRATEGY ANALYSIS"
    );

    System.out.println(
        "============================="
    );

    System.out.println();

    System.out.println(
        "Session assumptions:"
    );

    System.out.println(
        "Hands: "
        + SESSION_HANDS
    );

    System.out.println(
        "Average bet: $"
        + String.format(
            "%.2f",
            AVERAGE_BET
        )
    );

    System.out.println();

    analyzeDealerUpCard(2);
    analyzeDealerUpCard(3);

    analyzeCombinedRule();
  }

  private static void analyzeDealerUpCard(
      int dealerValue)
      throws IOException
  {
    WeightedStateAnalysis hard12 =
        getWeightedStateAnalysis(
            HARD_12_FILE,
            12,
            dealerValue,
            HandAction.STAND
        );

    WeightedStateAnalysis hard13 =
        getWeightedStateAnalysis(
            HARD_13_FILE,
            13,
            dealerValue,
            HandAction.HIT
        );

    double hard12Occurrences =
        hard12.frequencyPerRound()
        * SESSION_HANDS;

    double hard13Occurrences =
        hard13.frequencyPerRound()
        * SESSION_HANDS;

    double simplificationCost =
        hard12Occurrences
        * hard12.weightedEvCost()
        * AVERAGE_BET;

    double confusionCostAt100Percent =
        hard13Occurrences
        * hard13.weightedEvCost()
        * AVERAGE_BET;

    double breakEvenConfusionRate =
        simplificationCost
        / confusionCostAt100Percent;

    System.out.println(
        "Dealer "
        + dealerValue
    );

    System.out.println(
        "---------"
    );

    System.out.printf(
        "Hard 12 frequency: %.6f per round%n",
        hard12.frequencyPerRound()
    );

    System.out.printf(
        "Frequency-weighted Hard 12 EV cost: %.6f wager units%n",
        hard12.weightedEvCost()
    );

    System.out.printf(
        "Expected Hard 12 occurrences/session: %.3f%n",
        hard12Occurrences
    );

    System.out.printf(
        "Cost of standing on Hard 12: $%.3f/session%n",
        simplificationCost
    );

    System.out.println();

    System.out.printf(
        "Hard 13 frequency: %.6f per round%n",
        hard13.frequencyPerRound()
    );

    System.out.printf(
        "Frequency-weighted Hard 13 EV cost: %.6f wager units%n",
        hard13.weightedEvCost()
    );

    System.out.printf(
        "Expected Hard 13 occurrences/session: %.3f%n",
        hard13Occurrences
    );

    System.out.printf(
        "Cost if Hard 13 is mistakenly hit every time: $%.3f/session%n",
        confusionCostAt100Percent
    );

    System.out.printf(
        "Break-even Hard 13 confusion rate: %.1f%%%n",
        breakEvenConfusionRate
        * 100.0
    );

    System.out.println();
  }

  private static void analyzeCombinedRule()
      throws IOException
  {
    double totalSimplificationCost =
        0;

    double totalConfusionCost =
        0;

    for (int dealerValue :
         new int[] { 2, 3 })
    {
      WeightedStateAnalysis hard12 =
          getWeightedStateAnalysis(
              HARD_12_FILE,
              12,
              dealerValue,
              HandAction.STAND
          );

      WeightedStateAnalysis hard13 =
          getWeightedStateAnalysis(
              HARD_13_FILE,
              13,
              dealerValue,
              HandAction.HIT
          );

      totalSimplificationCost +=
          hard12.frequencyPerRound()
          * SESSION_HANDS
          * hard12.weightedEvCost()
          * AVERAGE_BET;

      totalConfusionCost +=
          hard13.frequencyPerRound()
          * SESSION_HANDS
          * hard13.weightedEvCost()
          * AVERAGE_BET;
    }

    double breakEvenRate =
        totalSimplificationCost
        / totalConfusionCost;

    System.out.println(
        "COMBINED 12 vs 2/3 RULE"
    );

    System.out.println(
        "-----------------------"
    );

    System.out.printf(
        "Expected cost of always standing on Hard 12 vs 2/3: $%.3f/session%n",
        totalSimplificationCost
    );

    System.out.printf(
        "Expected cost if Hard 13 vs 2/3 is mistakenly hit every time: $%.3f/session%n",
        totalConfusionCost
    );

    System.out.printf(
        "Break-even confusion rate: %.1f%%%n",
        breakEvenRate
        * 100.0
    );

    System.out.println();

    System.out.println(
        "Interpretation:"
    );

    System.out.println(
        "If learning the Hard 12 exception causes"
    );

    System.out.printf(
        "Hard 13 vs 2/3 to be misplayed more than about %.1f%%%n",
        breakEvenRate
        * 100.0
    );

    System.out.println(
        "of the time, the simplified rule would"
    );

    System.out.println(
        "cost less in these states."
    );

    System.out.println();

    System.out.println(
        "EV costs are now weighted by the actual"
    );

    System.out.println(
        "frequency of each two-card composition"
    );

    System.out.println(
        "observed in the 10-million-round"
    );

    System.out.println(
        "Basic Strategy frequency experiment."
    );
  }

  private static WeightedStateAnalysis
      getWeightedStateAnalysis(
          Path evFile,
          int playerTotal,
          int dealerValue,
          HandAction wrongAction)
      throws IOException
  {
    Map<String, Double> evCosts =
        getEvCostsByComposition(
            evFile,
            playerTotal,
            dealerValue,
            wrongAction
        );

    Map<String, Double> frequencies =
        getFrequenciesByComposition(
            playerTotal,
            dealerValue
        );

    double totalFrequency =
        0;

    double weightedCostTotal =
        0;

    for (Map.Entry<
        String,
        Double
    > entry :
        frequencies.entrySet())
    {
      String composition =
          entry.getKey();

      double frequency =
          entry.getValue();

      Double evCost =
          evCosts.get(
              composition
          );

      if (evCost == null)
      {
        throw new IllegalStateException(
            "No EV cost found for composition "
            + composition
            + " for player "
            + playerTotal
            + " vs dealer "
            + dealerValue
        );
      }

      totalFrequency +=
          frequency;

      weightedCostTotal +=
          frequency
          * evCost;
    }

    if (totalFrequency == 0)
    {
      throw new IllegalStateException(
          "No frequency rows found for player "
          + playerTotal
          + " vs dealer "
          + dealerValue
      );
    }

    double weightedEvCost =
        weightedCostTotal
        / totalFrequency;

    return new WeightedStateAnalysis(
        totalFrequency,
        weightedEvCost
    );
  }

  private static Map<String, Double>
      getEvCostsByComposition(
          Path file,
          int playerTotal,
          int dealerValue,
          HandAction wrongAction)
      throws IOException
  {
    Map<String, Double> costs =
        new HashMap<>();

    List<String> lines =
        Files.readAllLines(
            file
        );

    for (int i = 1;
         i < lines.size();
         i++)
    {
      String[] columns =
          lines.get(i)
              .split(",", -1);

      String playerCards =
          columns[5];

      int rowPlayerTotal =
          Integer.parseInt(
              columns[6]
          );

      int rowDealerValue =
          Integer.parseInt(
              columns[9]
          );

      HandAction action =
          HandAction.valueOf(
              columns[10]
          );

      if (rowPlayerTotal == playerTotal
          && rowDealerValue == dealerValue
          && action == wrongAction)
      {
        String composition =
            normalizeComposition(
                playerCards
            );

        double evCost =
            Double.parseDouble(
                columns[22]
            );

        costs.put(
            composition,
            evCost
        );
      }
    }

    if (costs.isEmpty())
    {
      throw new IllegalStateException(
          "No EV rows found for player "
          + playerTotal
          + " vs dealer "
          + dealerValue
          + " action "
          + wrongAction
      );
    }

    return costs;
  }

  private static Map<String, Double>
      getFrequenciesByComposition(
          int playerTotal,
          int dealerValue)
      throws IOException
  {
    Map<String, Double> frequencies =
        new HashMap<>();

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

      int rowPlayerTotal =
          Integer.parseInt(
              columns[1]
          );

      int rowDealerValue =
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

      String playerComposition =
          columns[6];

      if (handType.equals("HARD")
          && rowPlayerTotal == playerTotal
          && rowDealerValue == dealerValue
          && cardCount == 2
          && !cameFromSplit)
      {
        String composition =
            normalizeComposition(
                playerComposition
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
    }

    return frequencies;
  }

  /*
   * Converts both research formats into the
   * same representation.
   *
   * Examples:
   *
   *   "10+2"       -> "2+10"
   *   "TWO+TEN"    -> "2+10"
   *   "TWO+JACK"   -> "2+10"
   *   "TWO+QUEEN"  -> "2+10"
   *   "TWO+KING"   -> "2+10"
   *
   * All ten-value cards are equivalent for
   * blackjack decision purposes.
   */
  private static String normalizeComposition(
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
          compositionValue(
              part.trim()
          )
      );
    }

    values.sort(
        Integer::compareTo
    );

    StringBuilder result =
        new StringBuilder();

    for (int i = 0;
         i < values.size();
         i++)
    {
      if (i > 0)
      {
        result.append("+");
      }

      result.append(
          values.get(i)
      );
    }

    return result.toString();
  }

    private static int compositionValue(
        String value)
    {
      String rank =
          value;

      int underscoreIndex =
          rank.indexOf("_");

      if (underscoreIndex >= 0)
      {
        rank =
            rank.substring(
                0,
                underscoreIndex
            );
      }

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
            Integer.parseInt(
                rank
            );
      };
    }
    
  private record WeightedStateAnalysis(
      double frequencyPerRound,
      double weightedEvCost)
  {
  }
}
