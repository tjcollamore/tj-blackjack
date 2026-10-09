public class DealerHandStrategy
{
  private GameSettings settings;

  public DealerHandStrategy(GameSettings settings)
  {
    this.settings = settings;
  }
  
  public HandAction chooseAction(Hand hand)
  {
    if (hand.getScore() <= 16)
      return HandAction.HIT;

    if (hand.getScore() == 17
      && hand.isSoft()
      && settings.dealerHitsSoft17())
    {
      return HandAction.HIT;
    }
    
    return HandAction.STAND;
  }
}
