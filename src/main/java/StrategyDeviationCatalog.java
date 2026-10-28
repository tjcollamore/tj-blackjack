public class StrategyDeviationCatalog
{
  private static final BasicStrategy BASIC =
      new BasicStrategy();

  private static final SocialStrategy SOCIAL =
      new SocialStrategy();


  public static StrategyDeviation find(
      PlayerDecisionContext context)
  {
    /*
     * Social Strategy v1 was research-validated
     * against multi-deck H17 / DAS blackjack.
     *
     * Do not attach our research claims to
     * single- or double-deck games.
     */
    if (!isValidatedEnvironment(context))
      return null;


    Hand hand =
        context.getPlayerHand();

    int dealer =
        context.getDealerUpCard()
            .getPointValue();

    HandAction basicAction =
        BASIC.chooseRecommendedAction(
            context
        );

    HandAction socialAction =
        SOCIAL.chooseRecommendedAction(
            context
        );


    /*
     * If the strategies agree, there is no
     * deviation to explain.
     */
    if (basicAction == socialAction)
      return null;


    /*
     * Pair-specific deviations must be checked
     * before generic hard-total deviations.
     */
    if (isPairOf(hand, 8)
        && dealer == 10)
    {
      return deviation(
          "PAIR_88_VS_10",
          basicAction,
          socialAction,
          "Surrender 8,8 vs 10",
          "Basic Strategy splits 8s here. Social Strategy instead uses its simple surrender rule: hard 15 or 16 against a dealer 10.",
          "In direct simulation, surrender produced the better monetary result 53.3% of the time and splitting 46.7%. Splitting still had about $0.68 more expected value per occurrence at a $25 wager.",
          "Surrender wins the head-to-head result more often, while splitting keeps slightly more long-run expected value."
      );
    }


    if (isPairOf(hand, 8)
        && dealer == 11)
    {
      return deviation(
          "PAIR_88_VS_ACE",
          basicAction,
          socialAction,
          "Split 8,8 vs ace",
          "Basic Strategy surrenders this hand under the validated H17 rules. Social Strategy keeps the simpler pair rule and splits the 8s.",
          "The Basic Strategy expected-value advantage was about $0.41 per occurrence at a $25 wager. This hand appeared only about 0.05 times per 160-hand session in our study.",
          "This is a rare exception, so Social Strategy accepts the small cost to keep the pair rule easier to remember."
      );
    }


    if (isPairOf(hand, 9)
        && dealer == 7)
    {
      return deviation(
          "PAIR_99_VS_7",
          basicAction,
          socialAction,
          "Split 9s vs 7",
          "Basic Strategy stands on 18. Social Strategy extends the easy-to-remember 9s rule and splits against dealer 2 through 9.",
          "The Basic Strategy advantage was about $0.69 per occurrence at a $25 wager, but this situation occurred only about 0.07 times per 160-hand session.",
          "Social gives up a tiny amount of session EV to remove an awkward exception from the pair rule."
      );
    }


    if (isPairOf(hand, 6)
        && dealer == 7)
    {
      return deviation(
          "PAIR_66_VS_7",
          basicAction,
          socialAction,
          "Split 6s vs 7",
          "Basic Strategy hits here. Social Strategy groups 6s with the other small pairs and splits them against dealer 2 through 7.",
          "The Basic Strategy advantage was about $0.83 per occurrence at a $25 wager, with this situation appearing about 0.07 times per 160-hand session.",
          "The hand is uncommon enough that Social Strategy favors the simpler small-pair rule."
      );
    }


    if (isPairOf(hand, 4)
        && dealer == 5)
    {
      return deviation(
          "PAIR_44_VS_5",
          basicAction,
          socialAction,
          "Hit 4,4 vs 5",
          "Basic Strategy splits the 4s when double after split is allowed. Social Strategy removes the special 4s exception and simply says never split 4s.",
          "The Basic Strategy advantage was about $0.67 per occurrence at a $25 wager. The hand occurred about 0.07 times per 160-hand session.",
          "Social Strategy trades a very small amount of EV for the much simpler rule: never split 4s."
      );
    }


    if (isPairOf(hand, 4)
        && dealer == 6)
    {
      return deviation(
          "PAIR_44_VS_6",
          basicAction,
          socialAction,
          "Hit 4,4 vs 6",
          "Basic Strategy splits the 4s when double after split is allowed. Social Strategy removes the special 4s exception and simply says never split 4s.",
          "This is the more expensive 4s exception: Basic Strategy gained about $2.33 per occurrence at a $25 wager. It still appeared only about 0.07 times per 160-hand session.",
          "This costs more when it actually occurs, but the situation is rare enough that Social Strategy still favors eliminating the special-case rule."
      );
    }


    /*
     * Soft-hand deviations.
     *
     * These are specific two-card hands because
     * the Basic/Social difference involves the
     * initial doubling decision.
     */
    if (isSoftTwoCardHand(hand, 8)
        && dealer == 6)
    {
      return deviation(
          "SOFT_A8_VS_6",
          basicAction,
          socialAction,
          "Stand A,8 vs 6",
          "Basic Strategy doubles soft 19 against dealer 6 under H17. Social Strategy uses the much simpler rule: stand on soft 19 or better.",
          "Basic Strategy gained about $0.25 in expected value each time this situation occurred at a $25 wager.",
          "Social Strategy keeps the memorable rule 'soft 19 or better: stand' instead of teaching one unusual doubling exception."
      );
    }


    if (isSoftTwoCardHand(hand, 7)
        && dealer == 2)
    {
      return deviation(
          "SOFT_A7_VS_2",
          basicAction,
          socialAction,
          "Stand A,7 vs 2",
          "Basic Strategy doubles soft 18 here under H17. Social Strategy keeps dealer 2 outside its soft-hand doubling zone.",
          "The Basic Strategy edge was only about $0.04 per occurrence at a $25 wager.",
          "This is almost free to simplify, so Social Strategy chooses the easier soft-18 rule."
      );
    }


    if (isSoftTwoCardHand(hand, 7)
        && dealer == 3)
    {
      return deviation(
          "SOFT_A7_VS_3",
          basicAction,
          socialAction,
          "Stand A,7 vs 3",
          "Basic Strategy doubles soft 18 here. Social Strategy narrows the soft-hand doubling range to dealer 4 through 6.",
          "Basic Strategy gained about $0.82 per occurrence at a $25 wager.",
          "Social Strategy accepts the modest cost so its soft doubling rule can consistently focus on dealer 4 through 6."
      );
    }


    if (isSoftTwoCardHand(hand, 6)
        && dealer == 3)
    {
      return deviation(
          "SOFT_A6_VS_3",
          basicAction,
          socialAction,
          "Hit A,6 vs 3",
          "Basic Strategy doubles soft 17 here. Social Strategy again keeps its soft doubling zone limited to dealer 4 through 6.",
          "The Basic Strategy advantage was about $0.66 per occurrence at a $25 wager.",
          "The simplified rule removes another isolated dealer-3 doubling exception."
      );
    }


    if (isSoftTwoCardHand(hand, 2)
        && dealer == 4)
    {
      return deviation(
          "SOFT_A2_VS_4",
          basicAction,
          socialAction,
          "Double A,2 vs 4",
          "Basic Strategy hits this hand. Social Strategy applies its simplified rule of doubling soft 17 or lower against dealer 4 through 6.",
          "Basic Strategy retained about $0.91 more expected value per occurrence at a $25 wager. Interestingly, the Social double produced the better result slightly more often, but Basic won by more when it succeeded.",
          "Win frequency and expected value are not always the same thing; Social chooses the simpler consistent doubling pattern."
      );
    }


    if (isSoftTwoCardHand(hand, 3)
        && dealer == 4)
    {
      return deviation(
          "SOFT_A3_VS_4",
          basicAction,
          socialAction,
          "Double A,3 vs 4",
          "Basic Strategy hits this hand. Social Strategy applies its simplified rule of doubling soft 17 or lower against dealer 4 through 6.",
          "Basic Strategy retained about $0.35 more expected value per occurrence at a $25 wager. As with A,2 vs 4, the Social play produced the better result slightly more often despite having slightly lower expected value.",
          "Social Strategy prefers one consistent soft doubling pattern instead of memorizing this exception."
      );
    }


    /*
     * Hard-hand deviations.
     */
    if (!hand.isSoft()
        && hand.getScore() == 12
        && dealer == 2)
    {
      return deviation(
          "HARD_12_VS_2",
          basicAction,
          socialAction,
          "Stand 12 vs 2",
          "Basic Strategy hits hard 12 against dealer 2. Social Strategy removes that exception and uses the simple rule: against dealer 2 through 6, stand on hard 12 or higher.",
          "Across hard 12 vs 2 and 3, the situations appeared about 2.03 times per 160-hand session. Basic produced the better result 27.6% of the time, Social 25.5%, and they tied 46.9%. The combined cost was about $1.17 per session at a $25 wager.",
          "Basic beats Social only about 2.1 percentage points more often than Social beats Basic in these 12-vs-2/3 decisions."
      );
    }


    if (!hand.isSoft()
        && hand.getScore() == 12
        && dealer == 3)
    {
      return deviation(
          "HARD_12_VS_3",
          basicAction,
          socialAction,
          "Stand 12 vs 3",
          "Basic Strategy hits hard 12 against dealer 3. Social Strategy removes that exception and uses the simple rule: against dealer 2 through 6, stand on hard 12 or higher.",
          "Across hard 12 vs 2 and 3, the situations appeared about 2.03 times per 160-hand session. Basic produced the better result 27.6% of the time, Social 25.5%, and they tied 46.9%. The combined cost was about $1.17 per session at a $25 wager.",
          "The small mathematical edge is traded for one of Social Strategy's easiest rules to remember."
      );
    }


    if (!hand.isSoft()
        && hand.getScore() == 9
        && dealer == 2)
    {
      return deviation(
          "HARD_9_VS_2",
          basicAction,
          socialAction,
          "Double 9 vs 2",
          "Basic Strategy hits hard 9 against dealer 2 in the validated multi-deck game. Social Strategy extends its doubling range to dealer 2 through 6.",
          "The expected-value cost was about $0.10 per 160-hand session at a $25 wager. In direct trials, the Social double produced the better result about 48.1% of the time versus 44.8% for the Basic hit, with about 7.1% ties.",
          "This simplification was essentially free at the session level and makes the hard-9 rule much easier to memorize."
      );
    }


    /*
     * Surrender deviations.
     *
     * Pair-of-eights cases were handled above.
     */
    if (!hand.isSoft()
        && hand.getScore() == 15
        && dealer == 11)
    {
      return deviation(
          "HARD_15_VS_ACE",
          basicAction,
          socialAction,
          "Hit 15 vs ace",
          "Basic Strategy surrenders here under H17. Social Strategy restricts surrender to the much simpler rule of hard 15 or 16 against dealer 10.",
          "The Basic Strategy advantage was about $0.19 per occurrence at a $25 wager, or roughly $0.12 per 160-hand session.",
          "Social Strategy gives up a small amount of EV to make surrender dramatically easier to remember."
      );
    }


    if (!hand.isSoft()
        && hand.getScore() == 16
        && dealer == 9)
    {
      return deviation(
          "HARD_16_VS_9",
          basicAction,
          socialAction,
          "Hit 16 vs 9",
          "Basic Strategy surrenders hard 16 against dealer 9. Social Strategy surrenders only against dealer 10.",
          "The Basic Strategy advantage was about $0.14 per occurrence at a $25 wager, or about $0.10 per 160-hand session.",
          "Dropping the dealer-9 surrender exception costs very little while making the surrender rule much cleaner."
      );
    }


    if (!hand.isSoft()
        && hand.getScore() == 16
        && dealer == 11)
    {
      return deviation(
          "HARD_16_VS_ACE",
          basicAction,
          socialAction,
          "Hit 16 vs ace",
          "Basic Strategy surrenders this hard 16 under H17. Social Strategy uses its simple 15-or-16 vs 10 surrender rule instead.",
          "This is one of the more meaningful surrender omissions: Basic gained about $1.00 per occurrence at a $25 wager, corresponding to roughly $0.51 per 160-hand session.",
          "Social Strategy accepts this cost because removing ace-based surrender exceptions makes the rule substantially easier to learn."
      );
    }


    if (!hand.isSoft()
        && hand.getScore() == 17
        && dealer == 11)
    {
      return deviation(
          "HARD_17_VS_ACE",
          basicAction,
          socialAction,
          "Stand 17 vs ace",
          "Basic Strategy surrenders hard 17 against an ace under H17. Social Strategy follows the ordinary rule of standing on hard 17 or higher.",
          "Basic Strategy gained about $0.29 per occurrence at a $25 wager, or roughly $0.15 per 160-hand session.",
          "Rather than teach the surprising idea of surrendering 17, Social Strategy keeps the intuitive 'stand on 17+' rule."
      );
    }


    return null;
  }


  private static boolean isValidatedEnvironment(
      PlayerDecisionContext context)
  {
    int decks =
        context.getDeckCount();

    return (decks == 6 || decks == 8)
        && context.getSettings()
            .dealerHitsSoft17()
        && context.getSettings()
            .doubleAfterSplit();
  }


  private static boolean isPairOf(
      Hand hand,
      int value)
  {
    if (hand.getNumCards() != 2)
      return false;

    return hand.getCard(0)
               .getPointValue() == value
        && hand.getCard(1)
               .getPointValue() == value;
  }


  /*
   * otherValue means the non-ace card:
   *
   * 8 -> A,8
   * 7 -> A,7
   * 6 -> A,6
   * etc.
   */
  private static boolean isSoftTwoCardHand(
      Hand hand,
      int otherValue)
  {
    if (hand.getNumCards() != 2)
      return false;

    int first =
        hand.getCard(0)
            .getPointValue();

    int second =
        hand.getCard(1)
            .getPointValue();

    return (first == 11
            && second == otherValue)
        || (second == 11
            && first == otherValue);
  }


  private static StrategyDeviation deviation(
      String id,
      HandAction basicAction,
      HandAction socialAction,
      String title,
      String explanation,
      String evidence,
      String takeaway)
  {
    return new StrategyDeviation(
        id,
        basicAction,
        socialAction,
        title,
        explanation,
        evidence,
        takeaway
    );
  }
}
