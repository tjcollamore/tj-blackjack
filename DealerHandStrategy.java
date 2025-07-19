public class DealerHandStrategy implements HandStrategy
{
  private IOutputter outputter;
  
  public DealerHandStrategy(IOutputter outputter)
  {
    this.outputter = outputter;
  }
  
  public void play(Hand hand, ShoeMgr shoeMgr)
  {
    while (hand.getScore() <= 16)
    {
      Card card = shoeMgr.drawCard();
      outputter.output("Drawn card: " + card);
      hand.addCard(card);
    }
    outputter.output("Score is: " + hand.getScore());
    if (hand.isBlackjack())
      outputter.output("Blackjack!");
    else
    {
      if (hand.isBusted())
        outputter.output("Busted!");
    }
  }
}