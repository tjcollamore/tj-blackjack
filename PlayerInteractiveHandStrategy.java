import javafx.stage.Stage;

public class PlayerInteractiveHandStrategy implements HandStrategy
{
  private Stage stage;

  public PlayerInteractiveHandStrategy(Stage stage)
  {
    this.stage = stage;
  }
  
  public void play(Hand hand, ShoeMgr shoeMgr)
  { 
    while (!hand.isBusted() && 
           !hand.is21() && 
           ChoiceDialog.show(stage, "What do you want to do?", "Stand", "Hit").equals("Hit"))
    {
      Card card = shoeMgr.drawCard();
      hand.addCard(card);
    }
  }
}