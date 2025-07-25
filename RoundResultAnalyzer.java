public class RoundResultAnalyzer
{
  public static String analyze(Hand playerHand, Hand dealerHand, Wager wager, IOutputter outputter)
  {
    String result = "";
    if (playerHand.isBlackjack() && dealerHand.isBlackjack())
    {
      result = "It's a tie!";
      wager.push();
    }
    else if (playerHand.isBlackjack())
    {
      result = "Blackjack! You win!";
      wager.blackjack();
    }
    else if (dealerHand.isBlackjack())
    {
      result = "Dealer Blackjack! You lost!";
      wager.lose();
    }
    else if (wager.didSurrender)
    {
      result = "You surrendered!";
    }
    else if (playerHand.isBusted())
    {
      result = "Busted! You lost!";
      wager.lose();
    }
    else if (dealerHand.isBusted())
    {
      result = "Dealer Busted! You won!";
      wager.win();
    }
    else if (playerHand.getScore() > dealerHand.getScore())
    {
      result = "You won!";
      wager.win();
    }
    else if (playerHand.getScore() < dealerHand.getScore())
    {
      result = "You lost!";
      wager.lose();
    }
    else
    {
      result = "It's a tie!";
      wager.push();
    }
    outputter.output(result);
    return result;
  }
}