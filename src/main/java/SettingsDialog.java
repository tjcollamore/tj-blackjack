import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;

public class SettingsDialog
{
  public static void show(
        Stage owner,
        GameSettings settings,
        BlackjackSceneCreator sceneCreator)  
  {
    Stage settingsStage = new Stage();

    settingsStage.initOwner(owner);
    settingsStage.initModality(Modality.WINDOW_MODAL);
    settingsStage.setTitle("Settings");
    settingsStage.setResizable(false);

    Label title = new Label("SETTINGS");
    title.setStyle(
        "-fx-font-size: 20px;" +
        "-fx-font-weight: bold;" +
        "-fx-text-fill: #FFD700;"
    );

    Label comingSoon = new Label(
        "Game and appearance settings coming next."
    );

    comingSoon.setStyle(
        "-fx-text-fill: white;"
    );

    Button closeButton = new Button("CLOSE");

    closeButton.setOnAction(e ->
        settingsStage.close()
    );

    Label rulesLabel = new Label("CASINO RULES");
    
    rulesLabel.setStyle(
      "-fx-font-size: 14px;" +
      "-fx-font-weight: bold;" +
      "-fx-text-fill: white;"
    );

    Label decksLabel = new Label("Decks in shoe:");
    
    decksLabel.setStyle("-fx-text-fill: white;");
    
    ToggleGroup decksGroup = new ToggleGroup();
    RadioButton oneDeckButton = new RadioButton("1");
    RadioButton twoDeckButton = new RadioButton("2");
    RadioButton sixDeckButton = new RadioButton("6");
    RadioButton eightDeckButton = new RadioButton("8");
    
    oneDeckButton.setToggleGroup(decksGroup);
    twoDeckButton.setToggleGroup(decksGroup);
    sixDeckButton.setToggleGroup(decksGroup);
    eightDeckButton.setToggleGroup(decksGroup);
    
    oneDeckButton.setStyle("-fx-text-fill: white;");
    twoDeckButton.setStyle("-fx-text-fill: white;");
    sixDeckButton.setStyle("-fx-text-fill: white;");
    eightDeckButton.setStyle("-fx-text-fill: white;");

    CheckBox doubleAfterSplitCheckBox =
      new CheckBox("Double After Split (DAS)");

    doubleAfterSplitCheckBox.setSelected(
      settings.doubleAfterSplit()
    );

    doubleAfterSplitCheckBox.setStyle(
      "-fx-text-fill: white;"
    );

    doubleAfterSplitCheckBox.setOnAction(e ->
      settings.setDoubleAfterSplit(
          doubleAfterSplitCheckBox.isSelected()
      )
    );

    CheckBox surrenderCheckBox =
      new CheckBox("Allow Surrender");

    surrenderCheckBox.setSelected(
      settings.allowSurrender()
    );

    surrenderCheckBox.setStyle(
      "-fx-text-fill: white;"
    );

    surrenderCheckBox.setOnAction(e ->
      settings.setAllowSurrender(
          surrenderCheckBox.isSelected()
      )
    );
    
    CheckBox hitSoft17CheckBox = new CheckBox("Dealer hits soft 17:");
    hitSoft17CheckBox.setSelected(settings.dealerHitsSoft17());
    hitSoft17CheckBox.setStyle("-fx-text-fill: white;");
    
    hitSoft17CheckBox.setOnAction(e ->
    {
      settings.setDealerHitsSoft17(
          hitSoft17CheckBox.isSelected()
      );
      sceneCreator.updateTableRules();
    });
    
    switch (settings.getDecksInShoe())
    {
        case 1:
          oneDeckButton.setSelected(true);
          break;
        
        case 2:
          twoDeckButton.setSelected(true);
          break;
        
        case 6:
          sixDeckButton.setSelected(true);
          break;
        
        default:
          eightDeckButton.setSelected(true);
          break;
    }
          
    oneDeckButton.setOnAction(e -> settings.setDecksInShoe(1));
    twoDeckButton.setOnAction(e -> settings.setDecksInShoe(2));
    sixDeckButton.setOnAction(e -> settings.setDecksInShoe(6));
    eightDeckButton.setOnAction(e -> settings.setDecksInShoe(8));
    HBox deckButtons = new HBox(
      12,
      oneDeckButton,
      twoDeckButton,
      sixDeckButton,
      eightDeckButton
    );
    deckButtons.setAlignment(Pos.CENTER);
              
    Label blackjackPayoutLabel = new Label("Blackjack pays:");
    blackjackPayoutLabel.setStyle("-fx-text-fill: white;");
    
    ToggleGroup payoutGroup = new ToggleGroup();
    RadioButton sixToFiveButton = new RadioButton("6:5");
    RadioButton threeToTwoButton = new RadioButton("3:2");
    
    sixToFiveButton.setToggleGroup(payoutGroup);
    threeToTwoButton.setToggleGroup(payoutGroup);
    
    sixToFiveButton.setStyle("-fx-text-fill: white;");
    threeToTwoButton.setStyle("-fx-text-fill: white;");
    if (settings.getBlackjackPayout() == GameSettings.BlackjackPayout.SIX_TO_FIVE)
        sixToFiveButton.setSelected(true);
    else
        threeToTwoButton.setSelected(true);

    sixToFiveButton.setOnAction(e ->
    {
      settings.setBlackjackPayout(
          GameSettings.BlackjackPayout.SIX_TO_FIVE
      );
      sceneCreator.updateTableRules();
    });
    
    threeToTwoButton.setOnAction(e ->
    {
      settings.setBlackjackPayout(
          GameSettings.BlackjackPayout.THREE_TO_TWO
      );

      sceneCreator.updateTableRules();
    });
    
    HBox payoutButtons = new HBox(12, sixToFiveButton, threeToTwoButton);
    payoutButtons.setAlignment(Pos.CENTER);
          
    Button greenButton = new Button("GREEN");
    Button blueButton = new Button("BLUE");
    Button burgundyButton = new Button("BURGUNDY");
    greenButton.setOnAction(e -> {
      settings.setFeltColor(GameSettings.FeltColor.GREEN);
      sceneCreator.setFeltColor(GameSettings.FeltColor.GREEN);
    });
    
    blueButton.setOnAction(e -> {
      settings.setFeltColor(GameSettings.FeltColor.BLUE);
      sceneCreator.setFeltColor(GameSettings.FeltColor.BLUE);
    });

    burgundyButton.setOnAction(e -> {
      settings.setFeltColor(GameSettings.FeltColor.BURGUNDY);
      sceneCreator.setFeltColor(GameSettings.FeltColor.BURGUNDY);
    });

    Label feltLabel = new Label("TABLE FELT");
    feltLabel.setStyle(
      "-fx-font-size: 14px;" +
      "-fx-font-weight: bold;" +
      "-fx-text-fill: white;"
    );
    HBox feltButtons = new HBox(10, greenButton, blueButton, burgundyButton);
    feltButtons.setAlignment(Pos.CENTER);


    CheckBox coachingCheckBox =
      new CheckBox("Highlight Basic Strategy");

    coachingCheckBox.setSelected(
      settings.coachingEnabled()
    );

    coachingCheckBox.setStyle(
      "-fx-text-fill: white;"
    );

    coachingCheckBox.setOnAction(e ->
      settings.setCoachingEnabled(
          coachingCheckBox.isSelected()
      )
    );
    Label trainingLabel =
      new Label("TRAINING");

    trainingLabel.setStyle(
      "-fx-font-size: 14px;" +
      "-fx-font-weight: bold;" +
      "-fx-text-fill: white;"
    );

    CheckBox handSignalsCheckBox =
      new CheckBox(
          "Show Hand Signals"
      );

    handSignalsCheckBox.setSelected(
      settings.handSignalsEnabled()
    );

    handSignalsCheckBox.setStyle(
      "-fx-text-fill: white;"
    );

    handSignalsCheckBox.setOnAction(e ->
      settings.setHandSignalsEnabled(
          handSignalsCheckBox.isSelected()
      )
    );
    
    

    
    VBox root = new VBox(18, title, feltLabel, feltButtons, rulesLabel, doubleAfterSplitCheckBox, surrenderCheckBox, hitSoft17CheckBox, blackjackPayoutLabel, payoutButtons, decksLabel, deckButtons, trainingLabel, coachingCheckBox, handSignalsCheckBox, closeButton);
    
    root.setAlignment(Pos.CENTER);
    root.setPadding(new Insets(25));
    root.setStyle("-fx-background-color: #161616;");

  Scene scene =
    new Scene(
        root,
        360,
        570
    );

    settingsStage.setScene(
      scene
    );

    settingsStage.setOnShown(e ->
    {
      double x =
          owner.getX()
          + (owner.getWidth()
             - settingsStage.getWidth())
            / 2.0;
  
      double y =
          owner.getY()
          + (owner.getHeight()
             - settingsStage.getHeight())
            / 2.0;
  
      settingsStage.setX(x);
      settingsStage.setY(y);
    });

    settingsStage.showAndWait();
  }
}
