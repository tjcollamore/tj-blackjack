import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AceAwarenessDialog
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
      Stage ownerStage)
  {
    VBox root =
        new VBox(
            14
        );

    root.setAlignment(
        Pos.TOP_CENTER
    );

    root.setPadding(
        new Insets(
            18,
            20,
            18,
            20
        )
    );

    root.setStyle("""
        -fx-background-color: #282828;
    """);


    Label title =
        new Label(
            "ACE AWARENESS"
        );

    title.setStyle("""
        -fx-font-size: 22px;
        -fx-font-weight: bold;
        -fx-text-fill: #d4af37;
    """);


    Label subtitle =
        new Label(
            "One simple count. Better shoe awareness. Smarter side-bet timing."
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

    Button ruleButton =
        new Button(
            "ACE RULE"
        );

    Button super4Button =
        new Button(
            "SUPER 4"
        );

    Button researchButton =
        new Button(
            "RESEARCH"
        );


    HBox navigation =
        new HBox(
            10,
            overviewButton,
            ruleButton,
            super4Button,
            researchButton
        );

    navigation.setAlignment(
        Pos.CENTER
    );


    VBox contentHolder =
        new VBox();

    contentHolder.setAlignment(
        Pos.TOP_CENTER
    );


    ScrollPane scrollPane =
        new ScrollPane(
            contentHolder
        );

    scrollPane.setFitToWidth(
        true
    );

    scrollPane.setPannable(
        false
    );

    scrollPane.setHbarPolicy(
        ScrollPane.ScrollBarPolicy.NEVER
    );

    scrollPane.setVbarPolicy(
        ScrollPane.ScrollBarPolicy.AS_NEEDED
    );

    scrollPane.setStyle("""
        -fx-background: #282828;
        -fx-background-color: #282828;
        -fx-border-color: transparent;
    """);

    scrollPane.setPrefViewportHeight(
        650
    );


    Button closeButton =
        new Button(
            "CLOSE"
        );

    closeButton.setStyle("""
        -fx-padding: 7px 18px;
    """);


    Button[] tabButtons =
    {
        overviewButton,
        ruleButton,
        super4Button,
        researchButton
    };


    overviewButton.setOnAction(e ->
    {
      setContent(
          contentHolder,
          createOverview()
      );

      selectTab(
          overviewButton,
          tabButtons
      );

      scrollPane.setVvalue(
          0.0
      );
    });


    ruleButton.setOnAction(e ->
    {
      setContent(
          contentHolder,
          createAceRule()
      );

      selectTab(
          ruleButton,
          tabButtons
      );

      scrollPane.setVvalue(
          0.0
      );
    });


    super4Button.setOnAction(e ->
    {
      setContent(
          contentHolder,
          createSuper4()
      );

      selectTab(
          super4Button,
          tabButtons
      );

      scrollPane.setVvalue(
          0.0
      );
    });


    researchButton.setOnAction(e ->
    {
      setContent(
          contentHolder,
          createResearch()
      );

      selectTab(
          researchButton,
          tabButtons
      );

      scrollPane.setVvalue(
          0.0
      );
    });


    setContent(
        contentHolder,
        createOverview()
    );

    selectTab(
        overviewButton,
        tabButtons
    );


    root.getChildren().addAll(
        titleArea,
        navigation,
        scrollPane,
        closeButton
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
        "Ace Awareness"
    );

    dialog.setScene(
        new Scene(
            root,
            760,
            860
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


  // ============================================================
  // OVERVIEW
  // ============================================================

  private static Node createOverview()
  {
    VBox intro =
        createTextSection(
            "WHAT IS ACE AWARENESS?",
            "Ace Awareness is an optional advantage layer built for social blackjack. "
            + "Instead of constantly changing the main wager, it tracks only Aces and "
            + "uses that information for two practical decisions: whether the remaining "
            + "shoe is worth staying in, and when a $5 Super 4 side bet becomes interesting."
        );


    VBox why =
        createTextSection(
            "WHY ACE-ONLY?",
            "Our Ace/Five research found a real betting signal, but exploiting it with a "
            + "$500 bankroll required noticeably more risk for very little practical gain. "
            + "Ace-only awareness keeps the useful information while avoiding a continuous "
            + "bet ramp."
        );


    Label headline =
        new Label(
            "+$2.33 per 160-hand night"
        );

    headline.setStyle("""
        -fx-font-size: 18px;
        -fx-font-weight: bold;
        -fx-text-fill: #339af0;
    """);


    Label headlineText =
        createWrappedLabel(
            "In 500,000 paired sessions, checking the shoe near midpoint and moving "
            + "when the Ace balance was negative improved average P/L by about $2.33 "
            + "versus flat Basic Strategy, while leaving bankroll risk essentially unchanged.",
            650,
            12,
            "#cccccc"
        );


    HBox metrics =
        new HBox(
            18,
            createMetric(
                "+$2.33",
                "EV / 160-hand night"
            ),
            createMetric(
                "-0.08 pp",
                "Unable-to-continue change"
            ),
            createMetric(
                "+0.22 pp",
                "$400+ night change"
            )
        );

    metrics.setAlignment(
        Pos.CENTER
    );


    VBox midpointCallout =
        createCallout(
            "MIDPOINT RULE",
            "At roughly 4 decks remaining in an 8-deck shoe:\n"
            + "• Ace balance ≥ 0 → stay\n"
            + "• Ace Balance mildly negative → essentially a wash\n"
            + "• Ace Balance -1 or lower → meaningfully Ace-poor; consider changing tables"
        );


    VBox super4Callout =
        createCallout(
            "SIDE QUEST",
            "When the remaining shoe becomes especially Ace-rich, the same count can also "
            + "identify Super 4 opportunities. Our current balanced social trigger is "
            + "Ace balance +4 or higher."
        );


    Label note =
        createNote(
            "This is a social-advantage tool, not a claim that blackjack becomes a winning "
            + "game. Table switching is modeled as immediate in the research, and Super 4 "
            + "results depend heavily on the current progressive jackpot."
        );


    VBox content =
        new VBox(
            14,
            intro,
            why,
            createResearchPanel(
                "RESEARCH RESULT",
                headline,
                headlineText,
                metrics
            ),
            midpointCallout,
            super4Callout,
            note
        );

    content.setAlignment(
        Pos.TOP_CENTER
    );

    content.setPadding(
        new Insets(
            10,
            16,
            18,
            16
        )
    );

    return content;
  }


  // ============================================================
  // ACE RULE
  // ============================================================

  private static Node createAceRule()
  {
      VBox rule =
          createTextSection(
              "THE COUNT",
              "Track only the Aces you see.\n\n"
              + "Estimate how many decks have been discarded, then use:\n"
              + "Ace Balance = 4 × decks seen − Aces seen\n\n"
              + "Each full deck should contain about 4 Aces, so the discard tray "
              + "gives you an easy visual estimate of how many Aces should have appeared."
          );


      VBox meaning =
          createTextSection(
              "INTERPRETATION",
              "Positive Ace Balance means fewer Aces have appeared than expected, "
              + "so more Aces are likely still in the shoe. Negative Ace Balance means "
              + "too many Aces have already appeared, leaving the remaining shoe relatively Ace-poor."
          );


      VBox simpleRule =
          createCallout(
              "THE SOCIAL VERSION",
              "You do not need to count every card. Estimate the height of the discard tray "
              + "in decks and compare the number of Aces you have seen with 4 Aces per deck. "
              + "Near the midpoint of an 8-deck shoe, about 4 decks should contain roughly 16 Aces."
          );


    GridPane examples =
        new GridPane();

    examples.setHgap(
        24
    );

    examples.setVgap(
        8
    );

    examples.setAlignment(
        Pos.CENTER
    );

    addTableCell(
        examples,
        "DECKS SEEN",
        0,
        0,
        true
    );

    addTableCell(
        examples,
        "EXPECTED ACES",
        1,
        0,
        true
    );

    addTableCell(
        examples,
        "IF ACTUAL ACES...",
        2,
        0,
        true
    );

    addTableCell(
        examples,
        "RESULT",
        3,
        0,
        true
    );


    addTableCell(
        examples,
        "~2 decks",
        0,
        1,
        false
    );

    addTableCell(
        examples,
        "8",
        1,
        1,
        false
    );

    addTableCell(
        examples,
        "6",
        2,
        1,
        false
    );

    addTableCell(
        examples,
        "+2 balance",
        3,
        1,
        false
    );


    addTableCell(
        examples,
        "~4 decks",
        0,
        2,
        false
    );

    addTableCell(
        examples,
        "16",
        1,
        2,
        false
    );

    addTableCell(
        examples,
        "18",
        2,
        2,
        false
    );

    addTableCell(
        examples,
        "-2 balance",
        3,
        2,
        false
    );


    VBox examplesPanel =
        new VBox(
            8,
            sectionHeading(
                "QUICK EXAMPLES"
            ),
            examples
        );

    examplesPanel.setPadding(
        new Insets(
            12
        )
    );

    examplesPanel.setStyle("""
        -fx-background-color: rgba(255, 255, 255, 0.04);
        -fx-background-radius: 8px;
    """);


    VBox practical =
        createTextSection(
            "PRACTICAL USE",
            "The count is informational, not mandatory. If the shoe is Ace-poor but you like "
            + "the dealer, your friends are at the table, or moving is inconvenient, staying "
            + "is completely reasonable. The advantage comes from knowing when leaving is "
            + "mathematically favorable if you were already indifferent."
        );


    VBox content =
        new VBox(
            14,
            rule,
            meaning,
            simpleRule,
            examplesPanel,
            practical
        );

    content.setAlignment(
        Pos.TOP_CENTER
    );

    content.setPadding(
        new Insets(
            10,
            16,
            18,
            16
        )
    );

    return content;
  }


  // ============================================================
  // SUPER 4
  // ============================================================

  private static Node createSuper4()
  {
    VBox intro =
        createTextSection(
            "SUPER 4 + ACE BALANCE",
            "Super 4 is unusually compatible with Ace Awareness because dealer blackjack "
            + "requires an Ace and a ten-value card. As the remaining shoe becomes Ace-rich, "
            + "the side bet improves materially."
        );


    VBox recommendation =
        createCallout(
            "CURRENT BALANCED RULE: +4",
            "Play the $5 Super 4 side bet when Ace balance is +4 or higher. "
            + "This is our current balance between long-run expected value, bankroll preservation, "
            + "and getting enough opportunities for the side quest to remain fun."
        );


    Label assumption =
        createWrappedLabel(
            "The table below assumes a $650,000 major royal progressive. "
            + "The actual jackpot fluctuates, so the true expected value changes with the meter. "
            + "Lower thresholds become more attractive as the jackpot grows; higher thresholds "
            + "become more attractive as the jackpot falls.",
            670,
            11,
            "#aaaaaa"
        );

    assumption.setStyle(
        assumption.getStyle()
        + "-fx-font-style: italic;"
    );


    GridPane grid =
        new GridPane();

    grid.setHgap(
        14
    );

    grid.setVgap(
        7
    );

    grid.setAlignment(
        Pos.CENTER
    );


    String[] headings =
    {
        "TRIGGER",
        "EXTRA EV / NIGHT",
        "RUIN Δ",
        "$400+ Δ",
        "BETS / NIGHT"
    };


    for (int column = 0;
         column < headings.length;
         column++)
    {
      addTableCell(
          grid,
          headings[column],
          column,
          0,
          true
      );
    }


    String[][] rows =
    {
        {"+2.5", "+$24.02", "+2.90 pp", "-3.35 pp", "18.88"},
        {"+3.0", "+$18.83", "+2.05 pp", "-2.36 pp", "13.58"},
        {"+3.5", "+$12.60", "+1.32 pp", "-1.51 pp", "8.95"},
        {"+4.0", "+$7.68", "+0.89 pp", "-1.02 pp", "6.09"},
        {"+4.5", "+$2.28", "+0.53 pp", "-0.60 pp", "3.76"},
        {"+5.0", "+$2.74", "+0.35 pp", "-0.36 pp", "2.42"}
    };


    for (int row = 0;
         row < rows.length;
         row++)
    {
      for (int column = 0;
           column < rows[row].length;
           column++)
      {
        Label label =
            addTableCell(
                grid,
                rows[row][column],
                column,
                row + 1,
                false
            );


        if (row == 3)
        {
          label.setStyle("""
              -fx-font-size: 12px;
              -fx-font-weight: bold;
              -fx-text-fill: #d4af37;
          """);
        }
      }
    }


    VBox tablePanel =
        new VBox(
            10,
            sectionHeading(
                "RISK / REWARD AT A $650K MAJOR JACKPOT"
            ),
            grid,
            createNote(
                "Extra EV / night is measured versus Ace-switch-only. "
                + "\"Ruin\" means becoming unable to continue the intended session because "
                + "the bankroll fell below the $25 table minimum."
            )
        );

    tablePanel.setPadding(
        new Insets(
            12
        )
    );

    tablePanel.setStyle("""
        -fx-background-color: rgba(255, 255, 255, 0.04);
        -fx-background-radius: 8px;
    """);


    VBox interpretation =
        createTextSection(
            "WHY NOT JUST BET AT +2.5?",
            "Because progressive EV is extremely top-heavy. +2.5 produced the highest simulated "
            + "mean gain, but it also averaged nearly 19 side bets per night and worsened typical "
            + "session outcomes. The jackpot is remote; on most nights, an aggressive trigger "
            + "mostly means repeatedly feeding $5 into the side bet."
        );


    VBox bands =
        createTextSection(
            "HOW TO THINK ABOUT THE THRESHOLDS",
            "+3.0 → aggressive EV-maximizing version\n"
            + "+3.5 → reasonable aggressive version\n"
            + "+4.0 → balanced social recommendation\n"
            + "+4.5 → conservative version\n"
            + "+5.0 → bankroll-preservation / rare-opportunity version"
        );


    Label fluctuation =
        createNote(
            "Important: these are not permanent universal thresholds. The progressive meter matters. "
            + "Our 50-million-hand density research estimated that the side bet itself reached "
            + "breakeven at much lower jackpot values as Ace balance increased. A future productized "
            + "version could read the current meter and adjust its recommendation."
        );


    VBox content =
        new VBox(
            14,
            intro,
            recommendation,
            assumption,
            tablePanel,
            interpretation,
            bands,
            fluctuation
        );

    content.setAlignment(
        Pos.TOP_CENTER
    );

    content.setPadding(
        new Insets(
            10,
            10,
            18,
            10
        )
    );

    return content;
  }


  // ============================================================
  // RESEARCH
  // ============================================================

  private static Node createResearch()
  {
    VBox aceFive =
        createTextSection(
            "ACE/FIVE: VALID, BUT NOT OUR SOCIAL WINNER",
            "Ace/Five clearly predicts blackjack EV, but realistic wager spreads were a poor fit "
            + "for a $500 bankroll. Even the conservative 1–2 spread improved expected value by "
            + "less than $1 per 160-hand night while substantially increasing the chance of being "
            + "unable to continue the session."
        );


    VBox switching =
        createTextSection(
            "ACE-ONLY TABLE AWARENESS",
            "The midpoint switching rule performed much better on the effort-versus-risk tradeoff: "
            + "about +$2.33 per 160-hand night versus flat Basic Strategy, with essentially unchanged "
            + "session bankroll risk. That makes Ace Awareness useful even before considering Super 4."
        );


    VBox density =
        createTextSection(
            "50-MILLION-HAND DENSITY STUDY",
            "A separate state-density simulation confirmed a clean relationship between Ace balance "
            + "and Super 4 value. Fixed-payout-only Super 4 improved steadily from strongly negative "
            + "at low balance to approximately breakeven around +5.5 and positive around +6."
        );


    GridPane breakEven =
        new GridPane();

    breakEven.setHgap(
        20
    );

    breakEven.setVgap(
        7
    );

    breakEven.setAlignment(
        Pos.CENTER
    );


    addTableCell(
        breakEven,
        "ACE BALANCE",
        0,
        0,
        true
    );

    addTableCell(
        breakEven,
        "FIXED EDGE",
        1,
        0,
        true
    );

    addTableCell(
        breakEven,
        "EST. BREAK-EVEN MAJOR",
        2,
        0,
        true
    );


    String[][] values =
    {
        {"+2.0", "-18.39%", "~$315K"},
        {"+3.0", "-13.44%", "~$211K"},
        {"+4.0", "-8.87%", "~$125K"},
        {"+4.5", "-6.16%", "~$77K"},
        {"+5.0", "-3.26%", "~$46K"},
        {"+5.5", "+0.96%", "Already +EV"}
    };


    for (int row = 0;
         row < values.length;
         row++)
    {
      for (int column = 0;
           column < values[row].length;
           column++)
      {
        addTableCell(
            breakEven,
            values[row][column],
            column,
            row + 1,
            false
        );
      }
    }


    VBox breakEvenPanel =
        new VBox(
            9,
            sectionHeading(
                "SUPER 4 VALUE BY ACE BALANCE"
            ),
            breakEven,
            createNote(
                "Break-even figures assume one royal suit receives 100% of the major progressive "
                + "and the other three suits receive 10%. They are estimates from the simulation "
                + "model, not a guarantee of the live casino paytable."
            )
        );

    breakEvenPanel.setPadding(
        new Insets(
            12
        )
    );

    breakEvenPanel.setStyle("""
        -fx-background-color: rgba(255, 255, 255, 0.04);
        -fx-background-radius: 8px;
    """);


    VBox finalConclusion =
        createCallout(
            "CURRENT CONCLUSION",
            "Ace-only awareness is the preferred social advantage layer. It provides a simple "
            + "table-selection signal with little added bankroll risk and can also support selective "
            + "Super 4 betting. For the tested $650K progressive scenario, +4 is our current "
            + "balanced side-bet trigger."
        );


    VBox assumptions =
        createTextSection(
            "RESEARCH BASELINE",
            "8 decks • H17 • DAS • late surrender • 3:2 blackjack\n"
            + "$500 starting bankroll • $25 main wager • $5 Super 4 wager\n"
            + "160 intended hands • Basic Strategy • paired session simulations"
        );


    Label caveat =
        createNote(
            "Known caveats: table switching is modeled as immediate; the progressive jackpot fluctuates; "
            + "the live paytable should be verified against table signage; and progressive results "
            + "are dominated by rare royal outcomes, so typical-session risk matters alongside mean EV."
        );


    VBox content =
        new VBox(
            14,
            aceFive,
            switching,
            density,
            breakEvenPanel,
            finalConclusion,
            assumptions,
            caveat
        );

    content.setAlignment(
        Pos.TOP_CENTER
    );

    content.setPadding(
        new Insets(
            10,
            16,
            18,
            16
        )
    );

    return content;
  }


  // ============================================================
  // HELPERS
  // ============================================================

  private static VBox createResearchPanel(
      String headingText,
      Label headline,
      Label body,
      HBox metrics)
  {
    VBox panel =
        new VBox(
            9,
            sectionHeading(
                headingText
            ),
            headline,
            body,
            metrics
        );

    panel.setAlignment(
        Pos.TOP_CENTER
    );

    panel.setPadding(
        new Insets(
            12
        )
    );

    panel.setMaxWidth(
        690
    );

    panel.setStyle("""
        -fx-background-color: rgba(255, 255, 255, 0.04);
        -fx-background-radius: 8px;
    """);

    return panel;
  }


  private static VBox createTextSection(
      String headingText,
      String bodyText)
  {
    Label heading =
        sectionHeading(
            headingText
        );


    Label body =
        createWrappedLabel(
            bodyText,
            670,
            12,
            "#cccccc"
        );


    VBox section =
        new VBox(
            4,
            heading,
            body
        );

    section.setMaxWidth(
        680
    );

    section.setMinHeight(
        Region.USE_PREF_SIZE
    );

    return section;
  }


  private static VBox createCallout(
      String headingText,
      String bodyText)
  {
    VBox box =
        createTextSection(
            headingText,
            bodyText
        );

    box.setPadding(
        new Insets(
            10,
            12,
            10,
            12
        )
    );

    box.setStyle("""
        -fx-background-color: rgba(212, 175, 55, 0.055);
        -fx-background-radius: 7px;
    """);

    return box;
  }


  private static Label sectionHeading(
      String text)
  {
    Label label =
        new Label(
            text
        );

    label.setStyle("""
        -fx-font-size: 12px;
        -fx-font-weight: bold;
        -fx-text-fill: #d4af37;
    """);

    return label;
  }


  private static Label createWrappedLabel(
      String text,
      double width,
      int fontSize,
      String color)
  {
    Label label =
        new Label(
            text
        );

    label.setWrapText(
        true
    );

    label.setMaxWidth(
        width
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


  private static Label createNote(
      String text)
  {
    Label label =
        createWrappedLabel(
            text,
            670,
            10,
            "#999999"
        );

    label.setStyle(
        label.getStyle()
        + "-fx-font-style: italic;"
    );

    return label;
  }


  private static VBox createMetric(
      String valueText,
      String labelText)
  {
    Label value =
        new Label(
            valueText
        );

    value.setStyle("""
        -fx-font-size: 16px;
        -fx-font-weight: bold;
        -fx-text-fill: #339af0;
    """);


    Label label =
        createWrappedLabel(
            labelText,
            170,
            10,
            "#aaaaaa"
        );

    label.setAlignment(
        Pos.CENTER
    );


    VBox box =
        new VBox(
            2,
            value,
            label
        );

    box.setAlignment(
        Pos.CENTER
    );

    box.setPrefWidth(
        190
    );

    return box;
  }


  private static Label addTableCell(
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

    label.setAlignment(
        Pos.CENTER
    );

    label.setMinWidth(
        Region.USE_PREF_SIZE
    );

    if (heading)
    {
      label.setStyle("""
          -fx-font-size: 10px;
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
}

