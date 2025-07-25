import java.text.NumberFormat;
import java.util.Locale;

public class Wager
{
  private int betAmount;
  private double payout;

  public Wager(int betAmount)
  {
    this.betAmount = betAmount;
    this.payout = 0;
  }

  public void win()
  {
    payout = betAmount * 2;
  }
  public void lose()
  {
    payout = 0;
  }
  public void push()
  {
    payout = betAmount;
  }
  public void blackjack()
  {
    payout = betAmount * GameOptions.BLACKJACK_PAYOUT;
  }

  
  public int getBetAmount()
  {
    return betAmount;
  }
  public void setBetAmount(int betAmount)
  {
    this.betAmount = betAmount;
  }
  public double getPayout()
  {
    return payout;
  }
  public String getFormattedPayout()
  {
    return getFormattedCurrency(getPayout());
  }

  public static String getFormattedCurrency(double amount)
  {
    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
    return currencyFormat.format(amount);
  }
  
  public void setPayout(double payout)
  {
    this.payout = payout;
  }
}