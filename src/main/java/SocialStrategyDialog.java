import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SocialStrategyDialog
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


  public static void show(
      Stage ownerStage,
      GameSettings.BlackjackPayout payout)
  {
    BorderPane root =
        new BorderPane();

    root.setStyle("""
        -fx-background-color: #282828;
    """);


    Label title =
        new Label(
            "SOCIAL STRATEGY"
        );

    title.setStyle("""
        -fx-font-size: 22px;
        -fx-font-weight: bold;
        -fx-text-fill: #d4af37;
    """);


    Label subtitle =
        new Label(
            "Learn it. Compare it. Test it."
        );

    subtitle.setStyle("""
        -fx-font-size: 12px;
        -fx-text-fill: #aaaaaa;
    """);


    VBox titleArea =
        new VBox(
            3,
            title,
            subtitle
        );

    titleArea.setAlignment(
        Pos.CENTER
    );


    Button overviewButton =
        new Button(
            "OVERVIEW"
        );

    Button rulesButton =
        new Button(
            "RULES"
        );

    Button matrixButton =
        new Button(
            "MATRIX"
        );

    Button simulationButton =
        new Button(
            "SIMULATION"
        );


    HBox navigation =
        new HBox(
            10,
            overviewButton,
            rulesButton,
            matrixButton,
            simulationButton
        );

    navigation.setAlignment(
        Pos.CENTER
    );


    VBox topArea =
        new VBox(
            14,
            titleArea,
            navigation
        );

    topArea.setAlignment(
        Pos.CENTER
    );

    topArea.setPadding(
        new Insets(
            20,
            20,
            14,
            20
        )
    );


    VBox contentHolder =
        new VBox();

    contentHolder.setAlignment(
        Pos.TOP_CENTER
    );

    contentHolder.setPadding(
        new Insets(
            8,
            24,
            18,
            24
        )
    );


    Button closeButton =
        new Button(
            "CLOSE"
        );

    closeButton.setStyle("""
        -fx-padding: 7px 18px;
    """);


    HBox bottomArea =
        new HBox(
            closeButton
        );

    bottomArea.setAlignment(
        Pos.CENTER
    );

    bottomArea.setPadding(
        new Insets(
            0,
            0,
            20,
            0
        )
    );


    root.setTop(
        topArea
    );

    root.setCenter(
        contentHolder
    );

    root.setBottom(
        bottomArea
    );


    Button[] tabButtons =
    {
        overviewButton,
        rulesButton,
        matrixButton,
        simulationButton
    };


    overviewButton.setOnAction(e ->
    {
      setContent(
          contentHolder,
          createOverview(
              payout
          )
      );

      selectTab(
          overviewButton,
          tabButtons
      );
    });


    rulesButton.setOnAction(e ->
    {
      setContent(
          contentHolder,
          createRules()
      );

      selectTab(
          rulesButton,
          tabButtons
      );
    });


      matrixButton.setOnAction(e ->
      {
        setContent(
            contentHolder,
            StrategyMatrixView.createSocial()
        );

        selectTab(
            matrixButton,
            tabButtons
        );
      });
      
    simulationButton.setOnAction(e ->
    {
      setContent(
          contentHolder,
          createSimulation(
              payout
          )
      );

      selectTab(
          simulationButton,
          tabButtons
      );
    });


    /*
     * Start on Overview.
     */
    setContent(
        contentHolder,
        createOverview(
            payout
        )
    );

    selectTab(
        overviewButton,
        tabButtons
    );


    Stage dialog =
        new Stage();

    dialog.initOwner(
        ownerStage
    );

    dialog.initModality(
        Modality.APPLICATION_MODAL
    );

    dialog.setTitle(
        "Social Strategy"
    );

    dialog.setScene(
        new Scene(
            root,
            700,
            800
        )
    );

    dialog.setResizable(
        false
    );


    closeButton.setOnAction(
        e -> dialog.close()
    );


    dialog.showAndWait();
  }


  private static Node createOverview(
      GameSettings.BlackjackPayout payout)
  {
    Label subtitle =
        new Label(
            "A simpler way to play strong blackjack."
        );

    subtitle.setStyle("""
        -fx-font-size: 14px;
        -fx-text-fill: #dddddd;
    """);


    VBox whatArea =
        createTextSection(
            "WHAT IS IT?",
            "Social Strategy is a simplified alternative to perfect Basic Strategy. "
            + "It removes selected exceptions and irregular rules that are difficult "
            + "to remember, while trying to preserve nearly all of Basic Strategy's value."
        );


    VBox purposeArea =
        createTextSection(
            "WHY?",
            "Perfect Basic Strategy is mathematically stronger, but it contains many "
            + "small exceptions. Social Strategy asks whether each exception is actually "
            + "worth memorizing during real casino play."
        );


    VBox prioritiesArea =
        createTextSection(
            "DESIGNED AROUND",
            "• Simpler rules to remember\n"
            + "• Small expected-value sacrifice\n"
            + "• Bankroll and risk effects\n"
            + "• Natural, practical casino decisions"
        );


      Label researchTitle =
          new Label(
              "RESEARCH TESTED"
          );

      researchTitle.setStyle("""
          -fx-font-size: 12px;
          -fx-font-weight: bold;
          -fx-text-fill: #d4af37;
      """);


      Label researchHeadline =
          new Label(
              "About $3 per 160-hand night."
          );

      researchHeadline.setStyle("""
          -fx-font-size: 17px;
          -fx-font-weight: bold;
          -fx-text-fill: #339af0;
      """);


      Label researchText =
          new Label(
              "Across 1,000,000 paired simulated casino nights, "
              + "Social Strategy finished about $3 behind perfect Basic Strategy "
              + "on average over 160 hands at a $25 flat wager."
          );

      researchText.setWrapText(
          true
      );

      researchText.setMaxWidth(
          620
      );

      researchText.setMinHeight(
          Region.USE_PREF_SIZE
      );

      researchText.setStyle("""
          -fx-font-size: 12px;
          -fx-text-fill: #cccccc;
      """);


      Label evValue =
          createResearchMetricValue(
              "~$3"
          );

      Label evLabel =
          createResearchMetricLabel(
              "EV cost per night"
          );

      VBox evMetric =
          createResearchMetric(
              evValue,
              evLabel
          );


      Label ruinValue =
          createResearchMetricValue(
              "< 0.4 pp"
          );

      Label ruinLabel =
          createResearchMetricLabel(
              "Added risk of ruin"
          );

      VBox ruinMetric =
          createResearchMetric(
              ruinValue,
              ruinLabel
          );


      Label acceptableValue =
          createResearchMetricValue(
              "< 0.5 pp"
          );

      Label acceptableLabel =
          createResearchMetricLabel(
              "Lower acceptable-night rate"
          );

      VBox acceptableMetric =
          createResearchMetric(
              acceptableValue,
              acceptableLabel
          );


      HBox researchMetrics =
          new HBox(
              20,
              evMetric,
              ruinMetric,
              acceptableMetric
          );

      researchMetrics.setAlignment(
          Pos.CENTER
      );


      Label acceptableDefinition =
          new Label(
              "Acceptable night = finishing above $400 from a $500 starting bankroll "
              + "(losing less than $100)."
          );

      acceptableDefinition.setWrapText(
          true
      );

      acceptableDefinition.setMaxWidth(
          620
      );

      acceptableDefinition.setAlignment(
          Pos.CENTER
      );

      acceptableDefinition.setMinHeight(
          Region.USE_PREF_SIZE
      );

      acceptableDefinition.setStyle("""
          -fx-font-size: 10px;
          -fx-font-style: italic;
          -fx-text-fill: #999999;
      """);


      Label tableTitle =
          new Label(
              "TABLE SELECTION MATTERS MORE"
          );

      tableTitle.setStyle("""
          -fx-font-size: 11px;
          -fx-font-weight: bold;
          -fx-text-fill: #d4af37;
      """);


      Label tableText =
          new Label(
              "With Social Strategy, average P/L improved from -$74.26 at a 6:5 table "
              + "to -$24.91 at a 3:2 table. Risk of ruin also fell from 22.88% to 18.93%, "
              + "and the acceptable-night rate rose from 51.18% to 56.56%."
          );

      tableText.setWrapText(
          true
      );

      tableText.setMaxWidth(
          620
      );

      tableText.setMinHeight(
          Region.USE_PREF_SIZE
      );

      tableText.setStyle("""
          -fx-font-size: 11px;
          -fx-text-fill: #bbbbbb;
      """);


      VBox tableCallout =
          new VBox(
              4,
              tableTitle,
              tableText
          );

      tableCallout.setPadding(
          new Insets(
              9,
              10,
              9,
              10
          )
      );

      tableCallout.setStyle("""
          -fx-background-color: rgba(212, 175, 55, 0.055);
          -fx-background-radius: 6px;
      """);


      Label validationText =
          new Label(
              "Research baseline: 8 decks, dealer hits soft 17, double after split, "
              + "late surrender, $500 starting bankroll, $25 flat wager. "
              + "Both 6:5 and 3:2 blackjack payouts were tested."
          );

      validationText.setWrapText(
          true
      );

      validationText.setMaxWidth(
          620
      );

      validationText.setMinHeight(
          Region.USE_PREF_SIZE
      );

      validationText.setStyle("""
          -fx-font-size: 10px;
          -fx-text-fill: #777777;
      """);


      VBox researchArea =
          new VBox(
              9,
              researchTitle,
              researchHeadline,
              researchText,
              researchMetrics,
              acceptableDefinition,
              tableCallout,
              validationText
          );

      researchArea.setPadding(
          new Insets(
              12
          )
      );

      researchArea.setMaxWidth(
          650
      );

      researchArea.setStyle("""
          -fx-background-color: rgba(255, 255, 255, 0.04);
          -fx-background-radius: 8px;
      """);
      

    Label note =
        new Label(
            "Social Strategy is not mathematically optimal. "
            + "Basic Strategy remains the best choice for maximum expected value, "
            + "while Social Strategy prioritizes simplicity with only a small measured tradeoff."
        );

    note.setWrapText(
        true
    );

    note.setMaxWidth(
        640
    );
      note.setMinHeight(
          Region.USE_PREF_SIZE
      );
    note.setAlignment(
        Pos.CENTER
    );

    note.setStyle("""
        -fx-font-size: 11px;
        -fx-font-style: italic;
        -fx-text-fill: #aaaaaa;
    """);


    VBox content =
        new VBox(
            14,
            subtitle,
            whatArea,
            purposeArea,
            prioritiesArea,
            researchArea,
            note
        );

    content.setAlignment(
        Pos.TOP_CENTER
    );
      content.setMinHeight(
          Region.USE_PREF_SIZE
      );
    return content;
  }

    private static Label createResearchMetricValue(
        String text)
    {
      Label label =
          new Label(
              text
          );

      label.setStyle("""
          -fx-font-size: 16px;
          -fx-font-weight: bold;
          -fx-text-fill: #339af0;
      """);

      return label;
    }


    private static Label createResearchMetricLabel(
        String text)
    {
      Label label =
          new Label(
              text
          );

      label.setWrapText(
          true
      );

      label.setMaxWidth(
          150
      );

      label.setAlignment(
          Pos.CENTER
      );

      label.setStyle("""
          -fx-font-size: 10px;
          -fx-text-fill: #aaaaaa;
      """);

      return label;
    }


    private static VBox createResearchMetric(
        Label value,
        Label description)
    {
      VBox box =
          new VBox(
              2,
              value,
              description
          );

      box.setAlignment(
          Pos.CENTER
      );

      box.setPrefWidth(
          175
      );

      return box;
    }
    
    
  private static Node createRules()
  {
    Label subtitle =
        new Label(
            "The rules are checked in this order."
        );

    subtitle.setStyle("""
        -fx-font-size: 12px;
        -fx-text-fill: #aaaaaa;
    """);


    VBox surrender =
        createRuleSection(
            "1. SURRENDER",
            """
            Surrender hard 15 or 16 against dealer 10.
            """
        );

    surrender.setMaxWidth(
        680
    );


    VBox pairs =
        createRuleSection(
            "2. PAIRS",
            """
            A,A → Always split
            10,10 → Always stand
            9,9 → Split vs 2–9
            8,8 → Always split

            Pairs 7 or lower:
            • Split vs 2–7
            • Except never split 5s or 4s
            """
        );


    VBox soft =
        createRuleSection(
            "3. SOFT HANDS",
            """
            Soft 19+ → Stand

            Soft 18:
            • Double vs 4–6
            • Hit vs 9, 10, Ace
            • Otherwise stand

            Soft 17 or lower:
            • Double vs 4–6
            • Otherwise hit
            """
        );


    VBox hard =
        createRuleSection(
            "4. HARD HANDS",
            """
            Hard 17+ → Stand

            Hard 12–16:
            • Stand vs 2–6
            • Hit vs 7–Ace

            Hard 11 → Double

            Hard 10:
            • Double vs 2–9
            • Hit vs 10 or Ace

            Hard 9:
            • Double vs 2–6
            • Otherwise hit

            Hard 8 or lower → Hit
            """
        );


    GridPane rulesGrid =
        new GridPane();

    rulesGrid.setHgap(
        14
    );

    rulesGrid.setVgap(
        14
    );

    rulesGrid.setAlignment(
        Pos.CENTER
    );


    rulesGrid.add(
        surrender,
        0,
        0,
        2,
        1
    );

    rulesGrid.add(
        pairs,
        0,
        1
    );

    rulesGrid.add(
        soft,
        0,
        2
    );

    rulesGrid.add(
        hard,
        1,
        1,
        1,
        2
    );


    Label fallback =
        new Label(
            "If doubling or splitting is unavailable, "
            + "use the normal hit/stand fallback."
        );

    fallback.setWrapText(
        true
    );

    fallback.setMaxWidth(
        620
    );

    fallback.setAlignment(
        Pos.CENTER
    );

    fallback.setMinHeight(
        Region.USE_PREF_SIZE
    );

    fallback.setStyle("""
        -fx-font-size: 11px;
        -fx-font-style: italic;
        -fx-text-fill: #999999;
    """);


    VBox content =
        new VBox(
            14,
            subtitle,
            rulesGrid,
            fallback
        );

    content.setAlignment(
        Pos.TOP_CENTER
    );

    return content;
  }


    
  private static Node createSimulation(
      GameSettings.BlackjackPayout payout)
  {
    VBox simulationHolder =
        new VBox();

    simulationHolder.setAlignment(
        Pos.TOP_CENTER
    );


    showSimulationIntro(
        simulationHolder,
        payout
    );


    return simulationHolder;
  }


  private static void showSimulationIntro(
      VBox holder,
      GameSettings.BlackjackPayout payout)
  {
    Label title =
        new Label(
            "QUICK SIMULATION"
        );

    title.setStyle("""
        -fx-font-size: 18px;
        -fx-font-weight: bold;
        -fx-text-fill: #d4af37;
    """);


    Label subtitle =
        new Label(
            "10 casino nights • 160 paired hands each • $25 flat bet"
        );

    subtitle.setStyle("""
        -fx-font-size: 12px;
        -fx-text-fill: #aaaaaa;
    """);


    Label explanation =
        new Label(
            "Each hand gives Basic and Social the same starting cards. "
            + "This isolates the effect of the strategy choice while each "
            + "strategy's bankroll still carries through the full 160-hand night."
        );

    explanation.setWrapText(
        true
    );

    explanation.setMaxWidth(
        560
    );

    explanation.setAlignment(
        Pos.CENTER
    );

    explanation.setStyle("""
        -fx-font-size: 12px;
        -fx-text-fill: #cccccc;
    """);


    Button runButton =
        new Button(
            "RUN QUICK SIMULATION"
        );

    runButton.setStyle("""
        -fx-background-color: #d4af37;
        -fx-text-fill: #1f1f1f;
        -fx-font-weight: bold;
        -fx-background-radius: 5px;
        -fx-padding: 9px 18px;
    """);


    holder.getChildren().setAll(
        title,
        subtitle,
        explanation,
        runButton
    );

    holder.setSpacing(
        16
    );

    holder.setPadding(
        new Insets(
            70,
            20,
            20,
            20
        )
    );


    runButton.setOnAction(e ->
        showSimulationResults(
            holder,
            payout
        )
    );
  }


  private static void showSimulationResults(
      VBox holder,
      GameSettings.BlackjackPayout payout)
  {
    SocialStrategyQuickSimulation.Result result =
        SocialStrategyQuickSimulation.run(
            payout
        );


    Label title =
        new Label(
            "QUICK SIMULATION"
        );

    title.setStyle("""
        -fx-font-size: 18px;
        -fx-font-weight: bold;
        -fx-text-fill: #d4af37;
    """);


    Label subtitle =
        new Label(
            "10 casino nights • 160 paired hands each • $25 flat bet"
        );

    subtitle.setStyle("""
        -fx-font-size: 12px;
        -fx-text-fill: #aaaaaa;
    """);


    GridPane grid =
        new GridPane();

    grid.setHgap(
        28
    );

    grid.setVgap(
        7
    );

    grid.setAlignment(
        Pos.CENTER
    );


    addSimulationLabel(
        grid,
        "NIGHT",
        0,
        0,
        true
    );

    addSimulationLabel(
        grid,
        "BASIC",
        1,
        0,
        true
    );

    addSimulationLabel(
        grid,
        "SOCIAL",
        2,
        0,
        true
    );

    addSimulationLabel(
        grid,
        "DIFF",
        3,
        0,
        true
    );


    int row =
        1;


    for (SocialStrategyQuickSimulation.SessionResult session :
         result.getSessions())
    {
      addSimulationLabel(
          grid,
          Integer.toString(
              session.getSessionNumber()
          ),
          0,
          row,
          false
      );


      addSimulationLabel(
          grid,
          money(
              session.getBasicBankroll()
          ),
          1,
          row,
          false
      );


      addSimulationLabel(
          grid,
          money(
              session.getSocialBankroll()
          ),
          2,
          row,
          false
      );


      Label difference =
          addSimulationLabel(
              grid,
              signedMoney(
                  session.getDifference()
              ),
              3,
              row,
              false
          );


      if (session.getDifference() > 0)
      {
        difference.setStyle("""
            -fx-font-size: 12px;
            -fx-text-fill: #7bd88f;
        """);
      }
      else if (session.getDifference() < 0)
      {
        difference.setStyle("""
            -fx-font-size: 12px;
            -fx-text-fill: #ff6b6b;
        """);
      }


      row++;
    }


    Label comparison =
        new Label(
            "Social ahead: "
            + result.getSocialAhead()
            + "   •   Basic ahead: "
            + result.getBasicAhead()
            + "   •   Ties: "
            + result.getTies()
        );

    comparison.setStyle("""
        -fx-font-size: 13px;
        -fx-font-weight: bold;
        -fx-text-fill: #dddddd;
    """);


    Label average =
        new Label(
            "Average Social − Basic: "
            + signedMoney(
                result.getAverageDifference()
            )
            + " per night"
        );

    average.setStyle("""
        -fx-font-size: 15px;
        -fx-font-weight: bold;
        -fx-text-fill: #339af0;
    """);


    Label warning =
        new Label(
            "Each hand gives Basic and Social the same starting cards, "
            + "isolating the effect of their strategy choices. "
            + "Published research figures come from 1,000,000 full simulated sessions; this is 10 sessions."
        );

    warning.setWrapText(
        true
    );

    warning.setMaxWidth(
        560
    );

    warning.setAlignment(
        Pos.CENTER
    );

    warning.setStyle("""
        -fx-font-size: 11px;
        -fx-font-style: italic;
        -fx-text-fill: #999999;
    """);


    Button runAgainButton =
        new Button(
            "RUN AGAIN"
        );

    runAgainButton.setStyle(
        INACTIVE_TAB_STYLE
    );


    holder.getChildren().setAll(
        title,
        subtitle,
        grid,
        comparison,
        average,
        warning,
        runAgainButton
    );

    holder.setSpacing(
        12
    );

    holder.setPadding(
        new Insets(
            8,
            20,
            10,
            20
        )
    );


    runAgainButton.setOnAction(e ->
        showSimulationResults(
            holder,
            payout
        )
    );
  }


  private static VBox createTextSection(
      String headingText,
      String bodyText)
  {
    Label heading =
        new Label(
            headingText
        );

    heading.setStyle("""
        -fx-font-size: 12px;
        -fx-font-weight: bold;
        -fx-text-fill: #d4af37;
    """);


    Label body =
        new Label(
            bodyText
        );

    body.setWrapText(
        true
    );

    body.setMaxWidth(
        640
    );

    body.setMinHeight(
        Region.USE_PREF_SIZE
    );

    body.setStyle("""
        -fx-font-size: 12px;
        -fx-text-fill: #cccccc;
    """);


    VBox section =
        new VBox(
            4,
            heading,
            body
        );

    section.setMaxWidth(
        640
    );

    section.setMinHeight(
        Region.USE_PREF_SIZE
    );

    return section;
  }


  private static VBox createRuleSection(
      String headingText,
      String bodyText)
  {
    Label heading =
        new Label(
            headingText
        );

    heading.setStyle("""
        -fx-font-size: 13px;
        -fx-font-weight: bold;
        -fx-text-fill: #d4af37;
    """);


    Label body =
        new Label(
            bodyText.strip()
        );

    body.setWrapText(
        true
    );

    body.setMaxWidth(
        300
    );

    body.setMinHeight(
        Region.USE_PREF_SIZE
    );

    body.setStyle("""
        -fx-font-size: 12px;
        -fx-text-fill: #dddddd;
    """);


    VBox box =
        new VBox(
            5,
            heading,
            body
        );

    box.setMinHeight(
        Region.USE_PREF_SIZE
    );

    box.setMaxWidth(
        325
    );

    box.setPadding(
        new Insets(
            9,
            12,
            9,
            12
        )
    );

    box.setStyle("""
        -fx-background-color: rgba(255, 255, 255, 0.035);
        -fx-background-radius: 8px;
    """);


    return box;
  }


  private static Label addSimulationLabel(
      GridPane grid,
      String text,
      int column,
      int row,
      boolean heading)
  {
    Label label =
        new Label(
            text
        );


    if (heading)
    {
      label.setStyle("""
          -fx-font-size: 11px;
          -fx-font-weight: bold;
          -fx-text-fill: #aaaaaa;
      """);
    }
    else
    {
      label.setStyle("""
          -fx-font-size: 12px;
          -fx-text-fill: #dddddd;
      """);
    }


    grid.add(
        label,
        column,
        row
    );


    return label;
  }


  private static void setContent(
      VBox holder,
      Node content)
  {
    holder.getChildren().setAll(
        content
    );
  }


  private static void selectTab(
      Button selected,
      Button[] buttons)
  {
    for (Button button :
         buttons)
    {
      if (button == selected)
      {
        button.setStyle(
            ACTIVE_TAB_STYLE
        );
      }
      else
      {
        button.setStyle(
            INACTIVE_TAB_STYLE
        );
      }
    }
  }


  private static String money(
      double value)
  {
    return String.format(
        "$%.2f",
        value
    );
  }


  private static String signedMoney(
      double value)
  {
    return String.format(
        "%s$%.2f",
        value >= 0
            ? "+"
            : "-",
        Math.abs(value)
    );
  }
}
