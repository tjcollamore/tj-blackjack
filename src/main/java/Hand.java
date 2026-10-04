import javafx.scene.Node;
import java.util.*;

public class Hand
{
  private IOutputter outputter;
  private List<Card> cards = new ArrayList<Card>();
  private int aceCount = 0;
  private List<Node> cardNodes;
  private boolean isDealer = false;
  private boolean hasStarted = false;
  private boolean isSplittingBlocked = false;
  private boolean isSoft = false;
  private ScoreBadge scoreBadge;

  public Hand(IOutputter outputter, boolean isDealer, List<Node> cardNodes, boolean hasStarted, ScoreBadge scoreBadge)
  {
    this.outputter = outputter;
    this.isDealer = isDealer;
    this.cardNodes = cardNodes;
    this.hasStarted = hasStarted;
    this.scoreBadge = scoreBadge;

    scoreBadge.setVisible(hasStarted && !isDealer);
    outputter.output("");
  }

  public void showDealerHoleCard()
  {
    outputter.output("Dealer down card: " + cards.get(1));
    showScoreBadge();
  }

  public Card getDealerHoleCard()
  {
    return cards.get(1);
  }

  public CardView getDealerHoleCardView()
  {
    return (CardView) cardNodes.get(1);
  }
  
  public CardView addCard(Card card)
  {
    cards.add(card);

    if (card.isAce())
      aceCount++;

    if (hasStarted)
      return displayCard(card);

    return null;
  }

  private CardView displayCard(Card card)
  {
    CardView cardView = new CardView();

    if (isDealer && cards.size() == 2)
    {
      cardView.showBack();

      cardNodes.add(cardView);

      outputter.output(
          "Dealer drawn card: <Face Down>"
      );
    }
    else
    {
      cardView.showFront(card);

      cardNodes.add(cardView);

      if (isDealer)
        outputter.output(
            "Dealer drawn card: " + card
        );
      else
        outputter.output(
            "Your drawn card: " + card
        );

      showScoreBadge();
    }
    return cardView;
  }
  
  public void start()
  {
    if (hasStarted)
      return;
    hasStarted = true;
    cardNodes.clear();
    for (Card card : cards)
      displayCard(card);
  }

  // Used when splitting
  public Card removeCard(int slot)
  {
    if (cards.get(slot).isAce())
      aceCount--;
    if (hasStarted)
      cardNodes.remove(slot);
    return cards.remove(slot);
  }
  
  private void showScoreBadge()
  {
    scoreBadge.setScore(this);
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
    return !isBlackjack() && isSoft;
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
    
    return cards.get(0).getRank().getValue() == cards.get(1).getRank().getValue();
  }

  public void blockSplitting()
  {
    isSplittingBlocked = true;
  }

  public boolean isDoubleAces()
  {
    return cards.get(0).isAce() && cards.get(1).isAce();
  }

  public int getNumCards()
  {
    return cards.size();
  }
}