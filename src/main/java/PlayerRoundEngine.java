import java.util.ArrayList;
import java.util.List;

public class PlayerRoundEngine
{
  private static final int MAX_HANDS = 4;

  private final PlayerHandEngine playerHandEngine;
  private final HandSplitEngine handSplitEngine;

  public PlayerRoundEngine(
      GameSettings settings)
  {
    playerHandEngine =
        new PlayerHandEngine(
            settings
        );

    handSplitEngine =
        new HandSplitEngine();
  }

    public List<PlayerHandState> play(
        PlayerHandState initialState,
        Card dealerUpCard,
        HandStrategy strategy,
        Shoe shoe,
        PlayerRoundListener listener)
  {
    List<PlayerHandState> hands =
        new ArrayList<>();

    hands.add(initialState);

    for (int i = 0;
         i < hands.size();
         i++)
    {
      PlayerHandState state =
          hands.get(i);

      listener.handStarted(
          state,
          i,
          hands.size()
      );

      // Split aces receive exactly one new card
      // and are then finished.
      if (state.didSplitAces())
      {
        listener.handFinished(
            state,
            i,
            hands.size(),
            HandFinishReason.SPLIT_ACES
        );

        continue;
      }
    
      PlayerHandPlayResult result =
          playerHandEngine.play(
              state.getHand(),
              dealerUpCard,
              strategy,
              shoe,
              state.cameFromSplit(),
              listener.canDoubleOrSplit(
                  state
              ),
              listener.getWagerAmount(
                  state
              ),
              () ->
                  listener.handDoubled(
                      state
                  ),
              card ->
                  listener.cardDrawn(
                      state,
                      card
                  )
          );

    HandFinishReason finishReason = null;
    switch (result)
    {
      case FINISHED:
        finishReason = HandFinishReason.STAND;
        break;

      case CARD_TERMINATED:
      {
        finishReason = HandFinishReason.CARD_TERMINATED;
        break;
      }
        
      case DOUBLED:
      {
        state.markDoubled();
        finishReason = HandFinishReason.DOUBLE;
        break;
      }

      case SURRENDERED:
      {
        state.markSurrendered();
        listener.handSurrendered(state);
        finishReason = HandFinishReason.SURRENDER;
        break;
      }

      case SPLIT_REQUESTED:
      {
        HandSplitResult splitResult =
            handSplitEngine.split(
                state.getHand(),
                shoe
            );

        PlayerHandState newState =
            new PlayerHandState(
                splitResult.getNewHand()
            );

        state.setCameFromSplit(true);
        newState.setCameFromSplit(true);

        state.setSplitAces(
            splitResult.didSplitAces()
        );

        newState.setSplitAces(
            splitResult.didSplitAces()
        );

        hands.add(newState);

        listener.handSplit(
            state,
            newState,
            splitResult
        );

        if (hands.size() == MAX_HANDS)
        {
          blockSplitting(
              hands
          );
        }

        i--;

        continue;
      }
    }

    if (finishReason == null)
    {
      throw new IllegalStateException(
          "Hand finished without a finish reason"
      );
    }
        
    listener.handFinished(state, i, hands.size(), finishReason);
    }

    return hands;
  }

  private void blockSplitting(
      List<PlayerHandState> hands)
  {
    for (PlayerHandState state :
         hands)
    {
      state.getHand()
          .blockSplitting();
    }
  }
}
