public class ShoeMgr
{
  private IOutputter outputter;
  private Shoe shoe;
  private final int DeckCount;

  public ShoeMgr(IOutputter outputter, int deckCount)
  {
    this.outputter = outputter;
    DeckCount = deckCount;
    shoe = new Shoe(DeckCount);
  }

  public Card drawCard()
  {
    Card card = shoe.drawCard();

    // Very rare condition to run out of cards mid-round.
    if (card == null)
    {
      outputter.output("No more cards in the shoe. Shuffling new shoe...");
      shoe = new Shoe(DeckCount);
      card = shoe.drawCard();
    }
    return card;
  }

  public void checkCutCard()
  {
    final int CUT_CARD_PER_DECK = 12;
    final int CUT_CARD = CUT_CARD_PER_DECK * DeckCount;

    if (shoe.getRemainingCardCount() < CUT_CARD)
    {
      outputter.output("Cut card reached. Shuffling new shoe...");
      shoe = new Shoe(DeckCount);
    }
  }
}