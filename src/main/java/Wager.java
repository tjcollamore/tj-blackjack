public class Wager
{
  private int betAmount;
  private double payout;
  private Bankroll bankroll;
  private boolean didSurrender = false;

  public Wager(Bankroll bankroll, int betAmount)
  {
    this.betAmount = betAmount;
    this.payout = 0.0;
    this.bankroll = bankroll;
  }
  
  public void place()
  { 
    bankroll.deduct(betAmount);
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
  
  public void blackjack(GameSettings.BlackjackPayout blackjackPayout)
  {
    payout = betAmount * blackjackPayout.getTotalReturnMultiplier();
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

  public void doubleDown()
  {
    bankroll.deduct(betAmount);
    betAmount *= 2;
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
    return Bankroll.getFormattedCurrency(getPayout());
  }

  public boolean hasFundsToDoubleOrSplit()
  {
    return bankroll.get() >= getBetAmount();
  }

  public double getProfit()
  {
    return payout - betAmount;
  }

  public String getFormattedProfit()
  {
    return Bankroll.getFormattedCurrency(getProfit());
  }
}