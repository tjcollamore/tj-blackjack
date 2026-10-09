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
            hands.size()
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
              card ->
                  listener.cardDrawn(
                      state,
                      card
                  )
          );

    switch (result)
    {
      case FINISHED:
        break;

      case DOUBLED:
      {
        state.markDoubled();

        listener.handDoubled(
            state
        );

        break;
      }

      case SURRENDERED:
      {
        state.markSurrendered();

        listener.handSurrendered(
            state
        );

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

    listener.handFinished(
        state,
        i,
        hands.size()
    );
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
