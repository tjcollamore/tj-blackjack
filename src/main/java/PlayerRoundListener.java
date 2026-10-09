public interface PlayerRoundListener
{
  boolean canDoubleOrSplit(
      PlayerHandState state);

  double getWagerAmount(
      PlayerHandState state);

  void handStarted(
      PlayerHandState state,
      int index,
      int handCount);

  void cardDrawn(
      PlayerHandState state,
      Card card);

  void handDoubled(
      PlayerHandState state);

  void handSurrendered(
      PlayerHandState state);

  void handSplit(
      PlayerHandState originalState,
      PlayerHandState newState,
      HandSplitResult splitResult);

  void handFinished(
      PlayerHandState state,
      int index,
      int handCount);
}
