import java.util.List;

public class AceAwarenessPracticeHand
{
  private final List<Card> playerCards;
  private final List<Card> dealerCards;


  public AceAwarenessPracticeHand(
      List<Card> playerCards,
      List<Card> dealerCards)
  {
    this.playerCards =
        List.copyOf(
            playerCards
        );

    this.dealerCards =
        List.copyOf(
            dealerCards
        );
  }


  public List<Card> getPlayerCards()
  {
    return playerCards;
  }


  public List<Card> getDealerCards()
  {
    return dealerCards;
  }
}
