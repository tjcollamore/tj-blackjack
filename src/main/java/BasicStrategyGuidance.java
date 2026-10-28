public class BasicStrategyGuidance
  implements StrategyGuidanceProvider
{
private final BasicStrategy basicStrategy =
    new BasicStrategy();


@Override
public StrategyGuidance getGuidance(
    PlayerDecisionContext context)
{
  Hand hand =
      context.getPlayerHand();

  int dealerValue =
      context.getDealerUpCard()
          .getPointValue();

  HandAction action =
      basicStrategy.chooseAction(
          context
      );

  if (action == HandAction.SURRENDER)
  {
    return getSurrenderGuidance(
        hand,
        dealerValue
    );
  }

  if (isPair(hand))
  {
    return getPairGuidance(
        hand,
        dealerValue,
        action,
        context
    );
  }

  if (hand.isSoft())
  {
    return getSoftGuidance(
        hand,
        dealerValue,
        action
    );
  }

  return getHardGuidance(
      hand,
      dealerValue,
      action
  );
}


private StrategyGuidance getSurrenderGuidance(
    Hand hand,
    int dealerValue)
{
  return new StrategyGuidance(
      HandAction.SURRENDER,
      "Surrender hard "
          + hand.getScore()
          + " against dealer "
          + dealerName(dealerValue)
          + ".",
      "Under the current deck and table rules, Basic Strategy prefers giving up half the wager rather than playing out this especially weak matchup."
  );
}


private StrategyGuidance getPairGuidance(
    Hand hand,
    int dealerValue,
    HandAction action,
    PlayerDecisionContext context)
{
  int pairValue =
      hand.getCard(0)
          .getPointValue();

  if (action == HandAction.SPLIT)
  {
    return new StrategyGuidance(
        action,
        "Split this pair against dealer "
            + dealerName(dealerValue)
            + ".",
        "Basic Strategy treats pairs separately because playing two starting hands can be more valuable than keeping the combined total."
    );
  }

  if (action == HandAction.STAND)
  {
    if (pairValue == 10)
    {
      return new StrategyGuidance(
          action,
          "Never split 10-value pairs; stand on 20.",
          "A total of 20 is already one of the strongest possible hands."
      );
    }

    return new StrategyGuidance(
        action,
        "Stand on this pair against dealer "
            + dealerName(dealerValue)
            + ".",
        "Under the current table rules, Basic Strategy prefers keeping the combined total."
    );
  }

  if (action == HandAction.DOUBLE)
  {
    return new StrategyGuidance(
        action,
        "Play this pair as hard "
            + hand.getScore()
            + " and double against dealer "
            + dealerName(dealerValue)
            + ".",
        "Basic Strategy does not split this pair here; its combined hard total is a favorable doubling hand."
    );
  }

  return new StrategyGuidance(
      HandAction.HIT,
      "Hit this pair against dealer "
          + dealerName(dealerValue)
          + ".",
      context.isAvailable(
          HandAction.SPLIT)
          ? "Under the current rules, Basic Strategy prefers improving the combined hand instead of splitting."
          : "With splitting unavailable, Basic Strategy continues by playing the combined hand."
  );
}


private StrategyGuidance getSoftGuidance(
    Hand hand,
    int dealerValue,
    HandAction action)
{
  int score =
      hand.getScore();

  if (action == HandAction.DOUBLE)
  {
    return new StrategyGuidance(
        action,
        "Double soft "
            + score
            + " against dealer "
            + dealerName(dealerValue)
            + ".",
        "The ace gives the hand flexibility while the current matchup is favorable enough to increase the wager."
    );
  }

  if (action == HandAction.STAND)
  {
    return new StrategyGuidance(
        action,
        "Stand on soft "
            + score
            + " against dealer "
            + dealerName(dealerValue)
            + ".",
        "Under the current rules, this soft total is strong enough to keep."
    );
  }

  return new StrategyGuidance(
      HandAction.HIT,
      "Hit soft "
          + score
          + " against dealer "
          + dealerName(dealerValue)
          + ".",
      "The ace gives you flexibility, and Basic Strategy prefers improving this hand rather than standing."
  );
}


private StrategyGuidance getHardGuidance(
    Hand hand,
    int dealerValue,
    HandAction action)
{
  int score =
      hand.getScore();

  if (action == HandAction.DOUBLE)
  {
    return new StrategyGuidance(
        action,
        "Double hard "
            + score
            + " against dealer "
            + dealerName(dealerValue)
            + ".",
        "Under the current rules, this matchup is favorable enough to increase the wager."
    );
  }

  if (action == HandAction.STAND)
  {
    String explanation;

    if (dealerValue >= 2
        && dealerValue <= 6)
    {
      explanation =
          "The dealer is showing a weak card, so avoid taking unnecessary bust risk.";
    }
    else
    {
      explanation =
          "Your total is strong enough that Basic Strategy prefers standing.";
    }

    return new StrategyGuidance(
        action,
        "Stand on hard "
            + score
            + " against dealer "
            + dealerName(dealerValue)
            + ".",
        explanation
    );
  }

  return new StrategyGuidance(
      HandAction.HIT,
      "Hit hard "
          + score
          + " against dealer "
          + dealerName(dealerValue)
          + ".",
      score <= 11
          ? "Your total is too low to stand, so take another card."
          : "Against this dealer card, Basic Strategy favors improving the hand rather than standing."
  );
}


private boolean isPair(
    Hand hand)
{
  if (hand.getNumCards() != 2)
    return false;

  return hand.getCard(0)
      .getPointValue()
      == hand.getCard(1)
          .getPointValue();
}


private String dealerName(
    int dealerValue)
{
  if (dealerValue == 11)
    return "ace";

  return String.valueOf(
      dealerValue
  );
}
}
