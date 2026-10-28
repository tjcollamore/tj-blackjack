public class SocialStrategy implements HandStrategy
{
  /*
   * TJ's Social Strategy
   *
   * Research baseline:
   * - 8 decks
   * - dealer hits soft 17
   * - double after split allowed
   * - late surrender allowed
   *
   * Mental order:
   * 1. Surrender
   * 2. Pairs
   * 3. Soft hands
   * 4. Hard hands
   */

  @Override
  public HandAction chooseAction(
      PlayerDecisionContext context)
  {
    Hand hand =
        context.getPlayerHand();

    int dealer =
        context.getDealerUpCard()
            .getPointValue();

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

    return chooseAction(
        recommendationContext
    );
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

    if (hand.isSoft())
      return null;

    int total =
        hand.getScore();

    /*
     * SOCIAL RULE:
     * Surrender any hard 15 or 16 vs dealer 10.
     * This intentionally includes 8,8 vs 10.
     */
    if (dealer == 10
        && (total == 15
            || total == 16))
    {
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

    switch (pairValue)
    {
      case 11: // A,A
        return splitOrHit(
            context
        );

      case 10:
        return HandAction.STAND;

      case 9:
        if (dealer >= 2
            && dealer <= 9)
        {
          return splitOrStand(
              context
          );
        }

        return HandAction.STAND;

      case 8:
        /*
         * 8,8 vs 10 is already caught by surrender
         * when surrender is available.
         */
        return splitOrHit(
            context
        );

      case 7:
      case 6:
      case 3:
      case 2:
        if (dealer >= 2
            && dealer <= 7)
        {
          return splitOrHit(
              context
          );
        }

        return HandAction.HIT;

      case 5:
        return getHardAction(
            context,
            dealer
        );

      case 4:
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

    if (total >= 19)
      return HandAction.STAND;

    if (total == 18)
    {
      if (dealer >= 4
          && dealer <= 6)
      {
        return doubleOrStand(
            context
        );
      }

      if (dealer >= 9)
        return HandAction.HIT;

      return HandAction.STAND;
    }

    if (dealer >= 4
        && dealer <= 6)
    {
      return doubleOrHit(
          context
      );
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

    if (total >= 17)
      return HandAction.STAND;

    if (total >= 12)
    {
      if (dealer >= 2
          && dealer <= 6)
      {
        return HandAction.STAND;
      }

      return HandAction.HIT;
    }

    if (total == 11)
    {
      return doubleOrHit(
          context
      );
    }

    if (total == 10)
    {
      if (dealer >= 2
          && dealer <= 9)
      {
        return doubleOrHit(
            context
        );
      }

      return HandAction.HIT;
    }

    if (total == 9)
    {
      if (dealer >= 2
          && dealer <= 6)
      {
        return doubleOrHit(
            context
        );
      }

      return HandAction.HIT;
    }

    return HandAction.HIT;
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

