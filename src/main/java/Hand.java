import java.util.*;

public class Hand
{
  private List<Card> cards = new ArrayList<Card>();
  private int aceCount = 0;
  private boolean isSplittingBlocked = false;

  public Card getDealerHoleCard()
  {
    return cards.get(1);
  }

  public void addCard(Card card)
  {
    cards.add(card);

    if (card.isAce())
      aceCount++;
  }

  // Used when splitting
  public Card removeCard(int slot)
  {
    if (cards.get(slot).isAce())
      aceCount--;
    return cards.remove(slot);
  }
  
  public int getScore()
  {
    return calculateHandValue().score();
  }

  public boolean isSoft()
  {
    HandValue value = calculateHandValue();
    boolean blackjack = cards.size() == 2 && value.score() == 21;
    return !blackjack && value.soft();
  }
  
  public boolean isBusted()
  {
    return getScore() > 21;
  }

  public boolean isBlackjack()
  {
    return cards.size() == 2 && calculateHandValue().score() == 21;
  }

  public boolean is21()
  {
    return calculateHandValue().score() == 21;
  }

  public boolean isSplittable()
  {
    if (isSplittingBlocked || cards.size() != 2)
      return false;
    
    return cards.get(0).getRank().getValue() == cards.get(1).getRank().getValue();
  }

  public void blockSplitting()
  {
    isSplittingBlocked = true;
  }

  public boolean isDoubleAces()
  {
    return cards.size() == 2 && cards.get(0).isAce() && cards.get(1).isAce();
  }

  public int getNumCards()
  {
    return cards.size();
  }
  
  public Card getCard(int index)
  {
    return cards.get(index);
  }

  private record HandValue(int score, boolean soft)
  {
  }
  private HandValue calculateHandValue()
  {
    int score = 0;
    int localAceCount = aceCount;

    for (Card card : cards)
    {
      score += card.getPointValue();
    }

    while (localAceCount > 0
        && score > 21)
    {
      score -= 10;
      localAceCount--;
    }

    return new HandValue(
        score,
        localAceCount > 0
    );
  }
}