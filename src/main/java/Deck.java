import java.util.*;

public class Deck
{
  private List<Card> cards = new ArrayList<Card>();

  public Deck()
  {
    for (Rank rank : Rank.values())
    {
      cards.add(new Card(rank, Suit.HEARTS));
      cards.add(new Card(rank, Suit.DIAMONDS));
      cards.add(new Card(rank, Suit.CLUBS));
      cards.add(new Card(rank, Suit.SPADES));
    }
  }

  public Card drawCard()
  {
    if (cards.size() == 0)
      return null;

    Random numberGenerator = new Random();
    int position = numberGenerator.nextInt(cards.size());
    return cards.remove(position);
  }

  public int getRemainingCardCount()
  {
    return cards.size();
  }

  public Card remove(int index)
  {
    return cards.remove(index);
  } 
}