public class HiLoCount
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
  int value =
      card.getRank()
          .getValue();


  if (value >= 2
      && value <= 6)
  {
    runningCount++;
  }
  else if (value == 10
           || value == 11)
  {
    runningCount--;
  }
}


@Override
public double getSignal(
    int remainingCards)
{
  double decksRemaining =
      remainingCards / 52.0;


  if (decksRemaining <= 0.0)
    return 0.0;


  return runningCount
      / decksRemaining;
}


@Override
public String getName()
{
  return "HI_LO";
}


public int getRunningCount()
{
  return runningCount;
}
}
