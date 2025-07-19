import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

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
          "-fx-background-image: url('felt.png');" +
          "-fx-background-repeat: repeat;" +
          "-fx-background-size: auto;" +
          "-fx-background-position: center;");
    
      HBox dealerCards = new HBox(10);
      dealerCards.setPadding(new Insets(5));
      dealerCards.setAlignment(Pos.CENTER);
      
      final double width = 120;
      final double height = 180;
      Node card = CardFaceCreator.createFrontFace( Suit.HEARTS, Rank.ACE, width, height);
      dealerCards.getChildren().add( card);
      card = CardFaceCreator.createBackFace(width, height);
      dealerCards.getChildren().add(card);
      
      VBox dealerArea = new VBox(5, dealerCards);
      dealerArea.setAlignment(Pos.CENTER);
      dealerArea.setPadding(new Insets(10));
      root.setTop(dealerArea);

      HBox playerCards = new HBox(10);
      playerCards.setPadding(new Insets(5));
      playerCards.setAlignment(Pos.CENTER);
      
      card = CardFaceCreator.createFrontFace( Suit.CLUBS, Rank.TWO, width, height);
      playerCards.getChildren().add(card);
      card = CardFaceCreator.createFrontFace( Suit.SPADES, Rank.EIGHT, width, height);
      playerCards.getChildren().add( card);
    
      console = new TextArea();
      console.setPrefWidth(400);
      console.setPrefHeight(400);
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
    
      Scene scene = new Scene(root, 500, 600);
      primaryStage.setTitle("TJ's Blackjack!");
      primaryStage.setScene(scene);
      primaryStage.show();

      startGame(primaryStage, dealerCards.getChildren(), playerCards.getChildren());
  }

  private void startGame(Stage stage, ObservableList<Node> dealerCards, ObservableList<Node> playerCards)
  {
    IOutputter outputter = new TextAreaOutputter(console);
    ShoeMgr shoeMgr = new ShoeMgr(outputter, GameOptions.DECKS_IN_SHOE);
    HandStrategy playerHandStrategy = new DealerHandStrategy(outputter);

    do
    {
      dealerCards.clear();
      playerCards.clear();
      console.clear();
      Round round = new Round(outputter, shoeMgr, playerHandStrategy, dealerCards, playerCards);
      round.play();
      shoeMgr.checkCutCard();
    } while (ChoiceDialog.show(stage, "Play another round?", "Deal", "Exit").equals("Deal"));

    Platform.exit();
  }    
} 
