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
  public static void main(String[] args)
  {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage)
  {
    BlackjackSceneCreator sceneCreator = new BlackjackSceneCreator();
    Scene scene = sceneCreator.create();

    primaryStage.setTitle("TJ's Blackjack!");
    primaryStage.setScene(scene);
    primaryStage.show();
    
    startGame(primaryStage, sceneCreator.getDealerCards(),  sceneCreator.getPlayerCards(), sceneCreator.getBankrollLabel());
  }

  private void startGame(Stage stage, List<Node> dealerCardNodes, List<Node> playerCardNodes, Label bankrollLabel)
  {
    IOutputter outputter = new ConsoleOutputter();
    ShoeMgr shoeMgr = new ShoeMgr(outputter, GameOptions.DECKS_IN_SHOE);
    HandStrategy playerHandStrategy = new PlayerInteractiveHandStrategy(stage);

    String resultMessage;
    Wager wager = new Wager(GameOptions.MIN_BET, bankrollLabel, outputter);
    wager.addToBankroll(GameOptions.STARTING_BANKROLL);
   
    do
    {      
      dealerCardNodes.clear();
      playerCardNodes.clear();

      wager.addToBankrollIfBroke(stage, GameOptions.STARTING_BANKROLL, GameOptions.MIN_BET);
      wager.determineBetAmount(stage, GameOptions.MIN_BET, GameOptions.MAX_BET);
  
      Round round = new Round(outputter, shoeMgr, playerHandStrategy, dealerCardNodes, playerCardNodes, wager);
      resultMessage = round.play();
      outputter.output("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
      shoeMgr.checkCutCard();
    } while (ChoiceDialog.show(stage, resultMessage + "\nYour payout is: " + wager.getFormattedPayout() + "\nPlay another round?", "Deal", "Exit").equals("Deal"));

    Platform.exit();
  }    
} 
