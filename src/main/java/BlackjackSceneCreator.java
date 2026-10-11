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

public class BlackjackSceneCreator 
{
  private static final double ACTION_BAR_CLEARANCE = 50;
  
  private Stage primaryStage;
  private HBox dealerCards = new HBox(10);
  private HBox playerCards = new HBox(10);
  private Label bankrollLabel = new Label();
  private ChipStackView bankrollChipView = new ChipStackView();
  private ChipStackView wagerChipView = new ChipStackView();
  private ChipStackView wagerPreviewChipView = new ChipStackView();
  private ChipStackView splitWagerChipView = new ChipStackView();
  private Label shoeOverlay = new Label("[Shoe]");
  private Label discardOverlay = new Label("[Discard]");
  private SubroundBadge subroundBadge = new SubroundBadge();
  private final ScoreBadge playerScoreBadge = new ScoreBadge();
  private final ScoreBadge dealerScoreBadge = new ScoreBadge();
  private BorderPane root;
  private final ImageView gearIcon = new ImageView("file:src/main/resources/gear.png");
  private GameSettings settings;
  private Ellipse wagerSpot = new Ellipse(58, 40);
  private StackPane wagerArea;
  private VBox wagerPreviewArea;

  public BlackjackSceneCreator(GameSettings settings)
  {
    this.settings = settings;
  }

  public Scene create() 
  {
    // For now, don't show the placeholder strings
    shoeOverlay.setOpacity(0);
    discardOverlay.setOpacity(0);

    StackPane layeredPane = new StackPane();
    Scene scene = new Scene(layeredPane, 500, 760);

    root = new BorderPane();
    root.setStyle("-fx-background-repeat: repeat;" +
                  "-fx-background-size: auto;" +
                  "-fx-background-position: center;");

    setFeltColor(settings.getFeltColor());

    layeredPane.getChildren().add(root);

    // Discard pile upper right
    discardOverlay.setStyle(
      "-fx-text-fill: white;" +
      "-fx-font-weight: bold;" +
      "-fx-padding: 5px;"
    );
    layeredPane.getChildren().add(discardOverlay);
    StackPane.setAlignment(discardOverlay, Pos.TOP_RIGHT);
    StackPane.setMargin(discardOverlay, new Insets(10));
    
    // === LEFT PANEL ===
    VBox leftPanel = new VBox();
    leftPanel.setPadding(new Insets(10));
    leftPanel.setPrefWidth(140);
    leftPanel.setMaxWidth(140);
    leftPanel.setMinWidth(140);

    shoeOverlay.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5px;");
    VBox.setMargin(shoeOverlay, new Insets(0, 0, 10, 0));

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
      shoeOverlay,
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

    StackPane centeredWrapper = new StackPane(mainContent);
    StackPane.setAlignment(mainContent, Pos.CENTER);
    root.setCenter(centeredWrapper);

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

  public Node getShoeAnchor()
  {
    return shoeOverlay;
  }
  
  public Node getDiscardAnchor()
  {
    return discardOverlay;
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
        + "-fx-background-color: rgba(0, 0, 0, 0.50);"
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
}
