public class HandOutcomeAnalyzer
{
  public static HandOutcome analyze(
      Hand playerHand,
      Hand dealerHand,
      boolean naturalBlackjack,
      boolean surrendered)
  {
    if (naturalBlackjack
        && dealerHand.isBlackjack())
    {
      return HandOutcome.PUSH;
    }

    if (naturalBlackjack)
    {
      return HandOutcome.BLACKJACK;
    }

    if (dealerHand.isBlackjack())
    {
      return HandOutcome.LOSS;
    }

    if (surrendered)
    {
      return HandOutcome.SURRENDER;
    }

    if (playerHand.isBusted())
    {
      return HandOutcome.LOSS;
    }

    if (dealerHand.isBusted())
    {
      return HandOutcome.WIN;
    }

    if (playerHand.getScore()
        > dealerHand.getScore())
    {
      return HandOutcome.WIN;
    }

    if (playerHand.getScore()
        < dealerHand.getScore())
    {
      return HandOutcome.LOSS;
    }

    return HandOutcome.PUSH;
  }
}
