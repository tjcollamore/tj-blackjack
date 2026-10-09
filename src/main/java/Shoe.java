import java.util.*;

public class Shoe
{
  private List<Deck> decks = new ArrayList<Deck>();
  private Random random = new Random();
  private final int deckCount;
  
  public Shoe(int deckCount)
  {
    this.deckCount = deckCount;
    buildNewShoe();
  }

  private void buildNewShoe()
  {
    decks.clear();
    for (int deckIndex = 0; deckIndex < deckCount; deckIndex++)
    {
      Deck deck = new Deck();
      decks.add(deck);
    }
  }

  public int getDeckCount()
  {
    return deckCount;
  }
  
  public Card drawCard()
  {
    int remainingCardCount = getRemainingCardCount();
    if (remainingCardCount == 0)
    {
      buildNewShoe();
      remainingCardCount = getRemainingCardCount();
    }

    int randomCardIndex = random.nextInt(remainingCardCount);

    return removeCardFromDeckList(decks, randomCardIndex);
  }

  public boolean removeCard(Card target)
  {
    for (Deck deck : decks)
    {
      if (deck.removeCard(target))
        return true;
    }
    return false;
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

  public boolean needsShuffle()
  {
    final int CUT_CARD_PER_DECK = 12;
    int cutCard = CUT_CARD_PER_DECK * deckCount;
    return getRemainingCardCount() < cutCard;
  }

  public void shuffle()
  {
    buildNewShoe();
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