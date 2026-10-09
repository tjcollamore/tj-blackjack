import java.util.*;

public class Deck
{
  private List<Card> cards = new ArrayList<Card>();
  Random random = new Random();

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

  public boolean removeCard(Card target)
  {
    for (int i = 0; i < cards.size(); i++)
    {
      Card card = cards.get(i);

      if (card.getRank() == target.getRank()
          && card.getSuit() == target.getSuit())
      {
        cards.remove(i);
        return true;
      }
    }
    return false;
  }
  
  public Card drawCard()
  {
    if (cards.size() == 0)
      return null;

    int position = random.nextInt(cards.size());
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