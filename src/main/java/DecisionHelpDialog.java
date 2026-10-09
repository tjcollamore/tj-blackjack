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
          
    BasicStrategy basicStrategy =
        new BasicStrategy();

    HandAction recommendation =
        basicStrategy.chooseRecommendedAction(context);


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
      
    Label recommendationLabel =
        new Label(
            "Basic Strategy: "
            + getActionName(recommendation)
        );

    recommendationLabel.setStyle("""
        -fx-font-size: 16px;
        -fx-font-weight: bold;
        -fx-text-fill: #339af0;
    """);

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
      
      
      VBox centerContent =
          new VBox(
              12,
              dealerArea,
              playerArea,
              recommendationLabel,
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

      DecisionAnalysis analysis =
          engine.analyzeDecision(
              context,
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
    
  private static String getHandDescription(Hand hand)
  {
    if (hand.getNumCards() == 2
        && hand.getCard(0).getPointValue()
           == hand.getCard(1).getPointValue())
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
      double ev =
          result.getExpectedValue();
        double dollarEv =
            ev * wagerAmount;
            
      String evColor;

      if (ev > 0.01)
      {
        evColor = "#7bd88f";
      }
      else if (ev < -0.01)
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
            dollarEv
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
