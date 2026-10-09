public class PlayerHandState
{
  private final Hand hand;
  private boolean cameFromSplit;
  private boolean splitAces;
  private double wagerMultiplier = 1.0;
  private boolean surrendered;

  public PlayerHandState(
      Hand hand)
  {
    this.hand = hand;
  }

  public Hand getHand()
  {
    return hand;
  }

  public boolean cameFromSplit()
  {
    return cameFromSplit;
  }

  public void setCameFromSplit(
      boolean cameFromSplit)
  {
    this.cameFromSplit =
        cameFromSplit;
  }

  public boolean didSplitAces()
  {
    return splitAces;
  }

  public void setSplitAces(
      boolean splitAces)
  {
    this.splitAces =
        splitAces;
  }

  public double getWagerMultiplier()
  {
    return wagerMultiplier;
  }

  public void markDoubled()
  {
    wagerMultiplier = 2.0;
  }

  public boolean didSurrender()
  {
    return surrendered;
  }

  public void markSurrendered()
  {
    surrendered = true;
  }
}
