import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class PracticeTestCostAnalyzerTest
{
  private final PracticeTestEvaluator evaluator =
      new PracticeTestEvaluator();

  private final PracticeTestScorer scorer =
      new PracticeTestScorer();


  @Test
  public void testPerfectTestHasNoMistakeCosts()
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


    PracticeTestCostSummary summary =
        new PracticeTestCostAnalyzer()
            .analyze(
                result,
                10
            );


    assertTrue(
        summary.getMistakeCosts()
            .isEmpty()
    );

    assertEquals(
        0.0,
        summary.getTotalEstimatedCost()
    );

    assertNull(
        summary.getMostExpensiveMistake()
    );
  }


  @Test
  public void testRejectsZeroTrials()
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


    assertThrows(
        IllegalArgumentException.class,
        () ->
            new PracticeTestCostAnalyzer()
                .analyze(
                    result,
                    0
                )
    );
  }


  @Test
  public void testCorrectAnswerCannotBeAnalyzedAsMistake()
  {
    PracticeTestSession session =
        new PracticeTestSessionBuilder()
            .build(
                PracticeTestDifficulty.HONORS,
                12345L
            );


    PracticeTestQuestion question =
        session.getQuestion(
            0
        );


    HandAction basicAction =
        evaluator.getBasicAction(
            question
        );


    PracticeTestEvaluator.Evaluation evaluation =
        evaluator.evaluate(
            question,
            basicAction,
            PracticeTestDifficulty.HONORS
        );


    PracticeTestAnswer answer =
        new PracticeTestAnswer(
            question,
            basicAction,
            evaluation
        );


    assertThrows(
        IllegalArgumentException.class,
        () ->
            new PracticeTestCostAnalyzer()
                .analyzeMistake(
                    answer,
                    10
                )
    );
  }
}
