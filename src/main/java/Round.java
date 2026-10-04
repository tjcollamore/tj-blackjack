import java.util.*;
import javafx.util.Duration;
import javafx.scene.Node;
import javafx.stage.Stage;

public class Round
{
  private Stage stage;
  private BlackjackSceneCreator sceneCreator;
  private IOutputter outputter;
  private ShoeMgr shoeMgr;
  private HandStrategy playerHandStrategy;
  private int lastBetAmount;
  private Bankroll bankroll;
  private TableAnimator tableAnimator;
  
  public Round(Stage stage, BlackjackSceneCreator sceneCreator, IOutputter outputter, ShoeMgr shoeMgr, HandStrategy playerHandStrategy, int betAmount, Bankroll bankroll)
  {
    this.stage = stage;
    this.sceneCreator = sceneCreator;
    this.outputter = outputter;
    this.shoeMgr = shoeMgr;
    this.playerHandStrategy = playerHandStrategy;
    this.lastBetAmount = betAmount;
    this.bankroll = bankroll;
    this.tableAnimator = new TableAnimator(sceneCreator);
  }
  
  public String play()
  {
    Wager wager = new Wager(bankroll, lastBetAmount, outputter);
    wager.place();

    int maxNumSubrounds = 4;
    Subround[] subrounds = new Subround[maxNumSubrounds]; 

    Hand dealerHand = new Hand(outputter, true, sceneCreator.getDealerCards(), true, sceneCreator.getDealerScoreBadge());

    Hand firstPlayerHand = new Hand(outputter, false, sceneCreator.getPlayerCards(), true, sceneCreator.getPlayerScoreBadge());

    subrounds[0] = new Subround(firstPlayerHand, wager);
    
    // Player first card
    Card playerCard1 = shoeMgr.drawCard();
    CardView playerCardView1 = firstPlayerHand.addCard(playerCard1);
    tableAnimator.dealCard(playerCard1, playerCardView1, false);

    // Dealer first card
    Card dealerCard1 = shoeMgr.drawCard();
    CardView dealerCardView1 = dealerHand.addCard(dealerCard1);
    tableAnimator.dealCard(dealerCard1, dealerCardView1, false);

    // Player second card
    Card playerCard2 = shoeMgr.drawCard();
    //Testing: always split
        while (playerCard1.getRank().getValue() != playerCard2.getRank().getValue())
        {
            playerCard2 = shoeMgr.drawCard();
        }
    
    CardView playerCardView2 = firstPlayerHand.addCard(playerCard2);
    tableAnimator.dealCard(playerCard2, playerCardView2, false);

    // Dealer hole card
    Card dealerCard2 = shoeMgr.drawCard();
    CardView dealerCardView2 = dealerHand.addCard(dealerCard2);
    tableAnimator.dealCard(dealerCard2, dealerCardView2, true);

    boolean dealerBlackjack = dealerHand.isBlackjack();
    if (dealerBlackjack)
    {
      dealerHand.showDealerHoleCard();
      tableAnimator.flipCard(dealerHand.getDealerHoleCard(),
                              dealerHand.getDealerHoleCardView());
    }

    String resultMessage = "";
    if (!dealerBlackjack)
    {
      for (int i = 0; i < subrounds.length; i++)
      {
        Subround currentSubround = subrounds[i];
        if (currentSubround == null)
          break;
  
        Hand currentHand = currentSubround.getHand();
        Wager currentWager = currentSubround.getWager();
        
        currentHand.start();
  
        sceneCreator.getSubroundBadge().update(i+1, getNumSubrounds(subrounds));
        
        playPlayerSubround(currentSubround);
        if (currentWager.didSplit())
        {
          Hand newPlayerHand = new Hand(outputter, false, sceneCreator.getPlayerCards(), false, sceneCreator.getPlayerScoreBadge());
          
          int nextFreeSlot = nextFreeSlot(subrounds);
          boolean splittingAces = currentHand.isDoubleAces();
          Wager newWager = new Wager(bankroll, currentWager.getBetAmount(), outputter);
          Subround newSubround = new Subround(newPlayerHand, newWager);
          currentSubround.setSplitAces(splittingAces);
          newSubround.setSplitAces(splittingAces);
          subrounds[nextFreeSlot] = newSubround;
  
          if (nextFreeSlot == lastSlot(subrounds))
            blockSplitting(subrounds);
  
          // Move the second card from the original hand into the new split hand
          Card splitCard = currentHand.removeCard(1);
          newPlayerHand.addCard(splitCard);
  
          // Give each split hand 1 new card.
          currentHand.addCard(shoeMgr.drawCard());
          newPlayerHand.addCard(shoeMgr.drawCard());
          
          currentWager.reset();
          i--;
        }
      }
    }
  
    if (!dealerBlackjack)
    {
        playDealerHand(subrounds, dealerHand);
    }
    
    if (resultMessage != null && !resultMessage.isBlank())
    {
      ChoiceDialog.showGameMessage(stage, resultMessage);
      tableAnimator.pause(Duration.seconds(2.2));
    }
    
    tableAnimator.clearTable();
    sceneCreator.getSubroundBadge().reset();
    return resultMessage;
  }

  private void playDealerHand(Subround[] subrounds, Hand dealerHand)
  {
    if (!dealerNeedsToPlay(subrounds))
      return;
  
    DealerHandStrategy dealerHandStrategy = new DealerHandStrategy();
    dealerHand.showDealerHoleCard();

    tableAnimator.flipCard(dealerHand.getDealerHoleCard(), dealerHand.getDealerHoleCardView());

    while (dealerHandStrategy.chooseAction(
                                dealerHand, 
                                subrounds[0].getWager()
                                          ) == HandAction.HIT)
    {
      Card card = shoeMgr.drawCard();
      CardView cardView = dealerHand.addCard(card);
      tableAnimator.dealCard(card, cardView, false);
    }

    outputter.output("Dealer score is: " + dealerHand.getScore());
  }
  
  private boolean dealerNeedsToPlay(Subround[] subrounds)
  {
    for (Subround subround : subrounds)
    {
      if (subround == null)
        break;

      Hand hand = subround.getHand();
      Wager wager = subround.getWager();

      if (!hand.isBusted() && !wager.didSurrender() && !hand.isBlackjack())
        return true;
    }
    return false;
  }
  
  private int nextFreeSlot(Subround[] subrounds)
  {
    for (int i = 0; i < subrounds.length; i++)
    {
      if (subrounds[i] == null)
        return i;
    }
    throw new IllegalStateException("No free slots available");
  }

  private int lastSlot(Subround[] subrounds)
  {
    return subrounds.length - 1;
  }
  
  private int getNumSubrounds(Subround[] subrounds)
  {
    int count = 0;
    for (Subround subround : subrounds)
    {
      if (subround == null)
        break;
      count++;
    }
    return count;
  }

  private void blockSplitting(Subround[] subrounds)
  {
    for (Subround subround : subrounds)
    {
      if (subround != null)
        subround.getHand().blockSplitting();
    }
  }
  
  private boolean hasMultipleSubrounds(Subround[] subrounds)
  {
    return subrounds[1] != null;
  }

  private void playPlayerSubround(Subround subround)
  {
    Hand playerHand = subround.getHand();
    Wager wager = subround.getWager();

    if (playerHand.isBlackjack())
      return;

    if (subround.didSplitAces())
      return;

/*    
    if (dealerHand.isBlackjack())
    {
      dealerHand.showDealerHoleCard();
      tableAnimator.flipCard(
          dealerHand.getDealerHoleCard(),
          dealerHand.getDealerHoleCardView()
      );
    }
*/
    
    boolean playerFinished = false;
    while (!playerFinished)
    {
      HandAction action =
              playerHandStrategy.chooseAction(playerHand, wager);
    
      switch (action)
      {
        case HIT:
        {
          Card card = shoeMgr.drawCard();
          CardView cardView = playerHand.addCard(card);
          tableAnimator.dealCard(card, cardView, false);
    
          if (playerHand.isBusted() || playerHand.is21())
            playerFinished = true;
    
          break;
        }
    
        case STAND:
        {
          playerFinished = true;
          break;
        }
    
        case DOUBLE:
        {
          wager.doubleDown();
    
          Card card = shoeMgr.drawCard();
          CardView cardView = playerHand.addCard(card);
          tableAnimator.dealCard(card, cardView, false);
    
          // Double always ends the player's turn.
          playerFinished = true;
          break;
        }
    
        case SPLIT:
        {
          wager.split(playerHand);
          return;
        }
    
        case SURRENDER:
        {
          wager.surrender();
          playerFinished = true;
          break;
        }
      }
    }
    
    outputter.output("Your score is: " + playerHand.getScore());
/*    
      if (!playerHand.isBusted() && !wager.didSurrender())
      {
        DealerHandStrategy dealerHandStrategy =
            new DealerHandStrategy();
    
        dealerHand.showDealerHoleCard();
        tableAnimator.flipCard(
            dealerHand.getDealerHoleCard(),
            dealerHand.getDealerHoleCardView()
        );
    
        while (dealerHandStrategy.chooseAction(dealerHand, wager) == HandAction.HIT)
        {
          Card card = shoeMgr.drawCard();
          CardView cardView = dealerHand.addCard(card);
          tableAnimator.dealCard(card, cardView, false);
        }
        
        outputter.output(
            "Dealer score is: " + dealerHand.getScore());
      }
    }
    
    return RoundResultAnalyzer.analyze(
        playerHand,
        dealerHand,
        wager,
        outputter);
*/
  }
}
