public class BasicStrategy implements HandStrategy
{
  @Override
  public HandAction chooseAction(
      PlayerDecisionContext context)
  {
    int decks = context.getDeckCount();

    // TODO: exact single- and double-deck strategy.
    if (decks == 1 || decks == 2)
    {
      throw new UnsupportedOperationException(
          "Basic strategy for "
          + decks
          + " deck(s) is not implemented yet."
      );
    }

    Hand hand = context.getPlayerHand();

    int dealer =
        context.getDealerUpCard()
            .getPointValue();

    // Order matters.
    //
    // Surrender must come before pair strategy because,
    // for example, 8,8 vs Ace is a surrender in an H17
    // multi-deck game.
    HandAction surrenderAction =
        getSurrenderAction(
            context,
            dealer
        );

    if (surrenderAction != null)
      return surrenderAction;

    if (isPair(hand))
    {
      HandAction pairAction =
          getPairAction(
              context,
              dealer
          );

      if (pairAction != null)
        return pairAction;
    }

    if (hand.isSoft())
    {
      return getSoftAction(
          context,
          dealer
      );
    }

    return getHardAction(
        context,
        dealer
    );
  }

  public HandAction chooseRecommendedAction(
    PlayerDecisionContext context)
  {
    PlayerDecisionContext recommendationContext =
        new PlayerDecisionContext(
            context.getPlayerHand(),
            context.getDealerUpCard(),
            context.getSettings(),
            context.getAllowedActions(),
            context.getAllowedActions(),
            context.cameFromSplit(),
            context.getDeckCount(),
            context.getWagerAmount()
        );

    return chooseAction(recommendationContext);
  }
  
  private boolean isPair(Hand hand)
  {
    if (hand.getNumCards() != 2)
      return false;

    return hand.getCard(0).getPointValue()
        == hand.getCard(1).getPointValue();
  }

  private HandAction getSurrenderAction(
      PlayerDecisionContext context,
      int dealer)
  {
    if (!context.isAvailable(
        HandAction.SURRENDER))
    {
      return null;
    }

    Hand hand =
        context.getPlayerHand();

    int total =
        hand.getScore();

    boolean h17 =
        context.getSettings()
            .dealerHitsSoft17();

    boolean pairOfEights =
        isPair(hand)
        && hand.getCard(0).getPointValue()
           == 8;

    // H17: 15, 16, 17 vs Ace;
    // Note: this includes surrender 8,8 vs Ace.
    if (h17 && dealer == 11 && !hand.isSoft())
    {
      if (total == 15
          || total == 16
          || total == 17)
      {
        return HandAction.SURRENDER;
      }
    }

    // S17/H17:
    // hard 15 vs dealer 10.
    if (!hand.isSoft()
        && total == 15
        && dealer == 10)
    {
      return HandAction.SURRENDER;
    }

    // hard 16 vs 9, 10, A,
    // except pair of 8s under normal S17 rules.
    if (!hand.isSoft()
        && total == 16
        && dealer >= 9)
    {
      if (!pairOfEights)
        return HandAction.SURRENDER;
    }

    return null;
  }

  private HandAction getPairAction(
      PlayerDecisionContext context,
      int dealer)
  {
    Hand hand =
        context.getPlayerHand();

    int pairValue =
        hand.getCard(0)
            .getPointValue();

    boolean das =
        context.getSettings()
            .doubleAfterSplit();

    switch (pairValue)
    {
      case 11: // A,A
        return splitOrHit(context);

      case 10:
        // Never split tens.
        return HandAction.STAND;

      case 9:
        if ((dealer >= 2 && dealer <= 6)
            || dealer == 8
            || dealer == 9)
        {
          return splitOrStand(context);
        }

        return HandAction.STAND;

      case 8:
        return splitOrHit(context);

      case 7:
        if (dealer >= 2 && dealer <= 7)
          return splitOrHit(context);

        return HandAction.HIT;

      case 6:
        if (dealer >= 3 && dealer <= 6)
          return splitOrHit(context);

        if (dealer == 2 && das)
          return splitOrHit(context);

        return HandAction.HIT;

      case 5:
        // Never split 5s.
        // Treat as hard 10.
        return getHardAction(
            context,
            dealer
        );

      case 4:
        if (das
            && (dealer == 5
                || dealer == 6))
        {
          return splitOrHit(context);
        }

        return HandAction.HIT;

      case 3:
      case 2:
        if (dealer >= 4 && dealer <= 7)
          return splitOrHit(context);

        if (das
            && (dealer == 2
                || dealer == 3))
        {
          return splitOrHit(context);
        }

        return HandAction.HIT;

      default:
        return null;
    }
  }

  private HandAction getSoftAction(
      PlayerDecisionContext context,
      int dealer)
  {
    int total =
        context.getPlayerHand()
            .getScore();

    boolean h17 =
        context.getSettings()
            .dealerHitsSoft17();

    if (total >= 20)
      return HandAction.STAND;

    // Soft 19
    if (total == 19)
    {
      if (h17 && dealer == 6)
        return doubleOrStand(context);

      return HandAction.STAND;
    }

    // Soft 18
    if (total == 18)
    {
      if (dealer >= 3 && dealer <= 6)
        return doubleOrStand(context);

      if (h17 && dealer == 2)
        return doubleOrStand(context);

      if (dealer == 2
          || dealer == 7
          || dealer == 8)
      {
        return HandAction.STAND;
      }

      return HandAction.HIT;
    }

    // Soft 17
    if (total == 17)
    {
      if (dealer >= 3 && dealer <= 6)
        return doubleOrHit(context);

      return HandAction.HIT;
    }

    // Soft 15 / 16
    if (total == 15 || total == 16)
    {
      if (dealer >= 4 && dealer <= 6)
        return doubleOrHit(context);

      return HandAction.HIT;
    }

    // Soft 13 / 14
    if (total == 13 || total == 14)
    {
      if (dealer == 5 || dealer == 6)
        return doubleOrHit(context);

      return HandAction.HIT;
    }

    return HandAction.HIT;
  }

  private HandAction getHardAction(
      PlayerDecisionContext context,
      int dealer)
  {
    int total =
        context.getPlayerHand()
            .getScore();

    boolean h17 =
        context.getSettings()
            .dealerHitsSoft17();

    if (total >= 17)
      return HandAction.STAND;

    if (total >= 13)
    {
      if (dealer >= 2 && dealer <= 6)
        return HandAction.STAND;

      return HandAction.HIT;
    }

    if (total == 12)
    {
      if (dealer >= 4 && dealer <= 6)
        return HandAction.STAND;

      return HandAction.HIT;
    }

    if (total == 11)
    {
      // H17 adds double 11 vs Ace.
      if (dealer == 11 && !h17)
        return HandAction.HIT;

      return doubleOrHit(context);
    }

    if (total == 10)
    {
      if (dealer >= 2 && dealer <= 9)
        return doubleOrHit(context);

      return HandAction.HIT;
    }

    if (total == 9)
    {
      if (dealer >= 3 && dealer <= 6)
        return doubleOrHit(context);

      return HandAction.HIT;
    }

    return HandAction.HIT;
  }

  private HandAction doubleOrHit(
      PlayerDecisionContext context)
  {
    if (context.isAvailable(
        HandAction.DOUBLE))
    {
      return HandAction.DOUBLE;
    }

    return HandAction.HIT;
  }

  private HandAction doubleOrStand(
      PlayerDecisionContext context)
  {
    if (context.isAvailable(
        HandAction.DOUBLE))
    {
      return HandAction.DOUBLE;
    }

    return HandAction.STAND;
  }

  private HandAction splitOrHit(
      PlayerDecisionContext context)
  {
    if (context.isAvailable(
        HandAction.SPLIT))
    {
      return HandAction.SPLIT;
    }

    return HandAction.HIT;
  }

  private HandAction splitOrStand(
      PlayerDecisionContext context)
  {
    if (context.isAvailable(
        HandAction.SPLIT))
    {
      return HandAction.SPLIT;
    }

    return HandAction.STAND;
  }
}
