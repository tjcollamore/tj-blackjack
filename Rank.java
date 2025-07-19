public enum Rank
{
  ACE(11, "A", "Ace"),
  TWO(2),
  THREE(3),
  FOUR(4),
  FIVE(5),
  SIX(6),
  SEVEN(7),
  EIGHT(8),
  NINE(9),
  TEN(10),
  JACK(10, "J", "Jack"),
  QUEEN(10, "Q", "Queen"),
  KING(10, "K", "King");

  private int value;
  private String name;
  private String shortName;

  private Rank(int value)
  {
    this.value = value;
    name = Integer.toString(value);
    shortName = name;
  }

  private Rank(int value, String shortName, String name)
  {
    this.value = value;
    this.shortName = shortName;
    this.name = name;
  }

  public String getShortName()
  {
    return shortName;
  }
  
  public int getValue()
  {
    return value;
  }

  public String toString()
  {
    return name;
  }
}