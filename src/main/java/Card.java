public class Card
{
  private Rank rank;
  private Suit suit;

  public Card(Rank rank, Suit suit)
  {
    this.rank = rank; 
    this.suit = suit;
  }

  public Rank getRank()
  {
    return rank;
  }

  public Suit getSuit()
  {
    return suit;
  }

  public boolean isAce()
  {
    return rank == Rank.ACE;
  }

  public String toString()
  {
    return rank.toString() + suit.getSymbol();
  }

  public int getPointValue()
  {
    return rank.getValue();
  } 
}