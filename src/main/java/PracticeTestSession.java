import java.util.List;

public class PracticeTestSession
{
  private final PracticeTestDifficulty difficulty;
  private final List<PracticeTestQuestion> questions;


  public PracticeTestSession(
      PracticeTestDifficulty difficulty,
      List<PracticeTestQuestion> questions)
  {
    this.difficulty =
        difficulty;

    this.questions =
        List.copyOf(
            questions
        );
  }


  public PracticeTestDifficulty getDifficulty()
  {
    return difficulty;
  }


  public List<PracticeTestQuestion>
      getQuestions()
  {
    return questions;
  }


  public int size()
  {
    return questions.size();
  }


  public PracticeTestQuestion getQuestion(
      int index)
  {
    return questions.get(
        index
    );
  }
}
