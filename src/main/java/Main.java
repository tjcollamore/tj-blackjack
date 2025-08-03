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
    
    startGame(primaryStage, sceneCreator.getDealerCards(),  sceneCreator.getPlayerCards(), sceneCreator.getSubroundBadge(), sceneCreator.getBankrollLabel());
  }

  private void startGame(Stage stage, List<Node> dealerCardNodes, List<Node> playerCardNodes, SubroundBadge subroundBadge, Label bankrollLabel)
  {
    IOutputter outputter = new ConsoleOutputter();
    ShoeMgr shoeMgr = new ShoeMgr(outputter, GameOptions.DECKS_IN_SHOE);
    HandStrategy playerHandStrategy = new PlayerInteractiveHandStrategy(stage);

    String resultMessage;
    Bankroll bankroll = new Bankroll(GameOptions.STARTING_BANKROLL, bankrollLabel, outputter);

    int betAmount = GameOptions.TABLE_MIN_BET;
    
    do
    {      
      dealerCardNodes.clear();
      playerCardNodes.clear();

      bankroll.addFundsIfBroke(stage, GameOptions.STARTING_BANKROLL, GameOptions.TABLE_MIN_BET);
  
      Round round = new Round(stage, outputter, shoeMgr, playerHandStrategy, dealerCardNodes, subroundBadge, playerCardNodes, betAmount, bankroll);
      resultMessage = round.play();
      
      betAmount = round.getLastBetAmount();
      outputter.output("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
      shoeMgr.checkCutCard();
    } while (ChoiceDialog.show(stage, resultMessage + "\n\nPlay another round?", "Deal", "Exit").equals("Deal"));

    Platform.exit();
  }    
} 
