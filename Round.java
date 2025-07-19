import javafx.collections.ObservableList;
import javafx.scene.Node;

public class Round
{
  private IOutputter outputter;
  private ShoeMgr shoeMgr;
  private HandStrategy playerHandStrategy;
  private ObservableList<Node> dealerCardNodes;
  private ObservableList<Node> playerCardNodes;

  Round(IOutputter outputter, ShoeMgr shoeMgr, HandStrategy playerHandStrategy, ObservableList<Node> dealerCardNodes, ObservableList<Node> playerCardNodes)
  {
    this.outputter = outputter;
    this.shoeMgr = shoeMgr;
    this.playerHandStrategy = playerHandStrategy;
    this.dealerCardNodes = dealerCardNodes;
    this.playerCardNodes = playerCardNodes;
  }

  public void play()
  {
    Hand playerHand = new Hand(outputter, shoeMgr, false, playerCardNodes);
    Hand dealerHand = new Hand(outputter, shoeMgr, true, dealerCardNodes);

    if (playerHand.isBlackjack() && dealerHand.isBlackjack())
    {
      outputter.output("It's a tie!");
      dealerHand.showDealerDownCard();
      return;
    }
    if (playerHand.isBlackjack())
    {
      outputter.output("Blackjack! You win!");
      return;
    }
    if (dealerHand.isBlackjack())
    {
      dealerHand.showDealerDownCard();
      outputter.output("Dealer Blackjack! You lost!");
      return;
    }

    outputter.output("Your turn:");
    playerHandStrategy.play(playerHand, shoeMgr);

    if (playerHand.isBusted())
    {
      outputter.output("\nYou lost!");
      return;
    }

    outputter.output("");
    outputter.output("Dealer's turn:");
    DealerHandStrategy dealerHandStrategy = new DealerHandStrategy(outputter);
    dealerHand.showDealerDownCard();
    dealerHandStrategy.play(dealerHand, shoeMgr);
    outputter.output("");
    if (dealerHand.isBusted())
    {
      outputter.output("You win!");
      return;
    }

    if (playerHand.getScore() > dealerHand.getScore())
      outputter.output("You won!");
    else if (playerHand.getScore() < dealerHand.getScore())
      outputter.output("You lost!");
    else
      outputter.output("It's a tie!");
  }
}