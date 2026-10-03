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
    Platform.runLater(() -> startGame(primaryStage, sceneCreator));
  }

  private void startGame(Stage stage, BlackjackSceneCreator sceneCreator)
  {
    IOutputter outputter = new ConsoleOutputter();
    ShoeMgr shoeMgr = new ShoeMgr(outputter, GameOptions.DECKS_IN_SHOE);
    HandStrategy playerHandStrategy = new PlayerInteractiveHandStrategy(stage);

    Bankroll bankroll = new Bankroll(GameOptions.STARTING_BANKROLL, sceneCreator.getBankrollLabel(), outputter);

    WagerSelector wagerSelector = new WagerSelector(GameOptions.TABLE_MIN_BET, GameOptions.TABLE_MAX_BET);

    while(true)
    {      
      bankroll.addFundsIfBroke(stage, GameOptions.STARTING_BANKROLL, GameOptions.TABLE_MIN_BET);

      int maxBet = Math.min((int) bankroll.get(), GameOptions.TABLE_MAX_BET);
      wagerSelector.setMaxWager(maxBet);
      
      if (ChoiceDialog.show(stage, "",
          null,               // no gear until settings works
          wagerSelector, "DEAL", "COLOR UP")
          .equals("COLOR UP"))
        break;

      int betAmount = wagerSelector.getWager();
      
      sceneCreator.getDealerScoreBadge().setVisible(false);
      sceneCreator.getPlayerScoreBadge().setVisible(false);
        
      Round round = new Round(stage, sceneCreator, outputter, shoeMgr, playerHandStrategy, betAmount, bankroll);
      round.play();
     
      outputter.output("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
      shoeMgr.checkCutCard();
    }

    Platform.exit();
  }    
} 
