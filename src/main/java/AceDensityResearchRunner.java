import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class AceDensityResearchRunner
{
  private static final int ROUNDS =
      50_000_000;
//    100_000;

  private static final int DECKS =
      8;

  private static final double BASE_BET =
      25.0;

  private static final long BASE_SEED =
      10_24_2026L;


  private static final Path OUTPUT_FILE =
      Path.of(
          "research-data",
          "scratch",
          "ace-density-8d-h17-das-ls-3to2-50m-rounds.csv"
      );


  public static void main(
      String[] args)
      throws IOException
  {
    if (Files.exists(
            OUTPUT_FILE))
    {
      throw new IllegalStateException(
          "Research output already exists: "
          + OUTPUT_FILE
      );
    }


    Files.createDirectories(
        OUTPUT_FILE.getParent()
    );


    GameSettings settings =
        createSettings();


    Map<StateKey, Accumulator> states =
        new LinkedHashMap<>();


    Shoe shoe =
        new Shoe(
            DECKS,
            BASE_SEED
        );


    AceOnlyCount aceCount =
        new AceOnlyCount();


    PlayerRoundEngine playerRoundEngine =
        new PlayerRoundEngine(
            settings
        );


    DealerPlayEngine dealerPlayEngine =
        new DealerPlayEngine(
            settings
        );


    BasicStrategy basicStrategy =
        new BasicStrategy();


    for (int round = 1;
         round <= ROUNDS;
         round++)
    {
      if (shoe.needsShuffle())
      {
        shoe.shuffle();

        aceCount.reset();
      }


      /*
       * Capture everything the player knows
       * BEFORE the wager and deal.
       */
      StateKey state =
          new StateKey(
              aceCount.getAcesSeen(),
              aceCount.getVisibleCardsSeen(),
              shoe.getRemainingCardCount()
          );


      double result =
          playRound(
              settings,
              shoe,
              aceCount,
              playerRoundEngine,
              dealerPlayEngine,
              basicStrategy
          );


      states.computeIfAbsent(
          state,
          ignored ->
              new Accumulator()
      ).add(
          result
      );


      if (round % 1_000_000 == 0)
      {
        System.out.println(
            "Completed "
            + String.format(
                Locale.US,
                "%,d",
                round
            )
            + " rounds..."
        );
      }
    }


    writeResults(
        states
    );


    System.out.println();

    System.out.println(
        "Saved results to "
        + OUTPUT_FILE.toAbsolutePath()
    );
  }


  private static double playRound(
      GameSettings settings,
      Shoe shoe,
      AceOnlyCount aceCount,
      PlayerRoundEngine playerRoundEngine,
      DealerPlayEngine dealerPlayEngine,
      BasicStrategy strategy)
  {
    /*
     * Identity tracking prevents us from counting
     * the same visible Card object twice.
     */
    Map<Card, Boolean> observed =
        new IdentityHashMap<>();


    Hand playerHand =
        new Hand();


    Hand dealerHand =
        new Hand();


    // ------------------------------------------------------------
    // INITIAL DEAL
    // ------------------------------------------------------------

    Card playerCard1 =
        shoe.drawCard();

    playerHand.addCard(
        playerCard1
    );

    observeVisible(
        aceCount,
        observed,
        playerCard1
    );


    Card dealerUpCard =
        shoe.drawCard();

    dealerHand.addCard(
        dealerUpCard
    );

    observeVisible(
        aceCount,
        observed,
        dealerUpCard
    );


    Card playerCard2 =
        shoe.drawCard();

    playerHand.addCard(
        playerCard2
    );

    observeVisible(
        aceCount,
        observed,
        playerCard2
    );


    /*
     * Dealer hole card is physically removed
     * from the shoe, but is NOT counted yet.
     */
    Card dealerHoleCard =
        shoe.drawCard();

    dealerHand.addCard(
        dealerHoleCard
    );


    boolean playerBlackjack =
        playerHand.isBlackjack();


    boolean dealerBlackjack =
        dealerHand.isBlackjack();


    // ------------------------------------------------------------
    // NATURALS
    // ------------------------------------------------------------

    if (dealerBlackjack)
    {
      /*
       * The hole card is revealed.
       */
      observeVisible(
          aceCount,
          observed,
          dealerHoleCard
      );


      if (playerBlackjack)
        return 0.0;


      return -1.0;
    }


    if (playerBlackjack)
    {
      /*
       * Mirror the existing research model:
       * the dealer hole card is not treated as
       * observed when the round resolves here.
       */
      return 1.5;
    }


    // ------------------------------------------------------------
    // PLAYER PLAY
    // ------------------------------------------------------------

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
                return true;
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
                observeVisible(
                    aceCount,
                    observed,
                    card
                );
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
                /*
                 * HandSplitEngine deals replacement
                 * cards internally before this callback.
                 *
                 * Inspect both hands and count any
                 * newly visible Card objects.
                 */
                observeHand(
                    aceCount,
                    observed,
                    originalState.getHand()
                );

                observeHand(
                    aceCount,
                    observed,
                    newState.getHand()
                );
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


    // ------------------------------------------------------------
    // DEALER PLAY
    // ------------------------------------------------------------

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
      /*
       * Dealer hole card becomes visible before
       * subsequent dealer draws.
       */
      observeVisible(
          aceCount,
          observed,
          dealerHoleCard
      );


      dealerPlayEngine.play(
          dealerHand,
          shoe,
          card ->
              observeVisible(
                  aceCount,
                  observed,
                  card
              )
      );
    }


    // ------------------------------------------------------------
    // SETTLEMENT IN WAGER UNITS
    // ------------------------------------------------------------

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
          handResult =
              1.0;
          break;


        case LOSS:
          handResult =
              -1.0;
          break;


        case PUSH:
          handResult =
              0.0;
          break;


        case SURRENDER:
          handResult =
              -0.5;
          break;


        case BLACKJACK:
          throw new IllegalStateException(
              "Natural blackjack should have "
              + "been resolved before player play"
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


  // ============================================================
  // VISIBILITY
  // ============================================================

  private static void observeHand(
      AceOnlyCount aceCount,
      Map<Card, Boolean> observed,
      Hand hand)
  {
    for (int i = 0;
         i < hand.getNumCards();
         i++)
    {
      observeVisible(
          aceCount,
          observed,
          hand.getCard(i)
      );
    }
  }


  private static void observeVisible(
      AceOnlyCount aceCount,
      Map<Card, Boolean> observed,
      Card card)
  {
    if (observed.containsKey(
            card))
    {
      return;
    }


    observed.put(
        card,
        true
    );


    aceCount.observe(
        card
    );
  }


  // ============================================================
  // OUTPUT
  // ============================================================

  private static void writeResults(
      Map<StateKey, Accumulator> states)
      throws IOException
  {
    String header =
        String.join(
            ",",
            "aces_seen",
            "visible_cards_seen",
            "cards_remaining",
            "decks_remaining",
            "expected_aces_seen",
            "ace_running_surplus",
            "ace_true_surplus",
            "rounds",
            "mean_ev_units",
            "mean_ev_dollars_25",
            "standard_deviation_units",
            "standard_error_units"
        )
        + System.lineSeparator();


    Files.writeString(
        OUTPUT_FILE,
        header,
        StandardOpenOption.CREATE
    );


    for (Map.Entry<StateKey, Accumulator> entry :
         states.entrySet())
    {
      StateKey state =
          entry.getKey();


      Summary summary =
          entry.getValue()
              .finish();


      double decksRemaining =
          state.cardsRemaining()
          / 52.0;


      double expectedAcesSeen =
          state.visibleCardsSeen()
          / 13.0;


      double runningSurplus =
          expectedAcesSeen
          - state.acesSeen();


      double trueSurplus =
          decksRemaining <= 0.0
              ? 0.0
              : runningSurplus
                / decksRemaining;


      String row =
          String.format(
              Locale.US,
              "%d,%d,%d,"
              + "%.10f,%.10f,%.10f,%.10f,"
              + "%d,%.10f,%.10f,%.10f,%.10f%n",
              state.acesSeen(),
              state.visibleCardsSeen(),
              state.cardsRemaining(),
              decksRemaining,
              expectedAcesSeen,
              runningSurplus,
              trueSurplus,
              summary.count(),
              summary.mean(),
              summary.mean()
                  * BASE_BET,
              summary.standardDeviation(),
              summary.standardError()
          );


      Files.writeString(
          OUTPUT_FILE,
          row,
          StandardOpenOption.APPEND
      );
    }
  }


  // ============================================================
  // SETTINGS
  // ============================================================

  private static GameSettings createSettings()
  {
    GameSettings settings =
        new GameSettings();


    settings.setDecksInShoe(
        DECKS
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
        GameSettings.BlackjackPayout.THREE_TO_TWO
    );


    return settings;
  }


  // ============================================================
  // DATA
  // ============================================================

  private record StateKey(
      int acesSeen,
      int visibleCardsSeen,
      int cardsRemaining)
  {
  }


  private record Summary(
      long count,
      double mean,
      double standardDeviation,
      double standardError)
  {
  }


  private static class Accumulator
  {
    private long count;

    private double mean;
    private double m2;


    void add(
        double value)
    {
      count++;


      double delta =
          value
          - mean;


      mean +=
          delta
          / count;


      double delta2 =
          value
          - mean;


      m2 +=
          delta
          * delta2;
    }


    Summary finish()
    {
      double sd =
          count <= 1
              ? 0.0
              : Math.sqrt(
                  m2
                  / (count - 1)
              );


      double se =
          count == 0
              ? 0.0
              : sd
                / Math.sqrt(
                    count
                );


      return new Summary(
          count,
          mean,
          sd,
          se
      );
    }
  }
}
