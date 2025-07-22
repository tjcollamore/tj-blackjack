import javafx.scene.Node;
import java.util.*;

public class Hand
{
  private IOutputter outputter;
  private List<Card> cards = new ArrayList<Card>();
  private int aceCount = 0;
  private List<Node> cardNodes;
  private boolean isDealer;
  private boolean shouldShowScore;

  public Hand(IOutputter outputter, ShoeMgr shoeMgr, boolean isDealer, List<Node> cardNodes)
  {
    this.outputter = outputter;
    this.isDealer = isDealer;
    this.cardNodes = cardNodes;
    this.shouldShowScore = !isDealer;
    
//    outputter.output((isDealer ? "Dealer" : "Your") + " hand is:");

    Card card = shoeMgr.drawCard();
    addCard(card);
    
    card = shoeMgr.drawCard();
    addCard(card);

 //   outputter.output("");
  }

  public void showDealerDownCard()
  {
//    outputter.output("Dealer down card: " + cards.get(1));
    
    cardNodes.set(1, CardFaceCreator.createFrontFace(cards.get(1).getSuit(), cards.get(1).getRank()));

    shouldShowScore = true;

    showScoreBadge();
  }

  public void addCard(Card card)
  {
    cards.add(card);

    // Removes the score badge if it exists (It will be the last node)
    if (cardNodes.size() > 0 && cardNodes.get(cardNodes.size() - 1) instanceof ScoreBadge)
      cardNodes.remove(cardNodes.size() - 1);
    
    if (isDealer && cards.size() == 2)
    {
      cardNodes.add(CardFaceCreator.createBackFace());
//      outputter.output("  <Face Down>");
    }
    else
    {
      cardNodes.add(CardFaceCreator.createFrontFace(card.getSuit(), card.getRank()));
 //     outputter.output("  " + card);
    }
  
    if (card.isAce())
      aceCount++;

    if (shouldShowScore)
    {
    showScoreBadge();
    }
  }

  private void showScoreBadge()
  {
    ScoreBadge playerBadge = new ScoreBadge();
    playerBadge.setScore(this, getScore());
    cardNodes.add(playerBadge);
  }
  
  public int getScore()
  {
    int score = 0;
    for (Card card : cards)
    {
      score += card.getPointValue();
    }

    int localAceCount = aceCount;
    while (localAceCount > 0 && score > 21)
    {
      score -= 10;
      localAceCount--;
    }
    return score;
  }

  public boolean isBusted()
  {
    return getScore() > 21;
  }

  public boolean isBlackjack()
  {
    return getScore() == 21 && cards.size() == 2;
  }
}