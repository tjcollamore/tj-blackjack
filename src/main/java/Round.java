import java.util.*;
import javafx.scene.Node;
import javafx.stage.Stage;

public class Round
{
  private Stage stage;
  private IOutputter outputter;
  private ShoeMgr shoeMgr;
  private HandStrategy playerHandStrategy;
  private List<Node> dealerCardNodes;
  private List<Node> playerCardNodes;
  private SubroundBadge subroundBadge;
  private int lastBetAmount;
  private Bankroll bankroll;

  public Round(Stage stage, IOutputter outputter, ShoeMgr shoeMgr, HandStrategy playerHandStrategy, List<Node> dealerCardNodes, SubroundBadge subroundBadge, List<Node> playerCardNodes, int betAmount, Bankroll bankroll)
  {
    this.stage = stage;
    this.outputter = outputter;
    this.shoeMgr = shoeMgr;
    this.playerHandStrategy = playerHandStrategy;
    this.dealerCardNodes = dealerCardNodes;
    this.playerCardNodes = playerCardNodes;
    this.subroundBadge = subroundBadge;
    this.lastBetAmount = betAmount;
    this.bankroll = bankroll;
  }

  public String play()
  {
    Wager wager = new Wager(bankroll, lastBetAmount, outputter);

    lastBetAmount = wager.determineBetAmount(stage, GameOptions.TABLE_MIN_BET, GameOptions.TABLE_MAX_BET);

    int maxNumSubrounds = 4;
    Hand[] playerHands = new Hand[maxNumSubrounds]; 

    Hand dealerHand = new Hand(outputter, shoeMgr, true, dealerCardNodes, true);

playerHands[0] = new Hand(outputter, shoeMgr, false, playerCardNodes, true);

    String resultMessage = "";   
    for (int i = 0; i < playerHands.length; i++)
    {
      if (playerHands[i] == null)
        break;

      playerHands[i].display();

      subroundBadge.update(i+1, getNumSubrounds(playerHands));
      
      resultMessage = playSubRound(playerHands[i], dealerHand, wager);
      if (wager.didSplit())
      {
        Hand newPlayerHand = new Hand(outputter, shoeMgr, false, playerCardNodes, false);
        
        int nextFreeSlot = nextFreeSlot(playerHands);
        playerHands[nextFreeSlot] = newPlayerHand;

        if (nextFreeSlot == lastSlot(playerHands))
          blockSplitting(playerHands);
        
Card firstHandSecondCard = playerHands[i].removeCard(1);
        Card secondHandFirstCard = newPlayerHand.removeCard(0);
        newPlayerHand.addCardToFront(firstHandSecondCard);
        playerHands[i].addCard(secondHandFirstCard);
        wager.reset();
        i--;
      }
      else
      {
        //the subround was played. If any splits have occurred anywhere, then we're in the mode of showing interim results, rather than passing the resultMessage back to Main.
        if (hasMultipleSubrounds(playerHands))
        {
          ChoiceDialog.show(stage, resultMessage, "Ok");
          resultMessage = "";
        }
      }
    }
    return resultMessage;
  }

  private int nextFreeSlot(Hand[] playerHands)
  {
    for (int i = 0; i < playerHands.length; i++)
    {
      if (playerHands[i] == null)
        return i;
    }
    throw new IllegalStateException("No free slots available");
  }

  private int lastSlot(Hand[] playerHands)
  {
    return playerHands.length - 1;
  }
  
  private int getNumSubrounds(Hand[] playerHands)
  {
    int count = 0;
    for (Hand hand : playerHands)
    {
      if (hand == null)
        break;
      count++;
    }
    return count;
  }

  private void blockSplitting(Hand[] playerHands)
  {
    for (Hand hand : playerHands)
      hand.blockSplitting();
  }
  
  private boolean hasMultipleSubrounds(Hand[] playerHands)
  {
    return playerHands[1] != null;
  }
  
  public String playSubRound(Hand playerHand, Hand dealerHand, Wager wager)
  {
    if (dealerHand.isBlackjack())
      dealerHand.showDealerHoleCard();

    if (!playerHand.isBlackjack() && !dealerHand.isBlackjack())
    {
      playerHandStrategy.play(playerHand, wager, shoeMgr);

      if (wager.didSplit())
      {
        return "";
      }
      
      outputter.output("Your score is: " + playerHand.getScore());
      
      if (!playerHand.isBusted() && !wager.didSurrender())
      {
        DealerHandStrategy dealerHandStrategy = new DealerHandStrategy();
        dealerHand.showDealerHoleCard();
        dealerHandStrategy.play(dealerHand, wager, shoeMgr);
        outputter.output("Dealer score is: " + dealerHand.getScore());
      }
    }    
    return RoundResultAnalyzer.analyze(playerHand, dealerHand, wager, outputter);
  }

  public int getLastBetAmount()
  {
    return lastBetAmount;
  }
}