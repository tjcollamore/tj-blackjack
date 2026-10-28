import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.EnumSet;

import org.junit.jupiter.api.Test;

public class SocialStrategyTest
{
  private final BasicStrategy basicStrategy =
      new BasicStrategy();

  private final SocialStrategy socialStrategy =
      new SocialStrategy();

  private final GameSettings settings =
      createSettings();


  // ------------------------------------------------------------
  // Intentional Social Strategy deviations from Basic Strategy
  // ------------------------------------------------------------

  @Test
  public void testHard12Vs2()
  {
    assertStrategies(
        "Hard 12 vs 2",
        new Rank[] {
            rank(10),
            rank(2)
        },
        2,
        HandAction.HIT,
        HandAction.STAND
    );
  }


  @Test
  public void testHard12Vs3()
  {
    assertStrategies(
        "Hard 12 vs 3",
        new Rank[] {
            rank(10),
            rank(2)
        },
        3,
        HandAction.HIT,
        HandAction.STAND
    );
  }


  @Test
  public void testHard9Vs2()
  {
    assertStrategies(
        "Hard 9 vs 2",
        new Rank[] {
            rank(4),
            rank(5)
        },
        2,
        HandAction.HIT,
        HandAction.DOUBLE
    );
  }


  @Test
  public void testSoft19Vs6()
  {
    assertStrategies(
        "A,8 vs 6",
        new Rank[] {
            Rank.ACE,
            rank(8)
        },
        6,
        HandAction.DOUBLE,
        HandAction.STAND
    );
  }


  @Test
  public void testSoft13Vs4()
  {
    assertStrategies(
        "A,2 vs 4",
        new Rank[] {
            Rank.ACE,
            rank(2)
        },
        4,
        HandAction.HIT,
        HandAction.DOUBLE
    );
  }


  @Test
  public void testPair99Vs7()
  {
    assertStrategies(
        "9,9 vs 7",
        new Rank[] {
            rank(9),
            rank(9)
        },
        7,
        HandAction.STAND,
        HandAction.SPLIT
    );
  }


  @Test
  public void testPair44Vs6()
  {
    assertStrategies(
        "4,4 vs 6",
        new Rank[] {
            rank(4),
            rank(4)
        },
        6,
        HandAction.SPLIT,
        HandAction.HIT
    );
  }


  @Test
  public void testPair88Vs10()
  {
    assertStrategies(
        "8,8 vs 10",
        new Rank[] {
            rank(8),
            rank(8)
        },
        10,
        HandAction.SPLIT,
        HandAction.SURRENDER
    );
  }


  @Test
  public void testPair88VsAce()
  {
    assertStrategies(
        "8,8 vs Ace",
        new Rank[] {
            rank(8),
            rank(8)
        },
        11,
        HandAction.SURRENDER,
        HandAction.SPLIT
    );
  }


  @Test
  public void testHard16VsAce()
  {
    assertStrategies(
        "Hard 16 vs Ace",
        new Rank[] {
            rank(10),
            rank(6)
        },
        11,
        HandAction.SURRENDER,
        HandAction.HIT
    );
  }


  @Test
  public void testHard17VsAce()
  {
    assertStrategies(
        "Hard 17 vs Ace",
        new Rank[] {
            rank(10),
            rank(7)
        },
        11,
        HandAction.SURRENDER,
        HandAction.STAND
    );
  }


  // ------------------------------------------------------------
  // Representative cells where Social and Basic should agree
  // ------------------------------------------------------------

  @Test
  public void testHard10Vs6MatchesBasic()
  {
    assertStrategies(
        "Hard 10 vs 6",
        new Rank[] {
            rank(4),
            rank(6)
        },
        6,
        HandAction.DOUBLE,
        HandAction.DOUBLE
    );
  }


  @Test
  public void testHard13Vs6MatchesBasic()
  {
    assertStrategies(
        "Hard 13 vs 6",
        new Rank[] {
            rank(6),
            rank(7)
        },
        6,
        HandAction.STAND,
        HandAction.STAND
    );
  }


  @Test
  public void testSoft18Vs5MatchesBasic()
  {
    assertStrategies(
        "A,7 vs 5",
        new Rank[] {
            Rank.ACE,
            rank(7)
        },
        5,
        HandAction.DOUBLE,
        HandAction.DOUBLE
    );
  }


  @Test
  public void testAcesVs6MatchesBasic()
  {
    assertStrategies(
        "A,A vs 6",
        new Rank[] {
            Rank.ACE,
            Rank.ACE
        },
        6,
        HandAction.SPLIT,
        HandAction.SPLIT
    );
  }


  @Test
  public void testTensVs6MatchesBasic()
  {
    assertStrategies(
        "10,10 vs 6",
        new Rank[] {
            rank(10),
            rank(10)
        },
        6,
        HandAction.STAND,
        HandAction.STAND
    );
  }


  // ------------------------------------------------------------
  // Action-availability fallback behavior
  // ------------------------------------------------------------

  @Test
  public void test88Vs10SplitsWhenSurrenderUnavailable()
  {
    PlayerDecisionContext context =
        createContext(
            new Rank[] {
                rank(8),
                rank(8)
            },
            10,
            allActionsExcept(
                HandAction.SURRENDER
            )
        );

    assertEquals(
        HandAction.SPLIT,
        socialStrategy.chooseAction(
            context
        )
    );
  }


  @Test
  public void test99Vs7StandsWhenSplitUnavailable()
  {
    PlayerDecisionContext context =
        createContext(
            new Rank[] {
                rank(9),
                rank(9)
            },
            7,
            allActionsExcept(
                HandAction.SPLIT
            )
        );

    assertEquals(
        HandAction.STAND,
        socialStrategy.chooseAction(
            context
        )
    );
  }


  @Test
  public void testSoft18Vs5StandsWhenDoubleUnavailable()
  {
    PlayerDecisionContext context =
        createContext(
            new Rank[] {
                Rank.ACE,
                rank(7)
            },
            5,
            allActionsExcept(
                HandAction.DOUBLE
            )
        );

    assertEquals(
        HandAction.STAND,
        socialStrategy.chooseAction(
            context
        )
    );
  }


  @Test
  public void testSoft17Vs5HitsWhenDoubleUnavailable()
  {
    PlayerDecisionContext context =
        createContext(
            new Rank[] {
                Rank.ACE,
                rank(6)
            },
            5,
            allActionsExcept(
                HandAction.DOUBLE
            )
        );

    assertEquals(
        HandAction.HIT,
        socialStrategy.chooseAction(
            context
        )
    );
  }


  @Test
  public void testHard15Vs10HitsWhenSurrenderUnavailable()
  {
    PlayerDecisionContext context =
        createContext(
            new Rank[] {
                rank(10),
                rank(5)
            },
            10,
            allActionsExcept(
                HandAction.SURRENDER
            )
        );

    assertEquals(
        HandAction.HIT,
        socialStrategy.chooseAction(
            context
        )
    );
  }


  // ------------------------------------------------------------
  // Helpers
  // ------------------------------------------------------------

  private void assertStrategies(
      String label,
      Rank[] playerRanks,
      int dealerValue,
      HandAction expectedBasic,
      HandAction expectedSocial)
  {
    PlayerDecisionContext context =
        createContext(
            playerRanks,
            dealerValue,
            EnumSet.allOf(
                HandAction.class
            )
        );

    assertEquals(
        expectedBasic,
        basicStrategy.chooseRecommendedAction(
            context
        ),
        label + " Basic Strategy"
    );

    assertEquals(
        expectedSocial,
        socialStrategy.chooseRecommendedAction(
            context
        ),
        label + " Social Strategy"
    );
  }


  private PlayerDecisionContext createContext(
      Rank[] playerRanks,
      int dealerValue,
      EnumSet<HandAction> availableActions)
  {
    Hand hand =
        new Hand();

    Shoe source =
        new Shoe(
            8,
            1_234_567L
        );

    for (Rank rank :
         playerRanks)
    {
      hand.addCard(
          drawRank(
              source,
              rank
          )
      );
    }

    Card dealerUpCard =
        drawRank(
            source,
            rank(dealerValue)
        );

    EnumSet<HandAction> allowedActions =
        EnumSet.allOf(
            HandAction.class
        );

    return new PlayerDecisionContext(
        hand,
        dealerUpCard,
        settings,
        allowedActions,
        availableActions,
        false,
        8,
        25.0
    );
  }


  private EnumSet<HandAction> allActionsExcept(
      HandAction unavailableAction)
  {
    EnumSet<HandAction> actions =
        EnumSet.allOf(
            HandAction.class
        );

    actions.remove(
        unavailableAction
    );

    return actions;
  }


  private Card drawRank(
      Shoe shoe,
      Rank targetRank)
  {
    while (true)
    {
      Card card =
          shoe.drawCard();

      if (card.getRank()
          == targetRank)
      {
        return card;
      }
    }
  }


  private Rank rank(
      int value)
  {
    if (value == 11)
      return Rank.ACE;

    for (Rank rank :
         Rank.values())
    {
      if (rank.getValue()
          == value)
      {
        if (value != 10
            || rank == Rank.TEN)
        {
          return rank;
        }
      }
    }

    throw new IllegalArgumentException(
        "No rank for value "
        + value
    );
  }


  private GameSettings createSettings()
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
        GameSettings.BlackjackPayout.SIX_TO_FIVE
    );

    return settings;
  }
}
