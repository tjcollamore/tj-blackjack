import java.util.*;
import javafx.collections.ObservableList;
import javafx.scene.Node;

public class Hand
{
  private IOutputter outputter;
  private List<Card> cards = new ArrayList<Card>();
  private int aceCount = 0;
  private ObservableList<Node> cardNodes;

  public Hand(IOutputter outputter, ShoeMgr shoeMgr, boolean isDealer, ObservableList<Node> cardNodes)
  {
    this.outputter = outputter;
    outputter.output((isDealer ? "Dealer" : "Your") + " hand is:");

    Card card = shoeMgr.drawCard();
    addCard(card);
    outputter.output("  " + card);
    cardNodes.add(CardFaceCreator.createFrontFace(card.getRank(), card.getSuit(), 120, 180));
    
    card = shoeMgr.drawCard();
    addCard(card);
    if (isDealer)
    {
      outputter.output("  <Face Down>");
      cardNodes.add(CardFaceCreator.createBackFace(120, 180));}
    else
    {
      cardNodes.add(CardFaceCreator.createFrontFace(card.getRank(), card.getSuit(), 120, 180));
      outputter.output("  " + card);
    }

    outputter.output("");
  }

  public void showDealerDownCard()
  {
    outputter.output("Dealer down card: " + cards.get(1));
  }

  public void addCard(Card card)
  {
    cards.add(card);

    if (card.isAce())
      aceCount++;
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