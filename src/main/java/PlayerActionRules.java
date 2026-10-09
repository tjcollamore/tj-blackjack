import java.util.EnumSet;

public class PlayerActionRules
{
  public static EnumSet<HandAction> getAllowedActions(
      Hand hand, boolean cameFromSplit, GameSettings settings)
  {
    EnumSet<HandAction> actions =
        EnumSet.of(
            HandAction.HIT,
            HandAction.STAND
        );

    if (hand.getNumCards() == 2)
      {
        if (settings.allowSurrender()
            && (!cameFromSplit
                || settings.surrenderAfterSplit()))
        {
          actions.add(HandAction.SURRENDER);
        }

        if (!cameFromSplit || settings.doubleAfterSplit())
        {
          actions.add(HandAction.DOUBLE);
        }

        if (hand.isSplittable())
        {
          actions.add(HandAction.SPLIT);
        }
      }

      return actions;
  }
}
