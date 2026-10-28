import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.List;

public class StrategyFrequencyRunner
{
  private static final int ROUNDS = 10_000_000;

  private static final Path OUTPUT_FILE =
      Path.of(
          "research-data",
          "scratch",
          "frequency-basicstrategy-compositions-8d-h17-das-ls-6to5-10m-rounds.csv"
      );

  private static final Map<
      DecisionKey,
      Long
  > counts =
      new LinkedHashMap<>();

  private static long totalDecisions = 0;

  public static void main(String[] args)
      throws IOException
  {
    if (Files.exists(OUTPUT_FILE))
    {
      throw new IllegalStateException(
          "Research output already exists: "
          + OUTPUT_FILE
      );
    }

    GameSettings settings =
        createResearchSettings();

    Shoe shoe =
        new Shoe(
            settings.getDecksInShoe()
        );

    BasicStrategy basicStrategy =
        new BasicStrategy();

    PlayerRoundEngine playerRoundEngine =
        new PlayerRoundEngine(
            settings
        );

    DealerPlayEngine dealerPlayEngine =
        new DealerPlayEngine(
            settings
        );

    HandStrategy recordingStrategy =
        context ->
        {
          HandAction action =
              basicStrategy
                  .chooseRecommendedAction(
                      context
                  );

          recordDecision(
              context,
              action
          );

          return action;
        };

    for (int round = 0;
         round < ROUNDS;
         round++)
    {
      if (shoe.needsShuffle())
      {
        shoe.shuffle();
      }

      playRound(
          shoe,
          settings,
          playerRoundEngine,
          dealerPlayEngine,
          recordingStrategy
      );

      if ((round + 1) % 100_000 == 0)
      {
        System.out.println(
            "Completed "
            + (round + 1)
            + " rounds..."
        );
      }
    }

    writeResults();

    System.out.println(
        "Total rounds: "
        + ROUNDS
    );

    System.out.println(
        "Total player decisions: "
        + totalDecisions
    );

    System.out.println(
        "Saved results to "
        + OUTPUT_FILE.toAbsolutePath()
    );
  }

  private static void playRound(
      Shoe shoe,
      GameSettings settings,
      PlayerRoundEngine playerRoundEngine,
      DealerPlayEngine dealerPlayEngine,
      HandStrategy strategy)
  {
    Hand playerHand =
        new Hand();

    Hand dealerHand =
        new Hand();

    // Same dealing order as live Round.
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

    // The live game does not allow player
    // decisions if dealer has blackjack.
    if (dealerHand.isBlackjack())
    {
      return;
    }

    boolean playerNaturalBlackjack =
        playerHand.isBlackjack();

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
                // Research has no bankroll limitation.
                return true;
              }

              @Override
              public double getWagerAmount(
                  PlayerHandState state)
              {
                return 1.0;
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

    /*
     * Natural blackjack ends the player side
     * immediately. The live game does not make
     * the dealer draw out the hand afterward.
     */
    if (playerNaturalBlackjack)
    {
      return;
    }

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
  }

  private static void recordDecision(
      PlayerDecisionContext context,
      HandAction basicStrategyAction)
  {
    Hand hand =
        context.getPlayerHand();

    String handType;

    Integer pairValue =
        null;

    if (hand.isSplittable())
    {
      handType = "PAIR";

      pairValue =
          hand.getCard(0)
              .getPointValue();
    }
    else if (hand.isSoft())
    {
      handType = "SOFT";
    }
    else
    {
      handType = "HARD";
    }

    DecisionKey key =
        new DecisionKey(
            handType,
            hand.getScore(),
            pairValue,
            context.getDealerUpCard()
                .getPointValue(),
            hand.getNumCards(),
            context.cameFromSplit(),
            getComposition(hand),
            basicStrategyAction
        );

    counts.merge(
        key,
        1L,
        Long::sum
    );

    totalDecisions++;
  }

  private static String getComposition(
      Hand hand)
  {
    List<Rank> ranks =
        new ArrayList<>();

    for (int i = 0;
         i < hand.getNumCards();
         i++)
    {
      ranks.add(
          hand.getCard(i)
              .getRank()
      );
    }

    /*
     * Sort the ranks so card order does not
     * create separate frequency buckets.
     *
     * For example:
     * TWO + TEN
     * and
     * TEN + TWO
     *
     * become the same composition.
     */
    ranks.sort(
        Comparator
            .comparingInt(
                Rank::getValue
            )
            .thenComparing(
                Rank::name
            )
    );

    StringBuilder result =
        new StringBuilder();

    for (int i = 0;
         i < ranks.size();
         i++)
    {
      if (i > 0)
      {
        result.append("+");
      }

      result.append(
          ranks.get(i)
              .name()
      );
    }

    return result.toString();
  }

  private static void writeResults()
      throws IOException
  {
    Files.createDirectories(
        OUTPUT_FILE.getParent()
    );

    Files.writeString(
        OUTPUT_FILE,
        String.join(
            ",",
            "hand_type",
            "player_total",
            "pair_value",
            "dealer_value",
            "card_count",
            "came_from_split",
            "player_composition",
            "basic_strategy_action",
            "occurrences",
            "occurrences_per_round",
            "share_of_decisions"
        )
        + System.lineSeparator(),
        StandardOpenOption.CREATE
    );

    for (Map.Entry<
        DecisionKey,
        Long
    > entry :
        counts.entrySet())
    {
      DecisionKey key =
          entry.getKey();

      long occurrences =
          entry.getValue();

      double occurrencesPerRound =
          (double) occurrences
          / ROUNDS;

      double shareOfDecisions =
          (double) occurrences
          / totalDecisions;

      String pairValue =
          key.pairValue() == null
          ? ""
          : key.pairValue().toString();

      String row =
          String.format(
              Locale.US,
              "%s,%d,%s,%d,%d,%b,%s,%s,%d,%.10f,%.10f",
              key.handType(),
              key.playerTotal(),
              pairValue,
              key.dealerValue(),
              key.cardCount(),
              key.cameFromSplit(),
              key.playerComposition(),
              key.basicStrategyAction()
                  .name(),
              occurrences,
              occurrencesPerRound,
              shareOfDecisions
          );

      Files.writeString(
          OUTPUT_FILE,
          row + System.lineSeparator(),
          StandardOpenOption.APPEND
      );
    }
  }

  private static GameSettings
      createResearchSettings()
  {
    GameSettings settings =
        new GameSettings();

    settings.setDecksInShoe(8);
    settings.setDealerHitsSoft17(true);
    settings.setDoubleAfterSplit(true);
    settings.setAllowSurrender(true);

    settings.setBlackjackPayout(
        GameSettings.BlackjackPayout.SIX_TO_FIVE
    );

    return settings;
  }

  private record DecisionKey(
      String handType,
      int playerTotal,
      Integer pairValue,
      int dealerValue,
      int cardCount,
      boolean cameFromSplit,
      String playerComposition,
      HandAction basicStrategyAction)
  {
  }
}
