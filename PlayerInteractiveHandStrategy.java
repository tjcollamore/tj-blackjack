import java.util.Scanner;

public class PlayerInteractiveHandStrategy implements HandStrategy
{
  private IOutputter outputter;

  public PlayerInteractiveHandStrategy(IOutputter outputter)
  {
    this.outputter = outputter;
  }
  
  public void play(Hand hand, ShoeMgr shoeMgr)
  {
    Scanner reader = new Scanner(System.in);
    String instruction;

    do 
    {
      outputter.output("Do you want to stand (enter key) or hit (space bar)?");
      instruction = reader.nextLine();
      if (instruction.equals(""))
        break;
      if (!instruction.equals(" "))
      {
        outputter.output("Invalid input");
        continue;
      }
      Card card = shoeMgr.drawCard();
      outputter.output("Drawn card: " + card);
      hand.addCard(card);
    } while (!hand.isBusted());

    outputter.output("Your score is: " + hand.getScore());
    if (hand.isBlackjack())
      outputter.output("Blackjack!");
    else
    {
      if (hand.isBusted())
        outputter.output("Busted!");
    }
  }
}