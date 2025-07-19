import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Create card faces that consist solely of JavaFX code, no images.
 * You can easily exchange this class against another card face creator that creates the faces from images if you wish.
 */
public class CardFaceCreator {
  public static double CARD_ARC = 12;
  public static double CARD_GRID_SYMBOL_SIZE = 30;
  public static double CARD_CENTER_SYMBOL_SIZE = 46;
  public static double CARD_CORNER_SYMBOL_SIZE = 14;


  /**
   * Create the front face of the card.
   * @param suit
   * @param rank
   * @param width
   * @param height
   * @return
   */
  public static Node createFrontFace( Suit suit, Rank rank, double width, double height) {

    CardFace face = new CardFace();

    Shape shape = createShape( width, height);
    shape.setFill( Color.WHITE);

    face.getChildren().add( shape);

    addCornerText( face, suit, rank, width, height);
    addCenterItems( face, suit, rank, width, height);

    return face; 
  }

  // Back of Card PNG calling.
  public static Node createBackFace(double width, double height)
  {
    Image cardImage = new Image("card_back2.png");
    ImageView cardView = new ImageView(cardImage);
    cardView.setFitWidth(width);
    cardView.setFitHeight(height);
    cardView.setPreserveRatio(true);
    return cardView;
  }
  
  /**
   * The card's shape, e. g. a rectangle with rounded corners
   * @param width
   * @param height
   * @return
   */
  public static Rectangle createShape( double width, double height) {

    Rectangle shape = new Rectangle();
    shape.setWidth( width);
    shape.setHeight( height);
    shape.setArcWidth(CARD_ARC);
    shape.setArcHeight(CARD_ARC);
    shape.setStroke( Color.BLACK);
    shape.setFill( Color.TRANSPARENT);

    return shape;
  }


  /**
   * Create top/left and bottom/right corner text / symbols
   * @param face
   * @param suit
   * @param rank
   * @param width
   * @param height
   */
  private static void addCornerText( CardFace face, Suit suit, Rank rank, double width, double height) {

    Text rankText;
    Text suitText;

    // center suit text below rank text (6 is just an arbitrary value)
    double offsetY = 6;

    // top left
    // -----------------
    rankText = createText(rank.getShortName(), suit, CARD_CORNER_SYMBOL_SIZE);
    rankText.setRotate(0);
    rankText.relocate(4, 0);

    suitText = createText(suit.getSymbol(), suit, CARD_CORNER_SYMBOL_SIZE);
    rankText.setRotate(0);
    suitText.relocate( rankText.getBoundsInParent().getMinX() + (rankText.getBoundsInParent().getWidth() - suitText.getBoundsInParent().getWidth()) / 2.0, rankText.getBoundsInParent().getMaxY() - offsetY);

    face.getChildren().addAll( rankText,suitText);

    // bottom right
    // -----------------
    rankText = createText(rank.getShortName(), suit, CARD_CORNER_SYMBOL_SIZE);
    rankText.setRotate(180);
    rankText.relocate( width - rankText.getBoundsInParent().getWidth() - 4, height - rankText.getBoundsInParent().getHeight());

    suitText = createText(suit.getSymbol(), suit, CARD_CORNER_SYMBOL_SIZE);
    suitText.setRotate(180);
    suitText.relocate( rankText.getBoundsInParent().getMaxX() - (rankText.getBoundsInParent().getWidth() + suitText.getBoundsInParent().getWidth()) / 2.0, rankText.getBoundsInParent().getMinY() - suitText.getBoundsInParent().getHeight() + offsetY);

    face.getChildren().addAll( rankText,suitText);

  }

  /**
   * Combines commonly used methods to create common text / symbols for the card.
   * @param name
   * @param suit
   * @param fontSize
   * @return
   */
  private static Text createText( String name, Suit suit, double fontSize) {

     Text text = new Text( name);

     text.setTextOrigin( VPos.TOP);
     text.setFill( suit.getColor());
     text.setFont( Font.font( null, fontSize));

     return text;
  }

  /**
   * Centers the text vertically, visually correct.
   * If we wouldn't apply the VISUAL bounds type, then there would be an empty gap which would eg be reserved for letters like À.
   * You can verify it by applying grid lines, eg:
   * 
        double y = 0;
      while( y < height) {
        y +=20;
        Line line = new Line( 0,y,width,y);
        face.getChildren().addAll( line);
      }
   * @param name
   * @param suit
   * @param fontSize
   * @return
   */
  private static Text createTextCentered( String name, Suit suit, double fontSize) {

     Text text = new Text( name);

     text.setBoundsType(TextBoundsType.VISUAL);
     text.setFill( suit.getColor());
     text.setFont( Font.font( null, fontSize));

     return text;
  }

  private static void addCenterItems( CardFace face, Suit suit, Rank rank, double width, double height) {

    Text rankTextCenter;

    if( rank == Rank.ACE) {

      // create text / symbol
      rankTextCenter = createTextCentered(suit.getSymbol(), suit, CARD_CENTER_SYMBOL_SIZE);

      // center within card
      rankTextCenter.relocate( (width - rankTextCenter.getBoundsInParent().getWidth()) / 2, (height - rankTextCenter.getBoundsInParent().getHeight()) / 2);

      // add to card
      face.getChildren().addAll( rankTextCenter);

    }
    else if( rank == Rank.JACK || rank == Rank.QUEEN || rank == Rank.KING) {

      // create text / symbol
      rankTextCenter = createTextCentered(rank.getShortName(), suit, CARD_CENTER_SYMBOL_SIZE);

      // center within card
      rankTextCenter.relocate( (width - rankTextCenter.getBoundsInParent().getWidth()) / 2, (height - rankTextCenter.getBoundsInLocal().getHeight()) / 2);

      // add to card
      face.getChildren().addAll( rankTextCenter);

    }		
    else {

      GridPane gridPane = new GridPane();

      gridPane.setGridLinesVisible(false);

      gridPane.setHgap(0);
      gridPane.setVgap(0);
      gridPane.setPrefSize( width, height);
      gridPane.setPadding(new Insets(10));

      switch( rank) {
      case TWO:
          gridPane.add( createGridSymbol( suit), 0, 0);
          gridPane.add( createGridSymbol( suit, true), 0, 1);
        break;
      case THREE:
          gridPane.add( createGridSymbol( suit), 0, 0);
          gridPane.add( createGridSymbol( suit), 0, 1);
          gridPane.add( createGridSymbol( suit, true), 0, 2);
        break;
      case FOUR:
          gridPane.add( createGridSymbol( suit), 0, 0);
          gridPane.add( createGridSymbol( suit, true), 0, 1);
          gridPane.add( createGridSymbol( suit), 1, 0);
          gridPane.add( createGridSymbol( suit, true), 1, 1);
        break;
      case FIVE:
          gridPane.add( createGridSymbol( suit), 0, 0);
          gridPane.add( createGridSymbol( suit, true), 0, 2);
          gridPane.add( createGridSymbol( suit), 1, 1);
          gridPane.add( createGridSymbol( suit), 2, 0);
          gridPane.add( createGridSymbol( suit, true), 2, 2);
        break;
      case SIX:
          gridPane.add( createGridSymbol( suit), 0, 0);
          gridPane.add( createGridSymbol( suit), 0, 1);
          gridPane.add( createGridSymbol( suit, true), 0, 2);
          gridPane.add( createGridSymbol( suit), 1, 0);
          gridPane.add( createGridSymbol( suit), 1, 1);
          gridPane.add( createGridSymbol( suit, true), 1, 2);
        break;
      case SEVEN:
          gridPane.add( createGridSymbol( suit), 0, 0);
          gridPane.add( createGridSymbol( suit), 0, 1);
          gridPane.add( createGridSymbol( suit, true), 0, 2);
          gridPane.add( createGridSymbol( suit), 1, 0, 1, 2);
          gridPane.add( createGridSymbol( suit), 2, 0);
          gridPane.add( createGridSymbol( suit), 2, 1);
          gridPane.add( createGridSymbol( suit, true), 2, 2);
        break;
      case EIGHT:
          gridPane.add( createGridSymbol( suit), 0, 0);
          gridPane.add( createGridSymbol( suit), 0, 1);
          gridPane.add( createGridSymbol( suit, true), 0, 2);
          gridPane.add( createGridSymbol( suit), 1, 0, 1, 2);
          gridPane.add( createGridSymbol( suit, true), 1, 1, 1, 2);
          gridPane.add( createGridSymbol( suit), 2, 0);
          gridPane.add( createGridSymbol( suit), 2, 1);
          gridPane.add( createGridSymbol( suit, true), 2, 2);
        break;
      case NINE:
          gridPane.add( createGridSymbol( suit), 0, 0);
          gridPane.add( createGridSymbol( suit), 0, 1);
          gridPane.add( createGridSymbol( suit, true), 0, 2);
          gridPane.add( createGridSymbol( suit, true), 0, 3);
          gridPane.add( createGridSymbol( suit), 1, 1, 1, 2);
          gridPane.add( createGridSymbol( suit), 2, 0);
          gridPane.add( createGridSymbol( suit), 2, 1);
          gridPane.add( createGridSymbol( suit, true), 2, 2);
          gridPane.add( createGridSymbol( suit, true), 2, 3);
        break;
      case TEN:
          gridPane.add( createGridSymbol( suit), 0, 0);
          gridPane.add( createGridSymbol( suit), 0, 1);
          gridPane.add( createGridSymbol( suit, true), 0, 2);
          gridPane.add( createGridSymbol( suit, true), 0, 3);
          gridPane.add( createGridSymbol( suit), 1, 0, 1, 2);
          gridPane.add( createGridSymbol( suit), 2, 0);
          gridPane.add( createGridSymbol( suit), 2, 1);
          gridPane.add( createGridSymbol( suit, true), 2, 2);
          gridPane.add( createGridSymbol( suit, true), 2, 3);
          gridPane.add( createGridSymbol( suit, true), 1, 2, 1, 2);
        break;
      default:
        break;
      }


      // add to card
      face.getChildren().addAll( gridPane);

    }
  }

  private static Text createGridSymbol(Suit suit)
  {
    return createGridSymbol(suit, false);
  }

  private static Text createGridSymbol(Suit suit, boolean rotate)
  {

    Text text = new Text(suit.getSymbol());

    text.setTextOrigin(VPos.CENTER);
    text.setFill(suit.getColor());
    text.setFont(Font.font(null, CARD_GRID_SYMBOL_SIZE));

    if (rotate)
    {
      text.setRotate(180);
    }

    GridPane.setHalignment(text, HPos.CENTER);
    GridPane.setHgrow(text, Priority.ALWAYS);
    GridPane.setValignment(text, VPos.CENTER);
    GridPane.setVgrow(text, Priority.ALWAYS);

    return text;
  }

  public static class CardFace extends Pane
  {
  }
}