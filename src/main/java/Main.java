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
    sceneCreator.setPrimaryStage(primaryStage);
    Scene scene = sceneCreator.create();

    primaryStage.setTitle("TJ's Blackjack!");
    primaryStage.setScene(scene);
    primaryStage.show();
    primaryStage.centerOnScreen();
    Platform.runLater(() -> startGame(primaryStage, sceneCreator, settings));
  }

  private void startGame(Stage stage, BlackjackSceneCreator sceneCreator, GameSettings settings)
  {
    IOutputter outputter = new ConsoleOutputter();
    Shoe shoe = null;
    int lastDeckSetting = settings.getDecksInShoe();
    HandStrategy playerHandStrategy = new PlayerInteractiveHandStrategy(stage);

    Bankroll bankroll = new Bankroll(GameOptions.STARTING_BANKROLL, sceneCreator.getBankrollLabel(), sceneCreator.getBankrollChipView(), outputter);

    WagerSelector wagerSelector = new WagerSelector(GameOptions.TABLE_MIN_BET, GameOptions.TABLE_MAX_BET);
    wagerSelector.setOnWagerChanged(
        amount ->
        {
          sceneCreator
              .getWagerPreviewChipView()
              .setAmount(
                  amount,
                  ChipDisplayMode.WAGER
              );

          sceneCreator
              .getWagerChipView()
              .setAmount(
                  0,
                  ChipDisplayMode.WAGER
              );

          bankroll.showWagerPreview(
              amount
          );
        }
    );

    
    while(true)
    {      
      bankroll.addFundsIfBroke(stage, GameOptions.STARTING_BANKROLL, GameOptions.ATM_BANK_FEE, GameOptions.TABLE_MIN_BET);

      int maxBet = Math.min((int) bankroll.get(), GameOptions.TABLE_MAX_BET);
      wagerSelector.setMaxWager(maxBet);
      int selectedWager = wagerSelector.getWager();

      sceneCreator
        .getWagerPreviewChipView()
        .setAmount(
            selectedWager,
            ChipDisplayMode.WAGER
        );

      sceneCreator
        .getWagerChipView()
        .setAmount(
            0,
            ChipDisplayMode.WAGER
        );

      bankroll.showWagerPreview(
        selectedWager
      );
      
      String choice = ChoiceDialog.showActionBarWithHighlightedOption(stage,
          sceneCreator.getGearIcon(),
          wagerSelector, "DEAL", "DEAL", "COLOR UP");

      if (choice.equals("COLOR UP"))
      {
        bankroll.showActualBankroll();

        boolean confirmed =
            ColorUpDialog.show(
                stage,
                bankroll.get()
            );

        if (confirmed)
          break;

        bankroll.showWagerPreview(
            wagerSelector.getWager()
        );

        continue;
      }
      
      int betAmount = wagerSelector.getWager();
      
      sceneCreator.getDealerScoreBadge().setVisible(false);
      sceneCreator.getPlayerScoreBadge().setVisible(false);

      int currentDeckSetting = settings.getDecksInShoe();
      boolean deckSettingChanged = currentDeckSetting != lastDeckSetting;

      if (shoe == null || shoe.getDeckCount() != currentDeckSetting)
      {
        shoe = new Shoe(currentDeckSetting);

        if (deckSettingChanged)
        {
          outputter.output("Deck count changed. Starting new shoe...");
  
          ChoiceDialog.showGameMessage(
              stage,
              "Deck count changed. Shuffling new "
              + shoe.getDeckCount()
              + "-DECK SHOE.",
              MessageTone.DEFAULT
          );
        }
  
        lastDeckSetting = currentDeckSetting;
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
