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

    double playerBankroll = GameOptions.STARTING_BANKROLL;
    String resultMessage;
    Wager wager = new Wager(GameOptions.MIN_BET);
   
    do
    {
      outputter.output("Player bankroll: " + Wager.getFormattedCurrency(playerBankroll));
      bankrollLabel.setText(Wager.getFormattedCurrency(playerBankroll));
      
      dealerCardNodes.clear();
      playerCardNodes.clear();

      if (playerBankroll < GameOptions.MIN_BET)
      {
        outputter.output("You do not have enough funds. Withdrawing another $" + GameOptions.STARTING_BANKROLL  + " from the ATM.");
        ChoiceDialog.show(stage, "You do not have enough funds.\nWithdrawing another $" + GameOptions.STARTING_BANKROLL  + " from the ATM.", "OK");
        playerBankroll += GameOptions.STARTING_BANKROLL;
        outputter.output("Player bankroll: " + Wager.getFormattedCurrency(playerBankroll));              bankrollLabel.setText(Wager.getFormattedCurrency(playerBankroll));
      }

      int maxBet = Math.min((int)playerBankroll, GameOptions.MAX_BET);
      wager.setBetAmount(BetDialog.show(stage, GameOptions.MIN_BET, maxBet));
      outputter.output("Player bet: $" + wager.getBetAmount());
      playerBankroll -= wager.getBetAmount();
      bankrollLabel.setText(Wager.getFormattedCurrency(playerBankroll));
  
      Round round = new Round(outputter, shoeMgr, playerHandStrategy, dealerCardNodes, playerCardNodes, wager);
    
      resultMessage = round.play();
      playerBankroll += wager.getPayout();
      bankrollLabel.setText(Wager.getFormattedCurrency(playerBankroll));
      outputter.output("Player bankroll: " + Wager.getFormattedCurrency(playerBankroll));
      outputter.output("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
      shoeMgr.checkCutCard();
    } while (ChoiceDialog.show(stage, resultMessage + "\nYour payout is: " + wager.getFormattedPayout() + "\nPlay another round?", "Deal", "Exit").equals("Deal"));

    Platform.exit();
  }    
} 
