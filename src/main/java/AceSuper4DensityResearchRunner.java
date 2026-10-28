import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AceSuper4DensityResearchRunner
{
  private static final int ROUNDS =
      50_000_000;

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
          "ace-super4-density-8d-h17-das-ls-3to2-50m-rounds.csv"
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

    Map<StateKey, OutcomeAccumulator> states =
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
       * Capture the exact state known to the player
       * BEFORE the wager and initial deal.
       */
      StateKey state =
          new StateKey(
              aceCount.getAcesSeen(),
              aceCount.getVisibleCardsSeen(),
              shoe.getRemainingCardCount()
          );

      Super4Outcome outcome =
          playRoundAndClassifySuper4(
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
              new OutcomeAccumulator()
      ).add(
          outcome
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


  private static Super4Outcome
      playRoundAndClassifySuper4(
          GameSettings settings,
          Shoe shoe,
          AceOnlyCount aceCount,
          PlayerRoundEngine playerRoundEngine,
          DealerPlayEngine dealerPlayEngine,
          BasicStrategy strategy)
  {
    /*
     * Identity tracking prevents counting the same
     * visible Card object twice.
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
     * Dealer hole card is physically removed from the
     * shoe but is not visible yet.
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


    /*
     * Super 4 is determined by the original four cards.
     * Classify it now, but continue the blackjack round
     * afterward so shoe progression / visibility stays
     * identical to normal play.
     */
    Super4Outcome super4Outcome =
        classifySuper4(
            playerCard1,
            playerCard2,
            dealerUpCard,
            dealerHoleCard,
            dealerBlackjack
        );


    // ------------------------------------------------------------
    // NATURALS
    // ------------------------------------------------------------

    if (dealerBlackjack)
    {
      observeVisible(
          aceCount,
          observed,
          dealerHoleCard
      );

      return super4Outcome;
    }


    if (playerBlackjack)
    {
      /*
       * Mirror the existing AceDensityResearchRunner:
       * dealer hole card remains unobserved here.
       */
      return super4Outcome;
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
                 * Split replacement cards are dealt inside
                 * the split engine before this callback.
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


    return super4Outcome;
  }


  // ============================================================
  // SUPER 4 CLASSIFICATION
  // ============================================================

  private static Super4Outcome classifySuper4(
      Card player1,
      Card player2,
      Card dealerUp,
      Card dealerHole,
      boolean dealerBlackjack)
  {
    if (!dealerBlackjack)
    {
      if (dealerUp.getRank()
          == Rank.ACE)
      {
        return Super4Outcome.ACE_NO_BJ;
      }

      return Super4Outcome.LOSS;
    }


    Card[] cards =
    {
        player1,
        player2,
        dealerUp,
        dealerHole
    };


    if (isRoyal(cards))
    {
      return isSelectedSuitRoyal(cards)
          ? Super4Outcome.ROYAL_MAJOR
          : Super4Outcome.ROYAL_OTHER;
    }


    int maxRankCount =
        maxRankCount(
            cards
        );


    /*
     * Match the previous practical model:
     * 4 of a kind satisfies the 3-of-a-kind category.
     */
    if (maxRankCount >= 3)
    {
      return Super4Outcome.TRIPS;
    }


    if (isStraight(cards))
    {
      return Super4Outcome.STRAIGHT;
    }


    if (isFlush(cards))
    {
      return Super4Outcome.FLUSH;
    }


    if (isTwoPair(cards))
    {
      return Super4Outcome.TWO_PAIR;
    }


    if (isSameColor(cards))
    {
      return Super4Outcome.SAME_COLOR;
    }


    if (maxRankCount >= 2)
    {
      return Super4Outcome.PAIR;
    }


    return Super4Outcome.OTHER_BJ;
  }


  private static boolean isRoyal(
      Card[] cards)
  {
    if (!isFlush(cards))
    {
      return false;
    }

    boolean ace =
        false;

    boolean king =
        false;

    boolean queen =
        false;

    boolean jack =
        false;


    for (Card card :
         cards)
    {
      switch (card.getRank())
      {
        case ACE:
          ace = true;
          break;

        case KING:
          king = true;
          break;

        case QUEEN:
          queen = true;
          break;

        case JACK:
          jack = true;
          break;

        default:
          return false;
      }
    }


    return ace
        && king
        && queen
        && jack;
  }


  /*
   * The specific "major" suit is arbitrary for probability
   * purposes. Spades are used consistently here.
   */
  private static boolean isSelectedSuitRoyal(
      Card[] cards)
  {
    if (!isRoyal(cards))
    {
      return false;
    }

    return suitName(
        cards[0]
    ).contains("SPADE");
  }


  private static int maxRankCount(
      Card[] cards)
  {
    Map<Rank, Integer> counts =
        new EnumMap<>(
            Rank.class
        );

    int max =
        0;


    for (Card card :
         cards)
    {
      Rank rank =
          card.getRank();

      int count =
          counts.getOrDefault(
              rank,
              0
          ) + 1;

      counts.put(
          rank,
          count
      );

      max =
          Math.max(
              max,
              count
          );
    }


    return max;
  }


  private static boolean isTwoPair(
      Card[] cards)
  {
    Map<Rank, Integer> counts =
        new EnumMap<>(
            Rank.class
        );


    for (Card card :
         cards)
    {
      Rank rank =
          card.getRank();

      counts.put(
          rank,
          counts.getOrDefault(
              rank,
              0
          ) + 1
      );
    }


    int pairs =
        0;


    for (int count :
         counts.values())
    {
      if (count == 2)
      {
        pairs++;
      }
    }


    return pairs == 2;
  }


  private static boolean isStraight(
      Card[] cards)
  {
    int[] values =
        new int[4];


    for (int i = 0;
         i < 4;
         i++)
    {
      values[i] =
          pokerValue(
              cards[i].getRank()
          );
    }


    java.util.Arrays.sort(
        values
    );


    for (int i = 1;
         i < values.length;
         i++)
    {
      if (values[i]
          == values[i - 1])
      {
        return false;
      }
    }


    if (values[1]
            == values[0] + 1
        && values[2]
            == values[1] + 1
        && values[3]
            == values[2] + 1)
    {
      return true;
    }


    /*
     * A-2-3-4
     */
    return values[0] == 2
        && values[1] == 3
        && values[2] == 4
        && values[3] == 14;
  }


  private static int pokerValue(
      Rank rank)
  {
    switch (rank)
    {
      case TWO:
        return 2;

      case THREE:
        return 3;

      case FOUR:
        return 4;

      case FIVE:
        return 5;

      case SIX:
        return 6;

      case SEVEN:
        return 7;

      case EIGHT:
        return 8;

      case NINE:
        return 9;

      case TEN:
        return 10;

      case JACK:
        return 11;

      case QUEEN:
        return 12;

      case KING:
        return 13;

      case ACE:
        return 14;

      default:
        throw new IllegalStateException(
            "Unexpected rank: "
            + rank
        );
    }
  }


  private static boolean isFlush(
      Card[] cards)
  {
    String suit =
        suitName(
            cards[0]
        );


    for (int i = 1;
         i < cards.length;
         i++)
    {
      if (!suit.equals(
          suitName(
              cards[i]
          )))
      {
        return false;
      }
    }


    return true;
  }


  private static boolean isSameColor(
      Card[] cards)
  {
    String first =
        colorName(
            cards[0]
        );


    for (int i = 1;
         i < cards.length;
         i++)
    {
      if (!first.equals(
          colorName(
              cards[i]
          )))
      {
        return false;
      }
    }


    return true;
  }


  /*
   * Keeps this runner independent of the exact
   * compile-time Suit enum API.
   */
  private static String suitName(
      Card card)
  {
    try
    {
      Method method =
          card.getClass()
              .getMethod(
                  "getSuit"
              );

      Object suit =
          method.invoke(
              card
          );

      return String.valueOf(
          suit
      )
      .toUpperCase(
          Locale.US
      );
    }
    catch (ReflectiveOperationException e)
    {
      throw new IllegalStateException(
          "Card.getSuit() required",
          e
      );
    }
  }


  private static String colorName(
      Card card)
  {
    String suit =
        suitName(
            card
        );


    if (suit.contains("HEART")
        || suit.contains("DIAMOND"))
    {
      return "RED";
    }


    if (suit.contains("SPADE")
        || suit.contains("CLUB"))
    {
      return "BLACK";
    }


    throw new IllegalStateException(
        "Unknown suit: "
        + suit
    );
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
      Map<StateKey, OutcomeAccumulator> states)
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
            "loss",
            "ace_no_bj",
            "other_bj",
            "pair",
            "same_color",
            "two_pair",
            "flush",
            "straight",
            "trips",
            "royal_major",
            "royal_other"
        )
        + System.lineSeparator();


    Files.writeString(
        OUTPUT_FILE,
        header,
        StandardOpenOption.CREATE
    );


    for (Map.Entry<StateKey, OutcomeAccumulator> entry :
         states.entrySet())
    {
      StateKey state =
          entry.getKey();

      OutcomeAccumulator a =
          entry.getValue();

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
              + "%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d%n",
              state.acesSeen(),
              state.visibleCardsSeen(),
              state.cardsRemaining(),
              decksRemaining,
              expectedAcesSeen,
              runningSurplus,
              trueSurplus,
              a.total(),
              a.count(
                  Super4Outcome.LOSS
              ),
              a.count(
                  Super4Outcome.ACE_NO_BJ
              ),
              a.count(
                  Super4Outcome.OTHER_BJ
              ),
              a.count(
                  Super4Outcome.PAIR
              ),
              a.count(
                  Super4Outcome.SAME_COLOR
              ),
              a.count(
                  Super4Outcome.TWO_PAIR
              ),
              a.count(
                  Super4Outcome.FLUSH
              ),
              a.count(
                  Super4Outcome.STRAIGHT
              ),
              a.count(
                  Super4Outcome.TRIPS
              ),
              a.count(
                  Super4Outcome.ROYAL_MAJOR
              ),
              a.count(
                  Super4Outcome.ROYAL_OTHER
              )
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

  private enum Super4Outcome
  {
    LOSS,
    ACE_NO_BJ,
    OTHER_BJ,
    PAIR,
    SAME_COLOR,
    TWO_PAIR,
    FLUSH,
    STRAIGHT,
    TRIPS,
    ROYAL_MAJOR,
    ROYAL_OTHER
  }


  private record StateKey(
      int acesSeen,
      int visibleCardsSeen,
      int cardsRemaining)
  {
  }


  private static class OutcomeAccumulator
  {
    private final long[] counts =
        new long[
            Super4Outcome.values().length
        ];


    void add(
        Super4Outcome outcome)
    {
      counts[
          outcome.ordinal()
      ]++;
    }


    long count(
        Super4Outcome outcome)
    {
      return counts[
          outcome.ordinal()
      ];
    }


    long total()
    {
      long total =
          0;

      for (long count :
           counts)
      {
        total +=
            count;
      }

      return total;
    }
  }
}
