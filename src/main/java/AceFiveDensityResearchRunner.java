import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class AceFiveDensityResearchRunner
{
  private static final int ROUNDS =
      50_000_000;

  private static final long BASE_SEED =
      10_24_2026L;

  private static final Path OUTPUT_FILE =
      Path.of(
          "research-data",
          "scratch",
          "ace-five-density-8d-h17-das-ls-3to2-50m-rounds.csv"
      );


  private static final Map<
      StateKey,
      Accumulator
  > results =
      new LinkedHashMap<>();


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


    Files.createDirectories(
        OUTPUT_FILE.getParent()
    );


    GameSettings settings =
        createResearchSettings();


    Shoe shoe =
        new Shoe(
            settings.getDecksInShoe(),
            BASE_SEED
        );


    PlayerRoundEngine playerRoundEngine =
        new PlayerRoundEngine(
            settings
        );


    DealerPlayEngine dealerPlayEngine =
        new DealerPlayEngine(
            settings
        );


    BasicStrategy strategy =
        new BasicStrategy();


    AceFiveCount count =
        new AceFiveCount();


    for (int round = 0;
         round < ROUNDS;
         round++)
    {
      if (shoe.needsShuffle())
      {
        shoe.shuffle();

        count.reset();
      }


      /*
       * Capture the count BEFORE the next hand
       * is dealt. This is the information the
       * real player would have when choosing
       * the wager.
       */
      int remainingCards =
          shoe.getRemainingCardCount();


      int runningCount =
          (int) Math.round(
              count.getSignal(
                  remainingCards
              )
          );


      StateKey state =
          new StateKey(
              runningCount,
              remainingCards
          );


      double result =
          playRound(
              settings,
              playerRoundEngine,
              dealerPlayEngine,
              shoe,
              strategy,
              count
          );


      results
          .computeIfAbsent(
              state,
              ignored ->
                  new Accumulator()
          )
          .add(
              result
          );


      if ((round + 1)
          % 1_000_000
          == 0)
      {
        System.out.println(
            "Completed "
            + String.format(
                Locale.US,
                "%,d",
                round + 1
            )
            + " rounds..."
        );
      }
    }


    writeResults();


    System.out.println();

    System.out.println(
        "Total rounds: "
        + String.format(
            Locale.US,
            "%,d",
            ROUNDS
        )
    );


    System.out.println(
        "Exact states: "
        + String.format(
            Locale.US,
            "%,d",
            results.size()
        )
    );


    System.out.println(
        "Saved results to "
        + OUTPUT_FILE.toAbsolutePath()
    );
  }


  // ============================================================
  // ROUND
  // ============================================================

  private static double playRound(
      GameSettings settings,
      PlayerRoundEngine playerRoundEngine,
      DealerPlayEngine dealerPlayEngine,
      Shoe shoe,
      HandStrategy strategy,
      AceFiveCount count)
  {
    Map<Card, Boolean> observed =
        new IdentityHashMap<>();


    Hand playerHand =
        new Hand();


    Hand dealerHand =
        new Hand();


    // ----------------------------------------------------------
    // INITIAL DEAL
    // ----------------------------------------------------------

    Card playerCard1 =
        shoe.drawCard();


    playerHand.addCard(
        playerCard1
    );


    observeVisible(
        count,
        observed,
        playerCard1
    );


    Card dealerUpCard =
        shoe.drawCard();


    dealerHand.addCard(
        dealerUpCard
    );


    observeVisible(
        count,
        observed,
        dealerUpCard
    );


    Card playerCard2 =
        shoe.drawCard();


    playerHand.addCard(
        playerCard2
    );


    observeVisible(
        count,
        observed,
        playerCard2
    );


    /*
     * Dealer hole card is physically removed
     * from the shoe but is not observed yet.
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


    // ----------------------------------------------------------
    // NATURALS
    // ----------------------------------------------------------

    if (dealerBlackjack)
    {
      /*
       * Dealer blackjack reveals the hole card.
       */
      observeVisible(
          count,
          observed,
          dealerHoleCard
      );


      if (playerBlackjack)
      {
        return 0.0;
      }


      return -1.0;
    }


    if (playerBlackjack)
    {
      /*
       * Match the same visibility model used
       * in our Ace-only research: dealer hole
       * stays unobserved here.
       */
        return settings
            .getBlackjackPayout()
            .getTotalReturnMultiplier()
            - 1.0;
    }


    // ----------------------------------------------------------
    // PLAYER PLAY
    // ----------------------------------------------------------

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
                /*
                 * Unlimited research bankroll.
                 */
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
                observeVisible(
                    count,
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
                 * Replacement cards from a split
                 * may be dealt internally before
                 * this callback.
                 *
                 * Scan both exposed hands and
                 * identity-dedupe cards already
                 * counted.
                 */
                observeHand(
                    count,
                    observed,
                    originalState.getHand()
                );


                observeHand(
                    count,
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


    // ----------------------------------------------------------
    // DEALER PLAY
    // ----------------------------------------------------------

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
       * Dealer hole card is now visible.
       */
      observeVisible(
          count,
          observed,
          dealerHoleCard
      );


      dealerPlayEngine.play(
          dealerHand,
          shoe,
          card ->
              observeVisible(
                  count,
                  observed,
                  card
              )
      );
    }


    // ----------------------------------------------------------
    // SETTLEMENT
    // ----------------------------------------------------------

    double totalProfit =
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


      double wager =
          state.getWagerMultiplier();


      switch (outcome)
      {
        case WIN:
          totalProfit +=
              wager;
          break;


        case LOSS:
          totalProfit -=
              wager;
          break;


        case PUSH:
          break;


        case SURRENDER:
          /*
           * Surrender loses half of the ORIGINAL
           * one-unit wager.
           */
          totalProfit -=
              0.5;
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
    }


    return totalProfit;
  }


  // ============================================================
  // VISIBILITY
  // ============================================================

  private static void observeHand(
      AceFiveCount count,
      Map<Card, Boolean> observed,
      Hand hand)
  {
    for (int i = 0;
         i < hand.getNumCards();
         i++)
    {
      observeVisible(
          count,
          observed,
          hand.getCard(i)
      );
    }
  }


  private static void observeVisible(
      AceFiveCount count,
      Map<Card, Boolean> observed,
      Card card)
  {
    if (observed.containsKey(card))
    {
      return;
    }


    observed.put(
        card,
        true
    );


    count.observe(
        card
    );
  }


  // ============================================================
  // OUTPUT
  // ============================================================

  private static void writeResults()
      throws IOException
  {
    String header =
        String.join(
            ",",
            "ace_five_running_count",
            "cards_remaining",
            "decks_remaining",
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


    for (Map.Entry<
             StateKey,
             Accumulator
         > entry :
         results.entrySet())
    {
      StateKey state =
          entry.getKey();


      Summary summary =
          entry.getValue()
              .finish();


      double decksRemaining =
          state.remainingCards()
          / 52.0;


      String row =
          String.format(
              Locale.US,
              "%d,%d,%.10f,%d,"
              + "%.10f,%.10f,"
              + "%.10f,%.10f%n",
              state.runningCount(),
              state.remainingCards(),
              decksRemaining,
              summary.count(),
              summary.mean(),
              summary.mean()
                  * 25.0,
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

  private static GameSettings
      createResearchSettings()
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
        GameSettings.BlackjackPayout
            .THREE_TO_TWO
    );


    return settings;
  }


  // ============================================================
  // DATA
  // ============================================================

  private record StateKey(
      int runningCount,
      int remainingCards)
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
