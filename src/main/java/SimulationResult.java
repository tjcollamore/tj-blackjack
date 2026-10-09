public class SimulationResult
{
  private final int trials;

  private final double expectedValue;
  private final double standardDeviation;

  private final int wins;
  private final int pushes;
  private final int losses;

  public SimulationResult(
      int trials,
      double expectedValue,
      double standardDeviation,
      int wins,
      int pushes,
      int losses)
  {
    this.trials = trials;
    this.expectedValue = expectedValue;
    this.standardDeviation = standardDeviation;
    this.wins = wins;
    this.pushes = pushes;
    this.losses = losses;
  }

  public int getTrials()
  {
    return trials;
  }

  public double getExpectedValue()
  {
    return expectedValue;
  }

  public double getStandardDeviation()
  {
    return standardDeviation;
  }

  public int getWins()
  {
    return wins;
  }

  public int getPushes()
  {
    return pushes;
  }

  public int getLosses()
  {
    return losses;
  }

  public double getWinRate()
  {
    return rate(wins);
  }

  public double getPushRate()
  {
    return rate(pushes);
  }

  public double getLossRate()
  {
    return rate(losses);
  }

  public double getNonLossRate()
  {
    return rate(wins + pushes);
  }
  
  private double rate(int count)
  {
    if (trials == 0)
      return 0.0;

    return (double) count / trials;
  }
}
