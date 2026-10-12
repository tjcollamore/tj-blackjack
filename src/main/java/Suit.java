import javafx.scene.paint.Color;

public enum Suit
{
  HEARTS("Hearts", Color.RED, "\u2665"),
  SPADES("Spades", Color.BLACK, "\u2660"),
  DIAMONDS("Diamonds", Color.RED, "\u2666"),
  CLUBS("Clubs", Color.BLACK, "\u2663");
  
  private String name;
  private Color color;
  private String symbol;

  private Suit(String name, Color color, String symbol)
  {
    this.name = name;
    this.color = color;
    this.symbol = symbol;
  }

  public String toString()
  {
    return name;
  }

  public Color getColor()
  {
    return color;
  }

  public String getSymbol()
  {
    return symbol;
  }
}