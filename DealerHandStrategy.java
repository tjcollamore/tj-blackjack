public class DealerHandStrategy implements HandStrategy
{
  private IOutputter outputter;
  
  public DealerHandStrategy(IOutputter outputter)
  {
    this.outputter = outputter;
  }
  
  public void play(Hand hand, ShoeMgr shoeMgr)
  {
//    outputter.output("Drawn card(s):");
    while (hand.getScore() <= 16)
    {
      Card card = shoeMgr.drawCard();
      hand.addCard(card);
    }
  }
}