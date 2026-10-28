import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class Super4CompositionResearchRunner
{
  /*
   * Same scale as our Ace-density and
   * Ace/Five calibration runs.
   */
  private static final int ROUNDS =
      50_000_000;


  private static final int DECKS =
      8;


  private static final long BASE_SEED =
      10_24_2026L;


  /*
   * Penetration:
   * bucket decks remaining in 0.5-deck bins.
   *
   * Example:
   * 3.73 decks -> 3.5 bucket
   */
  private static final double
      DECK_BUCKET_SIZE =
          0.5;


  /*
   * Ace surplus:
   * expected Aces seen - actual Aces seen.
   *
   * Bucket in 0.5-Ace increments.
   */
  private static final double
      ACE_SURPLUS_BUCKET_SIZE =
          0.5;


  /*
   * Ten-value surplus has a wider numerical
   * range because 4/13 of cards are ten-valued.
   *
   * Use 1-card buckets for the first pass.
   */
  private static final double
      TEN_SURPLUS_BUCKET_SIZE =
          1.0;


  private static final Path OUTPUT_FILE =
      Path.of(
          "research-data",
          "scratch",
          "super4-composition-8d-50m.csv"
      );


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
            DECKS,
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


    HandStrategy strategy =
        new BasicStrategy();


    CountTracker tracker =
        new CountTracker();


    Map<StateKey, OutcomeAccumulator>
        baseline =
            new HashMap<>();


    Map<StateKey, OutcomeAccumulator>
        aceOnly =
            new HashMap<>();


    Map<StateKey, OutcomeAccumulator>
        aceFive =
            new HashMap<>();


    Map<StateKey, OutcomeAccumulator>
        aceTen =
            new HashMap<>();


    for (int round = 1;
         round <= ROUNDS;
         round++)
    {
      if (shoe.needsShuffle())
      {
        shoe.shuffle();

        tracker.reset();
      }


      /*
       * IMPORTANT:
       *
       * Capture state BEFORE the current
       * round is dealt.
       *
       * That is the information the player
       * would actually possess when deciding
       * whether to make a Super 4 wager.
       */
      PreDealState state =
          createPreDealState(
              shoe,
              tracker
          );


      Super4Outcome outcome =
          playRoundAndGetSuper4Outcome(
              playerRoundEngine,
              dealerPlayEngine,
              shoe,
              strategy,
              tracker
          );


      addResult(
          baseline,
          new StateKey(
              SignalType.BASELINE,
              state.decksRemainingBucket(),
              null,
              null,
              null
          ),
          outcome
      );


      addResult(
          aceOnly,
          new StateKey(
              SignalType.ACE_ONLY,
              state.decksRemainingBucket(),
              state.aceSurplusBucket(),
              null,
              null
          ),
          outcome
      );


      addResult(
          aceFive,
          new StateKey(
              SignalType.ACE_FIVE,
              state.decksRemainingBucket(),
              null,
              state.aceFiveRunningCount(),
              null
          ),
          outcome
      );


      addResult(
          aceTen,
          new StateKey(
              SignalType.ACE_TEN,
              state.decksRemainingBucket(),
              state.aceSurplusBucket(),
              null,
              state.tenSurplusBucket()
          ),
          outcome
      );


      if (round % 5_000_000 == 0)
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


    writeHeader();


    writeMap(
        baseline
    );


    writeMap(
        aceOnly
    );


    writeMap(
        aceFive
    );


    writeMap(
        aceTen
    );


    System.out.println();

    System.out.println(
        "========================================"
    );

    System.out.println(
        "SUPER 4 COMPOSITION CALIBRATION COMPLETE"
    );

    System.out.println(
        "========================================"
    );


    System.out.println(
        "Rounds: "
        + String.format(
            Locale.US,
            "%,d",
            ROUNDS
        )
    );


    System.out.println(
        "Baseline states: "
        + baseline.size()
    );


    System.out.println(
        "Ace-only states: "
        + aceOnly.size()
    );


    System.out.println(
        "Ace/Five states: "
        + aceFive.size()
    );


    System.out.println(
        "Ace+Ten states: "
        + aceTen.size()
    );


    System.out.println();

    System.out.println(
        "Saved results to "
        + OUTPUT_FILE.toAbsolutePath()
    );
  }


  // ============================================================
  // PRE-DEAL STATE
  // ============================================================

  private static PreDealState createPreDealState(
      Shoe shoe,
      CountTracker tracker)
  {
    double decksRemaining =
        shoe.getRemainingCardCount()
        / 52.0;


    double deckBucket =
        bucket(
            decksRemaining,
            DECK_BUCKET_SIZE
        );


    double aceSurplus =
        tracker.getAceRunningSurplus();


    double aceSurplusBucket =
        bucket(
            aceSurplus,
            ACE_SURPLUS_BUCKET_SIZE
        );


    double tenSurplus =
        tracker.getTenRunningSurplus();


    double tenSurplusBucket =
        bucket(
            tenSurplus,
            TEN_SURPLUS_BUCKET_SIZE
        );


    return new PreDealState(
        deckBucket,
        aceSurplusBucket,
        tracker.getAceFiveRunningCount(),
        tenSurplusBucket
    );
  }


  private static double bucket(
      double value,
      double bucketSize)
  {
    return Math.floor(
        value / bucketSize
    )
    * bucketSize;
  }


  // ============================================================
  // PLAY ONE REALISTIC BLACKJACK ROUND
  // ============================================================

  private static Super4Outcome
      playRoundAndGetSuper4Outcome(
          PlayerRoundEngine playerRoundEngine,
          DealerPlayEngine dealerPlayEngine,
          Shoe shoe,
          HandStrategy strategy,
          CountTracker tracker)
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
        tracker,
        observed,
        playerCard1
    );


    Card dealerUpCard =
        shoe.drawCard();


    dealerHand.addCard(
        dealerUpCard
    );


    observeVisible(
        tracker,
        observed,
        dealerUpCard
    );


    Card playerCard2 =
        shoe.drawCard();


    playerHand.addCard(
        playerCard2
    );


    observeVisible(
        tracker,
        observed,
        playerCard2
    );


    /*
     * Dealer hole card is physically removed
     * from the shoe but remains unseen.
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
     * Super 4 is resolved from the INITIAL
     * four cards.
     */
    Super4Outcome super4Outcome =
        evaluateSuper4(
            playerCard1,
            playerCard2,
            dealerUpCard,
            dealerHoleCard,
            dealerBlackjack
        );


    // ----------------------------------------------------------
    // DEALER BLACKJACK
    // ----------------------------------------------------------

    if (dealerBlackjack)
    {
      /*
       * Hole card becomes publicly visible.
       */
      observeVisible(
          tracker,
          observed,
          dealerHoleCard
      );


      return super4Outcome;
    }


    // ----------------------------------------------------------
    // PLAYER BLACKJACK
    // ----------------------------------------------------------

    if (playerBlackjack)
    {
      /*
       * Same public-information model as
       * our previous research.
       *
       * Dealer hole card remains unseen.
       */
      return super4Outcome;
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
                 * Composition research has no
                 * bankroll restriction.
                 */
                return true;
              }


              @Override
              public double getWagerAmount(
                  PlayerHandState state)
              {
                return 25.0;
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
                    tracker,
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
                 * Split replacement cards may
                 * have been drawn internally.
                 */
                observeHand(
                    tracker,
                    observed,
                    originalState.getHand()
                );


                observeHand(
                    tracker,
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
      observeVisible(
          tracker,
          observed,
          dealerHoleCard
      );


      dealerPlayEngine.play(
          dealerHand,
          shoe,
          card ->
              observeVisible(
                  tracker,
                  observed,
                  card
              )
      );
    }


    return super4Outcome;
  }


  // ============================================================
  // SUPER 4 EVALUATION
  // ============================================================

  private static Super4Outcome evaluateSuper4(
      Card playerCard1,
      Card playerCard2,
      Card dealerUpCard,
      Card dealerHoleCard,
      boolean dealerBlackjack)
  {
    /*
     * Massachusetts rule:
     *
     * If dealer does NOT have blackjack,
     * an Ace up pays $10.
     *
     * Any other dealer upcard loses.
     */
    if (!dealerBlackjack)
    {
      if (dealerUpCard.getRank()
          == Rank.ACE)
      {
        return Super4Outcome
            .DEALER_ACE_NO_BLACKJACK;
      }


      return Super4Outcome.LOSS;
    }


    Card[] cards =
    {
        playerCard1,
        playerCard2,
        dealerUpCard,
        dealerHoleCard
    };


    /*
     * Highest listed Super 4 category wins.
     */

    String royalSuit =
        getRoyalFlushSuit(
            cards
        );


    if (royalSuit != null)
    {
      return royalOutcomeForSuit(
          royalSuit
      );
    }


    int maxRankCount =
        getMaxRankCount(
            cards
        );


    /*
     * An eight-deck shoe can technically
     * produce four cards of the same rank.
     *
     * The current MGC table explicitly lists
     * "Three of a Kind" but does not separately
     * list Four of a Kind.
     *
     * Preserve this rare case separately instead
     * of silently assigning a payout.
     */
    if (maxRankCount == 4)
    {
      return Super4Outcome
          .FOUR_OF_A_KIND_UNMAPPED;
    }


    if (maxRankCount == 3)
    {
      return Super4Outcome
          .THREE_OF_A_KIND;
    }


    if (isStraight(
        cards))
    {
      return Super4Outcome.STRAIGHT;
    }


    if (isFlush(
        cards))
    {
      return Super4Outcome.FLUSH;
    }


    if (isTwoPair(
        cards))
    {
      return Super4Outcome.TWO_PAIR;
    }


    if (isSameColor(
        cards))
    {
      return Super4Outcome.SAME_COLOR;
    }


    if (hasPair(
        cards))
    {
      return Super4Outcome.PAIR;
    }


    return Super4Outcome.ANY_HAND;
  }


  // ============================================================
  // ROYAL FLUSH
  // ============================================================

  private static String getRoyalFlushSuit(
      Card[] cards)
  {
    if (!isFlush(cards))
    {
      return null;
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
          return null;
      }
    }


    if (ace
        && king
        && queen
        && jack)
    {
      return suitName(
          cards[0]
      );
    }


    return null;
  }


  private static Super4Outcome
      royalOutcomeForSuit(
          String suitName)
  {
    String normalized =
        suitName.toUpperCase(
            Locale.US
        );


    if (normalized.contains(
        "SPADE"))
    {
      return Super4Outcome
          .ROYAL_SPADES;
    }


    if (normalized.contains(
        "HEART"))
    {
      return Super4Outcome
          .ROYAL_HEARTS;
    }


    if (normalized.contains(
        "DIAMOND"))
    {
      return Super4Outcome
          .ROYAL_DIAMONDS;
    }


    if (normalized.contains(
        "CLUB"))
    {
      return Super4Outcome
          .ROYAL_CLUBS;
    }


    throw new IllegalStateException(
        "Unrecognized suit: "
        + suitName
    );
  }


  // ============================================================
  // POKER-LIKE FOUR-CARD CATEGORIES
  // ============================================================

  private static int getMaxRankCount(
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
          )
          + 1;


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


  private static boolean hasPair(
      Card[] cards)
  {
    return getMaxRankCount(
        cards
    ) >= 2;
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
          )
          + 1
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
         i < cards.length;
         i++)
    {
      values[i] =
          pokerRankValue(
              cards[i].getRank()
          );
    }


    java.util.Arrays.sort(
        values
    );


    /*
     * Need four different ranks.
     */
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


    /*
     * Normal consecutive sequence.
     */
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
     * Ace-low:
     *
     * A-2-3-4
     */
    return values[0] == 2
        && values[1] == 3
        && values[2] == 4
        && values[3] == 14;
  }


  private static int pokerRankValue(
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
    String firstSuit =
        suitName(
            cards[0]
        );


    for (int i = 1;
         i < cards.length;
         i++)
    {
      if (!firstSuit.equals(
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
    CardColor firstColor =
        cardColor(
            cards[0]
        );


    for (int i = 1;
         i < cards.length;
         i++)
    {
      if (cardColor(
          cards[i]
      ) != firstColor)
      {
        return false;
      }
    }


    return true;
  }


  /*
   * We deliberately use reflection here so
   * this research runner does not need to know
   * your Suit enum's compile-time type.
   *
   * It only assumes Card exposes getSuit(),
   * which is the natural Card API.
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
      );
    }
    catch (ReflectiveOperationException e)
    {
      throw new IllegalStateException(
          "Super4 research requires "
          + "Card.getSuit()",
          e
      );
    }
  }


  private static CardColor cardColor(
      Card card)
  {
    String suit =
        suitName(
            card
        )
        .toUpperCase(
            Locale.US
        );


    if (suit.contains(
            "HEART")
        || suit.contains(
            "DIAMOND"))
    {
      return CardColor.RED;
    }


    if (suit.contains(
            "SPADE")
        || suit.contains(
            "CLUB"))
    {
      return CardColor.BLACK;
    }


    throw new IllegalStateException(
        "Unrecognized suit: "
        + suit
    );
  }


  // ============================================================
  // CARD VISIBILITY / COUNT TRACKING
  // ============================================================

  private static void observeHand(
      CountTracker tracker,
      Map<Card, Boolean> observed,
      Hand hand)
  {
    for (int i = 0;
         i < hand.getNumCards();
         i++)
    {
      observeVisible(
          tracker,
          observed,
          hand.getCard(i)
      );
    }
  }


  private static void observeVisible(
      CountTracker tracker,
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


    tracker.observe(
        card
    );
  }


  private static class CountTracker
  {
    private int visibleCardsSeen;

    private int acesSeen;

    private int fivesSeen;

    private int tenValuesSeen;


    void reset()
    {
      visibleCardsSeen =
          0;


      acesSeen =
          0;


      fivesSeen =
          0;


      tenValuesSeen =
          0;
    }


    void observe(
        Card card)
    {
      visibleCardsSeen++;


      Rank rank =
          card.getRank();


      if (rank
          == Rank.ACE)
      {
        acesSeen++;
      }


      if (rank
          == Rank.FIVE)
      {
        fivesSeen++;
      }


      if (isTenValue(
          rank))
      {
        tenValuesSeen++;
      }
    }


    double getAceRunningSurplus()
    {
      /*
       * Expected Aces seen:
       *
       * visibleCardsSeen * 1/13
       */
      double expectedAcesSeen =
          visibleCardsSeen
          / 13.0;


      return expectedAcesSeen
          - acesSeen;
    }


    int getAceFiveRunningCount()
    {
      /*
       * Ace/Five:
       *
       * Five = +1
       * Ace  = -1
       */
      return fivesSeen
          - acesSeen;
    }


    double getTenRunningSurplus()
    {
      /*
       * There are four ten-value ranks:
       *
       * 10, J, Q, K
       *
       * Expected ten-value cards seen:
       *
       * visibleCardsSeen * 4/13
       *
       * Positive means fewer ten-value
       * cards have appeared than expected,
       * so the remaining shoe is relatively
       * ten-rich.
       */
      double expectedTensSeen =
          visibleCardsSeen
          * 4.0
          / 13.0;


      return expectedTensSeen
          - tenValuesSeen;
    }
  }


  private static boolean isTenValue(
      Rank rank)
  {
    return rank == Rank.TEN
        || rank == Rank.JACK
        || rank == Rank.QUEEN
        || rank == Rank.KING;
  }


  // ============================================================
  // ACCUMULATION
  // ============================================================

  private static void addResult(
      Map<StateKey, OutcomeAccumulator> map,
      StateKey key,
      Super4Outcome outcome)
  {
    OutcomeAccumulator accumulator =
        map.computeIfAbsent(
            key,
            unused ->
                new OutcomeAccumulator()
        );


    accumulator.add(
        outcome
    );
  }


  private static class OutcomeAccumulator
  {
    private long rounds;


    private final long[] outcomes =
        new long[
            Super4Outcome
                .values()
                .length
        ];


    void add(
        Super4Outcome outcome)
    {
      rounds++;


      outcomes[
          outcome.ordinal()
      ]++;
    }


    long get(
        Super4Outcome outcome)
    {
      return outcomes[
          outcome.ordinal()
      ];
    }
  }


  // ============================================================
  // OUTPUT
  // ============================================================

  private static void writeHeader()
      throws IOException
  {
    String header =
        String.join(
            ",",
            "signal_type",
            "decks_remaining_bucket",
            "ace_surplus_bucket",
            "ace_five_running_count",
            "ten_surplus_bucket",
            "rounds",
            "royal_spades",
            "royal_hearts",
            "royal_diamonds",
            "royal_clubs",
            "four_of_a_kind_unmapped",
            "three_of_a_kind",
            "straight",
            "flush",
            "two_pair",
            "same_color",
            "pair",
            "any_hand",
            "dealer_ace_no_blackjack",
            "loss"
        )
        + System.lineSeparator();


    Files.writeString(
        OUTPUT_FILE,
        header,
        StandardOpenOption.CREATE
    );
  }


  private static void writeMap(
      Map<StateKey, OutcomeAccumulator> map)
      throws IOException
  {
    List<Map.Entry<StateKey,
        OutcomeAccumulator>> entries =
            new ArrayList<>(
                map.entrySet()
            );


    entries.sort(
        Comparator
            .comparing(
                (
                    Map.Entry<
                        StateKey,
                        OutcomeAccumulator
                    > entry
                ) ->
                    entry.getKey()
                        .signalType()
                        .ordinal()
            )
            .thenComparingDouble(
                entry ->
                    entry.getKey()
                        .decksRemainingBucket()
            )
            .thenComparingDouble(
                entry ->
                    nullableDoubleForSort(
                        entry.getKey()
                            .aceSurplusBucket()
                    )
            )
            .thenComparingInt(
                entry ->
                    nullableIntegerForSort(
                        entry.getKey()
                            .aceFiveRunningCount()
                    )
            )
            .thenComparingDouble(
                entry ->
                    nullableDoubleForSort(
                        entry.getKey()
                            .tenSurplusBucket()
                    )
            )
    );


    StringBuilder builder =
        new StringBuilder();


    for (Map.Entry<
             StateKey,
             OutcomeAccumulator
         > entry :
         entries)
    {
      StateKey key =
          entry.getKey();


      OutcomeAccumulator acc =
          entry.getValue();


      builder.append(
          key.signalType()
      );


      builder.append(",");


      builder.append(
          formatDouble(
              key.decksRemainingBucket()
          )
      );


      builder.append(",");


      builder.append(
          formatNullableDouble(
              key.aceSurplusBucket()
          )
      );


      builder.append(",");


      builder.append(
          formatNullableInteger(
              key.aceFiveRunningCount()
          )
      );


      builder.append(",");


      builder.append(
          formatNullableDouble(
              key.tenSurplusBucket()
          )
      );


      builder.append(",");


      builder.append(
          acc.rounds
      );


      for (Super4Outcome outcome :
           Super4Outcome.values())
      {
        builder.append(",");


        builder.append(
            acc.get(
                outcome
            )
        );
      }


      builder.append(
          System.lineSeparator()
      );


      /*
       * Avoid holding the entire output
       * file in memory at once.
       */
      if (builder.length()
          >= 1_000_000)
      {
        Files.writeString(
            OUTPUT_FILE,
            builder.toString(),
            StandardOpenOption.APPEND
        );


        builder.setLength(
            0
        );
      }
    }


    if (builder.length() > 0)
    {
      Files.writeString(
          OUTPUT_FILE,
          builder.toString(),
          StandardOpenOption.APPEND
      );
    }
  }


  private static String formatDouble(
      double value)
  {
    return String.format(
        Locale.US,
        "%.2f",
        value
    );
  }


  private static String formatNullableDouble(
      Double value)
  {
    if (value == null)
    {
      return "";
    }


    return formatDouble(
        value
    );
  }


  private static String formatNullableInteger(
      Integer value)
  {
    if (value == null)
    {
      return "";
    }


    return Integer.toString(
        value
    );
  }


  private static double nullableDoubleForSort(
      Double value)
  {
    return value == null
        ? Double.NEGATIVE_INFINITY
        : value;
  }


  private static int nullableIntegerForSort(
      Integer value)
  {
    return value == null
        ? Integer.MIN_VALUE
        : value;
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
        GameSettings.BlackjackPayout
            .THREE_TO_TWO
    );


    return settings;
  }


  // ============================================================
  // DATA
  // ============================================================

  private enum SignalType
  {
    BASELINE,
    ACE_ONLY,
    ACE_FIVE,
    ACE_TEN
  }


  /*
   * Column order intentionally matches
   * Super4Outcome.values().
   */
  private enum Super4Outcome
  {
    ROYAL_SPADES,
    ROYAL_HEARTS,
    ROYAL_DIAMONDS,
    ROYAL_CLUBS,

    FOUR_OF_A_KIND_UNMAPPED,

    THREE_OF_A_KIND,

    STRAIGHT,

    FLUSH,

    TWO_PAIR,

    SAME_COLOR,

    PAIR,

    ANY_HAND,

    DEALER_ACE_NO_BLACKJACK,

    LOSS
  }


  private enum CardColor
  {
    RED,
    BLACK
  }


  private record PreDealState(
      double decksRemainingBucket,
      double aceSurplusBucket,
      int aceFiveRunningCount,
      double tenSurplusBucket)
  {
  }


  private record StateKey(
      SignalType signalType,
      double decksRemainingBucket,
      Double aceSurplusBucket,
      Integer aceFiveRunningCount,
      Double tenSurplusBucket)
  {
  }
}