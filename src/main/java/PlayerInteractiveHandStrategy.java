import javafx.stage.Stage;

public class PlayerInteractiveHandStrategy implements HandStrategy
{
  private Stage stage;

  public PlayerInteractiveHandStrategy(Stage stage)
  {
    this.stage = stage;
  }
  
  public HandAction chooseAction(Hand hand, Wager wager)
  { 
    String choice;

    // Double, Split, and Surrender are only offered with the initial hand
    if (hand.getNumCards() == 2)
    {
      if (wager.canDoubleOrSplit())
      {
        if (hand.isSplittable())
          choice = ChoiceDialog.show(stage, "", 
                                     HandAction.STAND.getDisplayText(), 
                                     HandAction.HIT.getDisplayText(), 
                                     HandAction.DOUBLE.getDisplayText(), 
                                     HandAction.SPLIT.getDisplayText(), 
                                     HandAction.SURRENDER.getDisplayText()); 
        else
          choice = ChoiceDialog.show(stage, "", 
                                     HandAction.STAND.getDisplayText(), 
                                     HandAction.HIT.getDisplayText(), 
                                     HandAction.DOUBLE.getDisplayText(), 
                                     HandAction.SURRENDER.getDisplayText()); 
      }
      else
        choice = ChoiceDialog.show(stage, "", 
                                   HandAction.STAND.getDisplayText(), 
                                   HandAction.HIT.getDisplayText(), 
                                   HandAction.SURRENDER.getDisplayText()); 
    }
    else
    {
      choice = ChoiceDialog.show(stage, "", 
                                  HandAction.STAND.getDisplayText(), 
                                  HandAction.HIT.getDisplayText());
    }
  
    return HandAction.fromDisplayText(choice);
  }
}
