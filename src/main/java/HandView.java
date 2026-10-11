import java.util.List;

import javafx.scene.Node;

public class HandView
{
  private IOutputter outputter;
  private List<Node> cardNodes;
  private boolean isDealer;
  private boolean hasStarted;
  private ScoreBadge scoreBadge;

  public HandView(
      IOutputter outputter,
      boolean isDealer,
      List<Node> cardNodes,
      boolean hasStarted,
      ScoreBadge scoreBadge)
  {
    this.outputter = outputter;
    this.isDealer = isDealer;
    this.cardNodes = cardNodes;
    this.hasStarted = hasStarted;
    this.scoreBadge = scoreBadge;

    scoreBadge.setVisible(
        hasStarted && !isDealer
    );
  }

  public CardView cardAdded(
      Hand hand,
      Card card)
  {
    if (!hasStarted)
      return null;

    return displayCard(
        hand,
        card
    );
  }

  public void cardRemoved(int slot)
  {
    if (hasStarted)
      cardNodes.remove(slot);
  }

  public CardView getCardView(int slot)
  {
    return (CardView) cardNodes.get(slot);
  }
  
  public void start(Hand hand)
  {
    if (hasStarted)
      return;

    hasStarted = true;

    cardNodes.clear();

    for (int i = 0;
         i < hand.getNumCards();
         i++)
    {
      displayCard(
          hand,
          hand.getCard(i)
      );
    }
  }

  public void showPlayerHand(
      Hand hand)
  {
    if (isDealer)
    {
      throw new IllegalStateException(
          "showPlayerHand cannot be used "
          + "for dealer hand"
      );
    }

    cardNodes.clear();

    for (int i = 0;
         i < hand.getNumCards();
         i++)
    {
      displayCard(
          hand,
          hand.getCard(i)
      );
    }
  }

  public boolean hasStarted()
  {
    return hasStarted;
  }
  
  public CardView startSplitHand(
    Hand hand)
  {
    if (hasStarted)
      return null;
  
    hasStarted = true;
  
    cardNodes.clear();
  
    /*
     * Show the original split card immediately.
     */
    displayCard(
        hand,
        hand.getCard(0)
    );
  
    /*
     * Create the fresh second card so it can
     * be animated in by TableAnimator.
     */
    return displayCard(
        hand,
        hand.getCard(1)
    );
  }
  
  public void showDealerHoleCard(
      Hand hand)
  {
    outputter.output(
        "Dealer down card: "
        + hand.getCard(1)
    );

    showScoreBadge(hand);
  }

  public CardView getDealerHoleCardView()
  {
    return (CardView) cardNodes.get(1);
  }

  private CardView displayCard(
      Hand hand,
      Card card)
  {
    CardView cardView =
        new CardView();

    if (isDealer
        && hand.getNumCards() == 2
        && card == hand.getCard(1))
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
      {
        outputter.output(
            "Dealer drawn card: "
            + card
        );
      }
      else
      {
        outputter.output(
            "Your drawn card: "
            + card
        );
      }

      showScoreBadge(hand);
    }

    return cardView;
  }

  private void showScoreBadge(
      Hand hand)
  {
    scoreBadge.setScore(hand);
  }
}
