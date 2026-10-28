public class AceAwarenessCheckpoint
{
  private final double decksSeenTarget;

  private final boolean askTableDecision;
  private final boolean askSuper4;


  public AceAwarenessCheckpoint(
      double decksSeenTarget,
      boolean askTableDecision,
      boolean askSuper4)
  {
    this.decksSeenTarget =
        decksSeenTarget;

    this.askTableDecision =
        askTableDecision;

    this.askSuper4 =
        askSuper4;
  }


  public double getDecksSeenTarget()
  {
    return decksSeenTarget;
  }


  public boolean shouldAskTableDecision()
  {
    return askTableDecision;
  }


  public boolean shouldAskSuper4()
  {
    return askSuper4;
  }
}