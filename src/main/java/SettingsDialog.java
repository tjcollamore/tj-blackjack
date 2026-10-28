import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;


public class SettingsDialog
{
  private static final String BACKGROUND =
      "#202020";

  private static final String PANEL_BACKGROUND =
      "#2a2a2a";

  private static final String GOLD =
      "#d4af37";

  private static final String BLUE =
      "#007bff";

  private static final String TEXT =
      "#dddddd";

  private static final String MUTED_TEXT =
      "#999999";


  public static void show(
      Stage owner,
      GameSettings settings,
      BlackjackSceneCreator sceneCreator)
  {
    Stage settingsStage =
        new Stage();


    settingsStage.initOwner(
        owner
    );

    settingsStage.initModality(
        Modality.WINDOW_MODAL
    );

    settingsStage.setTitle(
        "Settings"
    );

    settingsStage.setResizable(
        false
    );


    // ============================================================
    // TITLE
    // ============================================================

    Label title =
        new Label(
            "SETTINGS"
        );

    title.setStyle("""
        -fx-font-size: 23px;
        -fx-font-weight: bold;
        -fx-text-fill: #d4af37;
    """);


    Label subtitle =
        new Label(
            "Customize the table, rules, and training experience."
        );

    subtitle.setStyle("""
        -fx-font-size: 11px;
        -fx-text-fill: #999999;
    """);


    VBox titleArea =
        new VBox(
            3,
            title,
            subtitle
        );

    titleArea.setAlignment(
        Pos.CENTER
    );


    // ============================================================
    // CASINO RULES
    // ============================================================

    Label rulesTitle =
        sectionTitle(
            "CASINO RULES"
        );


    CheckBox doubleAfterSplitCheckBox =
        createCheckBox(
            "Double After Split (DAS)",
            settings.doubleAfterSplit()
        );


    doubleAfterSplitCheckBox.setOnAction(
        e ->
            settings.setDoubleAfterSplit(
                doubleAfterSplitCheckBox
                    .isSelected()
            )
    );


    CheckBox surrenderCheckBox =
        createCheckBox(
            "Allow Surrender",
            settings.allowSurrender()
        );


    surrenderCheckBox.setOnAction(
        e ->
            settings.setAllowSurrender(
                surrenderCheckBox
                    .isSelected()
            )
    );


    CheckBox hitSoft17CheckBox =
        createCheckBox(
            "Dealer hits soft 17",
            settings.dealerHitsSoft17()
        );


    hitSoft17CheckBox.setOnAction(e ->
    {
      settings.setDealerHitsSoft17(
          hitSoft17CheckBox.isSelected()
      );

      sceneCreator.updateTableRules();
    });


    VBox ruleChecks =
        new VBox(
            9,
            doubleAfterSplitCheckBox,
            surrenderCheckBox,
            hitSoft17CheckBox
        );

    ruleChecks.setAlignment(
        Pos.CENTER_LEFT
    );


    Label payoutLabel =
        smallLabel(
            "Blackjack pays"
        );


    ToggleGroup payoutGroup =
        new ToggleGroup();


    RadioButton sixToFiveButton =
        createRadioButton(
            "6:5"
        );

    RadioButton threeToTwoButton =
        createRadioButton(
            "3:2"
        );


    sixToFiveButton.setToggleGroup(
        payoutGroup
    );

    threeToTwoButton.setToggleGroup(
        payoutGroup
    );


    if (settings.getBlackjackPayout()
        == GameSettings.BlackjackPayout.SIX_TO_FIVE)
    {
      sixToFiveButton.setSelected(
          true
      );
    }
    else
    {
      threeToTwoButton.setSelected(
          true
      );
    }


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


    HBox payoutButtons =
        new HBox(
            18,
            sixToFiveButton,
            threeToTwoButton
        );

    payoutButtons.setAlignment(
        Pos.CENTER_LEFT
    );


    VBox payoutArea =
        new VBox(
            5,
            payoutLabel,
            payoutButtons
        );


    Label decksLabel =
        smallLabel(
            "Decks in shoe"
        );


    ToggleGroup decksGroup =
        new ToggleGroup();


    RadioButton oneDeckButton =
        createRadioButton(
            "1"
        );

    RadioButton twoDeckButton =
        createRadioButton(
            "2"
        );

    RadioButton sixDeckButton =
        createRadioButton(
            "6"
        );

    RadioButton eightDeckButton =
        createRadioButton(
            "8"
        );


    oneDeckButton.setToggleGroup(
        decksGroup
    );

    twoDeckButton.setToggleGroup(
        decksGroup
    );

    sixDeckButton.setToggleGroup(
        decksGroup
    );

    eightDeckButton.setToggleGroup(
        decksGroup
    );


    switch (settings.getDecksInShoe())
    {
      case 1:
        oneDeckButton.setSelected(
            true
        );
        break;

      case 2:
        twoDeckButton.setSelected(
            true
        );
        break;

      case 6:
        sixDeckButton.setSelected(
            true
        );
        break;

      default:
        eightDeckButton.setSelected(
            true
        );
        break;
    }


    oneDeckButton.setOnAction(
        e ->
            settings.setDecksInShoe(
                1
            )
    );

    twoDeckButton.setOnAction(
        e ->
            settings.setDecksInShoe(
                2
            )
    );

    sixDeckButton.setOnAction(
        e ->
            settings.setDecksInShoe(
                6
            )
    );

    eightDeckButton.setOnAction(
        e ->
            settings.setDecksInShoe(
                8
            )
    );


    HBox deckButtons =
        new HBox(
            16,
            oneDeckButton,
            twoDeckButton,
            sixDeckButton,
            eightDeckButton
        );

    deckButtons.setAlignment(
        Pos.CENTER_LEFT
    );


    VBox decksArea =
        new VBox(
            5,
            decksLabel,
            deckButtons
        );


    HBox tableRulesRow =
        new HBox(
            55,
            payoutArea,
            decksArea
        );

    tableRulesRow.setAlignment(
        Pos.CENTER_LEFT
    );


    VBox rulesContent =
        new VBox(
            14,
            ruleChecks,
            tableRulesRow
        );


    VBox rulesPanel =
        createPanel(
            rulesTitle,
            rulesContent
        );


    // ============================================================
    // TRAINING
    // ============================================================

    Label trainingTitle =
        sectionTitle(
            "TRAINING"
        );


    Label strategyLabel =
        smallLabel(
            "Training strategy"
        );


    ToggleGroup strategyGroup =
        new ToggleGroup();


    RadioButton socialStrategyButton =
        createRadioButton(
            "Social Strategy"
        );

    RadioButton basicStrategyButton =
        createRadioButton(
            "Basic Strategy"
        );


    socialStrategyButton.setToggleGroup(
        strategyGroup
    );

    basicStrategyButton.setToggleGroup(
        strategyGroup
    );


    if (settings.getStrategyMode()
        == StrategyMode.SOCIAL)
    {
      socialStrategyButton.setSelected(
          true
      );
    }
    else
    {
      basicStrategyButton.setSelected(
          true
      );
    }


    socialStrategyButton.setOnAction(
        e ->
            settings.setStrategyMode(
                StrategyMode.SOCIAL
            )
    );


    basicStrategyButton.setOnAction(
        e ->
            settings.setStrategyMode(
                StrategyMode.BASIC
            )
    );


    HBox strategyButtons =
        new HBox(
            28,
            socialStrategyButton,
            basicStrategyButton
        );

    strategyButtons.setAlignment(
        Pos.CENTER_LEFT
    );


    VBox strategyChoice =
        new VBox(
            5,
            strategyLabel,
            strategyButtons
        );


    Button socialStrategyButtonInfo =
        new Button(
            "SOCIAL STRATEGY"
        );

    styleGoldButton(
        socialStrategyButtonInfo
    );


    socialStrategyButtonInfo.setOnAction(
        e ->
            SocialStrategyDialog.show(
                settingsStage,
                settings.getBlackjackPayout()
            )
    );

      Button basicStrategyInfoButton =
          new Button(
              "BASIC STRATEGY"
          );

      styleGoldButton(
          basicStrategyInfoButton
      );

      basicStrategyInfoButton.setOnAction(
          e ->
              BasicStrategyDialog.show(
                  settingsStage,
                  settings
              )
      );

      Button aceAwarenessInfoButton =
          new Button(
              "ACE AWARENESS"
          );

      styleGoldButton(
          aceAwarenessInfoButton
      );

      aceAwarenessInfoButton.setOnAction(
          e ->
              AceAwarenessDialog.show(
                  settingsStage
              )
      );
      
    Button practiceTestButton =
        new Button(
            "PRACTICE TEST"
        );

    stylePrimaryButton(
        practiceTestButton
    );


    practiceTestButton.setOnAction(
        e ->
            PracticeTestDialog.show(
                settingsStage
            )
    );

      HBox strategyActions =
          new HBox(
              10,
              socialStrategyButtonInfo,
              basicStrategyInfoButton,
              aceAwarenessInfoButton
          );

      strategyActions.setAlignment(
          Pos.CENTER_LEFT
      );


      VBox trainingActions =
          new VBox(
              9,
              strategyActions,
              practiceTestButton
          );

      trainingActions.setAlignment(
          Pos.CENTER_LEFT
      );

      CheckBox discardTrayCheckBox =
          createCheckBox(
              "Show Discard Tray",
              settings.isDiscardTrayVisible()
          );


      discardTrayCheckBox.setOnAction(e ->
      {
        boolean visible =
            discardTrayCheckBox
                .isSelected();


        settings
            .setDiscardTrayVisible(
                visible
            );


        sceneCreator
            .setDiscardTrayVisible(
                visible
            );
        sceneCreator.updateAceAwarenessVisibility();
      });

      
      CheckBox aceCountCheckBox =
          createCheckBox(
              "Show Ace Count",
              settings.aceCountVisible()
          );


      aceCountCheckBox.setOnAction(e ->
      {
        boolean visible =
            aceCountCheckBox
                .isSelected();


        settings
            .setAceCountVisible(
                visible
            );

        sceneCreator.setAceCountVisible(visible);
        sceneCreator.updateAceAwarenessVisibility();
      });
      
    CheckBox coachingCheckBox =
        createCheckBox(
            "Highlight Recommended Action",
            settings.coachingEnabled()
        );


    coachingCheckBox.setOnAction(
        e ->
            settings.setCoachingEnabled(
                coachingCheckBox
                    .isSelected()
            )
    );


    CheckBox strategyGuidanceCheckBox =
        createCheckBox(
            "Show Strategy Guidance",
            settings.strategyGuidanceEnabled()
        );


    strategyGuidanceCheckBox.setOnAction(
        e ->
            settings.setStrategyGuidanceEnabled(
                strategyGuidanceCheckBox
                    .isSelected()
            )
    );


    CheckBox handSignalsCheckBox =
        createCheckBox(
            "Show Hand Signals",
            settings.handSignalsEnabled()
        );


    handSignalsCheckBox.setOnAction(
        e ->
        {
            settings.setHandSignalsEnabled(
                handSignalsCheckBox
                    .isSelected()
            );
            sceneCreator.updateAceAwarenessVisibility();
        }
    );


      VBox trainingChecks =
          new VBox(
              9,
              handSignalsCheckBox,
              strategyGuidanceCheckBox,
              coachingCheckBox,
              discardTrayCheckBox,
              aceCountCheckBox
          );
      
    trainingChecks.setAlignment(
        Pos.CENTER_LEFT
    );


    VBox trainingContent =
        new VBox(
            14,
            strategyChoice,
            trainingActions,
            separator(),
            trainingChecks
        );


    VBox trainingPanel =
        createPanel(
            trainingTitle,
            trainingContent
        );


    // ============================================================
    // TABLE APPEARANCE
    // ============================================================

    Label appearanceTitle =
        sectionTitle(
            "TABLE APPEARANCE"
        );


    Label feltLabel =
        smallLabel(
            "Felt color"
        );


    Button greenButton =
        new Button(
            "GREEN"
        );

    Button blueButton =
        new Button(
            "BLUE"
        );

    Button burgundyButton =
        new Button(
            "BURGUNDY"
        );


    updateFeltButtons(
        settings,
        greenButton,
        blueButton,
        burgundyButton
    );


    greenButton.setOnAction(e ->
    {
      settings.setFeltColor(
          GameSettings.FeltColor.GREEN
      );

      sceneCreator.setFeltColor(
          GameSettings.FeltColor.GREEN
      );

      updateFeltButtons(
          settings,
          greenButton,
          blueButton,
          burgundyButton
      );
    });


    blueButton.setOnAction(e ->
    {
      settings.setFeltColor(
          GameSettings.FeltColor.BLUE
      );

      sceneCreator.setFeltColor(
          GameSettings.FeltColor.BLUE
      );

      updateFeltButtons(
          settings,
          greenButton,
          blueButton,
          burgundyButton
      );
    });


    burgundyButton.setOnAction(e ->
    {
      settings.setFeltColor(
          GameSettings.FeltColor.BURGUNDY
      );

      sceneCreator.setFeltColor(
          GameSettings.FeltColor.BURGUNDY
      );

      updateFeltButtons(
          settings,
          greenButton,
          blueButton,
          burgundyButton
      );
    });


    HBox feltButtons =
        new HBox(
            9,
            greenButton,
            blueButton,
            burgundyButton
        );

    feltButtons.setAlignment(
        Pos.CENTER_LEFT
    );


    VBox appearanceContent =
        new VBox(
            5,
            feltLabel,
            feltButtons
        );


    VBox appearancePanel =
        createPanel(
            appearanceTitle,
            appearanceContent
        );


    // ============================================================
    // CLOSE
    // ============================================================

    Button closeButton =
        new Button(
            "CLOSE"
        );

    styleSecondaryButton(
        closeButton
    );


    closeButton.setOnAction(
        e ->
            settingsStage.close()
    );


    // ============================================================
    // ROOT
    // ============================================================

    VBox root =
        new VBox(
            14,
            titleArea,
            rulesPanel,
            trainingPanel,
            appearancePanel,
            closeButton
        );

    root.setAlignment(
        Pos.TOP_CENTER
    );

    root.setPadding(
        new Insets(
            20,
            24,
            20,
            24
        )
    );

    root.setStyle(
        "-fx-background-color: "
        + BACKGROUND
        + ";"
    );


    Scene scene =
        new Scene(
            root,
            530,
            770
        );


    settingsStage.setScene(
        scene
    );


    settingsStage.setOnShown(e ->
    {
      double x =
          owner.getX()
          + (
              owner.getWidth()
              - settingsStage.getWidth()
            )
            / 2.0;


      double y =
          owner.getY()
          + (
              owner.getHeight()
              - settingsStage.getHeight()
            )
            / 2.0;


      settingsStage.setX(
          x
      );

      settingsStage.setY(
          y
      );
    });


    settingsStage.showAndWait();
  }


  // ============================================================
  // PANELS
  // ============================================================

  private static VBox createPanel(
      Label title,
      VBox content)
  {
    VBox panel =
        new VBox(
            10,
            title,
            content
        );

    panel.setMaxWidth(
        470
    );

    panel.setPrefWidth(
        470
    );

    panel.setPadding(
        new Insets(
            13,
            16,
            14,
            16
        )
    );

    panel.setStyle(
        "-fx-background-color: "
        + PANEL_BACKGROUND
        + ";"
        + "-fx-background-radius: 9px;"
    );


    return panel;
  }


  private static Label sectionTitle(
      String text)
  {
    Label label =
        new Label(
            text
        );

    label.setStyle("""
        -fx-font-size: 13px;
        -fx-font-weight: bold;
        -fx-text-fill: #d4af37;
    """);


    return label;
  }


  private static Label smallLabel(
      String text)
  {
    Label label =
        new Label(
            text
        );

    label.setStyle("""
        -fx-font-size: 11px;
        -fx-font-weight: bold;
        -fx-text-fill: #999999;
    """);


    return label;
  }


  private static CheckBox createCheckBox(
      String text,
      boolean selected)
  {
    CheckBox checkBox =
        new CheckBox(
            text
        );

    checkBox.setSelected(
        selected
    );

    checkBox.setStyle("""
        -fx-text-fill: #dddddd;
        -fx-font-size: 12px;
    """);


    return checkBox;
  }


  private static RadioButton createRadioButton(
      String text)
  {
    RadioButton button =
        new RadioButton(
            text
        );

    button.setStyle("""
        -fx-text-fill: #dddddd;
        -fx-font-size: 12px;
    """);


    return button;
  }


  private static Region separator()
  {
    Region separator =
        new Region();

    separator.setPrefHeight(
        1
    );

    separator.setMaxHeight(
        1
    );

    separator.setStyle("""
        -fx-background-color: #404040;
    """);


    return separator;
  }


  // ============================================================
  // BUTTON STYLES
  // ============================================================

  private static void stylePrimaryButton(
      Button button)
  {
    button.setStyle(
        "-fx-background-color: "
        + BLUE
        + ";"
        + "-fx-text-fill: white;"
        + "-fx-font-size: 11px;"
        + "-fx-font-weight: bold;"
        + "-fx-background-radius: 5px;"
        + "-fx-padding: 8px 14px;"
    );
  }


  private static void styleGoldButton(
      Button button)
  {
    button.setStyle(
        "-fx-background-color: transparent;"
        + "-fx-border-color: "
        + GOLD
        + ";"
        + "-fx-border-width: 1px;"
        + "-fx-border-radius: 5px;"
        + "-fx-text-fill: "
        + GOLD
        + ";"
        + "-fx-font-size: 11px;"
        + "-fx-font-weight: bold;"
        + "-fx-background-radius: 5px;"
        + "-fx-padding: 7px 13px;"
    );
  }


  private static void styleSecondaryButton(
      Button button)
  {
    button.setStyle("""
        -fx-background-color: #444444;
        -fx-text-fill: white;
        -fx-font-size: 11px;
        -fx-font-weight: bold;
        -fx-background-radius: 5px;
        -fx-padding: 8px 18px;
    """);
  }


  private static void styleFeltButton(
      Button button,
      boolean selected)
  {
    if (selected)
    {
      button.setStyle("""
          -fx-background-color: #d4af37;
          -fx-text-fill: #202020;
          -fx-font-size: 11px;
          -fx-font-weight: bold;
          -fx-background-radius: 5px;
          -fx-padding: 7px 13px;
      """);
    }
    else
    {
      button.setStyle("""
          -fx-background-color: #404040;
          -fx-text-fill: #dddddd;
          -fx-font-size: 11px;
          -fx-font-weight: bold;
          -fx-background-radius: 5px;
          -fx-padding: 7px 13px;
      """);
    }
  }


  private static void updateFeltButtons(
      GameSettings settings,
      Button greenButton,
      Button blueButton,
      Button burgundyButton)
  {
    styleFeltButton(
        greenButton,
        settings.getFeltColor()
        == GameSettings.FeltColor.GREEN
    );

    styleFeltButton(
        blueButton,
        settings.getFeltColor()
        == GameSettings.FeltColor.BLUE
    );

    styleFeltButton(
        burgundyButton,
        settings.getFeltColor()
        == GameSettings.FeltColor.BURGUNDY
    );
  }
}