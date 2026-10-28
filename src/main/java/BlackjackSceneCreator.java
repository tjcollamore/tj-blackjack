import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.Cursor;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.scene.shape.Ellipse;
import java.util.*;
import javafx.scene.image.Image;
import javafx.scene.text.TextAlignment;
import javafx.scene.layout.Pane;
import javafx.geometry.Rectangle2D;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;

public class BlackjackSceneCreator 
{
  private static final double ACTION_BAR_CLEARANCE = 50;
  
  private Stage primaryStage;
  private HBox dealerCards = new HBox(10);
  private HBox playerCards = new HBox(10);
  private Label bankrollLabel = new Label();
  private Label tableRuleTop = new Label();
  private Label tableRuleBottom = new Label();
  private VBox tableRules;
  private Label strategyGuidanceLabel = new Label();
  private Label aceMentalBalanceLabel = new Label();
  private Label aceExactBalanceLabel = new Label();
  private VBox aceBalanceBadge =
        new VBox(
            1,
            aceMentalBalanceLabel,
            aceExactBalanceLabel
        );
  private final AceCountHandView aceCountHandView = new AceCountHandView();
  private ChipStackView bankrollChipView = new ChipStackView();
  private ChipStackView wagerChipView = new ChipStackView();
  private ChipStackView wagerPreviewChipView = new ChipStackView();
  private ChipStackView splitWagerChipView = new ChipStackView();
  private SubroundBadge subroundBadge = new SubroundBadge();
  private final ScoreBadge playerScoreBadge = new ScoreBadge();
  private final ScoreBadge dealerScoreBadge = new ScoreBadge();
  private BorderPane root;
  private final ImageView gearIcon =
    new ImageView(
        new Image(
            BlackjackSceneCreator.class
                .getResourceAsStream(
                    "/gear.png"
                )
        )
    );
    private final DiscardTrayView discardTrayView;
  private GameSettings settings;
  private Ellipse wagerSpot = new Ellipse(58, 40);
  private StackPane wagerArea;
  private VBox wagerPreviewArea;
  private Pane trainingOverlay = new Pane();
  
  public BlackjackSceneCreator(GameSettings settings)
  {
    this.settings = settings;
    this.discardTrayView = new DiscardTrayView(settings);
  }

  public Scene create() 
  {
    StackPane layeredPane = new StackPane();
    Scene scene = new Scene(layeredPane, 500, 760);

    trainingOverlay.setMouseTransparent(true);
    trainingOverlay.setPickOnBounds(false);

    trainingOverlay.prefWidthProperty()
      .bind(layeredPane.widthProperty());

    trainingOverlay.prefHeightProperty()
      .bind(layeredPane.heightProperty());

      trainingOverlay.getChildren().add(
          aceCountHandView
      );

      aceCountHandView.setLayoutX(115);
      aceCountHandView.setLayoutY(650);
      updateAceAwarenessVisibility();
      
        discardTrayView.setTranslateX(-18);
        discardTrayView.setTranslateY(8);
        discardTrayView.setVisible(settings.isDiscardTrayVisible());

      
      aceMentalBalanceLabel.setStyle("""
          -fx-text-fill: #d4af37;
          -fx-font-size: 12px;
          -fx-font-weight: bold;
      """);

      aceExactBalanceLabel.setStyle("""
          -fx-text-fill: #aaaaaa;
          -fx-font-size: 8px;
      """);

      aceBalanceBadge.setAlignment(Pos.CENTER);
      aceBalanceBadge.setStyle("""
          -fx-background-color: rgba(25, 25, 25, 0.72);
          -fx-background-radius: 8px;
          -fx-padding: 5px 9px;
      """);

      aceBalanceBadge.setMouseTransparent(true);
      aceBalanceBadge.setVisible(settings.aceCountVisible());
      aceBalanceBadge.setManaged(true);
      aceBalanceBadge.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
      updateAceBalance(0, 0.0);
     
      
    strategyGuidanceLabel.setStyle("""
        -fx-background-color: rgba(25, 25, 25, 0.68);
        -fx-background-radius: 10px;
        -fx-padding: 6px 10px;
        -fx-text-fill: rgba(255, 255, 255, 0.92);
        -fx-font-size: 12px;
        -fx-font-weight: normal;
    """);
    
    strategyGuidanceLabel.setWrapText(true);
    strategyGuidanceLabel.setMaxWidth(300);
    strategyGuidanceLabel.setVisible(false);
    strategyGuidanceLabel.setManaged(false);
    strategyGuidanceLabel.setAlignment(Pos.CENTER);
    strategyGuidanceLabel.setTextAlignment(TextAlignment.CENTER);
    strategyGuidanceLabel.setMaxWidth(250);
    
    trainingOverlay.getChildren().add(
      strategyGuidanceLabel
    );

    strategyGuidanceLabel.layoutXProperty()
      .bind(
          trainingOverlay.widthProperty()
              .subtract(
                  strategyGuidanceLabel.widthProperty()
              )
              .divide(2)
              .add(70)
      );
    
    strategyGuidanceLabel.layoutYProperty()
      .bind(
          trainingOverlay.heightProperty()
              .subtract(125)
      );
    
    root = new BorderPane();
    root.setStyle("-fx-background-repeat: repeat;" +
                  "-fx-background-size: auto;" +
                  "-fx-background-position: center;");

    setFeltColor(settings.getFeltColor());

      layeredPane.getChildren().add(
          root
      );

      layeredPane.getChildren().add(
          trainingOverlay
      );

      layeredPane.getChildren().add(
          aceBalanceBadge
      );

      StackPane.setAlignment(
          aceBalanceBadge,
          Pos.TOP_LEFT
      );

      StackPane.setMargin(
          aceBalanceBadge,
          new Insets(
              115,
              0,
              0,
              10
          )
      );
      
      
    // === LEFT PANEL ===
    VBox leftPanel = new VBox();
    leftPanel.setPadding(new Insets(10));
    leftPanel.setPrefWidth(140);
    leftPanel.setMaxWidth(140);
    leftPanel.setMinWidth(140);

    Region spacerTop = new Region();
    VBox.setVgrow(spacerTop, Priority.ALWAYS);

    Label statsPlaceholder = new Label("[Stats]");
    statsPlaceholder.setOpacity(0);
    statsPlaceholder.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
    VBox.setMargin(statsPlaceholder, new Insets(0, 0, 10, 0));

    Region spacerMiddle = new Region();
    VBox.setVgrow(spacerMiddle, Priority.ALWAYS);

    setupBankrollDisplay();

    StackPane bankrollPileArea =
        new StackPane(
            bankrollChipView
        );

    bankrollPileArea.setMinHeight(
        170
    );

    bankrollPileArea.setPrefHeight(
        170
    );

    bankrollPileArea.setMaxHeight(
        170
    );

    bankrollPileArea.setMinWidth(
        140
    );

    bankrollPileArea.setPrefWidth(
        140
    );

    bankrollPileArea.setMaxWidth(
        140
    );

    bankrollPileArea.setAlignment(
        Pos.BOTTOM_CENTER
    );

    StackPane.setAlignment(
        bankrollChipView,
        Pos.BOTTOM_CENTER
    );

    VBox bankrollBox =
        new VBox(
            2,
            bankrollPileArea,
            bankrollLabel
        );

    bankrollBox.setAlignment(
        Pos.BOTTOM_CENTER
    );

    bankrollBox.setFillWidth(
        false
    );
    bankrollBox.setTranslateX(
        -20
    );
    bankrollBox.setTranslateY(
        100
    );

    Region spacerBottom = new Region();
    VBox.setVgrow(spacerBottom, Priority.ALWAYS);

      leftPanel.getChildren().addAll(
          discardTrayView,
          spacerTop,
          statsPlaceholder,
          spacerMiddle,
          bankrollBox,
          spacerBottom
      );
      
    // === CENTER TABLE ===
    dealerCards.setPadding(new Insets(10, 5, 0, 5));
    dealerCards.setAlignment(Pos.CENTER);
    dealerCards.setMaxWidth(Double.MAX_VALUE);

    StackPane dealerStack = new StackPane(dealerCards);
    dealerStack.setAlignment(Pos.CENTER);
    StackPane.setAlignment(dealerScoreBadge, Pos.BOTTOM_RIGHT);
    StackPane.setMargin(dealerScoreBadge, new Insets(5, 5, 0, 0));
    dealerStack.getChildren().add(dealerScoreBadge);
    VBox dealerArea = new VBox(5, dealerStack);    
    dealerArea.setAlignment(Pos.TOP_CENTER);
    dealerArea.setPadding(new Insets(0, 10, 0, 10));
    VBox.setVgrow(dealerArea, Priority.ALWAYS);
    dealerArea.setMaxWidth(Double.MAX_VALUE);

    playerCards.setPadding(new Insets(0, 5, 10, 5));
    playerCards.setAlignment(Pos.CENTER);
    playerCards.setMaxWidth(Double.MAX_VALUE);

    StackPane playerStack = new StackPane(playerCards);
    playerStack.setAlignment(Pos.CENTER);
    StackPane.setAlignment(playerScoreBadge, Pos.TOP_RIGHT);
    StackPane.setMargin(playerScoreBadge, new Insets(2, 5, 0, 0));
    playerStack.getChildren().add(playerScoreBadge);

    StackPane.setAlignment(subroundBadge, Pos.BOTTOM_RIGHT);
    StackPane.setMargin(subroundBadge, new Insets(5));
    playerStack.getChildren().add(subroundBadge);
    playerStack.setMaxWidth(Double.MAX_VALUE);


    
    wagerChipView.setAmount(0, ChipDisplayMode.WAGER);

    wagerSpot.setFill(Color.rgb(255, 255, 255, 0.035));
    wagerSpot.setStroke(Color.rgb(255, 255, 255, 0.22));
    wagerSpot.setStrokeWidth(1.5);

    splitWagerChipView.setAmount(0, ChipDisplayMode.WAGER);
    wagerArea = new StackPane(wagerSpot, wagerChipView, splitWagerChipView);
    wagerArea.setAlignment(Pos.CENTER);
    wagerArea.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

    wagerPreviewArea = new VBox(wagerPreviewChipView);
    wagerPreviewArea.setAlignment(Pos.BOTTOM_CENTER);


    /*
    * The committed wager stays permanently centered
    * underneath the player's cards.
    *
    * The wager preview is independently positioned
    * to the left, so showing/hiding it cannot move
    * the betting spot.
    */
    StackPane wagerRow =
      new StackPane();

    wagerRow.setMinHeight(
      90
    );

    wagerRow.setPrefHeight(
      90
    );

    wagerRow.setMaxWidth(
      Double.MAX_VALUE
    );


    /*
    * Permanent betting spot:
    * centered below the player's cards.
    */
    StackPane.setAlignment(
      wagerArea,
      Pos.CENTER
    );

    playerStack.setTranslateY(
        -100
    );
    wagerArea.setTranslateY(
        -115
    );


    /*
    * Selected-wager preview:
    * positioned independently to the left.
    */
    StackPane.setAlignment(
      wagerPreviewArea,
      Pos.CENTER
    );

    wagerPreviewArea.setTranslateX(-130);
    wagerPreviewArea.setTranslateY(-2);
    wagerRow.getChildren().addAll(
      wagerArea,
      wagerPreviewArea
    );
    
    VBox playerArea = new VBox(4, playerStack, wagerRow);
    playerArea.setAlignment(Pos.BOTTOM_CENTER);
    playerArea.setPadding(new Insets(0, 10, ACTION_BAR_CLEARANCE, 10)); // extra bottom padding to leave space for ChoiceDialog
    VBox.setVgrow(playerArea, Priority.ALWAYS);
    playerArea.setMaxWidth(Double.MAX_VALUE);

    VBox tableCenter = new VBox(20, dealerArea, playerArea);
    tableCenter.setAlignment(Pos.CENTER);
    tableCenter.setPadding(new Insets(10));
    tableCenter.setMaxWidth(Double.MAX_VALUE);
    VBox.setVgrow(tableCenter, Priority.ALWAYS);

    // === MAIN AREA ===
    HBox mainContent = new HBox(leftPanel, tableCenter);
    mainContent.setAlignment(Pos.CENTER_LEFT);
    HBox.setHgrow(leftPanel, Priority.NEVER);
    HBox.setHgrow(tableCenter, Priority.ALWAYS);

    tableRules =
      createTableRules();

    StackPane centeredWrapper =
      new StackPane(
          tableRules,
          mainContent
      );

    StackPane.setAlignment(
      tableRules,
      Pos.CENTER
    );

    StackPane.setAlignment(
      mainContent,
      Pos.CENTER
    );

    root.setCenter(
      centeredWrapper
    );
    
    gearIcon.setFitWidth(20);
    gearIcon.setFitHeight(20);
    gearIcon.setPreserveRatio(true);
    ColorAdjust muted = new ColorAdjust();
    muted.setSaturation(-1.0);
    muted.setBrightness(0.25);

    gearIcon.setEffect(muted);
    gearIcon.setOpacity(0.75);

    gearIcon.setFitWidth(20);
    gearIcon.setFitHeight(20);
    gearIcon.setPreserveRatio(true);

    gearIcon.setPickOnBounds(true);
    gearIcon.setCursor(Cursor.HAND);
    
    gearIcon.setOnMouseEntered(e -> {
        gearIcon.setEffect(null);
        gearIcon.setOpacity(1.0);
    });

    gearIcon.setOnMouseExited(e -> {
        gearIcon.setEffect(muted);
        gearIcon.setOpacity(0.75);
    });

    gearIcon.setOnMouseClicked(e -> {
        SettingsDialog.show(primaryStage, settings, this);
    });
    
    return scene;
  }

  private VBox createTableRules()
  {
    tableRuleTop.setStyle(
        "-fx-font-size: 15px;"
        + "-fx-font-weight: bold;"
        + "-fx-text-fill: rgba(255, 215, 0, 0.55);"
    );

    tableRuleBottom.setStyle(
        "-fx-font-size: 11px;"
        + "-fx-font-weight: bold;"
        + "-fx-text-fill: rgba(255, 255, 255, 0.45);"
    );

    VBox rules =
        new VBox(
            2,
            tableRuleTop,
            tableRuleBottom
        );

    rules.setAlignment(
        Pos.CENTER
    );

    rules.setMouseTransparent(
        true
    );

    rules.setTranslateY(
        -118
    );

    updateTableRules();

    return rules;
  }

  public void updateTableRules()
  {
    tableRuleTop.setText(
        "BLACKJACK PAYS "
        + (settings.getBlackjackPayout()
            == GameSettings.BlackjackPayout.THREE_TO_TWO
            ? "3 TO 2"
            : "6 TO 5")
    );

    tableRuleBottom.setText(
        settings.dealerHitsSoft17()
            ? "DEALER HITS SOFT 17"
            : "DEALER STANDS ON ALL 17s"
    );
  }
    
    public Node getDiscardAnchor()
    {
      if (discardTrayView.isVisible())
      {
        return discardTrayView
            .getAnimationAnchor();
      }
    
      return dealerCards;
    }
        
  public ImageView getGearIcon()
  {
      return gearIcon;
  }
  
  public SubroundBadge getSubroundBadge() {
    return subroundBadge;
  }

  public ScoreBadge getPlayerScoreBadge()
  {
    return playerScoreBadge;
  }

  public ScoreBadge getDealerScoreBadge()
  {
    return dealerScoreBadge;
  }
  
  public List<Node> getDealerCards() 
  {
    return dealerCards.getChildren();
  }

  public Node getDealerArea()
  {
    return dealerCards;
  }

  public List<Node> getPlayerCards() {
    return playerCards.getChildren();
  }

  public ChipStackView getBankrollChipView()
  {
    return bankrollChipView;
  }
  
  public ChipStackView getWagerChipView()
  {
    return wagerChipView;
  }
 
  public ChipStackView getWagerPreviewChipView()
  {
    return wagerPreviewChipView;
  }

  public void showStrategyGuidance(
    StrategyGuidance guidance)
  {
  if (guidance == null)
  {
    hideStrategyGuidance();
    return;
  }

  strategyGuidanceLabel.setText(
      guidance.getRule()
  );

  strategyGuidanceLabel.setVisible(
      true
  );

  strategyGuidanceLabel.setManaged(
      true
  );
  }

  public void hideStrategyGuidance()
  {
  strategyGuidanceLabel.setVisible(
      false
  );

  strategyGuidanceLabel.setManaged(
      false
  );
  }
  
  public Pane getTrainingOverlay()
  {
    return trainingOverlay;
  }
  
  public Label getBankrollLabel() 
  {
    return bankrollLabel;
  }

  public Node getWagerArea()
  {
    return wagerArea;
  }

  public Node getWagerPreviewArea()
  {
    return wagerPreviewArea;
  }
  
  public ChipStackView getSplitWagerChipView()
  {
    return splitWagerChipView;
  }
  
  private void setupBankrollDisplay()
  {
    bankrollLabel.setText(
        "$0.00"
    );

    bankrollLabel.setStyle(
        "-fx-font-size: 14px;"
        + "-fx-font-weight: bold;"
        + "-fx-text-fill: #FFD700;"
        + "-fx-background-color: transparent;"
        + "-fx-background-radius: 7px;"
        + "-fx-padding: 5px 10px;"
    );

    bankrollLabel.setMinWidth(
        100
    );

    bankrollLabel.setWrapText(
        false
    );

    bankrollLabel.setAlignment(
        Pos.CENTER
    );
  }
  
  public void setPrimaryStage(Stage primaryStage)
  {
    this.primaryStage = primaryStage;
  }
  
  public void setFeltColor(GameSettings.FeltColor feltColor)
  {
    String image;
  
    switch (feltColor)
    {
      case BLUE:
        image = "/felt_blue.png";
        break;
  
      case BURGUNDY:
        image = "/felt_burgundy.png";
        break;
  
      default:
        image = "/felt_green.png";
        break;
    }
  
    root.setStyle(
        "-fx-background-image: url('" + image + "');" +
        "-fx-background-repeat: repeat;" +
        "-fx-background-size: auto;" +
        "-fx-background-position: center;"
    );
  }

    public void addDiscardedCards(
        int count)
    {
      discardTrayView.addDiscardedCards(
          count
      );
    }


    public void setDiscardedCardCount(
        int count)
    {
      discardTrayView.setDiscardedCardCount(
          count
      );
    }


    public void resetDiscardTray()
    {
      discardTrayView.reset();
    }


    public void setDiscardTrayVisible(
        boolean isVisible)
    {
      discardTrayView.setVisible(isVisible);
    }

    public boolean isDiscardTrayVisible()
    {
      return discardTrayView.isVisible();
    }
    public void setAceCountVisible(
        boolean visible)
    {
      aceBalanceBadge.setVisible(
          visible
      );
    }

public void updateAceBalance(
    int mentalBalance,
    double exactBalance)
{
  aceMentalBalanceLabel.setText(
      String.format(
          "ACE BALANCE  %+d",
          mentalBalance
      )
  );

  aceExactBalanceLabel.setText(
      String.format(
          "exact  %+.1f",
          exactBalance
      )
  );
}

    public void updateAceCountHand(
        int mentalBalance)
    {
      aceCountHandView.setBalance(
          mentalBalance
      );
    }


    public void updateAceAwarenessVisibility()
    {
      boolean aceCountOn =
          settings.aceCountVisible();

      boolean handSignalsOn =
          settings.handSignalsEnabled();

      boolean discardTrayOn =
          settings.isDiscardTrayVisible();

      boolean showAceHand =
          aceCountOn
          && handSignalsOn
          && discardTrayOn;

      boolean showBadge =
          aceCountOn
          && !showAceHand;

      aceCountHandView.setVisible(
          showAceHand
      );

      aceBalanceBadge.setVisible(
          showBadge
      );
    }
    
    public double getDiscardedDecksSeen()
    {
      return discardTrayView.getDecksSeen();
    }
    
    public void changeToDifferentFelt()
    {
      GameSettings.FeltColor current =
          settings.getFeltColor();

      GameSettings.FeltColor next;

      do
      {
        GameSettings.FeltColor[] colors =
            GameSettings.FeltColor.values();

        next =
            colors[
                new Random().nextInt(
                    colors.length
                )
            ];
      }
      while (next == current);

      settings.setFeltColor(
          next
      );

      setFeltColor(
          next
      );
    }
}
