public class RoundResultAnalyzer
{
  public static String analyze(Hand playerHand, Hand dealerHand, Wager wager, IOutputter outputter)
  {
    String result = "";
    if (playerHand.isBlackjack() && dealerHand.isBlackjack())
    {
      wager.push();
      result = "Push\nRecouped " + wager.getFormattedPayout();
    }
    else if (playerHand.isBlackjack())
    {
      wager.blackjack();
      result = "BLACKJACK! YOU WON " + wager.getFormattedPayout() + "!";
    }
    else if (dealerHand.isBlackjack())
    {
      wager.lose();
      result = "Dealer Blackjack!";
    }
    else if (wager.didSurrender())
    {
      result = "SURRENDERED!\nRecouped " + wager.getFormattedPayout();
    }
    else if (playerHand.isBusted())
    {
      wager.lose();
      result = "Bust!";
    }
    else if (dealerHand.isBusted())
    {
      wager.win();
      result = "YOU WON " + wager.getFormattedPayout() + "!";
    }
    else if (playerHand.getScore() > dealerHand.getScore())
    {
      wager.win();
      result = "YOU WON " + wager.getFormattedPayout() + "!";
    }
    else if (playerHand.getScore() < dealerHand.getScore())
    {
      wager.lose();
      result = "Dealer Wins";
    }
    else
    {
      wager.push();
      result = "Push\nRecouped " + wager.getFormattedPayout();
    }
    outputter.output(result);
    return result;
  }
}