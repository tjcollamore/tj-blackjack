import javafx.scene.layout.StackPane;

public class CardView extends StackPane
{
  public CardView()
  {
    setPrefSize(
        CardFaceCreator.CARD_WIDTH,
        CardFaceCreator.CARD_HEIGHT
    );
  }

  public void showBack()
  {
    getChildren().setAll(
        CardFaceCreator.createBackFace()
    );
  }

  public void showFront(Card card)
  {
    getChildren().setAll(
        CardFaceCreator.createFrontFace(
            card.getSuit(),
            card.getRank()
        )
    );
  }
}
