import java.util.ArrayList;
import java.util.List;

public class PracticeTestCostAnalyzer
{
  private static final int
      DEFAULT_TRIALS_PER_ACTION =
          10000;


  private final PracticeTestEvaluator evaluator =
      new PracticeTestEvaluator();


  public PracticeTestCostSummary analyze(
      PracticeTestResult result)
  {
    return analyze(
        result,
        DEFAULT_TRIALS_PER_ACTION
    );
  }


  public PracticeTestCostSummary analyze(
      PracticeTestResult result,
      int trialsPerAction)
  {
    if (trialsPerAction <= 0)
    {
      throw new IllegalArgumentException(
          "Trials per action must be greater than zero"
      );
    }


    List<PracticeTestMistakeCost> costs =
        new ArrayList<>();


    /*
     * Only mistakes under the selected test level
     * contribute to the test's mistake cost.
     *
     * Example:
     * a Social-only answer on CP receives full
     * credit, so it is not treated as a mistake.
     */
    for (PracticeTestAnswer answer :
         result.getIncorrectAnswers())
    {
      costs.add(
          analyzeMistake(
              answer,
              trialsPerAction
          )
      );
    }


    return new PracticeTestCostSummary(
        costs
    );
  }


  public PracticeTestMistakeCost analyzeMistake(
      PracticeTestAnswer answer,
      int trialsPerAction)
  {
    if (answer.isCorrect())
    {
      throw new IllegalArgumentException(
          "Cannot analyze a correct answer as a mistake"
      );
    }


    PracticeTestQuestion question =
        answer.getQuestion();


    PlayerDecisionContext context =
        evaluator.createContext(
            question
        );


    HandAction userAction =
        answer.getUserAction();


    if (!context.isAllowed(
            userAction))
    {
      throw new IllegalArgumentException(
          "User action is not legal for question "
          + question.getId()
          + ": "
          + userAction
      );
    }


    HandAction basicAction =
        answer.getEvaluation()
            .getBasicAction();


    SimulationEngine engine =
        new SimulationEngine(
            context.getSettings()
        );


    /*
     * Force each possible first action, then
     * continue with Basic Strategy.
     *
     * This isolates the expected-value impact
     * of the decision being tested.
     */
    DecisionAnalysis analysis =
        engine.analyzeDecision(
            context,
            new BasicStrategy(),
            trialsPerAction
        );


    SimulationResult basicResult =
        analysis.getResult(
            basicAction
        );


    SimulationResult userResult =
        analysis.getResult(
            userAction
        );


    if (basicResult == null
        || userResult == null)
    {
      throw new IllegalStateException(
          "Missing simulation result for "
          + question.getId()
      );
    }


    double basicEV =
        basicResult.getExpectedValue();

    double userEV =
        userResult.getExpectedValue();


    double costInWagerUnits =
        basicEV - userEV;


    /*
     * Monte Carlo noise can occasionally make
     * two very close actions appear reversed.
     *
     * For user-facing mistake cost, don't report
     * a negative cost.
     */
    double estimatedDollarCost =
        Math.max(
            0.0,
            costInWagerUnits
                * context.getWagerAmount()
        );


    return new PracticeTestMistakeCost(
        answer,
        basicAction,
        basicEV,
        userEV,
        estimatedDollarCost
    );
  }
}
