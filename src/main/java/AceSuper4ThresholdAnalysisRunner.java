import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AceSuper4ThresholdAnalysisRunner
{
  private static final double SUPER4_BET =
      5.0;

  private static final double PAY_TRIPS =
      800.0;

  private static final double PAY_STRAIGHT =
      400.0;

  private static final double PAY_FLUSH =
      300.0;

  private static final double PAY_TWO_PAIR =
      200.0;

  private static final double PAY_SAME_COLOR =
      100.0;

  private static final double PAY_PAIR =
      50.0;

  private static final double PAY_OTHER_BJ =
      25.0;

  private static final double PAY_ACE_NO_BJ =
      10.0;


  private static final double[] THRESHOLDS =
  {
      2.0,
      2.5,
      3.0,
      3.5,
      4.0,
      4.5,
      5.0,
      5.5,
      6.0
  };


  private static final double[] TEST_JACKPOTS =
  {
      400_000.0,
      650_000.0,
      900_000.0
  };


  private static final Path INPUT_FILE =
      Path.of(
          "research-data",
          "scratch",
          "ace-super4-density-8d-h17-das-ls-3to2-50m-rounds.csv"
      );


  private static final Path OUTPUT_FILE =
      Path.of(
          "research-data",
          "scratch",
          "ace-super4-threshold-analysis-8d-h17-das-ls-3to2-50m.csv"
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


    long totalRounds =
        0;

    for (StateRow row :
         rows)
    {
      totalRounds +=
          row.rounds();
    }


    writeHeader();


    System.out.println();

    System.out.println(
        "ACE SUPER 4 THRESHOLD ANALYSIS"
    );

    System.out.println(
        "=============================="
    );

    System.out.printf(
        Locale.US,
        "Total source rounds: %,d%n",
        totalRounds
    );


    for (double threshold :
         THRESHOLDS)
    {
      ThresholdSummary summary =
          summarize(
              rows,
              threshold,
              totalRounds
          );

      printSummary(
          summary
      );

      writeSummary(
          summary
      );
    }


    System.out.println();

    System.out.println(
        "Saved results to "
        + OUTPUT_FILE.toAbsolutePath()
    );
  }


  private static ThresholdSummary summarize(
      List<StateRow> rows,
      double threshold,
      long totalRounds)
  {
    long qualifying =
        0;

    long loss =
        0;

    long aceNoBj =
        0;

    long otherBj =
        0;

    long pair =
        0;

    long sameColor =
        0;

    long twoPair =
        0;

    long flush =
        0;

    long straight =
        0;

    long trips =
        0;

    long royalMajor =
        0;

    long royalOther =
        0;


    for (StateRow row :
         rows)
    {
      if (row.runningSurplus()
          < threshold)
      {
        continue;
      }


      qualifying +=
          row.rounds();

      loss +=
          row.loss();

      aceNoBj +=
          row.aceNoBj();

      otherBj +=
          row.otherBj();

      pair +=
          row.pair();

      sameColor +=
          row.sameColor();

      twoPair +=
          row.twoPair();

      flush +=
          row.flush();

      straight +=
          row.straight();

      trips +=
          row.trips();

      royalMajor +=
          row.royalMajor();

      royalOther +=
          row.royalOther();
    }


    if (qualifying == 0)
    {
      return new ThresholdSummary(
          threshold,
          0,
          0.0,
          0.0,
          0.0,
          0.0,
          0.0,
          0.0,
          0.0,
          0.0,
          0.0,
          0.0,
          0.0,
          0.0
      );
    }


    double fixedReturnPerBet =
        (
            aceNoBj
                * PAY_ACE_NO_BJ
            + otherBj
                * PAY_OTHER_BJ
            + pair
                * PAY_PAIR
            + sameColor
                * PAY_SAME_COLOR
            + twoPair
                * PAY_TWO_PAIR
            + flush
                * PAY_FLUSH
            + straight
                * PAY_STRAIGHT
            + trips
                * PAY_TRIPS
        )
        / qualifying;


    double majorRoyalProbability =
        (double) royalMajor
        / qualifying;


    double otherRoyalProbability =
        (double) royalOther
        / qualifying;


    double royalProbability =
        (
            (double) royalMajor
            + royalOther
        )
        / qualifying;


    double progressiveCoefficient =
        majorRoyalProbability
        + 0.10
          * otherRoyalProbability;


    double breakEvenJackpot =
        progressiveCoefficient <= 0.0
            ? Double.POSITIVE_INFINITY
            : (
                  SUPER4_BET
                  - fixedReturnPerBet
              )
              / progressiveCoefficient;


    double fixedProfitPerBet =
        fixedReturnPerBet
        - SUPER4_BET;


    double fixedEdge =
        fixedProfitPerBet
        / SUPER4_BET;


    double ev400 =
        profitPerBetAtJackpot(
            fixedReturnPerBet,
            majorRoyalProbability,
            otherRoyalProbability,
            400_000.0
        );


    double ev650 =
        profitPerBetAtJackpot(
            fixedReturnPerBet,
            majorRoyalProbability,
            otherRoyalProbability,
            650_000.0
        );


    double ev900 =
        profitPerBetAtJackpot(
            fixedReturnPerBet,
            majorRoyalProbability,
            otherRoyalProbability,
            900_000.0
        );


    return new ThresholdSummary(
        threshold,
        qualifying,
        (double) qualifying
            / totalRounds,
        fixedReturnPerBet,
        fixedProfitPerBet,
        fixedEdge,
        royalProbability,
        majorRoyalProbability,
        otherRoyalProbability,
        progressiveCoefficient,
        breakEvenJackpot,
        ev400,
        ev650,
        ev900
    );
  }


  private static double profitPerBetAtJackpot(
      double fixedReturnPerBet,
      double majorRoyalProbability,
      double otherRoyalProbability,
      double jackpot)
  {
    double progressiveReturn =
        majorRoyalProbability
            * jackpot
        + otherRoyalProbability
            * jackpot
            * 0.10;


    return fixedReturnPerBet
        + progressiveReturn
        - SUPER4_BET;
  }


  private static void printSummary(
      ThresholdSummary s)
  {
    System.out.println();

    System.out.printf(
        Locale.US,
        "RAW SURPLUS >= +%.1f%n",
        s.threshold()
    );

    System.out.println(
        "----------------------------------------"
    );

    System.out.printf(
        Locale.US,
        "Qualifying hands: %,d (%.4f%% of all hands)%n",
        s.qualifyingHands(),
        s.triggerRate()
            * 100.0
    );

    System.out.printf(
        Locale.US,
        "Royal probability: %.8f%% (1 in %.0f)%n",
        s.royalProbability()
            * 100.0,
        reciprocalOrInfinity(
            s.royalProbability()
        )
    );

    System.out.printf(
        Locale.US,
        "Major royal probability: %.8f%% (1 in %.0f)%n",
        s.majorRoyalProbability()
            * 100.0,
        reciprocalOrInfinity(
            s.majorRoyalProbability()
        )
    );

    System.out.printf(
        Locale.US,
        "Other royal probability: %.8f%% (1 in %.0f)%n",
        s.otherRoyalProbability()
            * 100.0,
        reciprocalOrInfinity(
            s.otherRoyalProbability()
        )
    );

    System.out.printf(
        Locale.US,
        "Fixed-payout-only P/L per bet: $%+.4f%n",
        s.fixedProfitPerBet()
    );

    System.out.printf(
        Locale.US,
        "Fixed-payout-only edge: %+.3f%%%n",
        s.fixedEdge()
            * 100.0
    );

    if (Double.isFinite(
            s.breakEvenJackpot()))
    {
      System.out.printf(
          Locale.US,
          "Breakeven major jackpot: $%,.0f%n",
          s.breakEvenJackpot()
      );
    }
    else
    {
      System.out.println(
          "Breakeven major jackpot: unavailable"
      );
    }


    System.out.printf(
        Locale.US,
        "P/L per bet at $400k major: $%+.4f (%+.3f%% edge)%n",
        s.profit400k(),
        s.profit400k()
            / SUPER4_BET
            * 100.0
    );

    System.out.printf(
        Locale.US,
        "P/L per bet at $650k major: $%+.4f (%+.3f%% edge)%n",
        s.profit650k(),
        s.profit650k()
            / SUPER4_BET
            * 100.0
    );

    System.out.printf(
        Locale.US,
        "P/L per bet at $900k major: $%+.4f (%+.3f%% edge)%n",
        s.profit900k(),
        s.profit900k()
            / SUPER4_BET
            * 100.0
    );
  }


  private static double reciprocalOrInfinity(
      double probability)
  {
    return probability <= 0.0
        ? Double.POSITIVE_INFINITY
        : 1.0
          / probability;
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


    String[] required =
    {
        "ace_running_surplus",
        "rounds",
        "loss",
        "ace_no_bj",
        "other_bj",
        "pair",
        "same_color",
        "two_pair",
        "flush",
        "straight",
        "trips",
        "royal_major",
        "royal_other"
    };


    for (String column :
         required)
    {
      requireColumn(
          index,
          column
      );
    }


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
      {
        continue;
      }


      String[] parts =
          line.split(
              ",",
              -1
          );


      rows.add(
          new StateRow(
              parseDouble(
                  parts,
                  index,
                  "ace_running_surplus"
              ),
              parseLong(
                  parts,
                  index,
                  "rounds"
              ),
              parseLong(
                  parts,
                  index,
                  "loss"
              ),
              parseLong(
                  parts,
                  index,
                  "ace_no_bj"
              ),
              parseLong(
                  parts,
                  index,
                  "other_bj"
              ),
              parseLong(
                  parts,
                  index,
                  "pair"
              ),
              parseLong(
                  parts,
                  index,
                  "same_color"
              ),
              parseLong(
                  parts,
                  index,
                  "two_pair"
              ),
              parseLong(
                  parts,
                  index,
                  "flush"
              ),
              parseLong(
                  parts,
                  index,
                  "straight"
              ),
              parseLong(
                  parts,
                  index,
                  "trips"
              ),
              parseLong(
                  parts,
                  index,
                  "royal_major"
              ),
              parseLong(
                  parts,
                  index,
                  "royal_other"
              )
          )
      );
    }


    return rows;
  }


  private static long parseLong(
      String[] parts,
      Map<String, Integer> index,
      String column)
  {
    return Long.parseLong(
        parts[
            index.get(
                column
            )
        ]
    );
  }


  private static double parseDouble(
      String[] parts,
      Map<String, Integer> index,
      String column)
  {
    return Double.parseDouble(
        parts[
            index.get(
                column
            )
        ]
    );
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
    Files.createDirectories(
        OUTPUT_FILE.getParent()
    );


    String header =
        String.join(
            ",",
            "threshold",
            "qualifying_hands",
            "trigger_rate",
            "fixed_return_per_bet",
            "fixed_profit_per_bet",
            "fixed_edge",
            "royal_probability",
            "major_royal_probability",
            "other_royal_probability",
            "progressive_coefficient",
            "breakeven_major_jackpot",
            "profit_per_bet_400k",
            "edge_400k",
            "profit_per_bet_650k",
            "edge_650k",
            "profit_per_bet_900k",
            "edge_900k"
        )
        + System.lineSeparator();


    Files.writeString(
        OUTPUT_FILE,
        header,
        StandardOpenOption.CREATE
    );
  }


  private static void writeSummary(
      ThresholdSummary s)
      throws IOException
  {
    String row =
        String.format(
            Locale.US,
            "%.1f,%d,%.10f,"
            + "%.10f,%.10f,%.10f,"
            + "%.12f,%.12f,%.12f,%.12f,"
            + "%.4f,"
            + "%.10f,%.10f,"
            + "%.10f,%.10f,"
            + "%.10f,%.10f%n",
            s.threshold(),
            s.qualifyingHands(),
            s.triggerRate(),
            s.fixedReturnPerBet(),
            s.fixedProfitPerBet(),
            s.fixedEdge(),
            s.royalProbability(),
            s.majorRoyalProbability(),
            s.otherRoyalProbability(),
            s.progressiveCoefficient(),
            s.breakEvenJackpot(),
            s.profit400k(),
            s.profit400k()
                / SUPER4_BET,
            s.profit650k(),
            s.profit650k()
                / SUPER4_BET,
            s.profit900k(),
            s.profit900k()
                / SUPER4_BET
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

  private record StateRow(
      double runningSurplus,
      long rounds,
      long loss,
      long aceNoBj,
      long otherBj,
      long pair,
      long sameColor,
      long twoPair,
      long flush,
      long straight,
      long trips,
      long royalMajor,
      long royalOther)
  {
  }


  private record ThresholdSummary(
      double threshold,
      long qualifyingHands,
      double triggerRate,
      double fixedReturnPerBet,
      double fixedProfitPerBet,
      double fixedEdge,
      double royalProbability,
      double majorRoyalProbability,
      double otherRoyalProbability,
      double progressiveCoefficient,
      double breakEvenJackpot,
      double profit400k,
      double profit650k,
      double profit900k)
  {
  }
}
