public class HandSplitResult
{
  private final Hand newHand;
  private final Card cardDealtToCurrentHand;
  private final Card cardDealtToNewHand;
  private final boolean splitAces;

  public HandSplitResult(
      Hand newHand,
      Card cardDealtToCurrentHand,
      Card cardDealtToNewHand,
      boolean splitAces)
  {
    this.newHand = newHand;
    this.cardDealtToCurrentHand =
        cardDealtToCurrentHand;
    this.cardDealtToNewHand =
        cardDealtToNewHand;
    this.splitAces = splitAces;
  }

  public Hand getNewHand()
  {
    return newHand;
  }

  public Card getCardDealtToCurrentHand()
  {
    return cardDealtToCurrentHand;
  }

  public Card getCardDealtToNewHand()
  {
    return cardDealtToNewHand;
  }

  public boolean didSplitAces()
  {
    return splitAces;
  }
}
