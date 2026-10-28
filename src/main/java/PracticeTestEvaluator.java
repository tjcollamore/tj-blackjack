import java.util.EnumSet;

public class PracticeTestEvaluator
{
  private final BasicStrategy basicStrategy =
      new BasicStrategy();

  private final SocialStrategy socialStrategy =
      new SocialStrategy();

  private final GameSettings settings;


  public PracticeTestEvaluator()
  {
    settings =
        createPracticeSettings();
  }


  public Evaluation evaluate(
      PracticeTestQuestion question,
      HandAction userAction,
      PracticeTestDifficulty difficulty)
  {
    PlayerDecisionContext context =
        createContext(
            question
        );


    HandAction basicAction =
        basicStrategy.chooseRecommendedAction(
            context
        );


    HandAction socialAction =
        socialStrategy.chooseRecommendedAction(
            context
        );


    boolean matchesBasic =
        userAction == basicAction;

    boolean matchesSocial =
        userAction == socialAction;


    boolean correct;


    switch (difficulty)
    {
      case CP:
        /*
         * CP accepts either approved strategy.
         */
        correct =
            matchesBasic
            || matchesSocial;
        break;


      case HONORS:
        /*
         * Honors requires perfect Basic Strategy.
         */
        correct =
            matchesBasic;
        break;


      default:
        throw new IllegalStateException(
            "Unexpected difficulty: "
            + difficulty
        );
    }


    boolean validSocialAlternative =
        !matchesBasic
        && matchesSocial;


    return new Evaluation(
        question,
        userAction,
        basicAction,
        socialAction,
        correct,
        matchesBasic,
        matchesSocial,
        validSocialAlternative
    );
  }


  public HandAction getBasicAction(
      PracticeTestQuestion question)
  {
    return basicStrategy
        .chooseRecommendedAction(
            createContext(
                question
            )
        );
  }


  public HandAction getSocialAction(
      PracticeTestQuestion question)
  {
    return socialStrategy
        .chooseRecommendedAction(
            createContext(
                question
            )
        );
  }


  public PlayerDecisionContext createContext(
      PracticeTestQuestion question)
  {
    Hand hand =
        new Hand();


    /*
     * We only need real Card objects so the
     * production strategy classes can inspect
     * the hand normally.
     */
    Shoe source =
        new Shoe(
            8,
            8_675_309L
        );


    for (Rank rank :
         question.getPlayerRanks())
    {
      hand.addCard(
          drawRank(
              source,
              rank
          )
      );
    }


    Card dealerUpCard =
        drawRank(
            source,
            question.getDealerRank()
        );


  EnumSet<HandAction> allowedActions =
      PlayerActionRules.getAllowedActions(
          hand,
          false,
          settings
      );


  EnumSet<HandAction> availableActions =
      EnumSet.copyOf(
          allowedActions
      );


  return new PlayerDecisionContext(
      hand,
      dealerUpCard,
      settings,
      allowedActions,
      availableActions,
      false,
      8,
      25.0
  );
  }


  private Card drawRank(
      Shoe shoe,
      Rank targetRank)
  {
    while (true)
    {
      Card card =
          shoe.drawCard();


      if (card.getRank()
          == targetRank)
      {
        return card;
      }
    }
  }


  private GameSettings createPracticeSettings()
  {
    GameSettings settings =
        new GameSettings();


    settings.setDecksInShoe(
        8
    );

    settings.setDealerHitsSoft17(
        true
    );

    settings.setDoubleAfterSplit(
        true
    );

    settings.setAllowSurrender(
        true
    );

    settings.setBlackjackPayout(
        GameSettings.BlackjackPayout.SIX_TO_FIVE
    );


    return settings;
  }


  public static class Evaluation
  {
    private final PracticeTestQuestion question;
    private final HandAction userAction;
    private final HandAction basicAction;
    private final HandAction socialAction;

    private final boolean correct;
    private final boolean matchesBasic;
    private final boolean matchesSocial;
    private final boolean validSocialAlternative;


    public Evaluation(
        PracticeTestQuestion question,
        HandAction userAction,
        HandAction basicAction,
        HandAction socialAction,
        boolean correct,
        boolean matchesBasic,
        boolean matchesSocial,
        boolean validSocialAlternative)
    {
      this.question =
          question;

      this.userAction =
          userAction;

      this.basicAction =
          basicAction;

      this.socialAction =
          socialAction;

      this.correct =
          correct;

      this.matchesBasic =
          matchesBasic;

      this.matchesSocial =
          matchesSocial;

      this.validSocialAlternative =
          validSocialAlternative;
    }


    public PracticeTestQuestion getQuestion()
    {
      return question;
    }


    public HandAction getUserAction()
    {
      return userAction;
    }


    public HandAction getBasicAction()
    {
      return basicAction;
    }


    public HandAction getSocialAction()
    {
      return socialAction;
    }


    public boolean isCorrect()
    {
      return correct;
    }


    public boolean matchesBasic()
    {
      return matchesBasic;
    }


    public boolean matchesSocial()
    {
      return matchesSocial;
    }


    public boolean isValidSocialAlternative()
    {
      return validSocialAlternative;
    }
  }
}
