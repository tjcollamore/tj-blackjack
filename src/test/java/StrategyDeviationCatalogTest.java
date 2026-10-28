import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.EnumSet;

import org.junit.jupiter.api.Test;

public class StrategyDeviationCatalogTest
{
  private final GameSettings settings =
      createSettings();


  // ------------------------------------------------------------
  // All 19 researched Social Strategy deviations
  // ------------------------------------------------------------

  @Test
  public void testAllKnownDeviations()
  {
    // Hard
    assertDeviation(
        "HARD_12_VS_2",
        new Rank[] {
            rank(10),
            rank(2)
        },
        2,
        HandAction.HIT,
        HandAction.STAND
    );

    assertDeviation(
        "HARD_12_VS_3",
        new Rank[] {
            rank(10),
            rank(2)
        },
        3,
        HandAction.HIT,
        HandAction.STAND
    );

    assertDeviation(
        "HARD_9_VS_2",
        new Rank[] {
            rank(4),
            rank(5)
        },
        2,
        HandAction.HIT,
        HandAction.DOUBLE
    );


    // Soft
    assertDeviation(
        "SOFT_A8_VS_6",
        new Rank[] {
            Rank.ACE,
            rank(8)
        },
        6,
        HandAction.DOUBLE,
        HandAction.STAND
    );

    assertDeviation(
        "SOFT_A7_VS_2",
        new Rank[] {
            Rank.ACE,
            rank(7)
        },
        2,
        HandAction.DOUBLE,
        HandAction.STAND
    );

    assertDeviation(
        "SOFT_A7_VS_3",
        new Rank[] {
            Rank.ACE,
            rank(7)
        },
        3,
        HandAction.DOUBLE,
        HandAction.STAND
    );

    assertDeviation(
        "SOFT_A6_VS_3",
        new Rank[] {
            Rank.ACE,
            rank(6)
        },
        3,
        HandAction.DOUBLE,
        HandAction.HIT
    );

    assertDeviation(
        "SOFT_A2_VS_4",
        new Rank[] {
            Rank.ACE,
            rank(2)
        },
        4,
        HandAction.HIT,
        HandAction.DOUBLE
    );

    assertDeviation(
        "SOFT_A3_VS_4",
        new Rank[] {
            Rank.ACE,
            rank(3)
        },
        4,
        HandAction.HIT,
        HandAction.DOUBLE
    );


    // Pairs
    assertDeviation(
        "PAIR_99_VS_7",
        new Rank[] {
            rank(9),
            rank(9)
        },
        7,
        HandAction.STAND,
        HandAction.SPLIT
    );

    assertDeviation(
        "PAIR_66_VS_7",
        new Rank[] {
            rank(6),
            rank(6)
        },
        7,
        HandAction.HIT,
        HandAction.SPLIT
    );

    assertDeviation(
        "PAIR_44_VS_5",
        new Rank[] {
            rank(4),
            rank(4)
        },
        5,
        HandAction.SPLIT,
        HandAction.HIT
    );

    assertDeviation(
        "PAIR_44_VS_6",
        new Rank[] {
            rank(4),
            rank(4)
        },
        6,
        HandAction.SPLIT,
        HandAction.HIT
    );


    // Surrender-related
    assertDeviation(
        "HARD_15_VS_ACE",
        new Rank[] {
            rank(10),
            rank(5)
        },
        11,
        HandAction.SURRENDER,
        HandAction.HIT
    );

    assertDeviation(
        "HARD_16_VS_9",
        new Rank[] {
            rank(10),
            rank(6)
        },
        9,
        HandAction.SURRENDER,
        HandAction.HIT
    );

    assertDeviation(
        "HARD_16_VS_ACE",
        new Rank[] {
            rank(10),
            rank(6)
        },
        11,
        HandAction.SURRENDER,
        HandAction.HIT
    );

    assertDeviation(
        "HARD_17_VS_ACE",
        new Rank[] {
            rank(10),
            rank(7)
        },
        11,
        HandAction.SURRENDER,
        HandAction.STAND
    );

    assertDeviation(
        "PAIR_88_VS_ACE",
        new Rank[] {
            rank(8),
            rank(8)
        },
        11,
        HandAction.SURRENDER,
        HandAction.SPLIT
    );

    assertDeviation(
        "PAIR_88_VS_10",
        new Rank[] {
            rank(8),
            rank(8)
        },
        10,
        HandAction.SPLIT,
        HandAction.SURRENDER
    );
  }


  // ------------------------------------------------------------
  // Situations where the catalog should NOT return a deviation
  // ------------------------------------------------------------

  @Test
  public void testAgreementReturnsNull()
  {
    PlayerDecisionContext context =
        createContext(
            new Rank[] {
                rank(10),
                rank(3)
            },
            6,
            settings,
            8
        );

    assertNull(
        StrategyDeviationCatalog.find(
            context
        )
    );
  }


  @Test
  public void testSingleDeckReturnsNull()
  {
    GameSettings singleDeckSettings =
        createSettings();

    singleDeckSettings.setDecksInShoe(
        1
    );

    PlayerDecisionContext context =
        createContext(
            new Rank[] {
                rank(10),
                rank(2)
            },
            2,
            singleDeckSettings,
            1
        );

    assertNull(
        StrategyDeviationCatalog.find(
            context
        )
    );
  }


  @Test
  public void testDoubleDeckReturnsNull()
  {
    GameSettings doubleDeckSettings =
        createSettings();

    doubleDeckSettings.setDecksInShoe(
        2
    );

    PlayerDecisionContext context =
        createContext(
            new Rank[] {
                rank(10),
                rank(2)
            },
            2,
            doubleDeckSettings,
            2
        );

    assertNull(
        StrategyDeviationCatalog.find(
            context
        )
    );
  }


  @Test
  public void testS17ReturnsNull()
  {
    GameSettings s17Settings =
        createSettings();

    s17Settings.setDealerHitsSoft17(
        false
    );

    PlayerDecisionContext context =
        createContext(
            new Rank[] {
                rank(10),
                rank(2)
            },
            2,
            s17Settings,
            8
        );

    assertNull(
        StrategyDeviationCatalog.find(
            context
        )
    );
  }


  @Test
  public void testNoDasReturnsNull()
  {
    GameSettings noDasSettings =
        createSettings();

    noDasSettings.setDoubleAfterSplit(
        false
    );

    PlayerDecisionContext context =
        createContext(
            new Rank[] {
                rank(10),
                rank(2)
            },
            2,
            noDasSettings,
            8
        );

    assertNull(
        StrategyDeviationCatalog.find(
            context
        )
    );
  }


  // ------------------------------------------------------------
  // Helpers
  // ------------------------------------------------------------

  private void assertDeviation(
      String expectedId,
      Rank[] playerRanks,
      int dealerValue,
      HandAction expectedBasic,
      HandAction expectedSocial)
  {
    PlayerDecisionContext context =
        createContext(
            playerRanks,
            dealerValue,
            settings,
            8
        );

    StrategyDeviation deviation =
        StrategyDeviationCatalog.find(
            context
        );

    assertNotNull(
        deviation,
        expectedId
            + " should be recognized"
    );

    assertEquals(
        expectedId,
        deviation.getId()
    );

    assertEquals(
        expectedBasic,
        deviation.getBasicAction(),
        expectedId
            + " Basic action"
    );

    assertEquals(
        expectedSocial,
        deviation.getSocialAction(),
        expectedId
            + " Social action"
    );
  }


  private PlayerDecisionContext createContext(
      Rank[] playerRanks,
      int dealerValue,
      GameSettings gameSettings,
      int deckCount)
  {
    Hand hand =
        new Hand();

    Shoe source =
        new Shoe(
            deckCount,
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

    EnumSet<HandAction> actions =
        EnumSet.allOf(
            HandAction.class
        );

    return new PlayerDecisionContext(
        hand,
        dealerUpCard,
        gameSettings,
        actions,
        actions,
        false,
        deckCount,
        25.0
    );
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
    GameSettings gameSettings =
        new GameSettings();

    gameSettings.setDecksInShoe(
        8
    );

    gameSettings.setDealerHitsSoft17(
        true
    );

    gameSettings.setDoubleAfterSplit(
        true
    );

    gameSettings.setAllowSurrender(
        true
    );

    gameSettings.setBlackjackPayout(
        GameSettings.BlackjackPayout.SIX_TO_FIVE
    );

    return gameSettings;
  }
}
