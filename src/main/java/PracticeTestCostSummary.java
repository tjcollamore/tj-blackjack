import java.util.Comparator;
import java.util.List;

public class PracticeTestCostSummary
{
  private final List<PracticeTestMistakeCost>
      mistakeCosts;


  public PracticeTestCostSummary(
      List<PracticeTestMistakeCost> mistakeCosts)
  {
    this.mistakeCosts =
        List.copyOf(
            mistakeCosts
        );
  }


  public List<PracticeTestMistakeCost>
      getMistakeCosts()
  {
    return mistakeCosts;
  }


  public double getTotalEstimatedCost()
  {
    double total =
        0.0;


    for (PracticeTestMistakeCost mistake :
         mistakeCosts)
    {
      total +=
          mistake.getEstimatedDollarCost();
    }


    return total;
  }


  public PracticeTestMistakeCost
      getMostExpensiveMistake()
  {
    return mistakeCosts
        .stream()
        .max(
            Comparator.comparingDouble(
                PracticeTestMistakeCost
                    ::getEstimatedDollarCost
            )
        )
        .orElse(
            null
        );
  }


  public PracticeTestMistakeCost
      getLeastExpensiveMistake()
  {
    return mistakeCosts
        .stream()
        .min(
            Comparator.comparingDouble(
                PracticeTestMistakeCost
                    ::getEstimatedDollarCost
            )
        )
        .orElse(
            null
        );
  }
}
