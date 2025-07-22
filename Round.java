import javafx.scene.Node;
import java.util.*;

public class Round
{
  private IOutputter outputter;
  private ShoeMgr shoeMgr;
  private HandStrategy playerHandStrategy;
  private List<Node> dealerCardNodes;
  private List<Node> playerCardNodes;

  public Round(IOutputter outputter, ShoeMgr shoeMgr, HandStrategy playerHandStrategy, List<Node> dealerCardNodes, List<Node> playerCardNodes)
  {
    this.outputter = outputter;
    this.shoeMgr = shoeMgr;
    this.playerHandStrategy = playerHandStrategy;
    this.dealerCardNodes = dealerCardNodes;
    this.playerCardNodes = playerCardNodes;
  }

  public String play()
  {
    String result = "";
    Hand playerHand = new Hand(outputter, shoeMgr, false, playerCardNodes);
    Hand dealerHand = new Hand(outputter, shoeMgr, true, dealerCardNodes);

    if (playerHand.isBlackjack() && dealerHand.isBlackjack())
    {
      result = "It's a tie!";
      outputter.output(result);
      dealerHand.showDealerDownCard();
      return result;
    }
    if (playerHand.isBlackjack())
    {
      result = "Blackjack! You win!";
      outputter.output(result);
      return result;
    }
    if (dealerHand.isBlackjack())
    {
      result = "Dealer Blackjack! You lost!";
      dealerHand.showDealerDownCard();
      outputter.output(result);
      return result;
    }

    //outputter.output("Your turn:");
    playerHandStrategy.play(playerHand, shoeMgr);

    outputter.output("Your score is: " + playerHand.getScore());
    
    if (playerHand.isBusted())
    {
      result = "Busted! You lost!";
      outputter.output(result);
      return result;
    }

    //outputter.output("");
   // outputter.output("Dealer's turn:");
    DealerHandStrategy dealerHandStrategy = new DealerHandStrategy(outputter);
    dealerHand.showDealerDownCard();
    dealerHandStrategy.play(dealerHand, shoeMgr);
   // outputter.output("");

    //    outputter.output("");
     //  outputter.output("Score is: " + hand.getScore());
    
    if (dealerHand.isBusted())
    {
      result = "Dealer Busted! You won!";
      outputter.output(result);
      return result;
    }

    if (playerHand.getScore() > dealerHand.getScore())
      result = "You won!";
    else if (playerHand.getScore() < dealerHand.getScore())
      result = "You lost!";
    else
      result = "It's a tie!";
    outputter.output(result);
    return result;
   //   RoundResultMessageCreator.describeResult(playerHand, dealerHand);
  }
}