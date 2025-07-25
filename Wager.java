import java.text.NumberFormat;
import java.util.Locale;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class Wager
{
  private int betAmount;
  private double payout;
  private boolean didDoubleDown = false;
  private double bankroll;
  private Label bankrollLabel;
  private IOutputter outputter;
  public boolean didSurrender = false;

  public Wager(int betAmount, Label bankrollLabel, IOutputter outputter)
  {
    this.betAmount = betAmount;
    this.payout = 0.0;
    this.bankroll = 0.0;
    this.bankrollLabel = bankrollLabel;
    this.outputter = outputter;
  }

  private void updateBankrollUi()
  {
    outputter.output("Player bankroll: " + getFormattedBankroll());
    bankrollLabel.setText(getFormattedBankroll());
  }
  
  public void addToBankroll(double amount)
  {
    bankroll += amount;
    updateBankrollUi();
  }

  public void addToBankrollIfBroke(Stage stage, double amount, int tableMinBet)
  {
    if (bankroll < tableMinBet)
    {
      outputter.output("You do not have enough funds. Withdrawing another $" + amount  + " from the ATM.");
      ChoiceDialog.show(stage, "You do not have enough funds.\nWithdrawing another $" + amount + " from the ATM.", "OK");
      addToBankroll(amount);
    }
  }

  public void determineBetAmount(Stage stage, int tableMinBet, int tableMaxBet)
  {
    // Reset double down flag for new round
    didDoubleDown = false;
    didSurrender = false;
    
    int maxBet = Math.min((int)bankroll, tableMaxBet);
    betAmount = BetDialog.show(stage, tableMinBet, maxBet);
    bankroll -= getBetAmount();
    updateBankrollUi();
  }
  
  public void win()
  {
    payout = betAmount * 2;
    bankroll += payout;
    updateBankrollUi();
  }
  public void lose()
  {
    payout = 0.0;
  }
  public void push()
  {
    payout = betAmount;
    bankroll += payout;
    updateBankrollUi();
  }
  
  public void blackjack()
  {
    payout = betAmount * GameOptions.BLACKJACK_PAYOUT;
    bankroll += payout;
    updateBankrollUi();
  }

  public void surrender()
  {
    payout = betAmount * 0.5;
    bankroll += payout;
    updateBankrollUi();
    didSurrender = true;
  }

  public boolean didSurrender()
  {
    return didSurrender;
  }

  public void doubleDown()
  {
    bankroll -= betAmount;
    betAmount *= 2;
    didDoubleDown = true;
    updateBankrollUi();
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
    return getFormattedCurrency(getPayout());
  }

  public String getFormattedBankroll()
  {
    return getFormattedCurrency(bankroll);
  }

  private static String getFormattedCurrency(double amount)
  {
    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
    return currencyFormat.format(amount);
  }
}