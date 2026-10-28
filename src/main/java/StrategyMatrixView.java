import java.util.EnumSet;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;


public class StrategyMatrixView
{
  private static final String ACTIVE_TAB_STYLE = """
      -fx-background-color: #d4af37;
      -fx-text-fill: #1f1f1f;
      -fx-font-weight: bold;
      -fx-background-radius: 5px;
      -fx-padding: 7px 14px;
  """;

  private static final String INACTIVE_TAB_STYLE = """
      -fx-background-color: #3b3b3b;
      -fx-text-fill: #dddddd;
      -fx-font-weight: bold;
      -fx-background-radius: 5px;
      -fx-padding: 7px 14px;
  """;


  public static Node createBasic(
      GameSettings settings)
  {
    return create(
        StrategyMode.BASIC,
        settings,
        false
    );
  }


  public static Node createSocial()
  {
    GameSettings settings =
        new GameSettings();


    /*
     * Social Strategy v1's validated baseline.
     */
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


    return create(
        StrategyMode.SOCIAL,
        settings,
        true
    );
  }


  private static Node create(
      StrategyMode strategyMode,
      GameSettings settings,
      boolean allowComparison)
  {
    String strategyName =
        strategyMode
            == StrategyMode.BASIC
                ? "BASIC STRATEGY"
                : "SOCIAL STRATEGY";


    Label title =
        new Label(
            strategyName
            + " MATRIX"
        );

    title.setStyle("""
        -fx-font-size: 18px;
        -fx-font-weight: bold;
        -fx-text-fill: #d4af37;
    """);


    Label subtitle =
        new Label(
            "Recommended action by player hand and dealer up-card"
        );

    subtitle.setStyle("""
        -fx-font-size: 12px;
        -fx-text-fill: #aaaaaa;
    """);


    VBox matrixHolder =
        new VBox();

    matrixHolder.setAlignment(
        Pos.TOP_CENTER
    );


    VBox detailHolder =
        new VBox();

    detailHolder.setAlignment(
        Pos.TOP_LEFT
    );

    detailHolder.setMaxWidth(
        610
    );


    CheckBox showDifferences =
        new CheckBox(
            "Explore differences from Basic Strategy"
        );

    showDifferences.setSelected(
        false
    );

    showDifferences.setStyle("""
        -fx-font-size: 11px;
        -fx-text-fill: #cccccc;
    """);


    if (!allowComparison)
    {
      showDifferences.setVisible(
          false
      );

      showDifferences.setManaged(
          false
      );

      detailHolder.setVisible(
          false
      );

      detailHolder.setManaged(
          false
      );
    }
    else
    {
      showMatrixHint(
          detailHolder
      );

      detailHolder.setVisible(
          false
      );

      detailHolder.setManaged(
          false
      );
    }


    ToggleButton hardButton =
        new ToggleButton(
            "HARD"
        );

    ToggleButton softButton =
        new ToggleButton(
            "SOFT"
        );

    ToggleButton pairsButton =
        new ToggleButton(
            "PAIRS"
        );


    ToggleGroup matrixGroup =
        new ToggleGroup();

    hardButton.setToggleGroup(
        matrixGroup
    );

    softButton.setToggleGroup(
        matrixGroup
    );

    pairsButton.setToggleGroup(
        matrixGroup
    );


    HBox matrixTabs =
        new HBox(
            8,
            hardButton,
            softButton,
            pairsButton
        );

    matrixTabs.setAlignment(
        Pos.CENTER
    );


    hardButton.setOnAction(e ->
    {
      showHardMatrix(
          matrixHolder,
          detailHolder,
          strategyMode,
          settings,
          allowComparison
              && showDifferences.isSelected()
      );

      styleMatrixToggle(
          hardButton,
          hardButton,
          softButton,
          pairsButton
      );
    });


    softButton.setOnAction(e ->
    {
      showSoftMatrix(
          matrixHolder,
          detailHolder,
          strategyMode,
          settings,
          allowComparison
              && showDifferences.isSelected()
      );

      styleMatrixToggle(
          softButton,
          hardButton,
          softButton,
          pairsButton
      );
    });


    pairsButton.setOnAction(e ->
    {
      showPairMatrix(
          matrixHolder,
          detailHolder,
          strategyMode,
          settings,
          allowComparison
              && showDifferences.isSelected()
      );

      styleMatrixToggle(
          pairsButton,
          hardButton,
          softButton,
          pairsButton
      );
    });


    hardButton.setSelected(
        true
    );


    if (allowComparison)
    {
      showDifferences.setOnAction(e ->
      {
        boolean comparing =
            showDifferences.isSelected();


        if (hardButton.isSelected())
        {
          showHardMatrix(
              matrixHolder,
              detailHolder,
              strategyMode,
              settings,
              comparing
          );
        }
        else if (softButton.isSelected())
        {
          showSoftMatrix(
              matrixHolder,
              detailHolder,
              strategyMode,
              settings,
              comparing
          );
        }
        else
        {
          showPairMatrix(
              matrixHolder,
              detailHolder,
              strategyMode,
              settings,
              comparing
          );
        }


        detailHolder.setVisible(
            comparing
        );

        detailHolder.setManaged(
            comparing
        );


        if (comparing)
        {
          showMatrixHint(
              detailHolder
          );
        }
      });
    }


    showHardMatrix(
        matrixHolder,
        detailHolder,
        strategyMode,
        settings,
        false
    );

    styleMatrixToggle(
        hardButton,
        hardButton,
        softButton,
        pairsButton
    );


    HBox legend =
        new HBox(
            18,
            createLegendItem(
                "H",
                "Hit"
            ),
            createLegendItem(
                "S",
                "Stand"
            ),
            createLegendItem(
                "D",
                "Double"
            ),
            createLegendItem(
                "P",
                "Split"
            ),
            createLegendItem(
                "R",
                "Surrender"
            )
        );

    legend.setAlignment(
        Pos.CENTER
    );


    VBox content =
        new VBox(
            10,
            title,
            subtitle,
            showDifferences,
            matrixTabs,
            matrixHolder,
            legend,
            detailHolder
        );

    content.setAlignment(
        Pos.TOP_CENTER
    );


    return content;
  }


  // ============================================================
  // HARD
  // ============================================================

  private static void showHardMatrix(
      VBox holder,
      VBox detailHolder,
      StrategyMode strategyMode,
      GameSettings settings,
      boolean showDifferences)
  {
    if (showDifferences)
    {
      showMatrixHint(
          detailHolder
      );
    }


    int[] totals =
    {
        17,
        16,
        15,
        14,
        13,
        12,
        11,
        10,
        9,
        8
    };


    GridPane grid =
        createMatrixGrid();

    addDealerHeader(
        grid
    );


    int row =
        1;


    for (int total :
         totals)
    {
      addMatrixRowLabel(
          grid,
          Integer.toString(
              total
          ),
          row
      );


      for (int dealerIndex = 0;
           dealerIndex < 10;
           dealerIndex++)
      {
        int dealerValue =
            dealerValue(
                dealerIndex
            );


        PlayerDecisionContext context =
            createMatrixContext(
                hardRanks(
                    total
                ),
                dealerValue,
                settings
            );


        addContextAction(
            grid,
            context,
            strategyMode,
            showDifferences,
            detailHolder,
            dealerIndex + 1,
            row
        );
      }


      row++;
    }


    holder.getChildren().setAll(
        grid
    );
  }


  // ============================================================
  // SOFT
  // ============================================================

  private static void showSoftMatrix(
      VBox holder,
      VBox detailHolder,
      StrategyMode strategyMode,
      GameSettings settings,
      boolean showDifferences)
  {
    if (showDifferences)
    {
      showMatrixHint(
          detailHolder
      );
    }


    int[] secondCards =
    {
        9,
        8,
        7,
        6,
        5,
        4,
        3,
        2
    };


    GridPane grid =
        createMatrixGrid();

    addDealerHeader(
        grid
    );


    int row =
        1;


    for (int secondCard :
         secondCards)
    {
      addMatrixRowLabel(
          grid,
          "A,"
          + secondCard,
          row
      );


      for (int dealerIndex = 0;
           dealerIndex < 10;
           dealerIndex++)
      {
        int dealerValue =
            dealerValue(
                dealerIndex
            );


        Rank[] playerRanks =
        {
            Rank.ACE,
            rank(
                secondCard
            )
        };


        PlayerDecisionContext context =
            createMatrixContext(
                playerRanks,
                dealerValue,
                settings
            );


        addContextAction(
            grid,
            context,
            strategyMode,
            showDifferences,
            detailHolder,
            dealerIndex + 1,
            row
        );
      }


      row++;
    }


    holder.getChildren().setAll(
        grid
    );
  }


  // ============================================================
  // PAIRS
  // ============================================================

  private static void showPairMatrix(
      VBox holder,
      VBox detailHolder,
      StrategyMode strategyMode,
      GameSettings settings,
      boolean showDifferences)
  {
    if (showDifferences)
    {
      showMatrixHint(
          detailHolder
      );
    }


    int[] pairValues =
    {
        11,
        10,
        9,
        8,
        7,
        6,
        5,
        4,
        3,
        2
    };


    GridPane grid =
        createMatrixGrid();

    addDealerHeader(
        grid
    );


    int row =
        1;


    for (int pairValue :
         pairValues)
    {
      String label =
          pairValue == 11
              ? "A,A"
              : pairValue
                + ","
                + pairValue;


      addMatrixRowLabel(
          grid,
          label,
          row
      );


      for (int dealerIndex = 0;
           dealerIndex < 10;
           dealerIndex++)
      {
        int dealerValue =
            dealerValue(
                dealerIndex
            );


        Rank pairRank =
            rank(
                pairValue
            );


        Rank[] playerRanks =
        {
            pairRank,
            pairRank
        };


        PlayerDecisionContext context =
            createMatrixContext(
                playerRanks,
                dealerValue,
                settings
            );


        addContextAction(
            grid,
            context,
            strategyMode,
            showDifferences,
            detailHolder,
            dealerIndex + 1,
            row
        );
      }


      row++;
    }


    holder.getChildren().setAll(
        grid
    );
  }


  // ============================================================
  // ACTION LOOKUP
  // ============================================================

  private static void addContextAction(
      GridPane grid,
      PlayerDecisionContext context,
      StrategyMode strategyMode,
      boolean showDifferences,
      VBox detailHolder,
      int column,
      int row)
  {
    HandAction action =
        getMatrixAction(
            context,
            strategyMode
        );


    StrategyDeviation deviation =
        null;


    if (strategyMode
            == StrategyMode.SOCIAL
        && showDifferences)
    {
      deviation =
          StrategyDeviationCatalog.find(
              context
          );
    }


    addMatrixAction(
        grid,
        action,
        deviation,
        detailHolder,
        column,
        row
    );
  }


  private static HandAction getMatrixAction(
      PlayerDecisionContext context,
      StrategyMode strategyMode)
  {
    if (strategyMode
        == StrategyMode.SOCIAL)
    {
      return new SocialStrategy()
          .chooseRecommendedAction(
              context
          );
    }


    return new BasicStrategy()
        .chooseRecommendedAction(
            context
        );
  }


  // ============================================================
  // CONTEXT
  // ============================================================

  private static PlayerDecisionContext
      createMatrixContext(
          Rank[] playerRanks,
          int dealerValue,
          GameSettings settings)
  {
    Hand hand =
        new Hand();


    Shoe source =
        new Shoe(
            settings.getDecksInShoe(),
            987_654_321L
        );


    for (Rank playerRank :
         playerRanks)
    {
      hand.addCard(
          drawRank(
              source,
              playerRank
          )
      );
    }


    Card dealerUpCard =
        drawRank(
            source,
            rank(
                dealerValue
            )
        );


    EnumSet<HandAction> allowedActions =
        PlayerActionRules.getAllowedActions(
            hand,
            false,
            settings
        );


    EnumSet<HandAction> availableActions =
        EnumSet.copyOf(
            allowedActions
        );


    return new PlayerDecisionContext(
        hand,
        dealerUpCard,
        settings,
        allowedActions,
        availableActions,
        false,
        settings.getDecksInShoe(),
        25.0
    );
  }


  private static Card drawRank(
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


  // ============================================================
  // GRID
  // ============================================================

  private static GridPane createMatrixGrid()
  {
    GridPane grid =
        new GridPane();

    grid.setHgap(
        3
    );

    grid.setVgap(
        3
    );

    grid.setAlignment(
        Pos.CENTER
    );

    grid.setPadding(
        new Insets(
            8
        )
    );

    grid.setStyle("""
        -fx-background-color: rgba(255, 255, 255, 0.025);
        -fx-background-radius: 8px;
    """);


    return grid;
  }


  private static void addDealerHeader(
      GridPane grid)
  {
    Label corner =
        new Label(
            "HAND"
        );

    corner.setPrefWidth(
        54
    );

    corner.setAlignment(
        Pos.CENTER
    );

    corner.setStyle("""
        -fx-font-size: 10px;
        -fx-font-weight: bold;
        -fx-text-fill: #888888;
    """);


    grid.add(
        corner,
        0,
        0
    );


    String[] dealers =
    {
        "2",
        "3",
        "4",
        "5",
        "6",
        "7",
        "8",
        "9",
        "10",
        "A"
    };


    for (int i = 0;
         i < dealers.length;
         i++)
    {
      Label label =
          new Label(
              dealers[i]
          );

      label.setPrefSize(
          42,
          28
      );

      label.setAlignment(
          Pos.CENTER
      );

      label.setStyle("""
          -fx-font-size: 11px;
          -fx-font-weight: bold;
          -fx-text-fill: #aaaaaa;
      """);


      grid.add(
          label,
          i + 1,
          0
      );
    }
  }


  private static void addMatrixRowLabel(
      GridPane grid,
      String text,
      int row)
  {
    Label label =
        new Label(
            text
        );

    label.setPrefSize(
        54,
        34
    );

    label.setAlignment(
        Pos.CENTER
    );

    label.setStyle("""
        -fx-font-size: 11px;
        -fx-font-weight: bold;
        -fx-text-fill: #dddddd;
    """);


    grid.add(
        label,
        0,
        row
    );
  }


  private static void addMatrixAction(
      GridPane grid,
      HandAction action,
      StrategyDeviation deviation,
      VBox detailHolder,
      int column,
      int row)
  {
    Label label =
        new Label(
            actionLetter(
                action
            )
        );

    label.setPrefSize(
        42,
        34
    );

    label.setAlignment(
        Pos.CENTER
    );


    if (deviation != null)
    {
      label.setStyle(
          deviationActionStyle(
              action
          )
      );

      label.setCursor(
          Cursor.HAND
      );

      label.setOnMouseClicked(e ->
          showDeviationDetails(
              detailHolder,
              deviation
          )
      );
    }
    else
    {
      label.setStyle(
          actionStyle(
              action
          )
      );
    }


    grid.add(
        label,
        column,
        row
    );
  }


  // ============================================================
  // DISPLAY HELPERS
  // ============================================================

  private static String actionLetter(
      HandAction action)
  {
    switch (action)
    {
      case HIT:
        return "H";

      case STAND:
        return "S";

      case DOUBLE:
        return "D";

      case SPLIT:
        return "P";

      case SURRENDER:
        return "R";

      default:
        return "?";
    }
  }


  private static String actionName(
      HandAction action)
  {
    switch (action)
    {
      case HIT:
        return "Hit";

      case STAND:
        return "Stand";

      case DOUBLE:
        return "Double";

      case SPLIT:
        return "Split";

      case SURRENDER:
        return "Surrender";

      default:
        return action.toString();
    }
  }


  private static String actionStyle(
      HandAction action)
  {
    String background;


    switch (action)
    {
      case HIT:
        background =
            "#38475a";
        break;

      case STAND:
        background =
            "#36533d";
        break;

      case DOUBLE:
        background =
            "#66572f";
        break;

      case SPLIT:
        background =
            "#554269";
        break;

      case SURRENDER:
        background =
            "#663c3c";
        break;

      default:
        background =
            "#3b3b3b";
        break;
    }


    return String.format(
        """
        -fx-background-color: %s;
        -fx-background-radius: 4px;
        -fx-text-fill: white;
        -fx-font-size: 12px;
        -fx-font-weight: bold;
        """,
        background
    );
  }


  private static String deviationActionStyle(
      HandAction action)
  {
    return actionStyle(
        action
    )
    + """
      -fx-border-color: #d4af37;
      -fx-border-width: 2px;
      -fx-border-radius: 4px;
      """;
  }


  private static HBox createLegendItem(
      String letter,
      String meaning)
  {
    Label letterLabel =
        new Label(
            letter
        );

    letterLabel.setStyle("""
        -fx-font-size: 11px;
        -fx-font-weight: bold;
        -fx-text-fill: #d4af37;
    """);


    Label meaningLabel =
        new Label(
            meaning
        );

    meaningLabel.setStyle("""
        -fx-font-size: 10px;
        -fx-text-fill: #999999;
    """);


    HBox item =
        new HBox(
            4,
            letterLabel,
            meaningLabel
        );

    item.setAlignment(
        Pos.CENTER
    );


    return item;
  }


  private static void styleMatrixToggle(
      ToggleButton selected,
      ToggleButton... buttons)
  {
    for (ToggleButton button :
         buttons)
    {
      button.setStyle(
          button == selected
              ? ACTIVE_TAB_STYLE
              : INACTIVE_TAB_STYLE
      );
    }
  }


  // ============================================================
  // SOCIAL COMPARISON DETAILS
  // ============================================================

  private static void showMatrixHint(
      VBox holder)
  {
    Label hint =
        new Label(
            "Gold-outlined cells are intentional Social Strategy "
            + "deviations from Basic Strategy. Select one to see "
            + "the research behind the change."
        );

    hint.setWrapText(
        true
    );

    hint.setMaxWidth(
        580
    );

    hint.setMinHeight(
        Region.USE_PREF_SIZE
    );

    hint.setAlignment(
        Pos.CENTER
    );

    hint.setStyle("""
        -fx-font-size: 11px;
        -fx-font-style: italic;
        -fx-text-fill: #888888;
    """);


    holder.getChildren().setAll(
        hint
    );

    holder.setAlignment(
        Pos.CENTER
    );

    holder.setPadding(
        new Insets(
            9,
            12,
            9,
            12
        )
    );

    holder.setMinHeight(
        Region.USE_PREF_SIZE
    );

    holder.setStyle("""
        -fx-background-color: rgba(255, 255, 255, 0.025);
        -fx-background-radius: 8px;
    """);
  }


  private static void showDeviationDetails(
      VBox holder,
      StrategyDeviation deviation)
  {
    Label title =
        new Label(
            deviation.getTitle()
        );

    title.setStyle("""
        -fx-font-size: 13px;
        -fx-font-weight: bold;
        -fx-text-fill: #d4af37;
    """);


    Label comparison =
        new Label(
            "Basic: "
            + actionName(
                deviation.getBasicAction()
            )
            + "   •   Social: "
            + actionName(
                deviation.getSocialAction()
            )
        );

    comparison.setStyle("""
        -fx-font-size: 12px;
        -fx-font-weight: bold;
        -fx-text-fill: #dddddd;
    """);


    Label explanation =
        detailLabel(
            deviation.getExplanation(),
            "#cccccc",
            11
        );


    Label evidence =
        detailLabel(
            deviation.getEvidence(),
            "#999999",
            10
        );


    Label takeaway =
        detailLabel(
            deviation.getTakeaway(),
            "#d4af37",
            11
        );

    takeaway.setStyle(
        takeaway.getStyle()
        + "-fx-font-style: italic;"
    );


    holder.getChildren().setAll(
        title,
        comparison,
        explanation,
        evidence,
        takeaway
    );

    holder.setSpacing(
        4
    );

    holder.setAlignment(
        Pos.TOP_LEFT
    );

    holder.setPadding(
        new Insets(
            9,
            12,
            9,
            12
        )
    );

    holder.setMinHeight(
        Region.USE_PREF_SIZE
    );

    holder.setStyle("""
        -fx-background-color: rgba(255, 255, 255, 0.04);
        -fx-background-radius: 8px;
    """);
  }


  private static Label detailLabel(
      String text,
      String color,
      int fontSize)
  {
    Label label =
        new Label(
            text
        );

    label.setWrapText(
        true
    );

    label.setMaxWidth(
        580
    );

    label.setMinHeight(
        Region.USE_PREF_SIZE
    );

    label.setStyle(
        "-fx-font-size: "
        + fontSize
        + "px;"
        + "-fx-text-fill: "
        + color
        + ";"
    );


    return label;
  }


  // ============================================================
  // REPRESENTATIVE CARDS
  // ============================================================

  private static Rank[] hardRanks(
      int total)
  {
    switch (total)
    {
      case 17:
        return new Rank[] {
            rank(10),
            rank(7)
        };

      case 16:
        return new Rank[] {
            rank(10),
            rank(6)
        };

      case 15:
        return new Rank[] {
            rank(10),
            rank(5)
        };

      case 14:
        return new Rank[] {
            rank(10),
            rank(4)
        };

      case 13:
        return new Rank[] {
            rank(10),
            rank(3)
        };

      case 12:
        return new Rank[] {
            rank(10),
            rank(2)
        };

      case 11:
        return new Rank[] {
            rank(6),
            rank(5)
        };

      case 10:
        return new Rank[] {
            rank(6),
            rank(4)
        };

      case 9:
        return new Rank[] {
            rank(5),
            rank(4)
        };

      case 8:
        return new Rank[] {
            rank(5),
            rank(3)
        };

      default:
        throw new IllegalArgumentException(
            "Unsupported hard total: "
            + total
        );
    }
  }


  private static int dealerValue(
      int dealerIndex)
  {
    if (dealerIndex == 9)
      return 11;


    return dealerIndex + 2;
  }


  private static Rank rank(
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
}
