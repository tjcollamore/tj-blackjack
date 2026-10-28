import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;


public class BasicStrategyDialog
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
      GameSettings settings)
  {
    BorderPane root =
        new BorderPane();

    root.setStyle("""
        -fx-background-color: #282828;
    """);


    Label title =
        new Label(
            "BASIC STRATEGY"
        );

    title.setStyle("""
        -fx-font-size: 22px;
        -fx-font-weight: bold;
        -fx-text-fill: #d4af37;
    """);


    Label subtitle =
        new Label(
            "Learn the mathematically optimal play."
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

    Button matrixButton =
        new Button(
            "MATRIX"
        );


    HBox navigation =
        new HBox(
            10,
            overviewButton,
            matrixButton
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
        matrixButton
    };


    overviewButton.setOnAction(e ->
    {
      setContent(
          contentHolder,
          createOverview(
              settings
          )
      );

      selectTab(
          overviewButton,
          tabButtons
      );
    });


    matrixButton.setOnAction(e ->
    {
      setContent(
          contentHolder,
          StrategyMatrixView.createBasic(
              settings
          )
      );

      selectTab(
          matrixButton,
          tabButtons
      );
    });


    setContent(
        contentHolder,
        createOverview(
            settings
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
        "Basic Strategy"
    );

    dialog.setScene(
        new Scene(
            root,
            700,
            690
        )
    );

    dialog.setResizable(
        false
    );


    closeButton.setOnAction(
        e ->
            dialog.close()
    );


    dialog.showAndWait();
  }


  private static Node createOverview(
      GameSettings settings)
  {
    Label intro =
        new Label(
            "Basic Strategy is the mathematically optimal way to play "
            + "each decision based on your hand, the dealer's up-card, "
            + "and the rules of the table."
        );

    intro.setWrapText(
        true
    );

    intro.setMaxWidth(
        590
    );

    intro.setMinHeight(
        Region.USE_PREF_SIZE
    );

    intro.setAlignment(
        Pos.CENTER
    );

    intro.setStyle("""
        -fx-font-size: 14px;
        -fx-text-fill: #dddddd;
    """);


    Label rulesTitle =
        new Label(
            "CURRENT TABLE RULES"
        );

    rulesTitle.setStyle("""
        -fx-font-size: 12px;
        -fx-font-weight: bold;
        -fx-text-fill: #d4af37;
    """);


    Label rules =
        new Label(
            getRulesText(
                settings
            )
        );

    rules.setStyle("""
        -fx-font-size: 15px;
        -fx-font-weight: bold;
        -fx-text-fill: #339af0;
    """);


    Label rulesNote =
        new Label(
            "The Basic Strategy matrix automatically reflects these settings."
        );

    rulesNote.setStyle("""
        -fx-font-size: 11px;
        -fx-text-fill: #999999;
    """);


    VBox rulesArea =
        new VBox(
            6,
            rulesTitle,
            rules,
            rulesNote
        );

    rulesArea.setAlignment(
        Pos.CENTER
    );

    rulesArea.setPadding(
        new Insets(
            18
        )
    );

    rulesArea.setMaxWidth(
        550
    );

    rulesArea.setStyle("""
        -fx-background-color: rgba(255, 255, 255, 0.04);
        -fx-background-radius: 8px;
    """);


    Label purposeTitle =
        new Label(
            "WHAT DOES IT DO?"
        );

    purposeTitle.setStyle("""
        -fx-font-size: 12px;
        -fx-font-weight: bold;
        -fx-text-fill: #d4af37;
    """);


    Label purpose =
        new Label(
            "For every playable hand, Basic Strategy chooses the action "
            + "with the highest long-run expected value. Following it "
            + "reduces the casino's mathematical advantage as much as "
            + "the table rules allow."
        );

    purpose.setWrapText(
        true
    );

    purpose.setMaxWidth(
        590
    );

    purpose.setMinHeight(
        Region.USE_PREF_SIZE
    );

    purpose.setStyle("""
        -fx-font-size: 12px;
        -fx-text-fill: #cccccc;
    """);


    VBox purposeArea =
        new VBox(
            5,
            purposeTitle,
            purpose
        );

    purposeArea.setMaxWidth(
        590
    );


    Label note =
        new Label(
            "Basic Strategy improves your long-run expected result, "
            + "but it does not guarantee winning any individual hand "
            + "or casino session."
        );

    note.setWrapText(
        true
    );

    note.setMaxWidth(
        590
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
        -fx-text-fill: #999999;
    """);


    VBox content =
        new VBox(
            24,
            intro,
            rulesArea,
            purposeArea,
            note
        );

    content.setAlignment(
        Pos.TOP_CENTER
    );

    content.setPadding(
        new Insets(
            40,
            20,
            20,
            20
        )
    );


    return content;
  }


  private static String getRulesText(
      GameSettings settings)
  {
    String dealerRule =
        settings.dealerHitsSoft17()
            ? "H17"
            : "S17";


    String dasRule =
        settings.doubleAfterSplit()
            ? "DAS"
            : "No DAS";


    String surrenderRule =
        settings.allowSurrender()
            ? "Surrender"
            : "No Surrender";


    return settings.getDecksInShoe()
        + " decks"
        + "   •   "
        + dealerRule
        + "   •   "
        + dasRule
        + "   •   "
        + surrenderRule;
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
      button.setStyle(
          button == selected
              ? ACTIVE_TAB_STYLE
              : INACTIVE_TAB_STYLE
      );
    }
  }
}
