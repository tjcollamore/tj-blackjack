public class DealerHandStrategy implements HandStrategy
{  
  public void play(Hand hand, Wager wager, ShoeMgr shoeMgr)
  {
    while (hand.getScore() <= 16)
    {
      Card card = shoeMgr.drawCard();
      hand.addCard(card);
    }
  }
}