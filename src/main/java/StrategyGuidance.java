public class StrategyGuidance
{
  private final HandAction recommendedAction;
  private final String rule;
  private final String explanation;

  public StrategyGuidance(
      HandAction recommendedAction,
      String rule,
      String explanation)
  {
    this.recommendedAction =
        recommendedAction;

    this.rule =
        rule;

    this.explanation =
        explanation;
  }

  public HandAction getRecommendedAction()
  {
    return recommendedAction;
  }

  public String getRule()
  {
    return rule;
  }

  public String getExplanation()
  {
    return explanation;
  }
}
