import java.util.EnumSet;
import java.util.function.Consumer;

public class PlayerHandEngine
{
  private final GameSettings settings;

  public PlayerHandEngine(
      GameSettings settings)
  {
    this.settings = settings;
  }

  public PlayerHandPlayResult play(
      Hand playerHand,
      Card dealerUpCard,
      HandStrategy strategy,
      Shoe shoe,
      boolean cameFromSplit,
      boolean canDoubleOrSplit,
      double wagerAmount,
      Consumer<Card> onCardDrawn)
  {
    if (playerHand.is21())
      return PlayerHandPlayResult.FINISHED;

    while (true)
    {
      EnumSet<HandAction> allowedActions =
          PlayerActionRules.getAllowedActions(
              playerHand,
              cameFromSplit,
              settings
          );

      EnumSet<HandAction> availableActions =
          EnumSet.copyOf(
              allowedActions
          );

      if (!canDoubleOrSplit)
      {
        availableActions.remove(
            HandAction.DOUBLE
        );

        availableActions.remove(
            HandAction.SPLIT
        );
      }

      PlayerDecisionContext context =
          new PlayerDecisionContext(
              playerHand,
              dealerUpCard,
              settings,
              allowedActions,
              availableActions,
              cameFromSplit,
              shoe.getDeckCount(),
              wagerAmount
          );

      HandAction action =
          strategy.chooseAction(
              context
          );

      switch (action)
      {
        case HIT:
        {
          Card card =
              shoe.drawCard();

          playerHand.addCard(card);

          if (onCardDrawn != null)
            onCardDrawn.accept(card);

          if (playerHand.isBusted()
              || playerHand.is21())
          {
            return PlayerHandPlayResult.FINISHED;
          }

          break;
        }

        case STAND:
          return PlayerHandPlayResult.FINISHED;

        case DOUBLE:
        {
          Card card =
              shoe.drawCard();

          playerHand.addCard(card);

          if (onCardDrawn != null)
            onCardDrawn.accept(card);

          return PlayerHandPlayResult.DOUBLED;
        }

        case SPLIT:
          return PlayerHandPlayResult.SPLIT_REQUESTED;

        case SURRENDER:
          return PlayerHandPlayResult.SURRENDERED;

        default:
          throw new IllegalStateException(
              "Unexpected action: "
              + action
          );
      }
    }
  }
}
