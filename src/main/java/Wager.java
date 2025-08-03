import javafx.stage.Stage;

public class Wager
{
  private int betAmount;
  private double payout;
  private boolean didDoubleDown = false;
  private Bankroll bankroll;
  private IOutputter outputter;
  private boolean didSurrender = false;
  private boolean didSplit = false;

  public Wager(Bankroll bankroll, int betAmount, IOutputter outputter)
  {
    this.betAmount = betAmount;
    this.payout = 0.0;
    this.bankroll = bankroll;
    this.outputter = outputter;
  }
  
  public int determineBetAmount(Stage stage, int tableMinBet, int tableMaxBet)
  { 
    int maxBet = Math.min((int)bankroll.get(), tableMaxBet);
    betAmount = BetDialog.show(stage, betAmount, tableMinBet, maxBet);
    bankroll.deduct(betAmount);
    return betAmount;
  }
  
  public void win()
  {
    payout = betAmount * 2;
    bankroll.deposit(payout);
  }
  public void lose()
  {
    payout = 0.0;
  }
  public void push()
  {
    payout = betAmount;
    bankroll.deposit(payout);
  }
  
  public void blackjack()
  {
    payout = betAmount * GameOptions.BLACKJACK_PAYOUT;
    bankroll.deposit(payout);
  }

  public void surrender()
  {
    payout = betAmount * 0.5;
    bankroll.deposit(payout);
    didSurrender = true;
  }

  public boolean didSurrender()
  {
    return didSurrender;
  }

  public void split()
  {
    didSplit = true;
    bankroll.deduct(betAmount);
  }
  
  public boolean didSplit()
  {
    return didSplit;
  }

  public void reset()
  {
    didSplit = false;
    didDoubleDown = false;
    didSurrender = false;
    payout = 0.0;
  }

  public void doubleDown()
  {
    bankroll.deduct(betAmount);
    betAmount *= 2;
    didDoubleDown = true;
  }

  public boolean didDoubleDown()
  {
    return didDoubleDown;
  }
  
  public int getBetAmount()
  {
    return betAmount;
  }

  public double getPayout()
  {
    return payout;
  }
  
  public String getFormattedPayout()
  {
    return bankroll.getFormattedCurrency(getPayout());
  }

  public boolean canDoubleOrSplit()
  {
    return bankroll.get() > getBetAmount();
  }
}