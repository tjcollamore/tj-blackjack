import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class PracticeTestScorerTest
{
  private final PracticeTestEvaluator evaluator =
      new PracticeTestEvaluator();

  private final PracticeTestScorer scorer =
      new PracticeTestScorer();


  @Test
  public void testPerfectBasicHonorsScore()
  {
    PracticeTestSession session =
        new PracticeTestSessionBuilder()
            .build(
                PracticeTestDifficulty.HONORS,
                12345L
            );


    List<HandAction> answers =
        new ArrayList<>();


    for (PracticeTestQuestion question :
         session.getQuestions())
    {
      answers.add(
          evaluator.getBasicAction(
              question
          )
      );
    }


    PracticeTestResult result =
        scorer.score(
            session,
            answers
        );


    assertEquals(
        50,
        result.getCorrectCount()
    );

    assertEquals(
        50,
        result.getBasicCorrectCount()
    );

    assertEquals(
        100.0,
        result.getScorePercent()
    );
  }


  @Test
  public void testPerfectSocialCPScore()
  {
    PracticeTestSession session =
        new PracticeTestSessionBuilder()
            .build(
                PracticeTestDifficulty.CP,
                12345L
            );


    List<HandAction> answers =
        new ArrayList<>();


    for (PracticeTestQuestion question :
         session.getQuestions())
    {
      answers.add(
          evaluator.getSocialAction(
              question
          )
      );
    }


    PracticeTestResult result =
        scorer.score(
            session,
            answers
        );


    assertEquals(
        50,
        result.getCorrectCount()
    );

    assertEquals(
        50,
        result.getCasinoReadyCount()
    );
  }


  @Test
  public void testSocialIsNotPerfectHonors()
  {
    PracticeTestSession session =
        new PracticeTestSessionBuilder()
            .build(
                PracticeTestDifficulty.HONORS,
                12345L
            );


    List<HandAction> answers =
        new ArrayList<>();


    for (PracticeTestQuestion question :
         session.getQuestions())
    {
      answers.add(
          evaluator.getSocialAction(
              question
          )
      );
    }


    PracticeTestResult result =
        scorer.score(
            session,
            answers
        );


    assertTrue(
        result.getCorrectCount()
        < 50
    );

    assertEquals(
        50,
        result.getCasinoReadyCount()
    );
  }


  @Test
  public void testIncorrectAnswersAreTracked()
  {
    PracticeTestSession session =
        new PracticeTestSessionBuilder()
            .build(
                PracticeTestDifficulty.HONORS,
                12345L
            );


    List<HandAction> answers =
        new ArrayList<>();


    for (PracticeTestQuestion question :
         session.getQuestions())
    {
      HandAction basic =
          evaluator.getBasicAction(
              question
          );


      HandAction answer =
          basic == HandAction.HIT
              ? HandAction.STAND
              : HandAction.HIT;


      answers.add(
          answer
      );
    }


    PracticeTestResult result =
        scorer.score(
            session,
            answers
        );


    assertTrue(
        result.getIncorrectAnswers()
            .size()
        > 0
    );
  }


  @Test
  public void testRejectsWrongNumberOfAnswers()
  {
    PracticeTestSession session =
        new PracticeTestSessionBuilder()
            .build(
                PracticeTestDifficulty.CP,
                12345L
            );


    assertThrows(
        IllegalArgumentException.class,
        () ->
            scorer.score(
                session,
                List.of(
                    HandAction.HIT
                )
            )
    );
  }
}
