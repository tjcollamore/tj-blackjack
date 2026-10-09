public class ForcedFirstActionStrategy
  implements HandStrategy
{
private final HandAction firstAction;
private final HandStrategy continuationStrategy;
private boolean firstDecision = true;

public ForcedFirstActionStrategy(
    HandAction firstAction,
    HandStrategy continuationStrategy)
{
  this.firstAction =
      firstAction;

  this.continuationStrategy =
      continuationStrategy;
}

@Override
public HandAction chooseAction(
    PlayerDecisionContext context)
{
  if (firstDecision)
  {
    firstDecision = false;

    return firstAction;
  }

  return continuationStrategy.chooseAction(
      context
  );
}
}