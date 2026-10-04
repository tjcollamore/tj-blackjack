public class Subround
{
  private Hand hand;
  private Wager wager;
  private boolean splitAces = false;

  public Subround(Hand hand, Wager wager)
  {
    this.hand = hand;
    this.wager = wager;
  }

  public Hand getHand()
  {
    return hand;
  }

  public Wager getWager()
  {
    return wager;
  }

  public boolean didSplitAces()
  {
    return splitAces;
  }

  public void setSplitAces(boolean splitAces)
  {
    this.splitAces = splitAces;
  }
}
