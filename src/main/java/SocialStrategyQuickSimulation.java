import java.util.ArrayList;
import java.util.List;

public class SocialStrategyQuickSimulation
{
  public static final int SESSION_COUNT =
      10;

  public static final int HANDS_PER_SESSION =
      160;

  public static final double STARTING_BANKROLL =
      500.0;

  public static final double BASE_BET =
      25.0;


  public static Result run(
      GameSettings.BlackjackPayout payout)
  {
    GameSettings settings =
        createSettings(
            payout
        );

    List<SessionResult> sessions =
        new ArrayList<>();


    int socialAhead =
        0;

    int basicAhead =
        0;

    int ties =
        0;

    double totalDifference =
        0.0;


    /*
     * Makes every click of RUN SIMULATION
     * produce a new experiment.
     */
    long baseSeed =
        System.nanoTime();


    for (int session = 0;
         session < SESSION_COUNT;
         session++)
    {
      SessionResult sessionResult =
          playPairedSession(
              settings,
              session + 1,
              baseSeed,
              session
          );


      sessions.add(
          sessionResult
      );


      double difference =
          sessionResult.getDifference();


      if (difference > 0)
      {
        socialAhead++;
      }
      else if (difference < 0)
      {
        basicAhead++;
      }
      else
      {
        ties++;
      }


      totalDifference +=
          difference;
    }


    return new Result(
        sessions,
        socialAhead,
        basicAhead,
        ties,
        totalDifference
            / SESSION_COUNT
    );
  }


  private static SessionResult playPairedSession(
      GameSettings settings,
      int sessionNumber,
      long baseSeed,
      int sessionIndex)
  {
    double[] basicBankroll =
    {
        STARTING_BANKROLL
    };


    double[] socialBankroll =
    {
        STARTING_BANKROLL
    };


    HandStrategy basicStrategy =
        new BasicStrategy();

    HandStrategy socialStrategy =
        new SocialStrategy();


    for (int handNumber = 0;
         handNumber < HANDS_PER_SESSION;
         handNumber++)
    {
      /*
       * Every hand gets its own shared seed.
       *
       * This means Basic and Social start
       * every hand from the exact same
       * shuffled card sequence.
       *
       * What either strategy does during this
       * hand cannot shift the next hand.
       */
      long handSeed =
          baseSeed
          + (sessionIndex * 100_000L)
          + handNumber;


      if (basicBankroll[0] >= BASE_BET)
      {
        playPairedHandBranch(
            settings,
            basicStrategy,
            handSeed,
            basicBankroll
        );
      }


      if (socialBankroll[0] >= BASE_BET)
      {
        playPairedHandBranch(
            settings,
            socialStrategy,
            handSeed,
            socialBankroll
        );
      }
    }


    double difference =
        socialBankroll[0]
        - basicBankroll[0];


    return new SessionResult(
        sessionNumber,
        basicBankroll[0],
        socialBankroll[0],
        difference
    );
  }


  private static void playPairedHandBranch(
      GameSettings settings,
      HandStrategy strategy,
      long seed,
      double[] bankroll)
  {
    /*
     * Fresh identical shoe state for this
     * particular paired hand.
     */
    Shoe shoe =
        new Shoe(
            settings.getDecksInShoe(),
            seed
        );


    PlayerRoundEngine playerRoundEngine =
        new PlayerRoundEngine(
            settings
        );


    DealerPlayEngine dealerPlayEngine =
        new DealerPlayEngine(
            settings
        );


    /*
     * Initial wager.
     */
    bankroll[0] -=
        BASE_BET;


    Hand playerHand =
        new Hand();

    Hand dealerHand =
        new Hand();


    /*
     * Same dealing order as the live game.
     */
    playerHand.addCard(
        shoe.drawCard()
    );


    Card dealerUpCard =
        shoe.drawCard();

    dealerHand.addCard(
        dealerUpCard
    );


    playerHand.addCard(
        shoe.drawCard()
    );


    dealerHand.addCard(
        shoe.drawCard()
    );


    boolean playerBlackjack =
        playerHand.isBlackjack();

    boolean dealerBlackjack =
        dealerHand.isBlackjack();


    /*
     * Dealer blackjack.
     */
    if (dealerBlackjack)
    {
      if (playerBlackjack)
      {
        bankroll[0] +=
            BASE_BET;
      }

      return;
    }


    /*
     * Player natural blackjack.
     */
    if (playerBlackjack)
    {
      bankroll[0] +=
          BASE_BET
          * settings
              .getBlackjackPayout()
              .getTotalReturnMultiplier();

      return;
    }


    PlayerHandState initialState =
        new PlayerHandState(
            playerHand
        );


    List<PlayerHandState> playerStates =
        playerRoundEngine.play(
            initialState,
            dealerUpCard,
            strategy,
            shoe,
            new PlayerRoundListener()
            {
              @Override
              public boolean canDoubleOrSplit(
                  PlayerHandState state)
              {
                return bankroll[0]
                    >= BASE_BET;
              }


              @Override
              public double getWagerAmount(
                  PlayerHandState state)
              {
                return BASE_BET;
              }


              @Override
              public void handStarted(
                  PlayerHandState state,
                  int index,
                  int handCount)
              {
              }


              @Override
              public void cardDrawn(
                  PlayerHandState state,
                  Card card)
              {
              }


              @Override
              public void handDoubled(
                  PlayerHandState state)
              {
                bankroll[0] -=
                    BASE_BET;
              }


              @Override
              public void handSurrendered(
                  PlayerHandState state)
              {
              }


              @Override
              public void handSplit(
                  PlayerHandState originalState,
                  PlayerHandState newState,
                  HandSplitResult splitResult)
              {
                bankroll[0] -=
                    BASE_BET;
              }


              @Override
              public void handFinished(
                  PlayerHandState state,
                  int index,
                  int handCount,
                  HandFinishReason reason)
              {
              }
            }
        );


    boolean dealerNeedsToPlay =
        false;


    for (PlayerHandState state :
         playerStates)
    {
      if (!state.getHand().isBusted()
          && !state.didSurrender())
      {
        dealerNeedsToPlay =
            true;

        break;
      }
    }


    if (dealerNeedsToPlay)
    {
      dealerPlayEngine.play(
          dealerHand,
          shoe,
          null
      );
    }


    /*
     * Resolve all player hands created
     * by this original hand.
     */
    for (PlayerHandState state :
         playerStates)
    {
      HandOutcome outcome =
          HandOutcomeAnalyzer.analyze(
              state.getHand(),
              dealerHand,
              false,
              state.didSurrender()
          );


      double wager =
          BASE_BET
          * state.getWagerMultiplier();


      switch (outcome)
      {
        case WIN:
          bankroll[0] +=
              wager * 2.0;
          break;


        case LOSS:
          break;


        case PUSH:
          bankroll[0] +=
              wager;
          break;


        case SURRENDER:
          bankroll[0] +=
              BASE_BET * 0.5;
          break;


        case BLACKJACK:
          throw new IllegalStateException(
              "Natural blackjack should "
              + "already be resolved"
          );


        default:
          throw new IllegalStateException(
              "Unexpected outcome: "
              + outcome
          );
      }
    }
  }


  private static GameSettings createSettings(
      GameSettings.BlackjackPayout payout)
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
        payout
    );


    return settings;
  }


  public static class SessionResult
  {
    private final int sessionNumber;
    private final double basicBankroll;
    private final double socialBankroll;
    private final double difference;


    public SessionResult(
        int sessionNumber,
        double basicBankroll,
        double socialBankroll,
        double difference)
    {
      this.sessionNumber =
          sessionNumber;

      this.basicBankroll =
          basicBankroll;

      this.socialBankroll =
          socialBankroll;

      this.difference =
          difference;
    }


    public int getSessionNumber()
    {
      return sessionNumber;
    }


    public double getBasicBankroll()
    {
      return basicBankroll;
    }


    public double getSocialBankroll()
    {
      return socialBankroll;
    }


    public double getDifference()
    {
      return difference;
    }
  }


  public static class Result
  {
    private final List<SessionResult> sessions;
    private final int socialAhead;
    private final int basicAhead;
    private final int ties;
    private final double averageDifference;


    public Result(
        List<SessionResult> sessions,
        int socialAhead,
        int basicAhead,
        int ties,
        double averageDifference)
    {
      this.sessions =
          sessions;

      this.socialAhead =
          socialAhead;

      this.basicAhead =
          basicAhead;

      this.ties =
          ties;

      this.averageDifference =
          averageDifference;
    }


    public List<SessionResult> getSessions()
    {
      return sessions;
    }


    public int getSocialAhead()
    {
      return socialAhead;
    }


    public int getBasicAhead()
    {
      return basicAhead;
    }


    public int getTies()
    {
      return ties;
    }


    public double getAverageDifference()
    {
      return averageDifference;
    }
  }
}