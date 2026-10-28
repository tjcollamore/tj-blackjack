import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class PracticeTestEvaluatorTest
{
  private final PracticeTestEvaluator evaluator =
      new PracticeTestEvaluator();


  @Test
  public void testBasicAndSocialAgree()
  {
    PracticeTestQuestion question =
        findQuestion(
            "HARD_11_VS_6"
        );


    assertEquals(
        HandAction.DOUBLE,
        evaluator.getBasicAction(
            question
        )
    );

    assertEquals(
        HandAction.DOUBLE,
        evaluator.getSocialAction(
            question
        )
    );
  }


  @Test
  public void testKnownSocialDeviation()
  {
    PracticeTestQuestion question =
        findQuestion(
            "HARD_12_VS_2"
        );


    assertEquals(
        HandAction.HIT,
        evaluator.getBasicAction(
            question
        )
    );

    assertEquals(
        HandAction.STAND,
        evaluator.getSocialAction(
            question
        )
    );
  }


  @Test
  public void testCPAcceptsBasicAnswer()
  {
    PracticeTestQuestion question =
        findQuestion(
            "HARD_12_VS_2"
        );


    PracticeTestEvaluator.Evaluation result =
        evaluator.evaluate(
            question,
            HandAction.HIT,
            PracticeTestDifficulty.CP
        );


    assertTrue(
        result.isCorrect()
    );

    assertTrue(
        result.matchesBasic()
    );

    assertFalse(
        result.matchesSocial()
    );

    assertFalse(
        result.isValidSocialAlternative()
    );
  }


  @Test
  public void testCPAcceptsSocialAnswer()
  {
    PracticeTestQuestion question =
        findQuestion(
            "HARD_12_VS_2"
        );


    PracticeTestEvaluator.Evaluation result =
        evaluator.evaluate(
            question,
            HandAction.STAND,
            PracticeTestDifficulty.CP
        );


    assertTrue(
        result.isCorrect()
    );

    assertFalse(
        result.matchesBasic()
    );

    assertTrue(
        result.matchesSocial()
    );

    assertTrue(
        result.isValidSocialAlternative()
    );
  }


  @Test
  public void testHonorsRequiresBasicAnswer()
  {
    PracticeTestQuestion question =
        findQuestion(
            "HARD_12_VS_2"
        );


    PracticeTestEvaluator.Evaluation result =
        evaluator.evaluate(
            question,
            HandAction.STAND,
            PracticeTestDifficulty.HONORS
        );


    assertFalse(
        result.isCorrect()
    );

    assertFalse(
        result.matchesBasic()
    );

    assertTrue(
        result.matchesSocial()
    );

    assertTrue(
        result.isValidSocialAlternative()
    );
  }


  @Test
  public void testHonorsAcceptsBasicAnswer()
  {
    PracticeTestQuestion question =
        findQuestion(
            "HARD_12_VS_2"
        );


    PracticeTestEvaluator.Evaluation result =
        evaluator.evaluate(
            question,
            HandAction.HIT,
            PracticeTestDifficulty.HONORS
        );


    assertTrue(
        result.isCorrect()
    );

    assertTrue(
        result.matchesBasic()
    );
  }


  @Test
  public void testWrongUnderBothStrategies()
  {
    PracticeTestQuestion question =
        findQuestion(
            "HARD_11_VS_6"
        );


    PracticeTestEvaluator.Evaluation result =
        evaluator.evaluate(
            question,
            HandAction.STAND,
            PracticeTestDifficulty.CP
        );


    assertFalse(
        result.isCorrect()
    );

    assertFalse(
        result.matchesBasic()
    );

    assertFalse(
        result.matchesSocial()
    );

    assertFalse(
        result.isValidSocialAlternative()
    );
  }


  @Test
  public void testPair88Vs10Deviation()
  {
    PracticeTestQuestion question =
        findQuestion(
            "PAIR_88_VS_10"
        );


    assertEquals(
        HandAction.SPLIT,
        evaluator.getBasicAction(
            question
        )
    );

    assertEquals(
        HandAction.SURRENDER,
        evaluator.getSocialAction(
            question
        )
    );
  }


    @Test
    public void testQuestionBankSizes()
    {
      assertEquals(
          64,
          PracticeTestQuestionBank
              .getQuestions()
              .size()
      );

      assertEquals(
          48,
          PracticeTestQuestionBank
              .getChallengingQuestions()
              .size()
      );

      assertEquals(
          16,
          PracticeTestQuestionBank
              .getControlQuestions()
              .size()
      );
    }
    
  private PracticeTestQuestion findQuestion(
      String id)
  {
    for (PracticeTestQuestion question :
         PracticeTestQuestionBank
             .getQuestions())
    {
      if (question.getId()
          .equals(id))
      {
        return question;
      }
    }


    fail(
        "Question not found: "
        + id
    );

    return null;
  }
    @Test
    public void testNonPairCannotSplit()
    {
      PracticeTestQuestion question =
          findQuestion(
              "HARD_11_VS_6"
          );


      PlayerDecisionContext context =
          evaluator.createContext(
              question
          );


      assertFalse(
          context.isAllowed(
              HandAction.SPLIT
          )
      );
    }


    @Test
    public void testPairCanSplit()
    {
      PracticeTestQuestion question =
          findQuestion(
              "PAIR_99_VS_7"
          );


      PlayerDecisionContext context =
          evaluator.createContext(
              question
          );


      assertTrue(
          context.isAllowed(
              HandAction.SPLIT
          )
      );
    }


    @Test
    public void testTwoCardHandCanDouble()
    {
      PracticeTestQuestion question =
          findQuestion(
              "HARD_11_VS_6"
          );


      PlayerDecisionContext context =
          evaluator.createContext(
              question
          );


      assertTrue(
          context.isAllowed(
              HandAction.DOUBLE
          )
      );
    }


    @Test
    public void testPracticeQuestionsAllowSurrender()
    {
      PracticeTestQuestion question =
          findQuestion(
              "HARD_16_VS_10"
          );


      PlayerDecisionContext context =
          evaluator.createContext(
              question
          );


      assertTrue(
          context.isAllowed(
              HandAction.SURRENDER
          )
      );
    }
    
}
