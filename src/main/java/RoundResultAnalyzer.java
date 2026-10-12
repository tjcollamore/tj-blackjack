public class RoundResultAnalyzer
{
  public static RoundResultMessage analyze(Subround subround, Hand dealerHand, IOutputter outputter, GameSettings settings)
  {
    Hand playerHand = subround.getHand();
    Wager wager = subround.getWager();

    HandOutcome outcome =
      HandOutcomeAnalyzer.analyze(
          playerHand,
          dealerHand,
          subround.isNaturalBlackjack(),
          wager.didSurrender()
      );
    
    
    String message;
    MessageTone tone;
      
    switch (outcome)
    {
      case BLACKJACK:
      {
        wager.blackjack(
            settings.getBlackjackPayout()
        );

        message =
            "BLACKJACK!\nYOU WON "
            + wager.getFormattedPayout()
            + "!";

        tone =
            MessageTone.POSITIVE;

        break;
      }

      case WIN:
      {
        wager.win();

        if (dealerHand.isBusted())
        {
          message =
              "DEALER BUSTED!\nYOU WON "
              + wager.getFormattedPayout()
              + "!";
        }
        else
        {
          message =
              "YOU WON "
              + wager.getFormattedPayout()
              + "!";
        }

        tone =
            MessageTone.POSITIVE;

        break;
      }

      case LOSS:
      {
        wager.lose();

        if (dealerHand.isBlackjack())
          message = "DEALER BLACKJACK!";
        else if (playerHand.isBusted())
          message = "   BUSTED!   ";  // Cover up felt "DEALER PAYS 3:2"
        else
          message = "Dealer Wins";

        tone =
            MessageTone.NEGATIVE;

        break;
      }

      case PUSH:
      {
        wager.push();

        if (subround.isNaturalBlackjack()
            && dealerHand.isBlackjack())
        {
          message =
              "PUSH!\nRecouped "
              + wager.getFormattedPayout();
        }
        else
        {
          message =
              "Push\nRecouped "
              + wager.getFormattedPayout();
        }

        tone =
            MessageTone.NEUTRAL;

        break;
      }

      case SURRENDER:
      {
        message =
            "SURRENDERED!\nRecouped "
            + wager.getFormattedPayout();

        tone =
            MessageTone.NEGATIVE;

        break;
      }

      default:
        throw new IllegalStateException(
            "Unexpected outcome: "
            + outcome
        );
    }
    outputter.output(message);
    return new RoundResultMessage(message, tone);
  }
}