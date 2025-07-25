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
    dealerArea.setAlignment(Pos.CENTER);
    dealerArea.setPadding(new Insets(10));
    root.setTop(dealerArea);

    playerCards.setPadding(new Insets(5));
    playerCards.setAlignment(Pos.CENTER);

    Circle betCircle = new Circle(60, Color.TRANSPARENT);
    betCircle.setStroke(Color.WHITE);
    betCircle.setStrokeWidth(3);
    betCircle.setCenterX(400);
    betCircle.setCenterY(400);
    VBox playerArea = new VBox(5, /*betCircle,*/ playerCards);
    playerArea.setAlignment(Pos.TOP_CENTER);
    playerArea.setPadding(new Insets(10));
    root.setBottom(playerArea);

    Scene scene = new Scene(root, 500, 600);
    return scene;
  }

  public List<Node> getDealerCards()
  {
    return dealerCards.getChildren();
  }

  public List<Node> getPlayerCards()
  {
    return playerCards.getChildren();
  }
  
}