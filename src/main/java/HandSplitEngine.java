public class HandSplitEngine
{
  public HandSplitResult split(
      Hand currentHand,
      Shoe shoe)
  {
    if (!currentHand.isSplittable())
    {
      throw new IllegalArgumentException(
          "Hand is not splittable"
      );
    }

    boolean splitAces =
        currentHand.isDoubleAces();

    Card splitCard =
        currentHand.removeCard(1);

    Hand newHand =
        new Hand();

    newHand.addCard(
        splitCard
    );

    Card currentHandCard =
        shoe.drawCard();

    currentHand.addCard(
        currentHandCard
    );

    Card newHandCard =
        shoe.drawCard();

    newHand.addCard(
        newHandCard
    );

    return new HandSplitResult(
        newHand,
        currentHandCard,
        newHandCard,
        splitAces
    );
  }
}
