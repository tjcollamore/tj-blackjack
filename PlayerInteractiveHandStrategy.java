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
    String choice = ChoiceDialog.show(stage, "What do you want to do?", "Stand", "Hit", "Double", "Surrender");


    if (choice.equals("Surrender"))
    {
      wager.surrender();
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