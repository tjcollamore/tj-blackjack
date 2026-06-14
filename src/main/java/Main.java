import java.util.List;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

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
    
    startGame(primaryStage, sceneCreator);
  }

  private void startGame(Stage stage, BlackjackSceneCreator sceneCreator)
  {
    IOutputter outputter = new ConsoleOutputter();
    ShoeMgr shoeMgr = new ShoeMgr(outputter, GameOptions.DECKS_IN_SHOE);
    HandStrategy playerHandStrategy = new PlayerInteractiveHandStrategy(stage);

    String resultMessage;
    Bankroll bankroll = new Bankroll(GameOptions.STARTING_BANKROLL, sceneCreator.getBankrollLabel(), outputter);

    int betAmount = GameOptions.TABLE_MIN_BET;
    
    do
    {      
      sceneCreator.getDealerCards().clear();
      sceneCreator.getPlayerCards().clear();
      sceneCreator.getDealerScoreBadge().setVisible(false);
      sceneCreator.getPlayerScoreBadge().setVisible(false);
      sceneCreator.getSubroundBadge().update(1, 1);

      bankroll.addFundsIfBroke(stage, GameOptions.STARTING_BANKROLL, GameOptions.TABLE_MIN_BET);
  
      Round round = new Round(stage, sceneCreator, outputter, shoeMgr, playerHandStrategy, betAmount, bankroll);
      resultMessage = round.play();
      
      betAmount = round.getLastBetAmount();
      outputter.output("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
      shoeMgr.checkCutCard();
    } while (ChoiceDialog.show(stage, resultMessage + "\n\nPlay another round?", "Deal", "Exit").equals("Deal"));

    Platform.exit();
  }    
} 
