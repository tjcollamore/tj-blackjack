public class AceAwarenessState
{
  private boolean decisionWindowReached;
  private boolean tableChangeRecommended;
  private boolean tableChangeMissed;
  private boolean lateWarningShown;
  private static final double CHANGE_THRESHOLD = -1.0;
  
  public void reset()
  {
    decisionWindowReached = false;
    tableChangeRecommended = false;
    tableChangeMissed = false;
    lateWarningShown = false;
  }

  public boolean isDecisionWindowReached()
  {
    return decisionWindowReached;
  }

  public void setDecisionWindowReached(
      boolean reached)
  {
    decisionWindowReached = reached;
  }

  public boolean isTableChangeRecommended()
  {
    return tableChangeRecommended;
  }

  public void setTableChangeRecommended(
      boolean recommended)
  {
    tableChangeRecommended = recommended;
  }

  public boolean isTableChangeMissed()
  {
    return tableChangeMissed;
  }

  public void setTableChangeMissed(
      boolean missed)
  {
    tableChangeMissed = missed;
  }

  public boolean isLateWarningShown()
  {
    return lateWarningShown;
  }

  public void setLateWarningShown(
      boolean shown)
  {
    lateWarningShown = shown;
  }

  // If at the midpoint we were a full -1.0 actual, consider that meaningful, and the user has
  // half a deck to pick up on it.
  public void update(
    AceOnlyCount count,
    int deckCount,
    double decksSeen)
  {
  double midpoint =
      deckCount / 2.0;

  double latePoint =
      midpoint + 0.5;

  if (!decisionWindowReached
      && decksSeen >= midpoint)
  {
    decisionWindowReached = true;

    if (count.getAceBalance()
        <= CHANGE_THRESHOLD)
    {
      tableChangeRecommended = true;
    }
  }

  if (decksSeen >= latePoint
      && tableChangeRecommended
      && !lateWarningShown)
  {
    lateWarningShown = true;
  }
  }
  
}
