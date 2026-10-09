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
    GameSettings settings = new GameSettings();
    BlackjackSceneCreator sceneCreator = new BlackjackSceneCreator(settings);
    Scene scene = sceneCreator.create();

    primaryStage.setTitle("TJ's Blackjack!");
    primaryStage.setScene(scene);
    primaryStage.show();
    Platform.runLater(() -> startGame(primaryStage, sceneCreator, settings));
  }

  private void startGame(Stage stage, BlackjackSceneCreator sceneCreator, GameSettings settings)
  {
    IOutputter outputter = new ConsoleOutputter();
    Shoe shoe = null;
    HandStrategy playerHandStrategy = new PlayerInteractiveHandStrategy(stage);

    Bankroll bankroll = new Bankroll(GameOptions.STARTING_BANKROLL, sceneCreator.getBankrollLabel(), outputter);

    WagerSelector wagerSelector = new WagerSelector(GameOptions.TABLE_MIN_BET, GameOptions.TABLE_MAX_BET);

    while(true)
    {      
      bankroll.addFundsIfBroke(stage, GameOptions.STARTING_BANKROLL, GameOptions.TABLE_MIN_BET);

      int maxBet = Math.min((int) bankroll.get(), GameOptions.TABLE_MAX_BET);
      wagerSelector.setMaxWager(maxBet);

      String choice = ChoiceDialog.show(stage,
          sceneCreator.getGearIcon(),
          wagerSelector, "DEAL", "COLOR UP");

      if (choice.equals("COLOR UP"))
        break;

      int betAmount = wagerSelector.getWager();
      
      sceneCreator.getDealerScoreBadge().setVisible(false);
      sceneCreator.getPlayerScoreBadge().setVisible(false);

      if (shoe == null)
      {
        shoe = new Shoe(settings.getDecksInShoe());
      }
      
      Round round = new Round(stage, sceneCreator, outputter, shoe, playerHandStrategy, betAmount, bankroll, settings);
      round.play();
     
      outputter.output("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
      if (shoe.needsShuffle())
      {
        outputter.output("Cut card reached. Shuffling new shoe...");
        shoe.shuffle();
        ChoiceDialog.showGameMessage(stage, "SHUFFLING NEW " + shoe.getDeckCount() + "-DECK SHOE", MessageTone.DEFAULT);
      }
    }

    Platform.exit();
  }    
} 
