public class SocialStrategyGuidance
    implements StrategyGuidanceProvider
{
  @Override
  public StrategyGuidance getGuidance(
      PlayerDecisionContext context)
  {
    Hand hand =
        context.getPlayerHand();

    int dealerValue =
        context.getDealerUpCard()
            .getPointValue();

    if (shouldSurrender(
        hand,
        dealerValue,
        context))
    {
      return new StrategyGuidance(
          HandAction.SURRENDER,
          "Surrender hard 15 or 16 against a dealer 10.",
          "These are especially weak hands against a strong dealer card, so surrendering limits the loss."
      );
    }

    StrategyGuidance pairGuidance =
        getPairGuidance(
            hand,
            dealerValue,
            context
        );

    if (pairGuidance != null)
      return pairGuidance;

    if (hand.isSoft())
    {
      return getSoftGuidance(
          hand,
          dealerValue,
          context
      );
    }

    return getHardGuidance(
        hand,
        dealerValue,
        context
    );
  }


  private boolean shouldSurrender(
      Hand hand,
      int dealerValue,
      PlayerDecisionContext context)
  {
    return context.isAvailable(
               HandAction.SURRENDER
           )
        && !hand.isSoft()
        && dealerValue == 10
        && (hand.getScore() == 15
            || hand.getScore() == 16);
  }


  private StrategyGuidance getPairGuidance(
      Hand hand,
      int dealerValue,
      PlayerDecisionContext context)
  {
    if (hand.getNumCards() != 2)
      return null;

    if (hand.getCard(0).getRank()
        != hand.getCard(1).getRank())
    {
      return null;
    }

    int value =
        hand.getCard(0)
            .getPointValue();


    // A,A
    if (value == 11)
    {
      if (context.isAvailable(
          HandAction.SPLIT))
      {
        return new StrategyGuidance(
            HandAction.SPLIT,
            "Always split aces.",
            "Two aces are much stronger as separate hands than as one soft 12."
        );
      }

      return new StrategyGuidance(
          HandAction.HIT,
          "Split aces whenever splitting is available.",
          "Since splitting is unavailable, play the hand as soft 12 and take another card."
      );
    }


    // 10,10
    if (value == 10)
    {
      return new StrategyGuidance(
          HandAction.STAND,
          "Never split 10s.",
          "A total of 20 is already one of the strongest hands in blackjack."
      );
    }


    // 9,9
    if (value == 9)
    {
      if (dealerValue >= 2
          && dealerValue <= 9)
      {
        if (context.isAvailable(
            HandAction.SPLIT))
        {
          return new StrategyGuidance(
              HandAction.SPLIT,
              "Split 9s against dealer 2–9.",
              "Against these dealer cards, two separate 9s have more potential than standing on 18."
          );
        }

        return new StrategyGuidance(
            HandAction.STAND,
            "Split 9s against dealer 2–9 when splitting is available.",
            "Since splitting is unavailable, keep the hard 18 and stand."
        );
      }

      return new StrategyGuidance(
          HandAction.STAND,
          "Stand on a pair of 9s against dealer 10 or ace.",
          "Against the strongest dealer cards, keep the made 18 instead of splitting."
      );
    }


    // 8,8
    if (value == 8)
    {
      if (context.isAvailable(
          HandAction.SPLIT))
      {
        return new StrategyGuidance(
            HandAction.SPLIT,
            "Always split 8s unless you can surrender 16 against 10.",
            "A hard 16 is a very weak hand, so splitting gives you two chances to improve."
        );
      }

      return new StrategyGuidance(
          HandAction.HIT,
          "Split 8s whenever splitting is available.",
          "Since splitting is unavailable, play the hand as hard 16 and take another card."
      );
    }


    // 7,7 / 6,6 / 3,3 / 2,2
    if (value == 7
        || value == 6
        || value == 3
        || value == 2)
    {
      if (dealerValue >= 2
          && dealerValue <= 7)
      {
        if (context.isAvailable(
            HandAction.SPLIT))
        {
          return new StrategyGuidance(
              HandAction.SPLIT,
              "Split small pairs against dealer 2–7.",
              "The dealer is weak enough that creating two hands can be worthwhile."
          );
        }

        return new StrategyGuidance(
            HandAction.HIT,
            "Split small pairs against dealer 2–7 when splitting is available.",
            "Since splitting is unavailable, keep improving the hand with a hit."
        );
      }

      return new StrategyGuidance(
          HandAction.HIT,
          "Hit small pairs against dealer 8 or higher.",
          "Against a strong dealer card, keep improving the hand instead of creating two weak hands."
      );
    }


    // 5,5
    if (value == 5)
    {
      return getHardTenGuidance(
          dealerValue,
          context,
          true
      );
    }


    // 4,4
    if (value == 4)
    {
      return new StrategyGuidance(
          HandAction.HIT,
          "Never split 4s.",
          "A pair of 4s is simply a hard 8, so take another card."
      );
    }

    return null;
  }


  private StrategyGuidance getSoftGuidance(
      Hand hand,
      int dealerValue,
      PlayerDecisionContext context)
  {
    int score =
        hand.getScore();


    // Soft 19+
    if (score >= 19)
    {
      return new StrategyGuidance(
          HandAction.STAND,
          "Stand on soft 19 or better.",
          "You already have a strong hand, so there is little reason to risk changing it."
      );
    }


    // Soft 18
    if (score == 18)
    {
      if (dealerValue >= 4
          && dealerValue <= 6)
      {
        if (context.isAvailable(
            HandAction.DOUBLE))
        {
          return new StrategyGuidance(
              HandAction.DOUBLE,
              "Double soft 18 against dealer 4–6.",
              "Your ace gives flexibility while the dealer is showing a weak card."
          );
        }

        return new StrategyGuidance(
            HandAction.STAND,
            "Stand on soft 18 when doubling is unavailable.",
            "Normally double against dealer 4–6, but if you can no longer double, keep the 18."
        );
      }

      if (dealerValue >= 9)
      {
        return new StrategyGuidance(
            HandAction.HIT,
            "Hit soft 18 against dealer 9, 10, or ace.",
            "A soft 18 is not strong enough against the dealer's strongest cards."
        );
      }

      return new StrategyGuidance(
          HandAction.STAND,
          "Stand on soft 18 against dealer 2, 3, 7, or 8.",
          "Soft 18 is strong enough here without taking another card."
      );
    }


    // Soft 17 or lower
    if (dealerValue >= 4
        && dealerValue <= 6)
    {
      if (context.isAvailable(
          HandAction.DOUBLE))
      {
        return new StrategyGuidance(
            HandAction.DOUBLE,
            "Double soft 17 or lower against dealer 4–6.",
            "The ace protects you from busting while the dealer is showing a weak card."
        );
      }

      return new StrategyGuidance(
          HandAction.HIT,
          "Hit soft 17 or lower when doubling is unavailable.",
          "Normally double against dealer 4–6, but once doubling is unavailable, keep improving the hand."
      );
    }

    return new StrategyGuidance(
        HandAction.HIT,
        "Hit soft 17 or lower unless doubling against dealer 4–6.",
        "These soft hands are flexible, so keep improving them."
    );
  }


  private StrategyGuidance getHardGuidance(
      Hand hand,
      int dealerValue,
      PlayerDecisionContext context)
  {
    int score =
        hand.getScore();


    // Hard 17+
    if (score >= 17)
    {
      return new StrategyGuidance(
          HandAction.STAND,
          "Stand on hard 17 or higher.",
          "You already have a strong total, and another card carries too much bust risk."
      );
    }


    // Hard 12–16
    if (score >= 12)
    {
      if (dealerValue >= 2
          && dealerValue <= 6)
      {
        return new StrategyGuidance(
            HandAction.STAND,
            "Against dealer 2–6, stand on hard 12 or higher.",
            "Let the dealer take the bust risk when showing a weak card."
        );
      }

      return new StrategyGuidance(
          HandAction.HIT,
          "Hit hard 12–16 against dealer 7 or higher.",
          "The dealer is too strong to rely on a bust, so improve your hand."
      );
    }


    // Hard 11
    if (score == 11)
    {
      if (context.isAvailable(
          HandAction.DOUBLE))
      {
        return new StrategyGuidance(
            HandAction.DOUBLE,
            "Double hard 11 whenever possible.",
            "Hard 11 is one of the strongest starting totals for doubling."
        );
      }

      return new StrategyGuidance(
          HandAction.HIT,
          "Hit hard 11 when doubling is unavailable.",
          "Normally double hard 11, but once doubling is unavailable, take another card."
      );
    }


    // Hard 10
    if (score == 10)
    {
      return getHardTenGuidance(
          dealerValue,
          context,
          false
      );
    }


    // Hard 9
    if (score == 9)
    {
      if (dealerValue >= 2
          && dealerValue <= 6)
      {
        if (context.isAvailable(
            HandAction.DOUBLE))
        {
          return new StrategyGuidance(
              HandAction.DOUBLE,
              "Double hard 9 against dealer 2–6.",
              "The dealer is weak enough to press the advantage."
          );
        }

        return new StrategyGuidance(
            HandAction.HIT,
            "Hit hard 9 when doubling is unavailable.",
            "Normally double against dealer 2–6, but once doubling is unavailable, take another card."
        );
      }

      return new StrategyGuidance(
          HandAction.HIT,
          "Hit hard 9 against dealer 7 or higher.",
          "Against stronger dealer cards, improve the hand without doubling."
      );
    }


    // Hard 8 or lower
    return new StrategyGuidance(
        HandAction.HIT,
        "Hit hard 8 or lower.",
        "Your total is too low to stand."
    );
  }


  private StrategyGuidance getHardTenGuidance(
      int dealerValue,
      PlayerDecisionContext context,
      boolean pairOfFives)
  {
    if (dealerValue >= 2
        && dealerValue <= 9)
    {
      if (context.isAvailable(
          HandAction.DOUBLE))
      {
        if (pairOfFives)
        {
          return new StrategyGuidance(
              HandAction.DOUBLE,
              "Never split 5s — play them like hard 10.",
              "Against dealer 2–9, that means doubling when it is available."
          );
        }

        return new StrategyGuidance(
            HandAction.DOUBLE,
            "Double hard 10 against dealer 2–9.",
            "You have a strong chance to make 20 while the dealer is not showing a 10 or ace."
        );
      }

      if (pairOfFives)
      {
        return new StrategyGuidance(
            HandAction.HIT,
            "Never split 5s — play them like hard 10.",
            "Normally double against dealer 2–9, but since doubling is unavailable, take another card."
        );
      }

      return new StrategyGuidance(
          HandAction.HIT,
          "Hit hard 10 when doubling is unavailable.",
          "Normally double against dealer 2–9, but once doubling is unavailable, take another card."
      );
    }


    if (pairOfFives)
    {
      return new StrategyGuidance(
          HandAction.HIT,
          "Never split 5s — play them like hard 10.",
          "Against dealer 10 or ace, hard 10 should be hit rather than doubled."
      );
    }

    return new StrategyGuidance(
        HandAction.HIT,
        "Hit hard 10 against dealer 10 or ace.",
        "The dealer's card is too strong to justify doubling."
    );
  }
}