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

public class BlackjackSceneCreator
{
  private HBox dealerCards = new HBox(10);
  private HBox playerCards = new HBox(10);
  private Label bankrollLabel = new Label();
  private SubroundBadge subroundBadge = new SubroundBadge();

  public Scene create()
  {
    BorderPane root = new BorderPane();
    root.setStyle(
        "-fx-background-image: url('Resources/felt_green.png');" +
        "-fx-background-repeat: repeat;" +
        "-fx-background-size: auto;" +
        "-fx-background-position: center;");

    dealerCards.setPadding(new Insets(5));
    dealerCards.setAlignment(Pos.CENTER);

    VBox dealerArea = new VBox(5, dealerCards);
    root.setTop(dealerArea);

    playerCards.setPadding(new Insets(5));
    playerCards.setAlignment(Pos.CENTER);

/*    Circle betCircle = new Circle(60, Color.TRANSPARENT);
    betCircle.setStroke(Color.WHITE);
    betCircle.setStrokeWidth(3);
    betCircle.setCenterX(400);
    betCircle.setCenterY(400);*/

    // playerCards stays in center, badge goes bottom right
    StackPane playerStack = new StackPane(playerCards);
    playerStack.setAlignment(Pos.CENTER);
    
    // Subround badge
    StackPane.setAlignment(subroundBadge, Pos.BOTTOM_RIGHT);
    StackPane.setMargin(subroundBadge, new Insets(5, 0, 0, 5));
    playerStack.getChildren().add(subroundBadge);
    
    VBox playerArea = new VBox(5, playerStack);
    playerArea.setAlignment(Pos.TOP_CENTER);
    playerArea.setPadding(new Insets(10));
    root.setBottom(playerArea);

    // Setup bankroll display
    setupBankrollDisplay();
    VBox leftPanel = new VBox(5);
    leftPanel.setPadding(new Insets(1));
    leftPanel.setAlignment(Pos.BOTTOM_CENTER);
    leftPanel.getChildren().add(bankrollLabel);
    root.setLeft(leftPanel);

    Scene scene = new Scene(root, 500, 600);
    return scene;
  }

  public SubroundBadge getSubroundBadge()
  {
    return subroundBadge;
  }

  public List<Node> getDealerCards()
  {
    return dealerCards.getChildren();
  }

  public List<Node> getPlayerCards()
  {
    return playerCards.getChildren();
  }

  public Label getBankrollLabel()
  {
    return bankrollLabel;
  }

  private void setupBankrollDisplay()
  {
    bankrollLabel.setText("$0.00");
    bankrollLabel.setStyle(
        "-fx-font-size: 18px;" +
        "-fx-font-weight: bold;" +
        "-fx-text-fill: #FFD700;" +
        "-fx-background-color: rgba(0, 0, 0, 0.7);" +
        "-fx-background-radius: 10px;" +
        "-fx-padding: 10px 15px;" +
        "-fx-border-color: #FFD700;" +
        "-fx-border-width: 2px;" +
        "-fx-border-radius: 10px;"
    );
    bankrollLabel.setPrefWidth(160);
    bankrollLabel.setAlignment(Pos.CENTER);
  }
  
}