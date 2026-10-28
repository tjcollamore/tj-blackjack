import java.util.ArrayList;
import java.util.List;

public class PracticeTestScorer
{
  private final PracticeTestEvaluator evaluator =
      new PracticeTestEvaluator();


  public PracticeTestResult score(
      PracticeTestSession session,
      List<HandAction> userAnswers)
  {
    if (userAnswers.size()
        != session.size())
    {
      throw new IllegalArgumentException(
          "Expected "
          + session.size()
          + " answers but received "
          + userAnswers.size()
      );
    }


    List<PracticeTestAnswer> answers =
        new ArrayList<>();


    for (int i = 0;
         i < session.size();
         i++)
    {
      PracticeTestQuestion question =
          session.getQuestion(
              i
          );

      HandAction userAction =
          userAnswers.get(
              i
          );


      PracticeTestEvaluator.Evaluation evaluation =
          evaluator.evaluate(
              question,
              userAction,
              session.getDifficulty()
          );


      answers.add(
          new PracticeTestAnswer(
              question,
              userAction,
              evaluation
          )
      );
    }


    return new PracticeTestResult(
        session.getDifficulty(),
        answers
    );
  }
}
