import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class PracticeTestSessionBuilderTest
{
  private final PracticeTestSessionBuilder builder =
      new PracticeTestSessionBuilder();


  @Test
  public void testSessionHas50Questions()
  {
    PracticeTestSession session =
        builder.build(
            PracticeTestDifficulty.CP,
            12345L
        );


    assertEquals(
        50,
        session.size()
    );
  }


  @Test
  public void testSessionHas40ChallengingQuestions()
  {
    PracticeTestSession session =
        builder.build(
            PracticeTestDifficulty.CP,
            12345L
        );


    int count =
        0;


    for (PracticeTestQuestion question :
         session.getQuestions())
    {
      if (question.isChallenging())
      {
        count++;
      }
    }


    assertEquals(
        40,
        count
    );
  }


  @Test
  public void testSessionHas10ControlQuestions()
  {
    PracticeTestSession session =
        builder.build(
            PracticeTestDifficulty.CP,
            12345L
        );


    int count =
        0;


    for (PracticeTestQuestion question :
         session.getQuestions())
    {
      if (!question.isChallenging())
      {
        count++;
      }
    }


    assertEquals(
        10,
        count
    );
  }


  @Test
  public void testChallengingCategoryDistribution()
  {
    PracticeTestSession session =
        builder.build(
            PracticeTestDifficulty.HONORS,
            12345L
        );


    assertEquals(
        12,
        countChallenging(
            session,
            PracticeTestCategory.HARD
        )
    );

    assertEquals(
        10,
        countChallenging(
            session,
            PracticeTestCategory.SOFT
        )
    );

    assertEquals(
        10,
        countChallenging(
            session,
            PracticeTestCategory.PAIR
        )
    );

    assertEquals(
        8,
        countChallenging(
            session,
            PracticeTestCategory.SURRENDER
        )
    );
  }


  @Test
  public void testSessionHasNoDuplicateQuestions()
  {
    PracticeTestSession session =
        builder.build(
            PracticeTestDifficulty.CP,
            12345L
        );


    Set<String> ids =
        new HashSet<>();


    for (PracticeTestQuestion question :
         session.getQuestions())
    {
      assertTrue(
          ids.add(
              question.getId()
          ),
          "Duplicate question: "
          + question.getId()
      );
    }
  }


  @Test
  public void testSeedProducesSameTest()
  {
    PracticeTestSession first =
        builder.build(
            PracticeTestDifficulty.CP,
            98765L
        );

    PracticeTestSession second =
        builder.build(
            PracticeTestDifficulty.CP,
            98765L
        );


    for (int i = 0;
         i < first.size();
         i++)
    {
      assertEquals(
          first.getQuestion(i)
              .getId(),
          second.getQuestion(i)
              .getId()
      );
    }
  }


  @Test
  public void testDifficultyIsPreserved()
  {
    PracticeTestSession session =
        builder.build(
            PracticeTestDifficulty.HONORS,
            12345L
        );


    assertEquals(
        PracticeTestDifficulty.HONORS,
        session.getDifficulty()
    );
  }


  @Test
  public void testQuestionBankIdsAreUnique()
  {
    Set<String> ids =
        new HashSet<>();


    for (PracticeTestQuestion question :
         PracticeTestQuestionBank
             .getQuestions())
    {
      assertTrue(
          ids.add(
              question.getId()
          ),
          "Duplicate bank ID: "
          + question.getId()
      );
    }
  }


  private int countChallenging(
      PracticeTestSession session,
      PracticeTestCategory category)
  {
    int count =
        0;


    for (PracticeTestQuestion question :
         session.getQuestions())
    {
      if (question.isChallenging()
          && question.getCategory()
              == category)
      {
        count++;
      }
    }


    return count;
  }
}
