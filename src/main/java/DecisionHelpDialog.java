import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.layout.GridPane;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.transform.Scale;
import javafx.scene.layout.HBox;
import javafx.scene.layout.BorderPane;

public class DecisionHelpDialog
{
  public static HandAction show(Stage ownerStage, PlayerDecisionContext context)
  {
    final HandAction[] acceptedAction =
          { null };

    HandAction recommendation =
        getRecommendedAction(
            context
        );


    Button acceptButton =
        new Button(
            "ACCEPT "
            + recommendation.getDisplayText()
    );
    acceptButton.setStyle("""
      -fx-background-color: #007bff;
      -fx-text-fill: white;
      -fx-font-weight: bold;
      -fx-background-radius: 4px;
      -fx-padding: 8px 16px;
    """);

    acceptButton.setDisable(
      !context.isAvailable(recommendation)
    );

    acceptButton.setVisible(
      context.isAvailable(recommendation)
    );

    acceptButton.setManaged(
      context.isAvailable(recommendation)
    );

    Label title =
        new Label("DECISION HELP");

    title.setStyle("""
        -fx-font-size: 18px;
        -fx-font-weight: bold;
        -fx-text-fill: #d4af37;
    """);

      Label dealerTitle =
          new Label("DEALER");

      dealerTitle.setStyle("""
          -fx-text-fill: #aaaaaa;
          -fx-font-size: 11px;
          -fx-font-weight: bold;
      """);

      Node dealerCard =
          createMiniCard(
              context.getDealerUpCard()
          );

      VBox dealerArea =
          new VBox(
              5,
              dealerTitle,
              dealerCard
          );

      dealerArea.setAlignment(
          Pos.CENTER
      );

      Label playerTitle =
          new Label("PLAYER");

      playerTitle.setStyle("""
          -fx-text-fill: #aaaaaa;
          -fx-font-size: 11px;
          -fx-font-weight: bold;
      """);

      HBox playerCards =
          new HBox(6);

      playerCards.setAlignment(
          Pos.CENTER
      );

      Hand playerHand =
          context.getPlayerHand();

      for (int i = 0;
           i < playerHand.getNumCards();
           i++)
      {
        playerCards.getChildren().add(
            createMiniCard(
                playerHand.getCard(i)
            )
        );
      }

      Label handDescription =
          new Label(
              getHandDescription(
                  playerHand
              )
          );

      handDescription.setStyle("""
          -fx-text-fill: #dddddd;
          -fx-font-size: 12px;
      """);

      VBox playerArea =
          new VBox(
              5,
              playerTitle,
              playerCards,
              handDescription
          );

      playerArea.setAlignment(
          Pos.CENTER
      );

      StrategyDeviation deviation =
          null;

      boolean showDeviation =
          context.getSettings()
              .getStrategyMode()
              == StrategyMode.SOCIAL;

      if (showDeviation)
      {
        deviation =
            StrategyDeviationCatalog.find(
                context
            );
      }
      
    Label recommendationLabel =
        new Label(
            getStrategyName(
                context
            )
            + ": "
            + getActionName(
                recommendation
            )
        );

    recommendationLabel.setStyle("""
        -fx-font-size: 16px;
        -fx-font-weight: bold;
        -fx-text-fill: #339af0;
    """);

      StrategyGuidance guidance =
          getStrategyGuidance(
              context
          );

      VBox guidanceArea =
          new VBox(4);

      guidanceArea.setAlignment(
          Pos.CENTER
      );

      VBox deviationArea =
          createDeviationArea(
              context
          );
      
      if (guidance != null)
      {
        Label ruleLabel =
            new Label(
                guidance.getRule()
            );

        ruleLabel.setStyle("""
            -fx-font-size: 13px;
            -fx-font-weight: bold;
            -fx-text-fill: white;
        """);

        ruleLabel.setWrapText(true);
        ruleLabel.setMaxWidth(290);
        ruleLabel.setAlignment(
            Pos.CENTER
        );

        Label explanationLabel =
            new Label(
                guidance.getExplanation()
            );

        explanationLabel.setStyle("""
            -fx-font-size: 12px;
            -fx-text-fill: #bbbbbb;
        """);

        explanationLabel.setWrapText(true);
        explanationLabel.setMaxWidth(290);
        explanationLabel.setAlignment(
            Pos.CENTER
        );

        guidanceArea.getChildren().addAll(
            ruleLabel,
            explanationLabel
        );
      }
      else
      {
        guidanceArea.setVisible(false);
        guidanceArea.setManaged(false);
      }
      
    Label availabilityLabel = new Label();

    if (!context.isAvailable(recommendation))
    {
      availabilityLabel.setText(
        recommendation.getDisplayText()
        + " is unavailable because your bankroll is too low.");

      availabilityLabel.setStyle("""
        -fx-text-fill: #f0c674;
        -fx-font-size: 13px;
      """);
        availabilityLabel.setWrapText(true);
        availabilityLabel.setMaxWidth(270);
        availabilityLabel.setPrefWidth(270);
        availabilityLabel.setAlignment(Pos.CENTER);
    }
    else
    {
      availabilityLabel.setVisible(false);
      availabilityLabel.setManaged(false);
    }

    VBox simulationResults =
      new VBox(8);

    simulationResults.setAlignment(
      Pos.CENTER
    );

    simulationResults.setVisible(false);
    simulationResults.setManaged(false);

    Button closeButton = new Button("CLOSE");

    HBox bottomButtons =
      new HBox(
          12,
          acceptButton,
          closeButton
      );

    bottomButtons.setAlignment(
      Pos.CENTER
    );

      Node handsArea;

      if (deviation != null)
      {
          dealerArea.setSpacing(3);
          playerArea.setSpacing(3);
        HBox compactHands =
            new HBox(
                36,
                dealerArea,
                playerArea
            );

        compactHands.setAlignment(
            Pos.CENTER
        );

        handsArea =
            compactHands;
      }
      else
      {
        VBox stackedHands =
            new VBox(
                12,
                dealerArea,
                playerArea
            );

        stackedHands.setAlignment(
            Pos.CENTER
        );

        handsArea =
            stackedHands;
      }

      VBox centerContent =
          new VBox(
              10,
              handsArea,
              recommendationLabel,
              guidanceArea,
              deviationArea,
              availabilityLabel,
              simulationResults
          );

      centerContent.setAlignment(
          Pos.CENTER
      );

      VBox titleArea =
          new VBox(title);

      titleArea.setAlignment(
          Pos.CENTER
      );

      titleArea.setPadding(
          new Insets(28, 0, 8, 0)
      );

      VBox bottomArea =
          new VBox(bottomButtons);

      bottomArea.setAlignment(
          Pos.CENTER
      );

      bottomArea.setPadding(
          new Insets(8, 0, 28, 0)
      );

      BorderPane root =
          new BorderPane();

      root.setTop(titleArea);
      root.setCenter(centerContent);
      root.setBottom(bottomArea);

      root.setPadding(
          new Insets(0, 22, 0, 22)
      );

      root.setStyle("""
          -fx-background-color: #282828;
      """);

    Stage dialog = new Stage();
    dialog.initOwner(ownerStage);
    dialog.initModality(Modality.APPLICATION_MODAL);
    dialog.setTitle("Decision Help");
    dialog.setScene(
        new Scene(root, ownerStage.getWidth(), ownerStage.getHeight()));

    if (!context.cameFromSplit())
    {
        runSimulation(
            context,
            recommendation,
            simulationResults
        );

        simulationResults.setVisible(true);
        simulationResults.setManaged(true);
    }

    closeButton.setOnAction(e ->
        dialog.close()
    );

    dialog.setOnShown(e ->
    {
      dialog.setX(
          ownerStage.getX()
      );

      dialog.setY(
          ownerStage.getY()
      );
    });

    acceptButton.setOnAction(e ->
    {
      acceptedAction[0] =
          recommendation;

      dialog.close();
    });


    dialog.setResizable(false);
    dialog.showAndWait();

    return acceptedAction[0];
  }

    private static void runSimulation(
        PlayerDecisionContext context,
        HandAction recommendation,
        VBox simulationResults)
    {
      SimulationEngine engine =
          new SimulationEngine(
              context.getSettings()
          );

      int trialsPerAction = 10000;

      HandStrategy continuationStrategy =
          getContinuationStrategy(
              context
          );

      DecisionAnalysis analysis =
          engine.analyzeDecision(
              context,
              continuationStrategy,
              trialsPerAction
          );

      simulationResults.getChildren().clear();

      Label heading =
          new Label(
              "SIMULATION — "
              + String.format("%,d", trialsPerAction)
              + " TRIALS EACH"
          );

      heading.setStyle("""
          -fx-font-size: 13px;
          -fx-font-weight: bold;
          -fx-text-fill: #d4af37;
      """);

      GridPane grid =
          new GridPane();

      grid.setHgap(16);
      grid.setVgap(7);
      grid.setAlignment(Pos.CENTER);

        addResultLabel(
            grid, "ACTION", 0, 0, true
        );

        addResultLabel(
            grid, "$ EV", 1, 0, true
        );

        addResultLabel(
            grid, "WIN", 2, 0, true
        );

        addResultLabel(
            grid, "PUSH", 3, 0, true
        );

      int row = 1;

      for (HandAction action :
           analysis.getResults().keySet())
      {
        SimulationResult result =
            analysis.getResult(action);

        boolean recommended =
            action == recommendation;

        addResultRow(
            grid,
            action,
            result,
            row,
            recommended,
            context.getWagerAmount()
        );

        row++;
      }

      simulationResults.getChildren().addAll(
          heading,
          grid
      );

      simulationResults.setVisible(true);
      simulationResults.setManaged(true);
    }

    private static VBox createDeviationArea(
        PlayerDecisionContext context)
    {
      VBox area =
          new VBox(5);

      area.setAlignment(
          Pos.CENTER
      );

      /*
       * Deviation explanations are specifically
       * about why Social Strategy differs from
       * perfect Basic Strategy.
       */
      if (context.getSettings()
          .getStrategyMode()
          != StrategyMode.SOCIAL)
      {
        area.setVisible(false);
        area.setManaged(false);

        return area;
      }

      StrategyDeviation deviation =
          StrategyDeviationCatalog.find(
              context
          );

      if (deviation == null)
      {
        area.setVisible(false);
        area.setManaged(false);

        return area;
      }


      Label heading =
          new Label(
              "WHY SOCIAL DIFFERS"
          );

      heading.setStyle("""
          -fx-font-size: 11px;
          -fx-font-weight: bold;
          -fx-text-fill: #d4af37;
      """);


      Label comparison =
          new Label(
              "Basic: "
              + getActionName(
                  deviation.getBasicAction()
              )
              + "   •   Social: "
              + getActionName(
                  deviation.getSocialAction()
              )
          );

      comparison.setStyle("""
          -fx-font-size: 12px;
          -fx-font-weight: bold;
          -fx-text-fill: #dddddd;
      """);


      Label explanation =
          new Label(
              deviation.getExplanation()
          );

      explanation.setStyle("""
          -fx-font-size: 12px;
          -fx-text-fill: #cccccc;
      """);

      explanation.setWrapText(true);
      explanation.setMaxWidth(310);
      explanation.setAlignment(
          Pos.CENTER
      );


      Label evidence =
          new Label(
              deviation.getEvidence()
          );

      evidence.setStyle("""
          -fx-font-size: 11px;
          -fx-text-fill: #aaaaaa;
      """);

      evidence.setWrapText(true);
      evidence.setMaxWidth(310);
      evidence.setAlignment(
          Pos.CENTER
      );


      Label takeaway =
          new Label(
              deviation.getTakeaway()
          );

      takeaway.setStyle("""
          -fx-font-size: 11px;
          -fx-font-style: italic;
          -fx-text-fill: #d4af37;
      """);

      takeaway.setWrapText(true);
      takeaway.setMaxWidth(310);
      takeaway.setAlignment(
          Pos.CENTER
      );


      area.getChildren().addAll(
          heading,
          comparison,
          explanation,
          evidence,
          takeaway
      );

      area.setStyle("""
          -fx-background-color: rgba(255, 255, 255, 0.035);
          -fx-background-radius: 8px;
          -fx-padding: 8px 10px;
      """);

      return area;
    }
    
    private static StrategyGuidance getStrategyGuidance(
        PlayerDecisionContext context)
    {
      StrategyGuidanceProvider provider =
          StrategyGuidanceFactory.create(
              context.getSettings()
                  .getStrategyMode()
          );

      return provider.getGuidance(
          context
      );
    }
    
  private static HandStrategy getContinuationStrategy(
      PlayerDecisionContext context)
  {
    if (context.getSettings()
        .getStrategyMode()
        == StrategyMode.SOCIAL)
    {
      return new SocialStrategy();
    }

    return new BasicStrategy();
  }


  private static HandAction getRecommendedAction(
      PlayerDecisionContext context)
  {
    if (context.getSettings()
        .getStrategyMode()
        == StrategyMode.SOCIAL)
    {
      SocialStrategy strategy =
          new SocialStrategy();

      return strategy
          .chooseRecommendedAction(
              context
          );
    }

    BasicStrategy strategy =
        new BasicStrategy();

    return strategy
        .chooseRecommendedAction(
            context
        );
  }


  private static String getStrategyName(
      PlayerDecisionContext context)
  {
    if (context.getSettings()
        .getStrategyMode()
        == StrategyMode.SOCIAL)
    {
      return "Social Strategy";
    }

    return "Basic Strategy";
  }


  private static String getHandDescription(Hand hand)
  {
    if (hand.getNumCards() == 2
        && hand.getCard(0).getRank()
           == hand.getCard(1).getRank())
    {
      return "Pair of "
            + hand.getCard(0)
                .getRank()
                .getShortName()
            + "s";
    }

    if (hand.isSoft())
      return "Soft " + hand.getScore();

    return "Hard " + hand.getScore();
  }
    private static void addResultLabel(
        GridPane grid,
        String text,
        int column,
        int row,
        boolean header)
    {
      Label label =
          new Label(text);

      label.setStyle(
          header
              ? """
                -fx-text-fill: #aaaaaa;
                -fx-font-size: 11px;
                -fx-font-weight: bold;
                """
              : """
                -fx-text-fill: white;
                -fx-font-size: 12px;
                """
      );

      grid.add(
          label,
          column,
          row
      );
    }

    private static String formatPercent(
        double rate)
    {
      return String.format(
          "%.1f%%",
          rate * 100
      );
    }

    private static void addDataLabel(
        GridPane grid,
        String text,
        int column,
        int row,
        String color,
        boolean recommended)
    {
      Label label =
          new Label(text);

      String weight =
          recommended
              ? "bold"
              : "normal";

      label.setStyle(
          "-fx-text-fill: "
          + color
          + ";"
          + "-fx-font-size: 12px;"
          + "-fx-font-weight: "
          + weight
          + ";"
      );

      grid.add(
          label,
          column,
          row
      );
    }

    private static void addResultRow(
        GridPane grid,
        HandAction action,
        SimulationResult result,
        int row,
        boolean recommended,
        double wagerAmount)
    {
      double ev = result.getExpectedValue();
      double dollarEv = ev * wagerAmount;
      double roundedDollarEv = 
          Math.round(dollarEv * 100.0) / 100.0;

      String evColor;
      if (roundedDollarEv > 0.0)
      {
        evColor = "#7bd88f";
      }
      else if (roundedDollarEv < 0.0)
      {
        evColor = "#ff7b7b";
      }
      else
      {
        evColor = "#dddddd";
      }

      String actionText;

      if (action == HandAction.SURRENDER)
      {
        actionText = "SURRENDER";
      }
      else
      {
        actionText =
            action.getDisplayText();
      }

      if (recommended)
      {
        actionText =
            "★ " + actionText;
      }

    addDataLabel(
        grid,
        actionText,
        0,
        row,
        recommended
            ? "#339af0"
            : "#dddddd",
        recommended
    );

    addDataLabel(
        grid,
        formatDollarEv(
            roundedDollarEv
        ),
        1,
        row,
        evColor,
        recommended
    );

    addDataLabel(
        grid,
        formatPercent(
            result.getWinRate()
        ),
        2,
        row,
        "#dddddd",
        recommended
    );

    addDataLabel(
        grid,
        formatPercent(
            result.getPushRate()
        ),
        3,
        row,
        "#dddddd",
        recommended
    );
    }

    private static String formatDollarEv(
        double dollarEv)
    {
      if (dollarEv >= 0)
      {
        return String.format(
            "+$%.2f",
            dollarEv
        );
      }

      return String.format(
          "-$%.2f",
          Math.abs(dollarEv)
      );
    }

    private static Node createMiniCard(
        Card card)
    {
      CardView cardView =
          new CardView();

      cardView.showFront(card);

      double scale = 0.52;

      cardView.getTransforms().add(
          new Scale(
              scale,
              scale,
              0,
              0
          )
      );

      return new Group(
          cardView
      );
    }

    private static String getActionName(
        HandAction action)
    {
      if (action == HandAction.SURRENDER)
        return "SURRENDER";

      return action.getDisplayText();
    }

}
