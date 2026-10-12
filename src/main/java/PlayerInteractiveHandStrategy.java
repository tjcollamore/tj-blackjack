import java.util.ArrayList;
import java.util.List;
import javafx.stage.Stage;

public class PlayerInteractiveHandStrategy implements HandStrategy
{
  private Stage stage;
  private final HandStrategy coachingStrategy = new BasicStrategy();
  
  public PlayerInteractiveHandStrategy(Stage stage)
  {
    this.stage = stage;
  }
  
  @Override
  public HandAction chooseAction(
      PlayerDecisionContext context)
  {
    List<String> options = new ArrayList<>();

    addIfAvailable(options, context, HandAction.STAND);
    addIfAvailable(options, context, HandAction.HIT);
    addIfAvailable(options, context, HandAction.DOUBLE);
    addIfAvailable(options, context, HandAction.SPLIT);
    addIfAvailable(options, context, HandAction.SURRENDER);

    String highlightedOption = null;

    if (context.getSettings().coachingEnabled())
    {
      HandAction recommendation =
          coachingStrategy.chooseAction(context);

      highlightedOption =
          recommendation.getDisplayText();
    }

    boolean showDecisionHelp = context.getPlayerHand().getNumCards() == 2;
    DecisionHelpButton helpButton = null;
    if (showDecisionHelp)
    {
        helpButton = new DecisionHelpButton(stage, context);
    }
          
    String choice;
    if (highlightedOption != null)
    {
      choice =
          ChoiceDialog.showWithHighlightedOption(
              stage,
              helpButton,
              highlightedOption,
              options.toArray(new String[0])
          );
    }
    else
    {
      choice =
          ChoiceDialog.showActionBar(
              stage,
              helpButton,
              null,
              options.toArray(new String[0])
          );
    }

    return HandAction.fromDisplayText(choice);
  }

  private void addIfAvailable(
    List<String> options,
    PlayerDecisionContext context,
    HandAction action)
  {
    if (context.isAvailable(action))
      options.add(action.getDisplayText());
  }
}
