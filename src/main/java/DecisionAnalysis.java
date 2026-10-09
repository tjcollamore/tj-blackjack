import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class DecisionAnalysis
{
  private final Map<
      HandAction,
      SimulationResult
  > results =
      new EnumMap<>(HandAction.class);

  public void addResult(
      HandAction action,
      SimulationResult result)
  {
    results.put(action, result);
  }

  public SimulationResult getResult(
      HandAction action)
  {
    return results.get(action);
  }

  public boolean hasResult(
      HandAction action)
  {
    return results.containsKey(action);
  }

  public Map<HandAction, SimulationResult>
      getResults()
  {
    return Collections.unmodifiableMap(
        results
    );
  }
    
public HandAction getBestAction()
{
  HandAction bestAction = null;
  double bestEV =
      Double.NEGATIVE_INFINITY;

  for (Map.Entry<
      HandAction,
      SimulationResult
  > entry : results.entrySet())
  {
    double ev =
        entry.getValue()
            .getExpectedValue();

    if (ev > bestEV)
    {
      bestEV = ev;
      bestAction = entry.getKey();
    }
  }

  return bestAction;
}

}
