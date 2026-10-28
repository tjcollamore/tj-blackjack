public class PracticeTestMistakeCost
{
  private final PracticeTestAnswer answer;

  private final HandAction basicAction;

  private final double basicExpectedValue;
  private final double userExpectedValue;

  private final double estimatedDollarCost;


  public PracticeTestMistakeCost(
      PracticeTestAnswer answer,
      HandAction basicAction,
      double basicExpectedValue,
      double userExpectedValue,
      double estimatedDollarCost)
  {
    this.answer =
        answer;

    this.basicAction =
        basicAction;

    this.basicExpectedValue =
        basicExpectedValue;

    this.userExpectedValue =
        userExpectedValue;

    this.estimatedDollarCost =
        estimatedDollarCost;
  }


  public PracticeTestAnswer getAnswer()
  {
    return answer;
  }


  public HandAction getBasicAction()
  {
    return basicAction;
  }


  public double getBasicExpectedValue()
  {
    return basicExpectedValue;
  }


  public double getUserExpectedValue()
  {
    return userExpectedValue;
  }


  public double getEstimatedDollarCost()
  {
    return estimatedDollarCost;
  }
}
