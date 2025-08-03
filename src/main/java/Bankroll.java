import javafx.scene.control.Label;
import java.text.NumberFormat;
import java.util.Locale;
import javafx.stage.Stage;

public class Bankroll
{
  private double bankroll;
  private IOutputter outputter;
  private Label bankrollLabel;

  public Bankroll(double bankroll, Label bankrollLabel, IOutputter outputter)
  {
    this.bankroll = bankroll;
    this.bankrollLabel = bankrollLabel;
    this.outputter = outputter;
    output();
  }

  public void deduct(int deduction)
  {
    bankroll -= deduction;
    output();
  }

  public void deposit(double deposit)
  {
    bankroll += deposit;
    output();
  }

  private void output()
  {
    outputter.output("Bankroll: " + getFormattedBankroll());
    bankrollLabel.setText(getFormattedBankroll());
  }

  public static String getFormattedCurrency(double amount)
  {
    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
    return currencyFormat.format(amount);
  }

  public String getFormattedBankroll()
  {
    return getFormattedCurrency(bankroll);
  }

  public void addFundsIfBroke(Stage stage, double amount, int tableMinBet)
    {
      if (bankroll < tableMinBet)
      {
        outputter.output("You do not have enough funds. Withdrawing another " + getFormattedCurrency(amount)  + " from the ATM.");
        ChoiceDialog.show(stage, "You do not have enough funds.\nWithdrawing another " + getFormattedCurrency(amount) + " from the ATM.", "OK");
        deposit(amount);
      }
    }


  public double get()
  {
    return bankroll;
  }
}