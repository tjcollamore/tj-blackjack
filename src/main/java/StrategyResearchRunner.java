import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;

public class StrategyResearchRunner
{
  private static final int TRIALS_PER_ACTION =
      1_000_000;

    private static final Path OUTPUT_FILE =
        Path.of(
            "research-data",
            "ev-hard13-vs2to3-compositions-8d-h17-das-ls-6to5-1m.csv"
        );
    
  public static void main(String[] args)
      throws IOException
  {
    GameSettings settings =
        createResearchSettings();

      if (Files.exists(OUTPUT_FILE))
      {
        throw new IllegalStateException(
            "Research output already exists: "
            + OUTPUT_FILE
        );
      }
      
//    Files.deleteIfExists(OUTPUT_FILE);

      SimulationEngine simulationEngine =
          new SimulationEngine(
              settings
          );

      Rank[][] compositions =
      {
          { Rank.TEN, Rank.THREE },
          { Rank.NINE, Rank.FOUR },
          { Rank.EIGHT, Rank.FIVE },
          { Rank.SEVEN, Rank.SIX }
      };

      Rank[] dealerRanks =
      {
          Rank.TWO,
          Rank.THREE
      };


  for (Rank dealerRank : dealerRanks)
  {
    for (Rank[] composition :
         compositions)
    {
      runResearchCase(
          simulationEngine,
          settings,
          new Card(
              composition[0],
              Suit.HEARTS
          ),
          new Card(
              composition[1],
              Suit.CLUBS
          ),
          new Card(
              dealerRank,
              Suit.SPADES
          )
      );
    }
  }
  }
    
    private static void runResearchCase(
        SimulationEngine simulationEngine,
        GameSettings settings,
        Card firstCard,
        Card secondCard,
        Card dealerUpCard)
        throws IOException
    {
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

      EnumSet<HandAction> availableActions =
          EnumSet.copyOf(
              allowedActions
          );

      PlayerDecisionContext context =
          new PlayerDecisionContext(
              playerHand,
              dealerUpCard,
              settings,
              allowedActions,
              availableActions,
              false,
              settings.getDecksInShoe(),
              1.0
          );

      DecisionAnalysis analysis =
          simulationEngine.analyzeDecision(
              context,
              TRIALS_PER_ACTION
          );

      printResults(
          context,
          analysis
      );

      writeResults(
          context,
          analysis
      );

      System.out.println();
    }
    
    
    private static GameSettings createResearchSettings()
    {
      GameSettings settings = new GameSettings();
      settings.setDecksInShoe(8);
      settings.setDealerHitsSoft17(true);
      settings.setDoubleAfterSplit(true);
      settings.setAllowSurrender(true);
      settings.setBlackjackPayout(GameSettings.BlackjackPayout.SIX_TO_FIVE);
      return settings;
    }
    
  private static void printResults(
      PlayerDecisionContext context,
      DecisionAnalysis analysis)
  {
    System.out.println(
        "Player: "
        + describeHand(
            context.getPlayerHand()
        )
    );

    System.out.println(
        "Dealer: "
        + describeCard(
            context.getDealerUpCard()
        )
    );

    System.out.println();

    for (Map.Entry<
        HandAction,
        SimulationResult
    > entry :
        analysis.getResults().entrySet())
    {
      HandAction action =
          entry.getKey();

      SimulationResult result =
          entry.getValue();

      System.out.printf(
          Locale.US,
          "%-10s EV: %.6f   SD: %.6f%n",
          action,
          result.getExpectedValue(),
          result.getStandardDeviation()
      );
    }

    System.out.println();

    System.out.println(
        "Best simulated action: "
        + analysis.getBestAction()
    );
  }

    private static void writeResults(
        PlayerDecisionContext context,
        DecisionAnalysis analysis)
        throws IOException
    {
      Files.createDirectories(
          OUTPUT_FILE.getParent()
      );

      boolean writeHeader =
          !Files.exists(OUTPUT_FILE);

      if (writeHeader)
      {
        Files.writeString(
            OUTPUT_FILE,
            csvHeader()
            + System.lineSeparator(),
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND
        );
      }

      HandAction bestAction =
          analysis.getBestAction();

        BasicStrategy basicStrategy =
            new BasicStrategy();

        HandAction basicStrategyAction =
            basicStrategy.chooseRecommendedAction(
                context
            );

        SimulationResult basicStrategyResult =
            analysis.getResult(
                basicStrategyAction
            );

        double basicStrategyEV =
            basicStrategyResult
                .getExpectedValue();
        
      for (Map.Entry<
          HandAction,
          SimulationResult
      > entry :
          analysis.getResults().entrySet())
      {
          String row =
              createCsvRow(
                  context,
                  entry.getKey(),
                  entry.getValue(),
                  basicStrategyAction,
                  bestAction,
                  basicStrategyResult,
                  basicStrategyEV
              );
          
        Files.writeString(
            OUTPUT_FILE,
            row + System.lineSeparator(),
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND
        );
      }

      System.out.println(
          "Saved results to "
          + OUTPUT_FILE.toAbsolutePath()
      );
    }
    
    private static String csvHeader()
    {
      return String.join(
          ",",
          "decks",
          "h17",
          "das",
          "surrender",
          "blackjack_payout",
          "player_cards",
          "player_total",
          "soft",
          "dealer_card",
          "dealer_value",
          "action",
          "trials",
          "ev",
          "standard_deviation",
          "wins",
          "pushes",
          "losses",
          "win_rate",
          "push_rate",
          "loss_rate",
          "basic_strategy_action",
          "simulated_best_action",
          "ev_cost_vs_basic_strategy",
          "ev_cost_standard_error",
          "ev_cost_ci95"
      );
    }
    
    private static String createCsvRow(
        PlayerDecisionContext context,
        HandAction action,
        SimulationResult result,
        HandAction basicStrategyAction,
        HandAction bestAction,
        SimulationResult basicStrategyResult,
        double basicStrategyEV)
    {
      Hand hand =
          context.getPlayerHand();

      GameSettings settings =
          context.getSettings();

        double evCostVsBasicStrategy =
            basicStrategyEV
            - result.getExpectedValue();

        double evCostStandardError;

        if (action == basicStrategyAction)
        {
          evCostStandardError = 0.0;
        }
        else
        {
          double actionSE =
              result.getStandardDeviation()
              / Math.sqrt(
                  result.getTrials()
              );

          double basicStrategySE =
              basicStrategyResult
                  .getStandardDeviation()
              / Math.sqrt(
                  basicStrategyResult
                      .getTrials()
              );

          evCostStandardError =
              Math.sqrt(
                  actionSE * actionSE
                  + basicStrategySE
                      * basicStrategySE
              );
        }

        double evCostCI95 =
            1.96 * evCostStandardError;
        
    return String.format(
        Locale.US,
        "%d,%b,%b,%b,%s,%s,%d,%b,%s,%d,%s,%d,%.8f,%.8f,%d,%d,%d,%.8f,%.8f,%.8f,%s,%s,%.8f,%.8f,%.8f",
        context.getDeckCount(),
        settings.dealerHitsSoft17(),
        settings.doubleAfterSplit(),
        settings.allowSurrender(),
        settings.getBlackjackPayout().name(),
        describeHand(hand),
        hand.getScore(),
        hand.isSoft(),
        describeCard(
            context.getDealerUpCard()
        ),
        context.getDealerUpCard()
            .getPointValue(),
        action.name(),
        result.getTrials(),
        result.getExpectedValue(),
        result.getStandardDeviation(),
        result.getWins(),
        result.getPushes(),
        result.getLosses(),
        result.getWinRate(),
        result.getPushRate(),
        result.getLossRate(),
        basicStrategyAction.name(),
        bestAction.name(),
        evCostVsBasicStrategy,
        evCostStandardError,
        evCostCI95
    );
    }
    
  private static String describeHand(
      Hand hand)
  {
    StringBuilder result =
        new StringBuilder();

    for (int i = 0;
         i < hand.getNumCards();
         i++)
    {
      if (i > 0)
        result.append("+");

      result.append(
          describeCard(
              hand.getCard(i)
          )
      );
    }

    return result.toString();
  }

  private static String describeCard(
      Card card)
  {
    return card.getRank().name()
        + "_"
        + card.getSuit().name();
  }
}
