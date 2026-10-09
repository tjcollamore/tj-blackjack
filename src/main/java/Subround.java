public class Subround
{
  private Hand hand;
  private Wager wager;
  private boolean splitAces = false;
  private RoundResultMessage result;
  private boolean cameFromSplit = false;
  
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

  public RoundResultMessage getResultMessage()
  {
    return result;
  }

  public void setResultMessage(
      RoundResultMessage result)
  {
    this.result = result;
  }
  
  public boolean cameFromSplit()
  {
    return cameFromSplit;
  }

  public void setCameFromSplit(boolean cameFromSplit)
  {
    this.cameFromSplit = cameFromSplit;
  }

  public boolean isNaturalBlackjack()
  {
    return !cameFromSplit && hand.isBlackjack();
  }
}
