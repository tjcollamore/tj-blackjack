import java.util.List;

public class SimulationEngine
{
  private final GameSettings settings;

  private final BasicStrategy basicStrategy =
      new BasicStrategy();

  private final DealerPlayEngine dealerPlayEngine;
  private final PlayerRoundEngine playerRoundEngine;

  public SimulationEngine(
      GameSettings settings)
  {
    this.settings =
        settings;

    playerRoundEngine =
        new PlayerRoundEngine(
            settings
        );

    dealerPlayEngine =
        new DealerPlayEngine(
            settings
        );
  }


  /*
   * Backward-compatible overload.
   *
   * This preserves the old research behavior:
   * force the first action, then continue with
   * perfect Basic Strategy.
   */
  public double runSingleTrial(
      PlayerDecisionContext context,
      HandAction forcedFirstAction)
  {
    return runSingleTrial(
        context,
        forcedFirstAction,
        basicStrategy
    );
  }


  /*
   * Force the first action, then continue the
   * entire rest of the round with the supplied
   * strategy.
   *
   * This is what Decision Help should use:
   * - Basic mode  -> BasicStrategy
   * - Social mode -> SocialStrategy
   */
  public double runSingleTrial(
      PlayerDecisionContext context,
      HandAction forcedFirstAction,
      HandStrategy continuationStrategy)
  {
    while (true)
    {
      Shoe shoe =
          createFreshShoe(
              context
          );

      Hand playerHand =
          copyHand(
              context.getPlayerHand()
          );

      Hand dealerHand =
          new Hand();

      dealerHand.addCard(
          context.getDealerUpCard()
      );

      dealerHand.addCard(
          shoe.drawCard()
      );

      /*
       * Decision Help only exists after the live
       * round has ruled out dealer blackjack.
       * Therefore simulation trials must be
       * conditioned on dealer NOT having blackjack.
       */
      if (dealerHand.isBlackjack())
        continue;

      ForcedFirstActionStrategy strategy =
          new ForcedFirstActionStrategy(
              forcedFirstAction,
              continuationStrategy
          );

      PlayerHandState initialState =
          new PlayerHandState(
              playerHand
          );

      initialState.setCameFromSplit(
          context.cameFromSplit()
      );

      List<PlayerHandState> playerStates =
          playerRoundEngine.play(
              initialState,
              context.getDealerUpCard(),
              strategy,
              shoe,
              new PlayerRoundListener()
              {
                @Override
                public boolean canDoubleOrSplit(
                    PlayerHandState state)
                {
                  // Simulation ignores bankroll.
                  return true;
                }

                @Override
                public double getWagerAmount(
                    PlayerHandState state)
                {
                  return context
                      .getWagerAmount();
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

      double totalResult =
          0.0;

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

        double handResult;

        switch (outcome)
        {
          case WIN:
            handResult = 1.0;
            break;

          case LOSS:
            handResult = -1.0;
            break;

          case PUSH:
            handResult = 0.0;
            break;

          case SURRENDER:
            handResult = -0.5;
            break;

          case BLACKJACK:
            throw new IllegalStateException(
                "Natural blackjack should not occur "
                + "during Decision Help simulation"
            );

          default:
            throw new IllegalStateException(
                "Unexpected outcome: "
                + outcome
            );
        }

        totalResult +=
            state.getWagerMultiplier()
            * handResult;
      }

      return totalResult;
    }
  }


  private Shoe createFreshShoe(
      PlayerDecisionContext context)
  {
    Shoe shoe =
        new Shoe(
            context.getDeckCount()
        );

    Hand playerHand =
        context.getPlayerHand();

    for (int i = 0;
         i < playerHand.getNumCards();
         i++)
    {
      shoe.removeCard(
          playerHand.getCard(i)
      );
    }

    shoe.removeCard(
        context.getDealerUpCard()
    );

    return shoe;
  }


  private Hand copyHand(
      Hand original)
  {
    Hand copy =
        new Hand();

    for (int i = 0;
         i < original.getNumCards();
         i++)
    {
      copy.addCard(
          original.getCard(i)
      );
    }

    return copy;
  }


  /*
   * Backward-compatible overload.
   *
   * Research callers that do not specify a
   * continuation strategy retain the original
   * "forced action, then Basic Strategy" behavior.
   */
  public SimulationResult runSimulation(
      PlayerDecisionContext context,
      HandAction forcedFirstAction,
      int trials)
  {
    return runSimulation(
        context,
        forcedFirstAction,
        basicStrategy,
        trials
    );
  }


  public SimulationResult runSimulation(
      PlayerDecisionContext context,
      HandAction forcedFirstAction,
      HandStrategy continuationStrategy,
      int trials)
  {
    if (trials <= 0)
    {
      throw new IllegalArgumentException(
          "Trials must be greater than zero"
      );
    }

    int wins = 0;
    int pushes = 0;
    int losses = 0;

    double mean = 0.0;
    double m2 = 0.0;

    for (int i = 1;
         i <= trials;
         i++)
    {
      double result =
          runSingleTrial(
              context,
              forcedFirstAction,
              continuationStrategy
          );

      if (result > 0)
        wins++;
      else if (result < 0)
        losses++;
      else
        pushes++;

      // Welford's algorithm.
      double delta =
          result - mean;

      mean +=
          delta / i;

      double delta2 =
          result - mean;

      m2 +=
          delta * delta2;
    }

    double standardDeviation =
        Math.sqrt(
            m2 / trials
        );

    return new SimulationResult(
        trials,
        mean,
        standardDeviation,
        wins,
        pushes,
        losses
    );
  }


  /*
   * Backward-compatible overload:
   * analyze each possible first action and then
   * continue with Basic Strategy.
   */
  public DecisionAnalysis analyzeDecision(
      PlayerDecisionContext context,
      int trialsPerAction)
  {
    return analyzeDecision(
        context,
        basicStrategy,
        trialsPerAction
    );
  }


  /*
   * Analyze every currently allowed first action,
   * then continue the rest of each simulated round
   * using the supplied strategy.
   */
  public DecisionAnalysis analyzeDecision(
      PlayerDecisionContext context,
      HandStrategy continuationStrategy,
      int trialsPerAction)
  {
    DecisionAnalysis analysis =
        new DecisionAnalysis();

    for (HandAction action :
         context.getAllowedActions())
    {
      SimulationResult result =
          runSimulation(
              context,
              action,
              continuationStrategy,
              trialsPerAction
          );

      analysis.addResult(
          action,
          result
      );
    }

    return analysis;
  }
}
