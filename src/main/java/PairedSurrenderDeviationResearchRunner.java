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

public class PairedSurrenderDeviationResearchRunner
{
  private static final int TRIALS_PER_COMPOSITION =
      1_000_000;

  private static final double BASE_BET =
      25.0;

  private static final double CI_95_Z =
      1.96;

  private static final long BASE_SEED =
      9_202_026L;

  private static final long FREQUENCY_ROUNDS =
      10_000_000L;

  private static final int SESSION_HANDS =
      160;

  private static final Path FREQUENCY_FILE =
      Path.of(
          "research-data",
          "canonical",
          "frequency-basicstrategy-compositions-8d-h17-das-ls-6to5-10m-rounds.csv"
      );

  private static final Path OUTPUT_FILE =
      Path.of(
          "research-data",
          "scratch",
          "paired-surrender-deviations-8d-h17-das-ls-6to5-1m.csv"
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

    if (!Files.exists(FREQUENCY_FILE))
    {
      throw new IllegalStateException(
          "Frequency file not found: "
          + FREQUENCY_FILE
      );
    }

    Files.createDirectories(
        OUTPUT_FILE.getParent()
    );

    writeHeader();

    GameSettings settings =
        createResearchSettings();

    List<FrequencyRow> frequencyRows =
        readFrequencyRows();

    List<Scenario> scenarios =
        createScenarios();

    System.out.println(
        "PAIRED SURRENDER DEVIATION RESEARCH"
    );

    System.out.println(
        "==================================="
    );

    System.out.println(
        "Trials per composition: "
        + String.format(
            Locale.US,
            "%,d",
            TRIALS_PER_COMPOSITION
        )
    );

    System.out.println();

    int compositionOrdinal =
        0;

    for (Scenario scenario :
         scenarios)
    {
      List<FrequencyRow> matchingRows =
          findMatchingRows(
              frequencyRows,
              scenario
          );

      if (matchingRows.isEmpty())
      {
        throw new IllegalStateException(
            "No frequency rows found for "
            + scenario.id()
        );
      }

      System.out.println(
          scenario.id()
      );

      System.out.println(
          "-----------------------------------"
      );

      List<CompositionSummary> summaries =
          new ArrayList<>();

      long cellOccurrences =
          0;

      for (FrequencyRow row :
           matchingRows)
      {
        compositionOrdinal++;

        PairAccumulator accumulator =
            new PairAccumulator();

        for (int trial = 1;
             trial <= TRIALS_PER_COMPOSITION;
             trial++)
        {
          long seed =
              BASE_SEED
              + (long) compositionOrdinal
                  * 10_000_000L
              + trial;

          double basicResult =
              runSingleTrial(
                  settings,
                  row.composition(),
                  scenario.dealerValue(),
                  scenario.basicAction(),
                  seed
              );

          double socialResult =
              runSingleTrial(
                  settings,
                  row.composition(),
                  scenario.dealerValue(),
                  scenario.socialAction(),
                  seed
              );

          accumulator.add(
              basicResult,
              socialResult
          );

          if (trial % 100_000 == 0)
          {
            System.out.println(
                "  "
                + row.composition()
                + ": completed "
                + trial
                + " paired trials..."
            );
          }
        }

        PairSummary pair =
            accumulator.finish();

        cellOccurrences +=
            row.occurrences();

        CompositionSummary summary =
            new CompositionSummary(
                scenario,
                row,
                pair
            );

        summaries.add(
            summary
        );

        printCompositionSummary(
            summary
        );

        writeCompositionSummary(
            summary
        );
      }

      CellSummary cellSummary =
          buildCellSummary(
              scenario,
              summaries,
              cellOccurrences
          );

      printCellSummary(
          cellSummary
      );

      writeCellSummary(
          cellSummary
      );

      System.out.println();
    }

    System.out.println(
        "Saved detailed results to "
        + OUTPUT_FILE.toAbsolutePath()
    );
  }

  private static double runSingleTrial(
      GameSettings settings,
      String composition,
      int dealerValue,
      HandAction forcedFirstAction,
      long seed)
  {
    int attempt =
        0;

    while (true)
    {
      long attemptSeed =
          seed
          + (long) attempt
              * 1_000_003L;

      KnownCards known =
          createKnownCards(
              composition,
              dealerValue
          );

      Shoe shoe =
          new Shoe(
              settings.getDecksInShoe(),
              attemptSeed
          );

      for (Card card :
           known.playerCards())
      {
        if (!shoe.removeCard(card))
        {
          throw new IllegalStateException(
              "Could not remove known player card "
              + card
          );
        }
      }

      if (!shoe.removeCard(
              known.dealerUpCard()))
      {
        throw new IllegalStateException(
            "Could not remove known dealer card "
            + known.dealerUpCard()
        );
      }

      Hand playerHand =
          new Hand();

      for (Card card :
           known.playerCards())
      {
        playerHand.addCard(
            card
        );
      }

      Hand dealerHand =
          new Hand();

      dealerHand.addCard(
          known.dealerUpCard()
      );

      dealerHand.addCard(
          shoe.drawCard()
      );

      /*
       * Late-surrender / Decision Help state:
       * the player decision exists only after
       * dealer blackjack has been ruled out.
       */
      if (dealerHand.isBlackjack())
      {
        attempt++;
        continue;
      }

      BasicStrategy basicStrategy =
          new BasicStrategy();

      ForcedFirstActionStrategy strategy =
          new ForcedFirstActionStrategy(
              forcedFirstAction,
              basicStrategy
          );

      PlayerRoundEngine playerRoundEngine =
          new PlayerRoundEngine(
              settings
          );

      DealerPlayEngine dealerPlayEngine =
          new DealerPlayEngine(
              settings
          );

      PlayerHandState initialState =
          new PlayerHandState(
              playerHand
          );

      List<PlayerHandState> playerStates =
          playerRoundEngine.play(
              initialState,
              known.dealerUpCard(),
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
                + "occur in surrender research"
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
  }

  private static KnownCards createKnownCards(
      String composition,
      int dealerValue)
  {
    String[] rankNames =
        composition.split(
            "\\+"
        );

    List<Rank> playerRanks =
        new ArrayList<>();

    for (String rankName :
         rankNames)
    {
      playerRanks.add(
          Rank.valueOf(
              rankName
          )
      );
    }

    Rank dealerRank =
        rankForPointValue(
            dealerValue
        );

    /*
     * We only need real Card objects with valid
     * rank/suit combinations. Pulling them from a
     * deterministic temporary shoe avoids coupling
     * this runner to Suit enum names.
     */
    Shoe source =
        new Shoe(
            8,
            1_234_567L
        );

    List<Card> playerCards =
        new ArrayList<>();

    for (Rank rank :
         playerRanks)
    {
      playerCards.add(
          drawRank(
              source,
              rank
          )
      );
    }

    Card dealerUpCard =
        drawRank(
            source,
            dealerRank
        );

    return new KnownCards(
        playerCards,
        dealerUpCard
    );
  }

  private static Card drawRank(
      Shoe shoe,
      Rank targetRank)
  {
    List<Card> held =
        new ArrayList<>();

    while (true)
    {
      Card card =
          shoe.drawCard();

      if (card.getRank() ==
          targetRank)
      {
        return card;
      }

      held.add(
          card
      );
    }
  }

  private static Rank rankForPointValue(
      int value)
  {
    if (value == 11)
      return Rank.ACE;

    for (Rank rank :
         Rank.values())
    {
      if (rank.getValue() ==
          value)
      {
        /*
         * Any ten-value rank is equivalent as
         * dealer upcard for this research.
         */
        if (value != 10
            || rank == Rank.TEN)
        {
          return rank;
        }
      }
    }

    throw new IllegalArgumentException(
        "No rank for dealer value "
        + value
    );
  }

  private static List<FrequencyRow>
      readFrequencyRows()
      throws IOException
  {
    List<String> lines =
        Files.readAllLines(
            FREQUENCY_FILE
        );

    if (lines.size() < 2)
    {
      throw new IllegalStateException(
          "Frequency file is empty: "
          + FREQUENCY_FILE
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
        "hand_type"
    );

    requireColumn(
        index,
        "player_total"
    );

    requireColumn(
        index,
        "pair_value"
    );

    requireColumn(
        index,
        "dealer_value"
    );

    requireColumn(
        index,
        "card_count"
    );

    requireColumn(
        index,
        "came_from_split"
    );

    requireColumn(
        index,
        "player_composition"
    );

    requireColumn(
        index,
        "basic_strategy_action"
    );

    requireColumn(
        index,
        "occurrences"
    );

    List<FrequencyRow> rows =
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
          new FrequencyRow(
              parts[
                  index.get(
                      "hand_type"
                  )
              ],
              Integer.parseInt(
                  parts[
                      index.get(
                          "player_total"
                      )
                  ]
              ),
              parts[
                  index.get(
                      "pair_value"
                  )
              ].isBlank()
                  ? null
                  : Integer.parseInt(
                      parts[
                          index.get(
                              "pair_value"
                          )
                      ]
                  ),
              Integer.parseInt(
                  parts[
                      index.get(
                          "dealer_value"
                      )
                  ]
              ),
              Integer.parseInt(
                  parts[
                      index.get(
                          "card_count"
                      )
                  ]
              ),
              Boolean.parseBoolean(
                  parts[
                      index.get(
                          "came_from_split"
                      )
                  ]
              ),
              parts[
                  index.get(
                      "player_composition"
                  )
              ],
              HandAction.valueOf(
                  parts[
                      index.get(
                          "basic_strategy_action"
                      )
                  ]
              ),
              Long.parseLong(
                  parts[
                      index.get(
                          "occurrences"
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
          "Frequency file missing column: "
          + column
      );
    }
  }

  private static List<FrequencyRow>
      findMatchingRows(
          List<FrequencyRow> rows,
          Scenario scenario)
  {
    List<FrequencyRow> matches =
        new ArrayList<>();

    for (FrequencyRow row :
         rows)
    {
      if (row.dealerValue()
          != scenario.dealerValue())
      {
        continue;
      }

      if (row.cardCount()
          != 2)
      {
        continue;
      }

      if (row.cameFromSplit())
      {
        continue;
      }

      if (row.basicAction()
          != scenario.basicAction())
      {
        continue;
      }

      if (scenario.pairOfEights())
      {
        if (!"PAIR".equals(
                row.handType()))
        {
          continue;
        }

        if (row.pairValue() == null
            || row.pairValue() != 8)
        {
          continue;
        }
      }
      else
      {
        if (!"HARD".equals(
                row.handType()))
        {
          continue;
        }

        if (row.playerTotal()
            != scenario.playerTotal())
        {
          continue;
        }
      }

      matches.add(
          row
      );
    }

    return matches;
  }

  private static List<Scenario>
      createScenarios()
  {
    List<Scenario> scenarios =
        new ArrayList<>();

    scenarios.add(
        new Scenario(
            "HARD_15_VS_10_KEEP",
            15,
            10,
            false,
            HandAction.SURRENDER,
            HandAction.HIT
        )
    );

    scenarios.add(
        new Scenario(
            "HARD_16_VS_10_KEEP",
            16,
            10,
            false,
            HandAction.SURRENDER,
            HandAction.HIT
        )
    );

    scenarios.add(
        new Scenario(
            "HARD_15_VS_ACE_OMIT",
            15,
            11,
            false,
            HandAction.SURRENDER,
            HandAction.HIT
        )
    );

    scenarios.add(
        new Scenario(
            "HARD_16_VS_9_OMIT",
            16,
            9,
            false,
            HandAction.SURRENDER,
            HandAction.HIT
        )
    );

    scenarios.add(
        new Scenario(
            "HARD_16_VS_ACE_OMIT",
            16,
            11,
            false,
            HandAction.SURRENDER,
            HandAction.HIT
        )
    );

    scenarios.add(
        new Scenario(
            "HARD_17_VS_ACE_OMIT",
            17,
            11,
            false,
            HandAction.SURRENDER,
            HandAction.STAND
        )
    );

    scenarios.add(
        new Scenario(
            "PAIR_88_VS_ACE_OMIT",
            16,
            11,
            true,
            HandAction.SURRENDER,
            HandAction.SPLIT
        )
    );

    scenarios.add(
        new Scenario(
            "PAIR_88_VS_10_CANDIDATE",
            16,
            10,
            true,
            HandAction.SPLIT,
            HandAction.SURRENDER
        )
    );

    return scenarios;
  }

  private static CellSummary buildCellSummary(
      Scenario scenario,
      List<CompositionSummary> summaries,
      long cellOccurrences)
  {
    double weightedBasicBetter =
        0.0;

    double weightedSame =
        0.0;

    double weightedSocialBetter =
        0.0;

    double weightedEvCost =
        0.0;

    double weightedVarianceOfMean =
        0.0;

    for (CompositionSummary summary :
         summaries)
    {
      double weight =
          (double) summary
              .frequencyRow()
              .occurrences()
          / cellOccurrences;

      PairSummary pair =
          summary.pairSummary();

      weightedBasicBetter +=
          weight
          * pair.basicBetterRate();

      weightedSame +=
          weight
          * pair.sameRate();

      weightedSocialBetter +=
          weight
          * pair.socialBetterRate();

      weightedEvCost +=
          weight
          * pair.meanBasicMinusSocial();

      weightedVarianceOfMean +=
          weight
          * weight
          * pair.standardError()
          * pair.standardError();
    }

    double weightedSe =
        Math.sqrt(
            weightedVarianceOfMean
        );

    double occurrencesPerSession =
        (double) cellOccurrences
        / FREQUENCY_ROUNDS
        * SESSION_HANDS;

    double evCostDollars =
        weightedEvCost
        * BASE_BET;

    double sessionCost =
        evCostDollars
        * occurrencesPerSession;

    double sessionSe =
        weightedSe
        * BASE_BET
        * occurrencesPerSession;

    return new CellSummary(
        scenario,
        cellOccurrences,
        occurrencesPerSession,
        weightedBasicBetter,
        weightedSame,
        weightedSocialBetter,
        weightedEvCost,
        weightedSe,
        evCostDollars,
        sessionCost,
        sessionSe
    );
  }

  private static void printCompositionSummary(
      CompositionSummary summary)
  {
    PairSummary pair =
        summary.pairSummary();

    FrequencyRow row =
        summary.frequencyRow();

    System.out.println();

    System.out.println(
        "  "
        + row.composition()
    );

    System.out.printf(
        Locale.US,
        "    Basic better: %.3f%%%n",
        pair.basicBetterRate()
        * 100.0
    );

    System.out.printf(
        Locale.US,
        "    Same: %.3f%%%n",
        pair.sameRate()
        * 100.0
    );

    System.out.printf(
        Locale.US,
        "    Social better: %.3f%%%n",
        pair.socialBetterRate()
        * 100.0
    );

    System.out.printf(
        Locale.US,
        "    EV cost: %.6f units = $%.3f/occurrence%n",
        pair.meanBasicMinusSocial(),
        pair.meanBasicMinusSocial()
        * BASE_BET
    );

    System.out.printf(
        Locale.US,
        "    95%% CI: %.6f to %.6f units%n",
        pair.meanBasicMinusSocial()
            - CI_95_Z
              * pair.standardError(),
        pair.meanBasicMinusSocial()
            + CI_95_Z
              * pair.standardError()
    );
  }

  private static void printCellSummary(
      CellSummary summary)
  {
    System.out.println();

    System.out.println(
        "  CELL SUMMARY"
    );

    System.out.printf(
        Locale.US,
        "    Occurrences / 160 hands: %.3f%n",
        summary.occurrencesPerSession()
    );

    System.out.printf(
        Locale.US,
        "    Basic better: %.3f%%%n",
        summary.basicBetterRate()
        * 100.0
    );

    System.out.printf(
        Locale.US,
        "    Same: %.3f%%%n",
        summary.sameRate()
        * 100.0
    );

    System.out.printf(
        Locale.US,
        "    Social better: %.3f%%%n",
        summary.socialBetterRate()
        * 100.0
    );

    System.out.printf(
        Locale.US,
        "    Basic advantage: %+.3f percentage points%n",
        (
            summary.basicBetterRate()
            - summary.socialBetterRate()
        )
        * 100.0
    );

    System.out.printf(
        Locale.US,
        "    EV cost / occurrence: %.6f units = $%.3f%n",
        summary.evCostUnits(),
        summary.evCostDollars()
    );

    System.out.printf(
        Locale.US,
        "    95%% CI / occurrence: $%.3f to $%.3f%n",
        (
            summary.evCostUnits()
            - CI_95_Z
              * summary.standardErrorUnits()
        )
        * BASE_BET,
        (
            summary.evCostUnits()
            + CI_95_Z
              * summary.standardErrorUnits()
        )
        * BASE_BET
    );

    System.out.printf(
        Locale.US,
        "    Expected session cost: $%.3f%n",
        summary.expectedSessionCost()
    );

    System.out.printf(
        Locale.US,
        "    95%% CI session cost: $%.3f to $%.3f%n",
        summary.expectedSessionCost()
            - CI_95_Z
              * summary.sessionStandardError(),
        summary.expectedSessionCost()
            + CI_95_Z
              * summary.sessionStandardError()
    );
  }

  private static void writeHeader()
      throws IOException
  {
    String header =
        String.join(
            ",",
            "row_type",
            "scenario",
            "player_total",
            "dealer_value",
            "pair_of_eights",
            "composition",
            "basic_action",
            "social_action",
            "frequency_occurrences",
            "occurrences_per_160",
            "trials",
            "basic_better_rate",
            "same_rate",
            "social_better_rate",
            "mean_basic_minus_social_units",
            "standard_error_units",
            "ci95_units",
            "ev_cost_per_25_occurrence",
            "expected_session_cost_25",
            "session_standard_error_25"
        );

    Files.writeString(
        OUTPUT_FILE,
        header
        + System.lineSeparator(),
        StandardOpenOption.CREATE
    );
  }

  private static void writeCompositionSummary(
      CompositionSummary summary)
      throws IOException
  {
    Scenario scenario =
        summary.scenario();

    FrequencyRow row =
        summary.frequencyRow();

    PairSummary pair =
        summary.pairSummary();

    double occurrencesPerSession =
        (double) row.occurrences()
        / FREQUENCY_ROUNDS
        * SESSION_HANDS;

    String csvRow =
        String.format(
            Locale.US,
            "COMPOSITION,%s,%d,%d,%b,%s,%s,%s,%d,%.10f,%d,"
            + "%.10f,%.10f,%.10f,%.10f,%.10f,%.10f,"
            + "%.10f,%.10f,%.10f",
            scenario.id(),
            scenario.playerTotal(),
            scenario.dealerValue(),
            scenario.pairOfEights(),
            row.composition(),
            scenario.basicAction().name(),
            scenario.socialAction().name(),
            row.occurrences(),
            occurrencesPerSession,
            TRIALS_PER_COMPOSITION,
            pair.basicBetterRate(),
            pair.sameRate(),
            pair.socialBetterRate(),
            pair.meanBasicMinusSocial(),
            pair.standardError(),
            CI_95_Z
                * pair.standardError(),
            pair.meanBasicMinusSocial()
                * BASE_BET,
            pair.meanBasicMinusSocial()
                * BASE_BET
                * occurrencesPerSession,
            pair.standardError()
                * BASE_BET
                * occurrencesPerSession
        );

    Files.writeString(
        OUTPUT_FILE,
        csvRow
        + System.lineSeparator(),
        StandardOpenOption.APPEND
    );
  }

  private static void writeCellSummary(
      CellSummary summary)
      throws IOException
  {
    Scenario scenario =
        summary.scenario();

    String csvRow =
        String.format(
            Locale.US,
            "CELL_SUMMARY,%s,%d,%d,%b,ALL,%s,%s,%d,%.10f,%d,"
            + "%.10f,%.10f,%.10f,%.10f,%.10f,%.10f,"
            + "%.10f,%.10f,%.10f",
            scenario.id(),
            scenario.playerTotal(),
            scenario.dealerValue(),
            scenario.pairOfEights(),
            scenario.basicAction().name(),
            scenario.socialAction().name(),
            summary.frequencyOccurrences(),
            summary.occurrencesPerSession(),
            TRIALS_PER_COMPOSITION,
            summary.basicBetterRate(),
            summary.sameRate(),
            summary.socialBetterRate(),
            summary.evCostUnits(),
            summary.standardErrorUnits(),
            CI_95_Z
                * summary.standardErrorUnits(),
            summary.evCostDollars(),
            summary.expectedSessionCost(),
            summary.sessionStandardError()
        );

    Files.writeString(
        OUTPUT_FILE,
        csvRow
        + System.lineSeparator(),
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

  private record KnownCards(
      List<Card> playerCards,
      Card dealerUpCard)
  {
  }

  private record Scenario(
      String id,
      int playerTotal,
      int dealerValue,
      boolean pairOfEights,
      HandAction basicAction,
      HandAction socialAction)
  {
  }

  private record FrequencyRow(
      String handType,
      int playerTotal,
      Integer pairValue,
      int dealerValue,
      int cardCount,
      boolean cameFromSplit,
      String composition,
      HandAction basicAction,
      long occurrences)
  {
  }

  private record CompositionSummary(
      Scenario scenario,
      FrequencyRow frequencyRow,
      PairSummary pairSummary)
  {
  }

  private record CellSummary(
      Scenario scenario,
      long frequencyOccurrences,
      double occurrencesPerSession,
      double basicBetterRate,
      double sameRate,
      double socialBetterRate,
      double evCostUnits,
      double standardErrorUnits,
      double evCostDollars,
      double expectedSessionCost,
      double sessionStandardError)
  {
  }

  private record PairSummary(
      double basicBetterRate,
      double sameRate,
      double socialBetterRate,
      double meanBasicMinusSocial,
      double standardDeviation,
      double standardError)
  {
  }

  private static class PairAccumulator
  {
    private int count;

    private long basicBetter;
    private long same;
    private long socialBetter;

    private double meanDifference;
    private double differenceM2;

    void add(
        double basicResult,
        double socialResult)
    {
      count++;

      double difference =
          basicResult
          - socialResult;

      if (difference > 0.0)
      {
        basicBetter++;
      }
      else if (difference < 0.0)
      {
        socialBetter++;
      }
      else
      {
        same++;
      }

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
    }

    PairSummary finish()
    {
      double sd =
          standardDeviation(
              differenceM2,
              count
          );

      double se =
          sd
          / Math.sqrt(
              count
          );

      return new PairSummary(
          (double) basicBetter
              / count,
          (double) same
              / count,
          (double) socialBetter
              / count,
          meanDifference,
          sd,
          se
      );
    }
  }
}

