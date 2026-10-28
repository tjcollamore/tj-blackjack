public class StrategyDeviation
{
  private final String id;
  private final HandAction basicAction;
  private final HandAction socialAction;
  private final String title;
  private final String explanation;
  private final String evidence;
  private final String takeaway;

  public StrategyDeviation(
      String id,
      HandAction basicAction,
      HandAction socialAction,
      String title,
      String explanation,
      String evidence,
      String takeaway)
  {
    this.id =
        id;

    this.basicAction =
        basicAction;

    this.socialAction =
        socialAction;

    this.title =
        title;

    this.explanation =
        explanation;

    this.evidence =
        evidence;

    this.takeaway =
        takeaway;
  }

  public String getId()
  {
    return id;
  }

  public HandAction getBasicAction()
  {
    return basicAction;
  }

  public HandAction getSocialAction()
  {
    return socialAction;
  }

  public String getTitle()
  {
    return title;
  }

  public String getExplanation()
  {
    return explanation;
  }

  public String getEvidence()
  {
    return evidence;
  }

  public String getTakeaway()
  {
    return takeaway;
  }
}
