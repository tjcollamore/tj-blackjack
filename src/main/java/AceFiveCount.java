public class AceFiveCount
  implements CountingSystem
{
private int runningCount;


@Override
public void reset()
{
  runningCount = 0;
}


@Override
public void observe(
    Card card)
{
  Rank rank =
      card.getRank();


  if (rank == Rank.FIVE)
  {
    runningCount++;
  }
  else if (rank == Rank.ACE)
  {
    runningCount--;
  }
}


@Override
public double getSignal(
    int remainingCards)
{
  return runningCount;
}


@Override
public String getName()
{
  return "ACE_FIVE";
}


public int getRunningCount()
{
  return runningCount;
}
}
