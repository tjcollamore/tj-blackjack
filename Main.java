import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Main extends Application 
{ 
  private TextArea console;

  public static void main(String[] args)
  {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage)
  {
      BorderPane root = new BorderPane();
      root.setStyle(
          "-fx-background-image: url('felt_green.png');" +
          "-fx-background-repeat: repeat;" +
          "-fx-background-size: auto;" +
          "-fx-background-position: center;");
    
      HBox dealerCards = new HBox(10);
      dealerCards.setPadding(new Insets(5));
      dealerCards.setAlignment(Pos.CENTER);
      
      VBox dealerArea = new VBox(5, dealerCards);
      dealerArea.setAlignment(Pos.CENTER);
      dealerArea.setPadding(new Insets(10));
      root.setTop(dealerArea);

      HBox playerCards = new HBox(10);
      playerCards.setPadding(new Insets(5));
      playerCards.setAlignment(Pos.CENTER);
    
      console = new TextArea();
      console.setPrefWidth(400);
      console.setPrefHeight(200);
      console.setEditable(false);
      console.setWrapText(true);
      console.setStyle("""
          -fx-font-family: 'Consolas';
          -fx-font-size: 14px;
          -fx-background-color: #ffffff;
          -fx-text-fill: #333;
          -fx-border-color: #ccc;
          -fx-border-radius: 4px;
          -fx-background-radius: 4px;
          -fx-focus-color: transparent;
          -fx-faint-focus-color: transparent;
      """);

    
      VBox playerArea = new VBox(5, playerCards, console);
      playerArea.setAlignment(Pos.TOP_CENTER);
      playerArea.setPadding(new Insets(10));
      root.setBottom(playerArea);
    
      Scene scene = new Scene(root, 500, 800);
      primaryStage.setTitle("TJ's Blackjack!");
      primaryStage.setScene(scene);
      primaryStage.show();

      startGame(primaryStage, dealerCards.getChildren(), playerCards.getChildren());
  }

  private void startGame(Stage stage, List<Node> dealerCardNodes, List<Node> playerCardNodes)
  {
    IOutputter outputter = new TextAreaOutputter(console);
    ShoeMgr shoeMgr = new ShoeMgr(outputter, GameOptions.DECKS_IN_SHOE);
    HandStrategy playerHandStrategy = new DealerHandStrategy(outputter);

    String resultMessage;
    do
    {
      console.clear();
      dealerCardNodes.clear();
      playerCardNodes.clear();

      Round round = new Round(outputter, shoeMgr, playerHandStrategy, dealerCardNodes, playerCardNodes);
      resultMessage = round.play();
      shoeMgr.checkCutCard();
    } while (ChoiceDialog.show(stage, resultMessage + "\nPlay another round?", "Deal", "Exit").equals("Deal"));

    Platform.exit();
  }    
} 
