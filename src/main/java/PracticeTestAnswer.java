public class PracticeTestAnswer
{
  private final PracticeTestQuestion question;
  private final HandAction userAction;
  private final PracticeTestEvaluator.Evaluation evaluation;


  public PracticeTestAnswer(
      PracticeTestQuestion question,
      HandAction userAction,
      PracticeTestEvaluator.Evaluation evaluation)
  {
    this.question =
        question;

    this.userAction =
        userAction;

    this.evaluation =
        evaluation;
  }


  public PracticeTestQuestion getQuestion()
  {
    return question;
  }


  public HandAction getUserAction()
  {
    return userAction;
  }


  public PracticeTestEvaluator.Evaluation
      getEvaluation()
  {
    return evaluation;
  }


  public boolean isCorrect()
  {
    return evaluation.isCorrect();
  }


  public boolean matchesBasic()
  {
    return evaluation.matchesBasic();
  }


  public boolean matchesSocial()
  {
    return evaluation.matchesSocial();
  }
}