import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AceSocialRuleAnalysisRunner
{
  private static final Path INPUT_FILE =
      Path.of(
          "research-data",
          "scratch",
          "ace-density-8d-h17-das-ls-3to2-50m-rounds.csv"
      );

  public static void main(String[] args)
      throws IOException
  {
    if (!Files.exists(INPUT_FILE))
    {
      throw new IllegalStateException(
          "Input file not found: "
          + INPUT_FILE
      );
    }

    List<StateRow> rows =
        readRows();

    List<Rule> rules =
        createRules();

    long totalRounds =
        0;

    double totalWeightedEv =
        0.0;

    for (StateRow row : rows)
    {
      totalRounds +=
          row.rounds();

      totalWeightedEv +=
          row.rounds()
          * row.meanEv();
    }

    double baselineEv =
        totalWeightedEv
        / totalRounds;

    System.out.println();
    System.out.println(
        "ACE SOCIAL RULE ANALYSIS"
    );
    System.out.println(
        "========================"
    );

    System.out.printf(
        Locale.US,
        "Overall flat-bet EV: %+.3f%%%n",
        baselineEv * 100.0
    );

    for (Rule rule : rules)
    {
      RuleResult result =
          evaluate(
              rule,
              rows,
              totalRounds
          );

      printResult(
          result,
          baselineEv
      );
    }
  }


  private static List<Rule> createRules()
  {
    List<Rule> rules =
        new ArrayList<>();

    /*
     * Raw-surplus rules.
     *
     * "4 decks or fewer" means only act
     * in approximately the back half.
     */
    rules.add(
        new Rule(
            "RAW >= +2, BACK HALF",
            row ->
                row.decksRemaining() <= 4.0
                && row.runningSurplus() >= 2.0
        )
    );

    rules.add(
        new Rule(
            "RAW >= +3, BACK HALF",
            row ->
                row.decksRemaining() <= 4.0
                && row.runningSurplus() >= 3.0
        )
    );

    rules.add(
        new Rule(
            "RAW >= +4, BACK HALF",
            row ->
                row.decksRemaining() <= 4.0
                && row.runningSurplus() >= 4.0
        )
    );

    rules.add(
        new Rule(
            "RAW >= +5, BACK HALF",
            row ->
                row.decksRemaining() <= 4.0
                && row.runningSurplus() >= 5.0
        )
    );

    /*
     * Avoid the sparsest final portion
     * of the shoe.
     */
    rules.add(
        new Rule(
            "RAW >= +2, 2-4 DECKS",
            row ->
                row.decksRemaining() <= 4.0
                && row.decksRemaining() >= 2.0
                && row.runningSurplus() >= 2.0
        )
    );

    rules.add(
        new Rule(
            "RAW >= +3, 2-4 DECKS",
            row ->
                row.decksRemaining() <= 4.0
                && row.decksRemaining() >= 2.0
                && row.runningSurplus() >= 3.0
        )
    );

    rules.add(
        new Rule(
            "RAW >= +4, 2-4 DECKS",
            row ->
                row.decksRemaining() <= 4.0
                && row.decksRemaining() >= 2.0
                && row.runningSurplus() >= 4.0
        )
    );

    rules.add(
        new Rule(
            "RAW >= +5, 2-4 DECKS",
            row ->
                row.decksRemaining() <= 4.0
                && row.decksRemaining() >= 2.0
                && row.runningSurplus() >= 5.0
        )
    );

    /*
     * True-surplus benchmarks.
     *
     * These are less socially attractive,
     * but they tell us how much information
     * raw rules are leaving on the table.
     */
    rules.add(
        new Rule(
            "TRUE >= +0.5, 2-4 DECKS",
            row ->
                row.decksRemaining() <= 4.0
                && row.decksRemaining() >= 2.0
                && row.trueSurplus() >= 0.5
        )
    );

    rules.add(
        new Rule(
            "TRUE >= +1.0, 2-4 DECKS",
            row ->
                row.decksRemaining() <= 4.0
                && row.decksRemaining() >= 2.0
                && row.trueSurplus() >= 1.0
        )
    );

    rules.add(
        new Rule(
            "TRUE >= +1.5, 2-4 DECKS",
            row ->
                row.decksRemaining() <= 4.0
                && row.decksRemaining() >= 2.0
                && row.trueSurplus() >= 1.5
        )
    );

    /*
     * Human milestone-style rules.
     *
     * These use only:
     * - Aces seen
     * - approximate decks remaining
     *
     * No continuous division.
     */
    rules.add(
        new Rule(
            "MILESTONE LOOSE",
            row ->
                milestoneLoose(row)
        )
    );

    rules.add(
        new Rule(
            "MILESTONE MEDIUM",
            row ->
                milestoneMedium(row)
        )
    );

    rules.add(
        new Rule(
            "MILESTONE STRICT",
            row ->
                milestoneStrict(row)
        )
    );

    return rules;
  }


  private static boolean milestoneLoose(
      StateRow row)
  {
    double decks =
        row.decksRemaining();

    if (decks > 4.0
        || decks < 2.0)
    {
      return false;
    }

    if (decks > 3.5)
      return row.acesSeen() <= 14;

    if (decks > 2.5)
      return row.acesSeen() <= 18;

    return row.acesSeen() <= 22;
  }


  private static boolean milestoneMedium(
      StateRow row)
  {
    double decks =
        row.decksRemaining();

    if (decks > 4.0
        || decks < 2.0)
    {
      return false;
    }

    if (decks > 3.5)
      return row.acesSeen() <= 13;

    if (decks > 2.5)
      return row.acesSeen() <= 17;

    return row.acesSeen() <= 21;
  }


  private static boolean milestoneStrict(
      StateRow row)
  {
    double decks =
        row.decksRemaining();

    if (decks > 4.0
        || decks < 2.0)
    {
      return false;
    }

    if (decks > 3.5)
      return row.acesSeen() <= 12;

    if (decks > 2.5)
      return row.acesSeen() <= 16;

    return row.acesSeen() <= 20;
  }


  private static RuleResult evaluate(
      Rule rule,
      List<StateRow> rows,
      long totalRounds)
  {
    long triggeredRounds =
        0;

    double triggeredSum =
        0.0;

    double triggeredSquares =
        0.0;

    for (StateRow row : rows)
    {
      if (!rule.test(row))
        continue;

      long n =
          row.rounds();

      triggeredRounds +=
          n;

      triggeredSum +=
          n
          * row.meanEv();

      double withinSquares =
          n <= 1
              ? 0.0
              : (n - 1)
                * row.standardDeviation()
                * row.standardDeviation();

      triggeredSquares +=
          withinSquares
          + n
            * row.meanEv()
            * row.meanEv();
    }

    if (triggeredRounds == 0)
    {
      return new RuleResult(
          rule.name(),
          0,
          0.0,
          0.0,
          0.0,
          0.0
      );
    }

    double mean =
        triggeredSum
        / triggeredRounds;

    double variance =
        triggeredRounds <= 1
            ? 0.0
            : (
                triggeredSquares
                - triggeredRounds
                  * mean
                  * mean
              )
              / (triggeredRounds - 1);

    variance =
        Math.max(
            0.0,
            variance
        );

    double sd =
        Math.sqrt(
            variance
        );

    double se =
        sd
        / Math.sqrt(
            triggeredRounds
        );

    double triggerRate =
        (double) triggeredRounds
        / totalRounds;

    return new RuleResult(
        rule.name(),
        triggeredRounds,
        triggerRate,
        mean,
        sd,
        se
    );
  }


  private static void printResult(
      RuleResult result,
      double baselineEv)
  {
    double margin =
        1.96
        * result.standardError();

    System.out.println();

    System.out.println(
        result.name()
    );

    System.out.println(
        "----------------------------------------"
    );

    System.out.printf(
        Locale.US,
        "Triggers: %,d hands (%.3f%% of all hands)%n",
        result.rounds(),
        result.triggerRate()
            * 100.0
    );

    System.out.printf(
        Locale.US,
        "EV when triggered: %+.3f%%%n",
        result.meanEv()
            * 100.0
    );

    System.out.printf(
        Locale.US,
        "95%% CI: %+.3f%% to %+.3f%%%n",
        (result.meanEv() - margin)
            * 100.0,
        (result.meanEv() + margin)
            * 100.0
    );

    System.out.printf(
        Locale.US,
        "Improvement vs overall baseline: %+.3f percentage points%n",
        (
            result.meanEv()
            - baselineEv
        )
        * 100.0
    );
  }


  private static List<StateRow> readRows()
      throws IOException
  {
    List<String> lines =
        Files.readAllLines(
            INPUT_FILE
        );

    List<StateRow> rows =
        new ArrayList<>();

    String[] header =
        lines.get(0)
            .split(
                ",",
                -1
            );

    int acesIndex =
        findColumn(
            header,
            "aces_seen"
        );

    int decksIndex =
        findColumn(
            header,
            "decks_remaining"
        );

    int runningIndex =
        findColumn(
            header,
            "ace_running_surplus"
        );

    int trueIndex =
        findColumn(
            header,
            "ace_true_surplus"
        );

    int roundsIndex =
        findColumn(
            header,
            "rounds"
        );

    int meanIndex =
        findColumn(
            header,
            "mean_ev_units"
        );

    int sdIndex =
        findColumn(
            header,
            "standard_deviation_units"
        );

    for (int i = 1;
         i < lines.size();
         i++)
    {
      String line =
          lines.get(i);

      if (line.isBlank())
        continue;

      String[] parts =
          line.split(
              ",",
              -1
          );

      rows.add(
          new StateRow(
              Integer.parseInt(
                  parts[acesIndex]
              ),
              Double.parseDouble(
                  parts[decksIndex]
              ),
              Double.parseDouble(
                  parts[runningIndex]
              ),
              Double.parseDouble(
                  parts[trueIndex]
              ),
              Long.parseLong(
                  parts[roundsIndex]
              ),
              Double.parseDouble(
                  parts[meanIndex]
              ),
              Double.parseDouble(
                  parts[sdIndex]
              )
          )
      );
    }

    return rows;
  }


  private static int findColumn(
      String[] header,
      String name)
  {
    for (int i = 0;
         i < header.length;
         i++)
    {
      if (header[i].equals(name))
        return i;
    }

    throw new IllegalStateException(
        "Missing column: "
        + name
    );
  }


  @FunctionalInterface
  private interface RulePredicate
  {
    boolean test(
        StateRow row);
  }


  private record Rule(
      String name,
      RulePredicate predicate)
  {
    boolean test(
        StateRow row)
    {
      return predicate.test(
          row
      );
    }
  }


  private record StateRow(
      int acesSeen,
      double decksRemaining,
      double runningSurplus,
      double trueSurplus,
      long rounds,
      double meanEv,
      double standardDeviation)
  {
  }


  private record RuleResult(
      String name,
      long rounds,
      double triggerRate,
      double meanEv,
      double standardDeviation,
      double standardError)
  {
  }
}
