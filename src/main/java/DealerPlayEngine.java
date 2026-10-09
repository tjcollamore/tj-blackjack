import java.util.function.Consumer;

public class DealerPlayEngine
{
  private final GameSettings settings;

  public DealerPlayEngine(
      GameSettings settings)
  {
    this.settings = settings;
  }

  public void play(
      Hand dealerHand,
      Shoe shoe,
      Consumer<Card> onCardDrawn)
  {
    DealerHandStrategy strategy =
        new DealerHandStrategy(
            settings
        );

    while (strategy.chooseAction(
        dealerHand) == HandAction.HIT)
    {
      Card card =
          shoe.drawCard();

      dealerHand.addCard(card);

      if (onCardDrawn != null)
      {
        onCardDrawn.accept(card);
      }
    }
  }
}
