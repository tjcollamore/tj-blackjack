import javafx.scene.Node;
import java.util.*;

public class Round
{
  private IOutputter outputter;
  private ShoeMgr shoeMgr;
  private HandStrategy playerHandStrategy;
  private List<Node> dealerCardNodes;
  private List<Node> playerCardNodes;
  private Wager wager;

  public Round(IOutputter outputter, ShoeMgr shoeMgr, HandStrategy playerHandStrategy, List<Node> dealerCardNodes, List<Node> playerCardNodes, Wager wager)
  {
    this.outputter = outputter;
    this.shoeMgr = shoeMgr;
    this.playerHandStrategy = playerHandStrategy;
    this.dealerCardNodes = dealerCardNodes;
    this.playerCardNodes = playerCardNodes;
    this.wager = wager;
  }

  public String play()
  {
    Hand playerHand = new Hand(outputter, shoeMgr, false, playerCardNodes);
    Hand dealerHand = new Hand(outputter, shoeMgr, true, dealerCardNodes);

    if (dealerHand.isBlackjack())
      dealerHand.showDealerDownCard();

    if (!playerHand.isBlackjack() && !dealerHand.isBlackjack())
    {
      playerHandStrategy.play(playerHand, wager, shoeMgr);
      outputter.output("Your score is: " + playerHand.getScore());
      
      if (!playerHand.isBusted() && !wager.didSurrender())
      {
        DealerHandStrategy dealerHandStrategy = new DealerHandStrategy();
        dealerHand.showDealerDownCard();
        dealerHandStrategy.play(dealerHand, wager, shoeMgr);
        outputter.output("Dealer score is: " + dealerHand.getScore());
      }
    }    
    return RoundResultAnalyzer.analyze(playerHand, dealerHand, wager, outputter);
  }
}