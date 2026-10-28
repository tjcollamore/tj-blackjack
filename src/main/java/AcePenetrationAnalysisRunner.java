import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class AcePenetrationAnalysisRunner
{
  private static final double CI_95_Z =
      1.96;


  private static final Path INPUT_FILE =
      Path.of(
          "research-data",
          "scratch",
          "ace-density-8d-h17-das-ls-3to2-50m-rounds.csv"
      );


  private static final Path OUTPUT_FILE =
      Path.of(
          "research-data",
          "scratch",
          "ace-penetration-analysis-8d-h17-das-ls-3to2-50m.csv"
      );


  public static void main(
      String[] args)
      throws IOException
  {
    if (!Files.exists(
            INPUT_FILE))
    {
      throw new IllegalStateException(
          "Input file not found: "
          + INPUT_FILE
      );
    }


    if (Files.exists(
            OUTPUT_FILE))
    {
      throw new IllegalStateException(
          "Output file already exists: "
          + OUTPUT_FILE
      );
    }


    List<StateRow> rows =
        readRows();


    Map<String, Accumulator>
        penetrationOnly =
            new LinkedHashMap<>();


    Map<String, Accumulator>
        rawBins =
            new LinkedHashMap<>();


    Map<String, Accumulator>
        trueBins =
            new LinkedHashMap<>();


    for (StateRow row :
         rows)
    {
      String penetration =
          penetrationBand(
              row.decksRemaining()
          );


      add(
          penetrationOnly,
          penetration,
          row
      );


      add(
          rawBins,
          penetration
              + ","
              + rawBand(
                  row.runningSurplus()
              ),
          row
      );


      add(
          trueBins,
          penetration
              + ","
              + trueBand(
                  row.trueSurplus()
              ),
          row
      );
    }


    writeHeader();


    System.out.println();

    System.out.println(
        "ACE PENETRATION ANALYSIS"
    );

    System.out.println(
        "========================"
    );


    System.out.println();

    System.out.println(
        "BASELINE BY PENETRATION"
    );

    System.out.println(
        "-----------------------"
    );


    for (Map.Entry<String, Accumulator> entry :
         penetrationOnly.entrySet())
    {
      Summary summary =
          entry.getValue()
              .finish();


      printSummary(
          entry.getKey(),
          summary
      );


      writeSummary(
          "PENETRATION_BASELINE",
          entry.getKey(),
          "ALL",
          summary
      );
    }


    System.out.println();

    System.out.println(
        "RAW ACE SURPLUS"
    );

    System.out.println(
        "---------------"
    );


    for (Map.Entry<String, Accumulator> entry :
         rawBins.entrySet())
    {
      String[] parts =
          entry.getKey()
              .split(
                  ",",
                  2
              );


      Summary summary =
          entry.getValue()
              .finish();


      printDetailedSummary(
          parts[0],
          parts[1],
          summary
      );


      writeSummary(
          "RAW_SURPLUS",
          parts[0],
          parts[1],
          summary
      );
    }


    System.out.println();

    System.out.println(
        "TRUE ACE SURPLUS"
    );

    System.out.println(
        "----------------"
    );


    for (Map.Entry<String, Accumulator> entry :
         trueBins.entrySet())
    {
      String[] parts =
          entry.getKey()
              .split(
                  ",",
                  2
              );


      Summary summary =
          entry.getValue()
              .finish();


      printDetailedSummary(
          parts[0],
          parts[1],
          summary
      );


      writeSummary(
          "TRUE_SURPLUS",
          parts[0],
          parts[1],
          summary
      );
    }


    System.out.println();

    System.out.println(
        "Saved results to "
        + OUTPUT_FILE.toAbsolutePath()
    );
  }


  // ============================================================
  // BANDS
  // ============================================================

  private static String penetrationBand(
      double decksRemaining)
  {
    if (decksRemaining >= 6.0)
      return "6_TO_8";

    if (decksRemaining >= 4.0)
      return "4_TO_6";

    if (decksRemaining >= 2.0)
      return "2_TO_4";

    return "UNDER_2";
  }


  private static String rawBand(
      double surplus)
  {
    if (surplus < -4.0)
      return "LT_-4";

    if (surplus < -2.0)
      return "-4_TO_-2";

    if (surplus < 0.0)
      return "-2_TO_0";

    if (surplus < 2.0)
      return "0_TO_2";

    if (surplus < 4.0)
      return "2_TO_4";

    if (surplus < 6.0)
      return "4_TO_6";

    return "GE_6";
  }


  private static String trueBand(
      double surplus)
  {
    if (surplus < -1.0)
      return "LT_-1";

    if (surplus < -0.5)
      return "-1_TO_-0.5";

    if (surplus < 0.0)
      return "-0.5_TO_0";

    if (surplus < 0.5)
      return "0_TO_0.5";

    if (surplus < 1.0)
      return "0.5_TO_1";

    if (surplus < 1.5)
      return "1_TO_1.5";

    return "GE_1.5";
  }


  // ============================================================
  // AGGREGATION
  // ============================================================

  private static void add(
      Map<String, Accumulator> map,
      String key,
      StateRow row)
  {
    map.computeIfAbsent(
        key,
        ignored ->
            new Accumulator()
    ).add(
        row.rounds(),
        row.mean(),
        row.standardDeviation()
    );
  }


  // ============================================================
  // INPUT
  // ============================================================

  private static List<StateRow> readRows()
      throws IOException
  {
    List<String> lines =
        Files.readAllLines(
            INPUT_FILE
        );


    if (lines.size() < 2)
    {
      throw new IllegalStateException(
          "Input file is empty"
      );
    }


    String[] header =
        lines.get(0)
            .split(
                ",",
                -1
            );


    Map<String, Integer> index =
        new LinkedHashMap<>();


    for (int i = 0;
         i < header.length;
         i++)
    {
      index.put(
          header[i],
          i
      );
    }


    requireColumn(
        index,
        "decks_remaining"
    );

    requireColumn(
        index,
        "ace_running_surplus"
    );

    requireColumn(
        index,
        "ace_true_surplus"
    );

    requireColumn(
        index,
        "rounds"
    );

    requireColumn(
        index,
        "mean_ev_units"
    );

    requireColumn(
        index,
        "standard_deviation_units"
    );


    List<StateRow> rows =
        new ArrayList<>();


    for (int lineIndex = 1;
         lineIndex < lines.size();
         lineIndex++)
    {
      String line =
          lines.get(
              lineIndex
          );


      if (line.isBlank())
        continue;


      String[] parts =
          line.split(
              ",",
              -1
          );


      rows.add(
          new StateRow(
              Double.parseDouble(
                  parts[
                      index.get(
                          "decks_remaining"
                      )
                  ]
              ),
              Double.parseDouble(
                  parts[
                      index.get(
                          "ace_running_surplus"
                      )
                  ]
              ),
              Double.parseDouble(
                  parts[
                      index.get(
                          "ace_true_surplus"
                      )
                  ]
              ),
              Long.parseLong(
                  parts[
                      index.get(
                          "rounds"
                      )
                  ]
              ),
              Double.parseDouble(
                  parts[
                      index.get(
                          "mean_ev_units"
                      )
                  ]
              ),
              Double.parseDouble(
                  parts[
                      index.get(
                          "standard_deviation_units"
                      )
                  ]
              )
          )
      );
    }


    return rows;
  }


  private static void requireColumn(
      Map<String, Integer> index,
      String column)
  {
    if (!index.containsKey(
            column))
    {
      throw new IllegalStateException(
          "Missing column: "
          + column
      );
    }
  }


  // ============================================================
  // OUTPUT
  // ============================================================

  private static void writeHeader()
      throws IOException
  {
    String header =
        String.join(
            ",",
            "analysis_type",
            "penetration_band",
            "signal_band",
            "rounds",
            "mean_ev_units",
            "mean_ev_percent",
            "standard_deviation_units",
            "standard_error_units",
            "ci95_low_units",
            "ci95_high_units"
        )
        + System.lineSeparator();


    Files.writeString(
        OUTPUT_FILE,
        header,
        StandardOpenOption.CREATE
    );
  }


  private static void writeSummary(
      String analysisType,
      String penetration,
      String signalBand,
      Summary summary)
      throws IOException
  {
    double margin =
        CI_95_Z
        * summary.standardError();


    String row =
        String.format(
            Locale.US,
            "%s,%s,%s,%d,"
            + "%.10f,%.10f,%.10f,%.10f,"
            + "%.10f,%.10f%n",
            analysisType,
            penetration,
            signalBand,
            summary.count(),
            summary.mean(),
            summary.mean()
                * 100.0,
            summary.standardDeviation(),
            summary.standardError(),
            summary.mean()
                - margin,
            summary.mean()
                + margin
        );


    Files.writeString(
        OUTPUT_FILE,
        row,
        StandardOpenOption.APPEND
    );
  }


  private static void printSummary(
      String label,
      Summary summary)
  {
    double margin =
        CI_95_Z
        * summary.standardError();


    System.out.printf(
        Locale.US,
        "%-12s  %,10d hands   EV %+.3f%%   "
        + "95%% CI %+.3f%% to %+.3f%%%n",
        label,
        summary.count(),
        summary.mean()
            * 100.0,
        (summary.mean() - margin)
            * 100.0,
        (summary.mean() + margin)
            * 100.0
    );
  }


  private static void printDetailedSummary(
      String penetration,
      String signalBand,
      Summary summary)
  {
    double margin =
        CI_95_Z
        * summary.standardError();


    System.out.printf(
        Locale.US,
        "%-10s %-12s %,10d hands   "
        + "EV %+.3f%%   "
        + "95%% CI %+.3f%% to %+.3f%%%n",
        penetration,
        signalBand,
        summary.count(),
        summary.mean()
            * 100.0,
        (summary.mean() - margin)
            * 100.0,
        (summary.mean() + margin)
            * 100.0
    );
  }


  // ============================================================
  // DATA
  // ============================================================

  private record StateRow(
      double decksRemaining,
      double runningSurplus,
      double trueSurplus,
      long rounds,
      double mean,
      double standardDeviation)
  {
  }


  private record Summary(
      long count,
      double mean,
      double standardDeviation,
      double standardError)
  {
  }


  private static class Accumulator
  {
    private long count;

    private double sum;
    private double sumSquares;


    void add(
        long groupCount,
        double groupMean,
        double groupStandardDeviation)
    {
      /*
       * Recover the aggregate sum.
       */
      sum +=
          groupCount
          * groupMean;


      /*
       * Recover sum(x^2) from:
       *
       * sample variance
       * =
       * [sum(x^2) - n * mean^2]
       * / (n - 1)
       */
      double withinGroupSquares =
          groupCount <= 1
              ? 0.0
              : (groupCount - 1)
                * groupStandardDeviation
                * groupStandardDeviation;


      sumSquares +=
          withinGroupSquares
          + groupCount
            * groupMean
            * groupMean;


      count +=
          groupCount;
    }


    Summary finish()
    {
      if (count == 0)
      {
        return new Summary(
            0,
            0.0,
            0.0,
            0.0
        );
      }


      double mean =
          sum
          / count;


      double variance =
          count <= 1
              ? 0.0
              : (
                  sumSquares
                  - count
                    * mean
                    * mean
                )
                / (count - 1);


      /*
       * Protect against a tiny negative caused
       * by floating-point rounding.
       */
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
              count
          );


      return new Summary(
          count,
          mean,
          sd,
          se
      );
    }
  }
}