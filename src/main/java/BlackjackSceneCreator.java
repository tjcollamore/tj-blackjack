import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.util.*;

public class BlackjackSceneCreator {
  private HBox dealerCards = new HBox(10);
  private HBox playerCards = new HBox(10);
  private Label bankrollLabel = new Label();
  private SubroundBadge subroundBadge = new SubroundBadge();
  private final ScoreBadge playerScoreBadge = new ScoreBadge();
  private final ScoreBadge dealerScoreBadge = new ScoreBadge();

  public Scene create() {
    StackPane layeredPane = new StackPane();
    Scene scene = new Scene(layeredPane, 500, 600);

    BorderPane root = new BorderPane();
    root.setStyle("-fx-background-image: url('/felt_green.png');" +
                  "-fx-background-repeat: repeat;" +
                  "-fx-background-size: auto;" +
                  "-fx-background-position: center;");

    layeredPane.getChildren().add(root);

    // === LEFT PANEL ===
    VBox leftPanel = new VBox();
    leftPanel.setPadding(new Insets(10));
    leftPanel.setPrefWidth(140);
    leftPanel.setMaxWidth(140);
    leftPanel.setMinWidth(140);

    Label shoeOverlay = new Label("[Shoe]");
    shoeOverlay.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5px;");
    VBox.setMargin(shoeOverlay, new Insets(0, 0, 10, 0));

    Region spacerTop = new Region();
    VBox.setVgrow(spacerTop, Priority.ALWAYS);

    Label statsPlaceholder = new Label("[Stats]");
    statsPlaceholder.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
    VBox.setMargin(statsPlaceholder, new Insets(0, 0, 10, 0));

    Region spacerMiddle = new Region();
    VBox.setVgrow(spacerMiddle, Priority.ALWAYS);

    Label chipStackPlaceholder = new Label("[Chips]");
    chipStackPlaceholder.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

    setupBankrollDisplay();
    VBox bankrollBox = new VBox(5, chipStackPlaceholder, bankrollLabel);
    bankrollBox.setAlignment(Pos.BOTTOM_CENTER);

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
    StackPane.setMargin(playerScoreBadge, new Insets(5, 5, 0, 0));
    playerStack.getChildren().add(playerScoreBadge);

    StackPane.setAlignment(subroundBadge, Pos.BOTTOM_RIGHT);
    StackPane.setMargin(subroundBadge, new Insets(5));
    playerStack.getChildren().add(subroundBadge);
    playerStack.setMaxWidth(Double.MAX_VALUE);

    VBox playerArea = new VBox(playerStack);
    playerArea.setAlignment(Pos.BOTTOM_CENTER);
    playerArea.setPadding(new Insets(0, 10, 30, 10)); // extra bottom padding to leave space for ChoiceDialog
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

    ImageView gearIcon = new ImageView("file:src/main/resources/gear.png");
    gearIcon.setFitWidth(28);
    gearIcon.setFitHeight(28);
    gearIcon.setPreserveRatio(true);
    gearIcon.setOpacity(0.8);
    gearIcon.setOnMouseEntered(e -> gearIcon.setOpacity(1.0));
    gearIcon.setOnMouseExited(e -> gearIcon.setOpacity(0.8));
    StackPane.setAlignment(gearIcon, Pos.BOTTOM_LEFT);
    StackPane.setMargin(gearIcon, new Insets(10));
    layeredPane.getChildren().add(gearIcon);

    return scene;
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
  
  public List<Node> getDealerCards() {
    return dealerCards.getChildren();
  }

  public List<Node> getPlayerCards() {
    return playerCards.getChildren();
  }

  public Label getBankrollLabel() {
    return bankrollLabel;
  }

  private void setupBankrollDisplay() {
    bankrollLabel.setText("$0.00");
    bankrollLabel.setStyle("-fx-font-size: 18px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-text-fill: #FFD700;" +
                            "-fx-background-color: rgba(0, 0, 0, 0.7);" +
                            "-fx-background-radius: 10px;" +
                            "-fx-padding: 10px 15px;" +
                            "-fx-border-color: #FFD700;" +
                            "-fx-border-width: 2px;" +
                            "-fx-border-radius: 10px;");
    bankrollLabel.setMinWidth(120);
    bankrollLabel.setWrapText(true);
    bankrollLabel.setAlignment(Pos.CENTER);
  }
}
