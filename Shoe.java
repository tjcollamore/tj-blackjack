import java.util.*;

public class Shoe
{
  private List<Deck> decks = new ArrayList<Deck>();

  public Shoe(int deckCount)
  {
    for (int deckIndex = 0; deckIndex < deckCount; deckIndex++)
    {
      Deck deck = new Deck();
      decks.add(deck);
    }
  }

  public Card drawCard()
  {
    int remainingCardCount = getRemainingCardCount();
    if (remainingCardCount == 0)
      return null;

    Random random = new Random();
    int randomCardIndex = random.nextInt(remainingCardCount);

    return removeCardFromDeckList(decks, randomCardIndex);
  }


  private Card removeCardFromDeckList(List<Deck> decks, int cardIndex)
  {
    Deck firstDeck = decks.get(0);
    if (cardIndex < firstDeck.getRemainingCardCount())
      return firstDeck.remove(cardIndex);

    List<Deck> restOfDecks = decks.subList(1, decks.size());
    cardIndex -= firstDeck.getRemainingCardCount();
    return removeCardFromDeckList(restOfDecks, cardIndex);
  }


  public int getRemainingCardCount()
  {
    int remainingCardCount = 0;
    for (Deck deck : decks)
    {
      remainingCardCount += deck.getRemainingCardCount();
    }
    return remainingCardCount;
  }
}