import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class PracticeTestResult
{
  private final PracticeTestDifficulty difficulty;
  private final List<PracticeTestAnswer> answers;


  public PracticeTestResult(
      PracticeTestDifficulty difficulty,
      List<PracticeTestAnswer> answers)
  {
    this.difficulty =
        difficulty;

    this.answers =
        List.copyOf(
            answers
        );
  }


  public PracticeTestDifficulty getDifficulty()
  {
    return difficulty;
  }


  public List<PracticeTestAnswer> getAnswers()
  {
    return Collections.unmodifiableList(
        answers
    );
  }


  public int getTotalQuestions()
  {
    return answers.size();
  }


  public int getCorrectCount()
  {
    int count =
        0;


    for (PracticeTestAnswer answer :
         answers)
    {
      if (answer.isCorrect())
      {
        count++;
      }
    }


    return count;
  }


  public int getBasicCorrectCount()
  {
    int count =
        0;


    for (PracticeTestAnswer answer :
         answers)
    {
      if (answer.matchesBasic())
      {
        count++;
      }
    }


    return count;
  }


  public int getCasinoReadyCount()
  {
    int count =
        0;


    for (PracticeTestAnswer answer :
         answers)
    {
      if (answer.matchesBasic()
          || answer.matchesSocial())
      {
        count++;
      }
    }


    return count;
  }
  

  public double getScorePercent()
  {
    if (answers.isEmpty())
      return 0.0;


    return 100.0
        * getCorrectCount()
        / answers.size();
  }


  public int getCorrectCount(
      PracticeTestCategory category)
  {
    int count =
        0;


    for (PracticeTestAnswer answer :
         answers)
    {
      if (answer.getQuestion()
              .getCategory()
          == category
          && answer.isCorrect())
      {
        count++;
      }
    }


    return count;
  }


  public int getQuestionCount(
      PracticeTestCategory category)
  {
    int count =
        0;


    for (PracticeTestAnswer answer :
         answers)
    {
      if (answer.getQuestion()
              .getCategory()
          == category)
      {
        count++;
      }
    }


    return count;
  }


  public List<PracticeTestAnswer>
      getIncorrectAnswers()
  {
    List<PracticeTestAnswer> incorrect =
        new ArrayList<>();


    for (PracticeTestAnswer answer :
         answers)
    {
      if (!answer.isCorrect())
      {
        incorrect.add(
            answer
        );
      }
    }


    return incorrect;
  }


  public Map<PracticeTestCategory, Integer>
      getCorrectCountsByCategory()
  {
    Map<PracticeTestCategory, Integer> result =
        new EnumMap<>(
            PracticeTestCategory.class
        );


    for (PracticeTestCategory category :
         PracticeTestCategory.values())
    {
      result.put(
          category,
          getCorrectCount(
              category
          )
      );
    }


    return result;
  }
}
