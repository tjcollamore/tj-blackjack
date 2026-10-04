public class DealerHandStrategy implements HandStrategy
{
  public HandAction chooseAction(Hand hand, Wager wager)
  {
    if (hand.getScore() <= 16)
      return HandAction.HIT;

    return HandAction.STAND;
  }
}
