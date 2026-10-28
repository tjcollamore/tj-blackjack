import java.util.ArrayList;
import java.util.List;
import javafx.stage.Stage;

public class PlayerInteractiveHandStrategy implements HandStrategy
{
  private Stage stage;
  private BlackjackSceneCreator sceneCreator;
    
  private final BasicStrategy basicStrategy =
      new BasicStrategy();

  private final SocialStrategy socialStrategy =
      new SocialStrategy();

    public PlayerInteractiveHandStrategy(
        Stage stage,
        BlackjackSceneCreator sceneCreator)
    {
      this.stage =
          stage;
    
      this.sceneCreator =
          sceneCreator;
    }
    
  @Override
  public HandAction chooseAction(
      PlayerDecisionContext context)
  {
    List<String> options =
        new ArrayList<>();

    addIfAvailable(
        options,
        context,
        HandAction.STAND
    );

    addIfAvailable(
        options,
        context,
        HandAction.HIT
    );

    addIfAvailable(
        options,
        context,
        HandAction.DOUBLE
    );

    addIfAvailable(
        options,
        context,
        HandAction.SPLIT
    );

    addIfAvailable(
        options,
        context,
        HandAction.SURRENDER
    );

    String highlightedOption =
        null;

    if (context.getSettings()
        .coachingEnabled())
    {
      HandAction recommendation =
          getSelectedStrategy(
              context
          ).chooseAction(
              context
          );

      highlightedOption =
          recommendation
              .getDisplayText();
    }

    boolean showDecisionHelp =
        context.getPlayerHand()
            .getNumCards() == 2;

    DecisionHelpButton helpButton =
        null;

    if (showDecisionHelp)
    {
      helpButton =
          new DecisionHelpButton(
              stage,
              context
          );
    }
      
    updateStrategyGuidance(context);

    String choice;

    if (highlightedOption != null)
    {
      choice =
          ChoiceDialog
              .showWithHighlightedOption(
                  stage,
                  helpButton,
                  highlightedOption,
                  options.toArray(
                      new String[0]
                  )
              );
    }
    else
    {
      choice =
          ChoiceDialog.showActionBar(
              stage,
              helpButton,
              null,
              options.toArray(
                  new String[0]
              )
          );
    }

    sceneCreator.hideStrategyGuidance();
      
    return HandAction
        .fromDisplayText(
            choice
        );
  }

  private HandStrategy getSelectedStrategy(
      PlayerDecisionContext context)
  {
    if (context.getSettings()
        .getStrategyMode()
        == StrategyMode.SOCIAL)
    {
      return socialStrategy;
    }

    return basicStrategy;
  }

    private void updateStrategyGuidance(
        PlayerDecisionContext context)
    {
      if (!context.getSettings()
          .strategyGuidanceEnabled())
      {
        sceneCreator.hideStrategyGuidance();
        return;
      }

      StrategyGuidanceProvider provider =
          StrategyGuidanceFactory.create(
              context.getSettings()
                  .getStrategyMode()
          );

      StrategyGuidance guidance =
          provider.getGuidance(
              context
          );

      sceneCreator.showStrategyGuidance(
          guidance
      );
    }
    
  private void addIfAvailable(
      List<String> options,
      PlayerDecisionContext context,
      HandAction action)
  {
    if (context.isAvailable(
            action))
    {
      options.add(
          action.getDisplayText()
      );
    }
  }
}
