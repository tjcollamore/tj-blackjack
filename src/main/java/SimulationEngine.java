import java.util.List;

public class SimulationEngine
{
  private final GameSettings settings;
  private final BasicStrategy basicStrategy = new BasicStrategy();
  private final DealerPlayEngine dealerPlayEngine;
  private final PlayerRoundEngine playerRoundEngine;
    
  public SimulationEngine(GameSettings settings)
  {
    this.settings = settings;
    playerRoundEngine = new PlayerRoundEngine(settings);
    dealerPlayEngine = new DealerPlayEngine(settings);
  }

    public double runSingleTrial(
        PlayerDecisionContext context,
        HandAction forcedFirstAction)
    {
      while (true)
      {
        Shoe shoe =
            createFreshShoe(context);

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

        // Decision Help only exists after the live
        // round has ruled out dealer blackjack.
        // Therefore simulation trials must be
        // conditioned on dealer NOT having blackjack.
        if (dealerHand.isBlackjack())
          continue;

          ForcedFirstActionStrategy strategy =
              new ForcedFirstActionStrategy(
                  forcedFirstAction,
                  basicStrategy
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
                  return context.getWagerAmount();
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
                    int handCount)
                {
                }
              }
          ); 

          boolean dealerNeedsToPlay = false;

          for (PlayerHandState state :
               playerStates)
          {
            if (!state.getHand().isBusted()
                && !state.didSurrender())
            {
              dealerNeedsToPlay = true;
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
          
      double totalResult = 0.0;

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

    shoe.removeCard(
        playerHand.getCard(0)
    );

    shoe.removeCard(
        playerHand.getCard(1)
    );

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

    
    public SimulationResult runSimulation(
        PlayerDecisionContext context, HandAction forcedFirstAction,
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

      for (int i = 1; i <= trials; i++)
      {
        double result =
            runSingleTrial(context, forcedFirstAction);

        if (result > 0)
          wins++;
        else if (result < 0)
          losses++;
        else
          pushes++;

        // Welford's algorithm:
        // keeps a running mean and variance
        // without storing every trial result.
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

    public DecisionAnalysis analyzeDecision(
        PlayerDecisionContext context,
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