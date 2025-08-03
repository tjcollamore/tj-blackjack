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
  private boolean isDisplayed;
  private boolean isSplittingBlocked = false;
  private boolean isSoft = false;

  public Hand(IOutputter outputter, ShoeMgr shoeMgr, boolean isDealer, List<Node> cardNodes, boolean isDisplayed)
  {
    this.outputter = outputter;
    this.isDealer = isDealer;
    this.cardNodes = cardNodes;
    this.shouldShowScore = !isDealer;
    this.isDisplayed = isDisplayed;

    Card card = shoeMgr.drawCard();
    addCard(card);

   // card = shoeMgr.drawCard();
  //  addCard(card);

    //Testing: can always split
    Card card2 = shoeMgr.drawCard();
    while (card.getRank() != card2.getRank())
    {
      card2 = shoeMgr.drawCard();
    }
    addCard(card2);
    
    
    outputter.output("");
  }

  public void showDealerHoleCard()
  {
    outputter.output("Dealer down card: " + cards.get(1));
    
    cardNodes.set(1, CardFaceCreator.createFrontFace(cards.get(1).getSuit(), cards.get(1).getRank()));

    if (!shouldShowScore)
    {
      shouldShowScore = true;
      showScoreBadge();
    }
  }

  public void addCard(Card card)
  {
    cards.add(card);

    if (card.isAce())
      aceCount++;

    if (isDisplayed)
      displayCard(card);
  }

  // This variant used when splitting
  public void addCardToFront(Card card)
  {
    cards.add(0, card);

    if (card.isAce())
      aceCount++;

    if (isDisplayed)
      displayCard(card);
  }

  private void displayCard(Card card)
  {
    // Removes the score badge if it exists (It will be the last node)
    if (cardNodes.size() > 0 && cardNodes.get(cardNodes.size() - 1) instanceof ScoreBadge)
      cardNodes.remove(cardNodes.size() - 1);
    
    if (isDealer && cards.size() == 2)
    {
      cardNodes.add(CardFaceCreator.createBackFace());
      outputter.output("Dealer drawn card: <Face Down>");
    }
    else
    {
cardNodes.add(CardFaceCreator.createFrontFace(card.getSuit(), card.getRank()));
      if (isDealer)
        outputter.output("Dealer drawn card: " + card);
      else
        outputter.output("Your drawn card: " + card);
    }
    if (shouldShowScore)
      showScoreBadge();
  }

  public void display()
  {
    if (isDisplayed)
      return;
    isDisplayed = true;
    cardNodes.clear();
    for (Card card : cards)
      displayCard(card);
  }

  // Used when splitting
  public Card removeCard(int slot)
  {
    if (cards.get(slot).isAce())
      aceCount--;
    if (isDisplayed)
      cardNodes.remove(slot);
    return cards.remove(slot);
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
    isSoft = (localAceCount > 0);
    return score;
  }

  public boolean isSoft()
  {
    getScore();
    return isSoft;
  }

  public boolean isBusted()
  {
    return getScore() > 21;
  }

  public boolean isBlackjack()
  {
    return is21() && cards.size() == 2;
  }

  public boolean is21()
  {
    return getScore() == 21;
  }

  public boolean isSplittable()
  {
    if (isSplittingBlocked)
      return false;
    
    return cards.get(0).getRank() == cards.get(1).getRank();
  }

  public void blockSplitting()
  {
    isSplittingBlocked = true;
  }
}