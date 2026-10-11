import javafx.scene.control.Label;
import java.text.NumberFormat;
import java.util.Locale;
import javafx.stage.Stage;

public class Bankroll
{
  private double bankroll;
  private IOutputter outputter;
  private Label bankrollLabel;
  private ChipStackView chipStackView;
  
  public Bankroll(double bankroll, Label bankrollLabel, ChipStackView chipStackView, IOutputter outputter)
  {
    this.bankroll = bankroll;
    this.bankrollLabel = bankrollLabel;
    this.chipStackView = chipStackView;
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
  chipStackView.setAmount(
      bankroll,
      ChipDisplayMode.BANKROLL
  );
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

  public void addFundsIfBroke(Stage stage, double amount, double atmFee, int tableMinBet)
    {
      if (bankroll < tableMinBet)
      {
        outputter.output("You do not have funds. Withdrawing another " + getFormattedCurrency(amount)  + " from the ATM.");
        ChoiceDialog.showWithMessage(stage, "You do not have funds.\nWithdrawing another " + getFormattedCurrency(amount) + " from the ATM (minus a " + getFormattedCurrency(atmFee) + " bank fee).", MessageTone.NEGATIVE, "OK");
        deposit(amount - atmFee);
      }
    }

  public void showWagerPreview(
    int wagerAmount)
  {
  double previewBankroll =
      Math.max(
          0,
          bankroll - wagerAmount
      );

  bankrollLabel.setText(
      getFormattedCurrency(
          previewBankroll
      )
  );

  chipStackView.setAmount(
      previewBankroll,
      ChipDisplayMode.BANKROLL
  );
  }

  public void showActualBankroll()
  {
  bankrollLabel.setText(
      getFormattedBankroll()
  );

  chipStackView.setAmount(
      bankroll,
      ChipDisplayMode.BANKROLL
  );
  }
  
  public double get()
  {
    return bankroll;
  }
}