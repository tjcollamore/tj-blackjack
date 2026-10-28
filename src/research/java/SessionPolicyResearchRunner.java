import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SessionPolicyResearchRunner
{
  private static final int SESSIONS =
      100_000;

  private static final int HANDS_PER_SESSION =
      160;

  private static final double STARTING_BANKROLL =
      500.0;

  private static final double BASE_BET =
      25.0;

  private static final double CI_95_Z =
      1.96;

  private static final Path OUTPUT_FILE =
      Path.of(
          "research-data",
          "scratch",
          "session-policy-hard9-100k-160hands-500bankroll-25bet.csv"
      );

  private final GameSettings settings;
  private final BasicStrategy basicStrategy;
  private final PlayerRoundEngine playerRoundEngine;
  private final DealerPlayEngine dealerPlayEngine;

  public SessionPolicyResearchRunner(
      GameSettings settings)
  {
    this.settings =
        settings;

    basicStrategy =
        new BasicStrategy();

    playerRoundEngine =
        new PlayerRoundEngine(
            settings
        );

    dealerPlayEngine =
        new DealerPlayEngine(
            settings
        );
  }

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

    writeHeader();

    GameSettings.BlackjackPayout[] payouts =
    {
        GameSettings.BlackjackPayout.SIX_TO_FIVE,
        GameSettings.BlackjackPayout.THREE_TO_TWO
    };

    Policy[] policies =
    {
        Policy.BASIC_STRATEGY,
        Policy.NO_HARD_9_DOUBLES,
        Policy.HARD_9_DOUBLE_2_TO_6
    };

    for (GameSettings.BlackjackPayout payout :
         payouts)
    {
      System.out.println();

      System.out.println(
          "======================================"
      );

      System.out.println(
          "BLACKJACK PAYOUT: "
          + payout
      );

      System.out.println(
          "======================================"
      );

      for (Policy policy :
           policies)
      {
        GameSettings settings =
            createResearchSettings(
                payout
            );

        SessionPolicyResearchRunner runner =
            new SessionPolicyResearchRunner(
                settings
            );

        SessionSummary summary =
            runner.runPolicy(
                policy
            );

        printSummary(
            payout,
            policy,
            summary
        );

        writeSummary(
            payout,
            policy,
            summary
        );
      }
    }

    System.out.println();

    System.out.println(
        "Saved results to "
        + OUTPUT_FILE.toAbsolutePath()
    );
  }

  private SessionSummary runPolicy(
      Policy policy)
  {
    double[] endingBankrolls =
        new double[SESSIONS];

    long unableToContinue =
        0;

    long finishDown25 =
        0;

    long finishDown50 =
        0;

    long finishDown75 =
        0;

    long totalHandsPlayed =
        0;

    long totalDoubles =
        0;

    long totalSplits =
        0;

    double totalAmountWagered =
        0.0;

    double meanEndingBankroll =
        0.0;

    double endingBankrollM2 =
        0.0;

    for (int session = 1;
         session <= SESSIONS;
         session++)
    {
      SessionState state =
          playSession(
              policy
          );

      double endingBankroll =
          state.bankroll();

      endingBankrolls[
          session - 1
      ] =
          endingBankroll;

      totalHandsPlayed +=
          state.handsPlayed();

      totalDoubles +=
          state.doubles();

      totalSplits +=
          state.splits();

      totalAmountWagered +=
          state.totalAmountWagered();

      if (state.unableToContinue())
      {
        unableToContinue++;
      }

      if (endingBankroll
          <= STARTING_BANKROLL * 0.75)
      {
        finishDown25++;
      }

      if (endingBankroll
          <= STARTING_BANKROLL * 0.50)
      {
        finishDown50++;
      }

      if (endingBankroll
          <= STARTING_BANKROLL * 0.25)
      {
        finishDown75++;
      }

      double delta =
          endingBankroll
          - meanEndingBankroll;

      meanEndingBankroll +=
          delta / session;

      double delta2 =
          endingBankroll
          - meanEndingBankroll;

      endingBankrollM2 +=
          delta
          * delta2;

      if (session % 10_000 == 0)
      {
        System.out.println(
            "  "
            + policy
            + ": completed "
            + session
            + " sessions..."
        );
      }
    }

    Arrays.sort(
        endingBankrolls
    );

    double medianEndingBankroll =
        median(
            endingBankrolls
        );

    double variance =
        endingBankrollM2
        / (SESSIONS - 1);

    double standardDeviation =
        Math.sqrt(
            variance
        );

    double standardError =
        standardDeviation
        / Math.sqrt(
            SESSIONS
        );

    double meanCI95 =
        CI_95_Z
        * standardError;

    double unableRate =
        (double) unableToContinue
        / SESSIONS;

    double unableSE =
        binomialStandardError(
            unableRate,
            SESSIONS
        );

    double unableCI95 =
        CI_95_Z
        * unableSE;

    return new SessionSummary(
        meanEndingBankroll,
        medianEndingBankroll,
        standardDeviation,
        standardError,
        meanCI95,
        unableRate,
        unableCI95,
        (double) finishDown25
            / SESSIONS,
        (double) finishDown50
            / SESSIONS,
        (double) finishDown75
            / SESSIONS,
        (double) totalHandsPlayed
            / SESSIONS,
        (double) totalDoubles
            / SESSIONS,
        (double) totalSplits
            / SESSIONS,
        totalAmountWagered
            / SESSIONS
    );
  }

  private SessionState playSession(
      Policy policy)
  {
    double[] bankroll =
    {
        STARTING_BANKROLL
    };

    long[] doubles =
    {
        0
    };

    long[] splits =
    {
        0
    };

    double[] totalAmountWagered =
    {
        0.0
    };

    int handsPlayed =
        0;

    Shoe shoe =
        new Shoe(
            settings.getDecksInShoe()
        );

    HandStrategy strategy =
        createStrategy(
            policy
        );

    for (int handNumber = 0;
         handNumber < HANDS_PER_SESSION;
         handNumber++)
    {
      if (bankroll[0] < BASE_BET)
      {
        return new SessionState(
            bankroll[0],
            handsPlayed,
            true,
            doubles[0],
            splits[0],
            totalAmountWagered[0]
        );
      }

      if (shoe.needsShuffle())
      {
        shoe.shuffle();
      }

      playRound(
          shoe,
          strategy,
          bankroll,
          doubles,
          splits,
          totalAmountWagered
      );

      handsPlayed++;
    }

    return new SessionState(
        bankroll[0],
        handsPlayed,
        false,
        doubles[0],
        splits[0],
        totalAmountWagered[0]
    );
  }

  private void playRound(
      Shoe shoe,
      HandStrategy strategy,
      double[] bankroll,
      long[] doubles,
      long[] splits,
      double[] totalAmountWagered)
  {
    /*
     * Place initial wager.
     */
    bankroll[0] -=
        BASE_BET;

    totalAmountWagered[0] +=
        BASE_BET;

    Hand playerHand =
        new Hand();

    Hand dealerHand =
        new Hand();

    /*
     * Same initial dealing order as live Round:
     * player, dealer, player, dealer.
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
     * Dealer blackjack ends the round before
     * any player decision.
     */
    if (dealerBlackjack)
    {
      if (playerBlackjack)
      {
        /*
         * Blackjack vs blackjack pushes.
         * Return original wager.
         */
        bankroll[0] +=
            BASE_BET;
      }

      return;
    }

    /*
     * Player natural blackjack ends the round
     * immediately. No dealer draw-out is needed.
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

    /*
     * Every state represents one original
     * BASE_BET wager. Splits create another
     * BASE_BET wager, and doubles add another
     * BASE_BET to that state's wager.
     */
    Map<PlayerHandState, Double> baseWagers =
        new IdentityHashMap<>();

    baseWagers.put(
        initialState,
        BASE_BET
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

                totalAmountWagered[0] +=
                    BASE_BET;

                doubles[0]++;
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

                totalAmountWagered[0] +=
                    BASE_BET;

                splits[0]++;

                baseWagers.put(
                    newState,
                    BASE_BET
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
  }

  private HandStrategy createStrategy(
      Policy policy)
  {
    return context ->
    {
      HandAction basicAction =
          basicStrategy.chooseAction(
              context
          );

      Hand hand =
          context.getPlayerHand();

      boolean hardNine =
          !hand.isSoft()
          && hand.getScore() == 9;

      if (!hardNine)
      {
        return basicAction;
      }

      switch (policy)
      {
        case BASIC_STRATEGY:
          return basicAction;

        case NO_HARD_9_DOUBLES:
        {
          /*
           * Remove every Hard-9 double.
           */
          if (basicAction
              == HandAction.DOUBLE)
          {
            return HandAction.HIT;
          }

          return basicAction;
        }

        case HARD_9_DOUBLE_2_TO_6:
        {
          int dealer =
              context
                  .getDealerUpCard()
                  .getPointValue();

          /*
           * Candidate Social Strategy:
           * Double Hard 9 against dealer 2-6.
           *
           * If bankroll does not permit the
           * double, fall back to HIT.
           */
          if (dealer >= 2
              && dealer <= 6)
          {
            if (context.isAvailable(
                HandAction.DOUBLE))
            {
              return HandAction.DOUBLE;
            }

            return HandAction.HIT;
          }

          return basicAction;
        }

        default:
          throw new IllegalStateException(
              "Unexpected policy: "
              + policy
          );
      }
    };
  }

  private static void printSummary(
      GameSettings.BlackjackPayout payout,
      Policy policy,
      SessionSummary summary)
  {
    System.out.println();

    System.out.println(
        policy
    );

    System.out.println(
        "--------------------------------------"
    );

    System.out.printf(
        Locale.US,
        "Average ending bankroll: $%.2f%n",
        summary.meanEndingBankroll()
    );

    System.out.printf(
        Locale.US,
        "Average profit/loss: %+.2f%n",
        summary.meanEndingBankroll()
        - STARTING_BANKROLL
    );

    System.out.printf(
        Locale.US,
        "95%% CI for average ending bankroll: $%.2f to $%.2f%n",
        summary.meanEndingBankroll()
            - summary.meanCI95(),
        summary.meanEndingBankroll()
            + summary.meanCI95()
    );

    System.out.printf(
        Locale.US,
        "Median ending bankroll: $%.2f%n",
        summary.medianEndingBankroll()
    );

    System.out.printf(
        Locale.US,
        "Ending bankroll SD: $%.2f%n",
        summary.standardDeviation()
    );

    System.out.println();

    System.out.printf(
        Locale.US,
        "Unable to continue before 160 hands: %.3f%%%n",
        summary.unableToContinueRate()
        * 100.0
    );

    System.out.printf(
        Locale.US,
        "95%% CI for unable-to-continue rate: %.3f%% to %.3f%%%n",
        Math.max(
            0.0,
            summary.unableToContinueRate()
            - summary.unableCI95()
        ) * 100.0,
        Math.min(
            1.0,
            summary.unableToContinueRate()
            + summary.unableCI95()
        ) * 100.0
    );

    System.out.printf(
        Locale.US,
        "Finish down at least 25%%: %.3f%%%n",
        summary.finishDown25Rate()
        * 100.0
    );

    System.out.printf(
        Locale.US,
        "Finish down at least 50%%: %.3f%%%n",
        summary.finishDown50Rate()
        * 100.0
    );

    System.out.printf(
        Locale.US,
        "Finish down at least 75%%: %.3f%%%n",
        summary.finishDown75Rate()
        * 100.0
    );

    System.out.println();

    System.out.printf(
        Locale.US,
        "Average hands played: %.2f%n",
        summary.averageHandsPlayed()
    );

    System.out.printf(
        Locale.US,
        "Average doubles/session: %.3f%n",
        summary.averageDoubles()
    );

    System.out.printf(
        Locale.US,
        "Average splits/session: %.3f%n",
        summary.averageSplits()
    );

    System.out.printf(
        Locale.US,
        "Average total wager exposure: $%.2f%n",
        summary.averageAmountWagered()
    );

    System.out.println();
  }

  private static void writeHeader()
      throws IOException
  {
    Files.writeString(
        OUTPUT_FILE,
        String.join(
            ",",
            "blackjack_payout",
            "policy",
            "sessions",
            "hands_per_session",
            "starting_bankroll",
            "base_bet",
            "mean_ending_bankroll",
            "mean_profit_loss",
            "median_ending_bankroll",
            "ending_bankroll_sd",
            "mean_standard_error",
            "mean_ci95",
            "unable_to_continue_rate",
            "unable_to_continue_ci95",
            "finish_down_25_rate",
            "finish_down_50_rate",
            "finish_down_75_rate",
            "average_hands_played",
            "average_doubles",
            "average_splits",
            "average_amount_wagered"
        )
        + System.lineSeparator(),
        StandardOpenOption.CREATE
    );
  }

  private static void writeSummary(
      GameSettings.BlackjackPayout payout,
      Policy policy,
      SessionSummary summary)
      throws IOException
  {
    String row =
        String.format(
            Locale.US,
            "%s,%s,%d,%d,%.2f,%.2f,%.8f,%.8f,%.8f,%.8f,%.8f,%.8f,%.8f,%.8f,%.8f,%.8f,%.8f,%.8f,%.8f,%.8f,%.8f",
            payout.name(),
            policy.name(),
            SESSIONS,
            HANDS_PER_SESSION,
            STARTING_BANKROLL,
            BASE_BET,
            summary.meanEndingBankroll(),
            summary.meanEndingBankroll()
                - STARTING_BANKROLL,
            summary.medianEndingBankroll(),
            summary.standardDeviation(),
            summary.standardError(),
            summary.meanCI95(),
            summary.unableToContinueRate(),
            summary.unableCI95(),
            summary.finishDown25Rate(),
            summary.finishDown50Rate(),
            summary.finishDown75Rate(),
            summary.averageHandsPlayed(),
            summary.averageDoubles(),
            summary.averageSplits(),
            summary.averageAmountWagered()
        );

    Files.writeString(
        OUTPUT_FILE,
        row + System.lineSeparator(),
        StandardOpenOption.APPEND
    );
  }

  private static GameSettings
      createResearchSettings(
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

  private static double median(
      double[] sorted)
  {
    int length =
        sorted.length;

    if (length % 2 == 1)
    {
      return sorted[
          length / 2
      ];
    }

    return (
        sorted[
            length / 2 - 1
        ]
        +
        sorted[
            length / 2
        ]
    ) / 2.0;
  }

  private static double binomialStandardError(
      double probability,
      int trials)
  {
    return Math.sqrt(
        probability
        * (1.0 - probability)
        / trials
    );
  }

  private enum Policy
  {
    BASIC_STRATEGY,
    NO_HARD_9_DOUBLES,
    HARD_9_DOUBLE_2_TO_6
  }

  private record SessionState(
      double bankroll,
      int handsPlayed,
      boolean unableToContinue,
      long doubles,
      long splits,
      double totalAmountWagered)
  {
  }

  private record SessionSummary(
      double meanEndingBankroll,
      double medianEndingBankroll,
      double standardDeviation,
      double standardError,
      double meanCI95,
      double unableToContinueRate,
      double unableCI95,
      double finishDown25Rate,
      double finishDown50Rate,
      double finishDown75Rate,
      double averageHandsPlayed,
      double averageDoubles,
      double averageSplits,
      double averageAmountWagered)
  {
  }
}
