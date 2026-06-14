import javafx.stage.Stage;

public class PlayerInteractiveHandStrategy implements HandStrategy
{
  private Stage stage;

  public PlayerInteractiveHandStrategy(Stage stage)
  {
    this.stage = stage;
  }
  
  public void play(Hand hand, Wager wager, ShoeMgr shoeMgr)
  { 
    String choice;
    if (wager.canDoubleOrSplit())
    {
      if (hand.isSplittable())
        choice = ChoiceDialog.show(stage, "What do you want to do?", "Stand", "Hit", "Double", "Split", "⚑"); //🏳(U+1F3F3)
      else
        choice = ChoiceDialog.show(stage, "What do you want to do?", "Stand", "Hit", "Double", "⚑");
    }
    else
      choice = ChoiceDialog.show(stage, "What do you want to do?", "Stand", "Hit", "⚑");

    if (choice.equals("⚑"))
    {
      wager.surrender();
      return;  
    }
    if (choice.equals("Split"))
    {
      wager.split(hand);
      return;
    }
    if (choice.equals("Stand"))
      return;
    if (choice.equals("Double"))
      wager.doubleDown();
    
    do 
    {
      Card card = shoeMgr.drawCard();
      hand.addCard(card);
    } while (!wager.didDoubleDown() &&
        !hand.isBusted() && 
        !hand.is21() && 
        ChoiceDialog.show(stage, "What do you want to do?", "Stand", "Hit").equals("Hit"));
  }
}