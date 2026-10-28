import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.text.TextAlignment;
import java.util.Random;

public class PracticeTestDialog
{
    private static final long[] AP_SWITCH_SEEDS =
    {
      22L, 49L, 54L, 59L, 65L,
      84L, 90L, 93L, 107L, 126L,
      192L, 200L, 210L, 212L, 268L,
      271L, 337L, 340L, 372L, 378L,
      386L, 400L, 403L, 416L, 447L,
      459L, 462L, 470L, 502L, 503L,
      580L, 599L, 660L, 663L, 673L,
      697L, 730L, 746L, 751L, 756L,
      788L, 794L, 805L, 811L, 818L,
      823L, 832L, 835L, 852L, 869L
    };


    private static final long[] AP_RICH_SEEDS =
    {
      1206L, 1651L, 2768L, 3733L, 3905L,
      4138L, 5008L, 6000L, 6750L, 10399L,
      10960L, 11173L, 11207L, 11726L, 12382L,
      12834L, 13061L, 13887L, 14213L, 14266L,
      14740L, 15365L, 15438L, 16253L, 17014L,
      17393L, 17530L, 17741L, 18194L, 19830L,
      19928L, 24238L, 25794L, 25922L, 26260L,
      26743L, 26810L, 27628L, 27931L, 28182L,
      28419L, 29354L, 30638L, 30740L, 31626L,
      32085L, 33404L, 33450L, 33526L, 33892L
    };


    private final Random aceAwarenessRandom =
        new Random();

    private long firstAceAwarenessSeed;
  private final Stage dialog;

  private final BorderPane root;

  private final PracticeTestSessionBuilder
      sessionBuilder =
          new PracticeTestSessionBuilder();

  private final PracticeTestEvaluator
      evaluator =
          new PracticeTestEvaluator();

  private final PracticeTestScorer
      scorer =
          new PracticeTestScorer();


  private PracticeTestSession session;

  private final List<HandAction> userAnswers =
      new ArrayList<>();

  private int questionIndex = 0;
    
  private AceAwarenessPracticeSession aceAwarenessSession;
  private DiscardTrayView acePracticeDiscardTray;
    
  private PracticeTestDialog(
      Stage ownerStage)
  {
    dialog =
        new Stage();

    dialog.initOwner(
        ownerStage
    );

    dialog.initModality(
        Modality.APPLICATION_MODAL
    );

    dialog.setTitle(
        "Practice Test"
    );


    root =
        new BorderPane();

    root.setStyle("""
        -fx-background-color: #282828;
    """);


    dialog.setScene(
        new Scene(
            root,
            700,
            720
        )
    );

    dialog.setResizable(
        false
    );
  }


  public static void show(
      Stage ownerStage)
  {
    PracticeTestDialog practiceTest =
        new PracticeTestDialog(
            ownerStage
        );


    practiceTest.showStartScreen();

    practiceTest.dialog.showAndWait();
  }


  // ============================================================
  // START SCREEN
  // ============================================================

  private void showStartScreen()
  {
    Label title =
        new Label(
            "PRACTICE TEST"
        );

    title.setStyle("""
        -fx-font-size: 23px;
        -fx-font-weight: bold;
        -fx-text-fill: #d4af37;
    """);


    Label subtitle =
        new Label(
            "Test your blackjack decision-making."
        );

    subtitle.setStyle("""
        -fx-font-size: 13px;
        -fx-text-fill: #bbbbbb;
    """);


    Button cpButton =
        createLevelButton(
            "CP — CASINO READY",
            "50 hands • Social or Basic Strategy accepted"
        );


    Button honorsButton =
        createLevelButton(
            "HONORS — PERFECT STRATEGY",
            "50 hands • Basic Strategy required"
        );


    cpButton.setOnAction(
        e ->
            startTest(
                PracticeTestDifficulty.CP
            )
    );


    honorsButton.setOnAction(
        e ->
            startTest(
                PracticeTestDifficulty.HONORS
            )
    );


     Button apButton =
          createLevelButton(
              "AP — ACE AWARENESS",
              "2-deck shoe • Track Ace counts"
          );

      apButton.setOnAction(
          e ->
              startAceAwarenessTest()
      );

    Button closeButton =
        new Button(
            "CLOSE"
        );

    styleSecondaryButton(
        closeButton
    );

    closeButton.setOnAction(
        e ->
            dialog.close()
    );


    VBox content =
        new VBox(
            18,
            title,
            subtitle,
            spacer(8),
            cpButton,
            honorsButton,
            apButton,
            spacer(10),
            closeButton
        );

    content.setAlignment(
        Pos.CENTER
    );

    content.setPadding(
        new Insets(
            35
        )
    );


    root.setTop(
        null
    );

    root.setBottom(
        null
    );

    root.setCenter(
        content
    );
  }


  // ============================================================
  // TEST
  // ============================================================
    private void startAceAwarenessTest()
    {
      showAceAwarenessIntro();
    }

    private void showAceAwarenessIntro()
    {
      Label title =
          new Label(
              "AP — ACE AWARENESS"
          );

      title.setStyle("""
          -fx-font-size: 23px;
          -fx-font-weight: bold;
          -fx-text-fill: #d4af37;
      """);

        Label description =
            new Label(
                "Track Ace Balance through a continuous shoe.\n\n"
                + "ACE BALANCE\n"
                + "4 × full decks seen − Aces seen\n\n"
                + "TABLE SELECTION\n"
                + "Before midpoint: stay.\n"
                + "At midpoint: if Ace Balance is negative, "
                + "consider switching tables.\n\n"
                + "SUPER 4\n"
                + "Play the $5 Super 4 side bet when "
                + "Ace Balance is +4 or higher."
            );

      description.setWrapText(
          true
      );

      description.setMaxWidth(
          430
      );

      description.setAlignment(
          Pos.CENTER
      );

      description.setStyle("""
          -fx-font-size: 13px;
          -fx-text-fill: #bbbbbb;
          -fx-text-alignment: center;
      """);

        Label checkpointInfo =
            new Label(
                "At checkpoints you may be asked:\n"
                + "• What is the current Ace Balance?\n"
                + "• Stay or switch tables?\n"
                + "• Bet $5 Super 4?"
            );

        checkpointInfo.setWrapText(
            true
        );

        checkpointInfo.setMaxWidth(
            430
        );

        checkpointInfo.setAlignment(
            Pos.CENTER
        );

        checkpointInfo.setStyle("""
            -fx-font-size: 12px;
            -fx-text-fill: #bbbbbb;
            -fx-text-alignment: center;
            -fx-background-color: rgba(255, 255, 255, 0.035);
            -fx-background-radius: 8px;
            -fx-padding: 10px 14px;
        """);
        
        
      Button beginButton =
          new Button(
              "BEGIN"
          );

      stylePrimaryButton(
          beginButton
      );

        beginButton.setOnAction(
            e ->
                startAceAwarenessSession()
        );
    
      Button backButton =
          new Button(
              "BACK"
          );

      styleSecondaryButton(
          backButton
      );

      backButton.setOnAction(
          e ->
              showStartScreen()
      );

        HBox buttons =
            new HBox(
                12,
                backButton,
                beginButton
        );

        buttons.setAlignment(
            Pos.CENTER
        );
        
      VBox content =
          new VBox(
              18,
              title,
              description,
              spacer(4),
              checkpointInfo,
              spacer(10),
              buttons
          );

      content.setAlignment(
          Pos.CENTER
      );

      content.setPadding(
          new Insets(
              35
          )
      );

      root.setTop(
          null
      );

      root.setBottom(
          null
      );

      root.setCenter(
          content
      );
    }

    private void styleAceBalanceButton(
        Button button)
    {
      button.setMinWidth(
          60
      );

      button.setPrefWidth(
          60
      );

      button.setPrefHeight(
          38
      );

      button.setStyle("""
          -fx-background-color: #3b4656;
          -fx-text-fill: white;
          -fx-font-size: 13px;
          -fx-font-weight: bold;
          -fx-background-radius: 5px;
      """);
    }

    private long randomAceAwarenessSeed(
        long[] seeds)
    {
      int index =
          aceAwarenessRandom.nextInt(
              seeds.length
          );

      return seeds[index];
    }
    
    private void startAceAwarenessSession()
    {
        firstAceAwarenessSeed =
            randomAceAwarenessSeed(
                AP_SWITCH_SEEDS
            );

        aceAwarenessSession =
            new AceAwarenessPracticeSession(
                firstAceAwarenessSeed
            );
        
        GameSettings traySettings =
            new GameSettings();

        traySettings.setDecksInShoe(
            aceAwarenessSession.getDeckCount()
        );

        acePracticeDiscardTray =
            new DiscardTrayView(
                traySettings
            );

        acePracticeDiscardTray.setManaged(
            true
        );

        showNextAceAwarenessHand();
    }

    private void showNextAceAwarenessHand()
    {
      /*
       * The tray should contain cards from
       * PREVIOUS completed hands only.
       */
      acePracticeDiscardTray
          .setDiscardedCardCount(
              aceAwarenessSession
                  .getDiscardedCardCount()
          );


      AceAwarenessPracticeHand hand =
          aceAwarenessSession.playNextHand();


      Label title =
          new Label(
              "AP — ACE AWARENESS"
          );

      title.setStyle("""
          -fx-font-size: 17px;
          -fx-font-weight: bold;
          -fx-text-fill: #d4af37;
      """);


      Label handNumber =
          new Label(
              "HAND "
              + aceAwarenessSession
                  .getHandsPlayed()
          );

      handNumber.setStyle("""
          -fx-font-size: 12px;
          -fx-font-weight: bold;
          -fx-text-fill: #aaaaaa;
      """);


      VBox titleBox =
          new VBox(
              3,
              title,
              handNumber
          );

      titleBox.setAlignment(
          Pos.CENTER_LEFT
      );


      Label shoeLabel =
          new Label(
              "2-DECK SHOE"
          );

      shoeLabel.setStyle("""
          -fx-font-size: 12px;
          -fx-font-weight: bold;
          -fx-text-fill: #aaaaaa;
      """);


      HBox top =
          new HBox(
              titleBox,
              spacerHorizontal(),
              shoeLabel
          );

      top.setAlignment(
          Pos.CENTER
      );

      top.setPadding(
          new Insets(
              22,
              28,
              8,
              28
          )
      );


      VBox dealerArea =
          createAcePracticeCardArea(
              "DEALER",
              hand.getDealerCards()
          );


      VBox playerArea =
          createAcePracticeCardArea(
              "PLAYER",
              hand.getPlayerCards()
          );


      VBox hands =
          new VBox(
              24,
              dealerArea,
              playerArea
          );

      hands.setAlignment(
          Pos.CENTER
      );


      BorderPane tableArea =
          new BorderPane();

      tableArea.setLeft(
          acePracticeDiscardTray
      );

      tableArea.setCenter(
          hands
      );

      tableArea.setMaxWidth(
          520
      );


      Button nextButton =
          new Button(
              "NEXT HAND"
          );

      stylePrimaryButton(
          nextButton
      );

        nextButton.setOnAction(
            e ->
            {
              if (aceAwarenessSession
                      .isCheckpointReached())
              {
                showAceAwarenessCheckpoint();
              }
              else
              {
                showNextAceAwarenessHand();
              }
            }
        );

      VBox center =
          new VBox(
              24,
              tableArea,
              nextButton
          );

      center.setAlignment(
          Pos.CENTER
      );

      center.setPadding(
          new Insets(
              20,
              50,
              35,
              50
          )
      );


      root.setTop(
          top
      );

      root.setCenter(
          center
      );

      root.setBottom(
          null
      );
    }

    private void showAceAwarenessCheckpoint()
    {
      AceAwarenessCheckpoint checkpoint =
          aceAwarenessSession
              .getCurrentCheckpoint();


      /*
       * The hand that triggered this checkpoint
       * has now been discarded.
       */
      acePracticeDiscardTray
          .setDiscardedCardCount(
              aceAwarenessSession
                  .getDiscardedCardCount()
          );


      Label title =
          new Label(
              "ACE AWARENESS CHECKPOINT"
          );

      title.setStyle("""
          -fx-font-size: 19px;
          -fx-font-weight: bold;
          -fx-text-fill: #d4af37;
      """);


      Label progress =
          new Label(
              "Checkpoint "
              + aceAwarenessSession
                  .getCheckpointNumber()
              + " of "
              + aceAwarenessSession
                  .getCheckpointCount()
          );

      progress.setStyle("""
          -fx-font-size: 12px;
          -fx-font-weight: bold;
          -fx-text-fill: #aaaaaa;
      """);

        Label midpointLabel =
            new Label();

        if (aceAwarenessSession
                .getDecksSeen()
            >= 1.0)
        {
          midpointLabel.setText(
              "MIDPOINT REACHED"
          );

          midpointLabel.setStyle("""
              -fx-font-size: 11px;
              -fx-font-weight: bold;
              -fx-text-fill: #d4af37;
          """);
        }
        
        VBox heading =
            new VBox(
                4,
                title,
                progress,
                midpointLabel
            );
        
      heading.setAlignment(
          Pos.CENTER
      );

        int correctBalance =
            aceAwarenessSession
                .getAceCount()
                .getMentalBalance(
                    aceAwarenessSession
                        .getDecksSeen()
                );
        

      Label question =
          new Label(
              "What is the current Ace Balance?"
          );

      question.setStyle("""
          -fx-font-size: 17px;
          -fx-font-weight: bold;
          -fx-text-fill: white;
      """);


        Label feedback =
            new Label();
        feedback.setMinHeight(
            20
        );

        feedback.setPrefHeight(
            20
        );
        
        Button continueButton =
            new Button(
                "CONTINUE"
            );

        stylePrimaryButton(
            continueButton
        );

        continueButton.setVisible(
            false
        );


        continueButton.setOnAction(
            e ->
            {
              if (checkpoint
                      .shouldAskTableDecision())
              {
                showAceAwarenessTableQuestion();
              }
              else if (checkpoint
                           .shouldAskSuper4())
              {
                showAceAwarenessSuper4Question();
              }
              else
              {
                finishAceAwarenessCheckpoint();
              }
            }
        );
        
        HBox balanceButtons =
            createAceBalanceButtons(
                correctBalance,
                feedback,
                continueButton
            );

        VBox questionArea =
            new VBox(
                12,
                question,
                balanceButtons,
                feedback,
                continueButton
            );
        
        questionArea.setAlignment(
            Pos.CENTER
        );
        


      BorderPane checkpointArea =
          new BorderPane();

      checkpointArea.setLeft(
          acePracticeDiscardTray
      );

      checkpointArea.setCenter(
          questionArea
      );

      checkpointArea.setMaxWidth(
          520
      );


      VBox center =
          new VBox(
              28,
              heading,
              checkpointArea
          );

      center.setAlignment(
          Pos.CENTER
      );

      center.setPadding(
          new Insets(
              30,
              50,
              35,
              50
          )
      );


      root.setTop(
          null
      );

      root.setCenter(
          center
      );

      root.setBottom(
          null
      );
    }

    private HBox createAceBalanceButtons(
        int correctBalance,
        Label feedback,
        Button continueButton)
    {
      HBox buttons =
          new HBox(
              8
          );

      buttons.setAlignment(
          Pos.CENTER
      );


      Random random =
          new Random();

      int correctIndex =
          random.nextInt(
              5
          );


      int firstValue =
          correctBalance
          - correctIndex;


      for (int i = 0;
           i < 5;
           i++)
      {
        int answer =
            firstValue + i;


        Button button =
            new Button(
                formatAceBalance(answer)
            );

          styleAceBalanceButton(
              button
          );

        button.setOnAction(
            e ->
            {
              boolean correct =
                    answer
                    == correctBalance;
    
                aceAwarenessSession.recordAnswer(
                    AceAwarenessQuestionType.ACE_BALANCE,
                    correct
                );
   
    
              if (correct)
              {
                  feedback.setText(
                      "✓ Correct"
                  );
                    
                  feedback.setStyle("""
                        -fx-font-size: 13px;
                        -fx-font-weight: bold;
                        -fx-text-fill: #7bd88f;
                    """);
              }
              else
              {
                feedback.setText(
                    "Correct Ace Balance: "
                    + formatAceBalance(correctBalance)
                );

                feedback.setStyle("""
                    -fx-font-size: 13px;
                    -fx-font-weight: bold;
                    -fx-text-fill: #ff7b7b;
                """);
              }


              for (Node node :
                   buttons.getChildren())
              {
                node.setDisable(
                    true
                );
              }
                continueButton.setVisible(
                    true
                );
            }
        );


        buttons.getChildren().add(
            button
        );
      }


      return buttons;
    }

    private void showAceAwarenessTableQuestion()
    {
      AceAwarenessCheckpoint checkpoint =
          aceAwarenessSession
              .getCurrentCheckpoint();


      Label title =
          new Label(
              "ACE AWARENESS CHECKPOINT"
          );

      title.setStyle("""
          -fx-font-size: 19px;
          -fx-font-weight: bold;
          -fx-text-fill: #d4af37;
      """);


      Label question =
          new Label(
              "Stay at this table or switch?"
          );

      question.setStyle("""
          -fx-font-size: 17px;
          -fx-font-weight: bold;
          -fx-text-fill: white;
      """);

        question.setWrapText(
            true
        );

        question.setMaxWidth(
            330
        );

        question.setTextAlignment(
            TextAlignment.CENTER
        );

        question.setAlignment(
            Pos.CENTER
        );
        
      Label feedback =
          new Label();
        feedback.setMinHeight(
            20
        );

        feedback.setPrefHeight(
            20
        );

      Button stayButton =
          new Button(
              "STAY"
          );

      Button switchButton =
          new Button(
              "SWITCH"
          );

      styleActionButton(
          stayButton
      );

      styleActionButton(
          switchButton
      );


      HBox answerButtons =
          new HBox(
              10,
              stayButton,
              switchButton
          );

      answerButtons.setAlignment(
          Pos.CENTER
      );


      Button continueButton =
          new Button(
              "CONTINUE"
          );

      stylePrimaryButton(
          continueButton
      );

      continueButton.setVisible(
          false
      );

      continueButton.setManaged(
          true
      );


      /*
       * Before midpoint: stay.
       *
       * At/after midpoint:
       * negative Ace Balance -> switch.
       */
      int balance =
          aceAwarenessSession
              .getAceCount()
              .getMentalBalance(
                  aceAwarenessSession
                      .getDecksSeen()
              );

      boolean atMidpoint =
          aceAwarenessSession
              .getDecksSeen()
          >= 1.0;

      boolean correctStay =
          !atMidpoint
          || balance >= 0;


        stayButton.setOnAction(
            e ->
            {
              boolean correct =
                  correctStay;

                aceAwarenessSession.recordAnswer(
                    AceAwarenessQuestionType.TABLE_SELECTION,
                    correct
                );
              showBinaryFeedback(
                  correct,
                  correctStay
                      ? "STAY"
                      : "SWITCH",
                  feedback,
                  answerButtons,
                  continueButton
              );
            }
        );


        switchButton.setOnAction(
            e ->
            {
              boolean correct =
                  !correctStay;

                aceAwarenessSession.recordAnswer(
                    AceAwarenessQuestionType.TABLE_SELECTION,
                    correct
                );
              showBinaryFeedback(
                  correct,
                  correctStay
                      ? "STAY"
                      : "SWITCH",
                  feedback,
                  answerButtons,
                  continueButton
              );
            }
        );
        

      continueButton.setOnAction(
          e ->
          {
            if (checkpoint
                    .shouldAskSuper4())
            {
              showAceAwarenessSuper4Question();
            }
            else
            {
              finishAceAwarenessCheckpoint();
            }
          }
      );


      VBox questionArea =
          new VBox(
              12,
              question,
              answerButtons,
              feedback,
              continueButton
          );

      questionArea.setAlignment(
          Pos.CENTER
      );


      showAceCheckpointQuestionArea(
          title,
          questionArea
      );
    }

    private void showAceAwarenessSuper4Question()
    {
      Label title =
          new Label(
              "ACE AWARENESS CHECKPOINT"
          );

      title.setStyle("""
          -fx-font-size: 19px;
          -fx-font-weight: bold;
          -fx-text-fill: #d4af37;
      """);


      Label question =
          new Label(
              "Bet $5 on Super 4?"
          );

      question.setStyle("""
          -fx-font-size: 17px;
          -fx-font-weight: bold;
          -fx-text-fill: white;
      """);


      Label feedback =
          new Label();
        feedback.setMinHeight(
            20
        );

        feedback.setPrefHeight(
            20
        );

      Button noButton =
          new Button(
              "NO"
          );

      Button yesButton =
          new Button(
              "YES"
          );

      styleActionButton(
          noButton
      );

      styleActionButton(
          yesButton
      );


      HBox answerButtons =
          new HBox(
              10,
              noButton,
              yesButton
          );

      answerButtons.setAlignment(
          Pos.CENTER
      );


      Button continueButton =
          new Button(
              "CONTINUE"
          );

      stylePrimaryButton(
          continueButton
      );

      continueButton.setVisible(
          false
      );

      continueButton.setManaged(
          true
      );


      int balance =
          aceAwarenessSession
              .getAceCount()
              .getMentalBalance(
                  aceAwarenessSession
                      .getDecksSeen()
              );

      boolean correctYes =
          balance >= 4;


        noButton.setOnAction(
            e ->
            {
              boolean correct =
                  !correctYes;

                aceAwarenessSession.recordAnswer(
                    AceAwarenessQuestionType.SUPER_4,
                    correct
                );
                
              showBinaryFeedback(
                  correct,
                  correctYes
                      ? "YES"
                      : "NO",
                  feedback,
                  answerButtons,
                  continueButton
              );
            }
        );


        yesButton.setOnAction(
            e ->
            {
              boolean correct =
                  correctYes;

                aceAwarenessSession.recordAnswer(
                    AceAwarenessQuestionType.SUPER_4,
                    correct
                );
                
              showBinaryFeedback(
                  correct,
                  correctYes
                      ? "YES"
                      : "NO",
                  feedback,
                  answerButtons,
                  continueButton
              );
            }
        );

      continueButton.setOnAction(
          e ->
              finishAceAwarenessCheckpoint()
      );


      VBox questionArea =
          new VBox(
              12,
              question,
              answerButtons,
              feedback,
              continueButton
          );

      questionArea.setAlignment(
          Pos.CENTER
      );


      showAceCheckpointQuestionArea(
          title,
          questionArea
      );
    }

    private void showBinaryFeedback(
        boolean correct,
        String correctAnswer,
        Label feedback,
        HBox buttons,
        Button continueButton)
    {
      if (correct)
      {
        feedback.setText(
            "✓ Correct"
        );

        feedback.setStyle("""
            -fx-font-size: 13px;
            -fx-font-weight: bold;
            -fx-text-fill: #7bd88f;
        """);
      }
      else
      {
        feedback.setText(
            "Correct answer: "
            + correctAnswer
        );

        feedback.setStyle("""
            -fx-font-size: 13px;
            -fx-font-weight: bold;
            -fx-text-fill: #ff7b7b;
        """);
      }


      for (Node node :
           buttons.getChildren())
      {
        node.setDisable(
            true
        );
      }


      continueButton.setVisible(
          true
      );
    }

    private void finishAceAwarenessCheckpoint()
    {
      int completedCheckpoint =
          aceAwarenessSession
              .getCheckpointNumber();


      aceAwarenessSession
          .advanceCheckpoint();


      /*
       * After the midpoint decision on Shoe 1,
       * move to a fresh table regardless of the
       * user's answer.
       */
      if (completedCheckpoint == 3)
      {
        startSecondAceAwarenessShoe();

        return;
      }


      if (aceAwarenessSession
              .isFinished())
      {
        showAceAwarenessResults();

        return;
      }


      showNextAceAwarenessHand();
    }

    private void startSecondAceAwarenessShoe()
    {
      boolean aceRich =
          aceAwarenessRandom.nextBoolean();


      long seed;

      if (aceRich)
      {
        seed =
            randomAceAwarenessSeed(
                AP_RICH_SEEDS
            );
      }
      else
      {
        seed =
            randomDifferentSwitchSeed();
      }


      aceAwarenessSession
          .configureSecondShoe(
              aceRich
          );

      aceAwarenessSession
          .switchToNewShoe(
              seed
          );

      acePracticeDiscardTray.reset();


      showAceAwarenessNewShoeScreen();
    }

    private long randomDifferentSwitchSeed()
    {
      long seed;

      do
      {
        seed =
            randomAceAwarenessSeed(
                AP_SWITCH_SEEDS
            );
      }
      while (seed
             == firstAceAwarenessSeed);


      return seed;
    }
    
    private void showAceAwarenessNewShoeScreen()
    {
      Label title =
          new Label(
              "NEW TABLE"
          );

      title.setStyle("""
          -fx-font-size: 23px;
          -fx-font-weight: bold;
          -fx-text-fill: #d4af37;
      """);


      Label message =
          new Label(
              "This shoe has reached a point where "
              + "switching tables is the correct play.\n\n"
              + "The Ace count and discard tray reset "
              + "with the fresh shoe."
          );

      message.setWrapText(
          true
      );

      message.setMaxWidth(
          430
      );

      message.setAlignment(
          Pos.CENTER
      );

      message.setTextAlignment(
          TextAlignment.CENTER
      );

      message.setStyle("""
          -fx-font-size: 13px;
          -fx-text-fill: #bbbbbb;
      """);


      Button continueButton =
          new Button(
              "START NEW SHOE"
          );

      stylePrimaryButton(
          continueButton
      );

      continueButton.setOnAction(
          e ->
              showNextAceAwarenessHand()
      );


      VBox content =
          new VBox(
              20,
              title,
              message,
              continueButton
          );

      content.setAlignment(
          Pos.CENTER
      );

      content.setPadding(
          new Insets(
              35
          )
      );


      root.setTop(
          null
      );

      root.setCenter(
          content
      );

      root.setBottom(
          null
      );
    }
    
    private String formatAceBalance(
        int balance)
    {
      if (balance == 0)
      {
        return "0";
      }

      return String.format(
          "%+d",
          balance
      );
    }

    private void showAceAwarenessResults()
    {
      int correct =
          aceAwarenessSession
              .getQuestionsCorrect();

      int total =
          aceAwarenessSession
              .getQuestionsAnswered();

      double percent =
          total == 0
              ? 0.0
              : 100.0 * correct / total;


      Label title =
          new Label(
              "AP — ACE AWARENESS"
          );

      title.setStyle("""
          -fx-font-size: 18px;
          -fx-font-weight: bold;
          -fx-text-fill: #d4af37;
      """);


      Label heading =
          new Label(
              "TEST COMPLETE"
          );

      heading.setStyle("""
          -fx-font-size: 12px;
          -fx-font-weight: bold;
          -fx-text-fill: #aaaaaa;
      """);


      Label score =
          new Label(
              correct
              + " / "
              + total
          );

      score.setStyle("""
          -fx-font-size: 42px;
          -fx-font-weight: bold;
          -fx-text-fill: white;
      """);


      Label percentLabel =
          new Label(
              String.format(
                  "%.0f%%",
                  percent
              )
          );

      percentLabel.setStyle("""
          -fx-font-size: 22px;
          -fx-font-weight: bold;
          -fx-text-fill: #339af0;
      """);


      Label aceBalanceScore =
          createAceResultLabel(
              "ACE BALANCE",
              aceAwarenessSession
                  .getAceBalanceCorrect(),
              aceAwarenessSession
                  .getAceBalanceAnswered()
          );


      Label tableScore =
          createAceResultLabel(
              "TABLE SELECTION",
              aceAwarenessSession
                  .getTableSelectionCorrect(),
              aceAwarenessSession
                  .getTableSelectionAnswered()
          );


      Label super4Score =
          createAceResultLabel(
              "SUPER 4",
              aceAwarenessSession
                  .getSuper4Correct(),
              aceAwarenessSession
                  .getSuper4Answered()
          );


      VBox categories =
          new VBox(
              8,
              aceBalanceScore,
              tableScore,
              super4Score
          );

      categories.setAlignment(
          Pos.CENTER
      );


      Label hands =
          new Label(
              aceAwarenessSession
                  .getHandsPlayed()
              + " hands played"
          );

      hands.setStyle("""
          -fx-font-size: 11px;
          -fx-text-fill: #888888;
      """);


      Button anotherButton =
          new Button(
              "TAKE ANOTHER TEST"
          );

      stylePrimaryButton(
          anotherButton
      );

      anotherButton.setOnAction(
          e ->
              showStartScreen()
      );


      Button closeButton =
          new Button(
              "CLOSE"
          );

      styleSecondaryButton(
          closeButton
      );

      closeButton.setOnAction(
          e ->
              dialog.close()
      );


      HBox buttons =
          new HBox(
              12,
              anotherButton,
              closeButton
          );

      buttons.setAlignment(
          Pos.CENTER
      );


      VBox content =
          new VBox(
              10,
              title,
              heading,
              spacer(8),
              score,
              percentLabel,
              spacer(12),
              categories,
              spacer(8),
              hands,
              spacer(12),
              buttons
          );

      content.setAlignment(
          Pos.CENTER
      );

      content.setPadding(
          new Insets(
              35
          )
      );


      root.setTop(
          null
      );

      root.setCenter(
          content
      );

      root.setBottom(
          null
      );
    }

    private Label createAceResultLabel(
        String name,
        int correct,
        int total)
    {
      Label label =
          new Label(
              name
              + "   "
              + correct
              + " / "
              + total
          );

      label.setStyle("""
          -fx-font-size: 13px;
          -fx-font-weight: bold;
          -fx-text-fill: #dddddd;
      """);

      return label;
    }
    
    private void showAceCheckpointQuestionArea(
        Label title,
        VBox questionArea)
    {
       
        
      Label progress =
          new Label(
              "Checkpoint "
              + aceAwarenessSession
                  .getCheckpointNumber()
              + " of "
              + aceAwarenessSession
                  .getCheckpointCount()
          );

      progress.setStyle("""
          -fx-font-size: 12px;
          -fx-font-weight: bold;
          -fx-text-fill: #aaaaaa;
      """);


        VBox heading =
            new VBox(
                4,
                title,
                progress
            );
        
      heading.setAlignment(
          Pos.CENTER
      );


      BorderPane checkpointArea =
          new BorderPane();

      checkpointArea.setLeft(
          acePracticeDiscardTray
      );

      checkpointArea.setCenter(
          questionArea
      );

      checkpointArea.setMaxWidth(
          520
      );


      VBox center =
          new VBox(
              28,
              heading,
              checkpointArea
          );

      center.setAlignment(
          Pos.CENTER
      );

      center.setPadding(
          new Insets(
              30,
              50,
              35,
              50
          )
      );


      root.setTop(
          null
      );

      root.setCenter(
          center
      );

      root.setBottom(
          null
      );
    }
    
    private VBox createAcePracticeCardArea(
        String heading,
        List<Card> cards)
    {
      Label label =
          new Label(
              heading
          );

      label.setStyle("""
          -fx-font-size: 11px;
          -fx-font-weight: bold;
          -fx-text-fill: #aaaaaa;
      """);


      HBox cardRow =
          new HBox(
              7
          );

      cardRow.setAlignment(
          Pos.CENTER
      );


      for (Card card :
           cards)
      {
        cardRow.getChildren().add(
            createMiniCard(
                card
            )
        );
      }


      VBox area =
          new VBox(
              7,
              label,
              cardRow
          );

      area.setAlignment(
          Pos.CENTER
      );


      return area;
    }
    
    
  private void startTest(
      PracticeTestDifficulty difficulty)
  {
    session =
        sessionBuilder.build(
            difficulty
        );


    userAnswers.clear();

    questionIndex =
        0;


    showCurrentQuestion();
  }


  private void showCurrentQuestion()
  {
    PracticeTestQuestion question =
        session.getQuestion(
            questionIndex
        );


    PlayerDecisionContext context =
        evaluator.createContext(
            question
        );


    Label title =
        new Label(
            "PRACTICE TEST"
        );

    title.setStyle("""
        -fx-font-size: 17px;
        -fx-font-weight: bold;
        -fx-text-fill: #d4af37;
    """);


    Label difficulty =
        new Label(
            getDifficultyText(
                session.getDifficulty()
            )
        );

    difficulty.setStyle("""
        -fx-font-size: 11px;
        -fx-font-weight: bold;
        -fx-text-fill: #aaaaaa;
    """);


    VBox titleBox =
        new VBox(
            3,
            title,
            difficulty
        );

    titleBox.setAlignment(
        Pos.CENTER_LEFT
    );


    Label progressLabel =
        new Label(
            (questionIndex + 1)
            + " of "
            + session.size()
        );

    progressLabel.setStyle("""
        -fx-font-size: 13px;
        -fx-font-weight: bold;
        -fx-text-fill: white;
    """);


    ProgressBar progressBar =
        new ProgressBar(
        (double) (questionIndex + 1)
            / session.size()
        );

    progressBar.setPrefWidth(
        130
    );


    VBox progressBox =
        new VBox(
            5,
            progressLabel,
            progressBar
        );

    progressBox.setAlignment(
        Pos.CENTER_RIGHT
    );


    HBox top =
        new HBox(
            20,
            titleBox,
            spacerHorizontal(),
            progressBox
        );

    top.setAlignment(
        Pos.CENTER
    );

    top.setPadding(
        new Insets(
            22,
            28,
            8,
            28
        )
    );


    VBox dealerArea =
        createDealerArea(
            context.getDealerUpCard()
        );


    VBox playerArea =
        createPlayerArea(
            context.getPlayerHand()
        );


    Label prompt =
        new Label(
            "What should you do?"
        );

    prompt.setStyle("""
        -fx-font-size: 17px;
        -fx-font-weight: bold;
        -fx-text-fill: white;
    """);


    HBox actionButtons =
        createActionButtons(
            context
        );


    VBox center =
        new VBox(
            18,
            dealerArea,
            playerArea,
            spacer(3),
            prompt,
            actionButtons
        );

    center.setAlignment(
        Pos.CENTER
    );

    center.setPadding(
        new Insets(
            8,
            30,
            24,
            30
        )
    );


    root.setTop(
        top
    );

    root.setCenter(
        center
    );

    root.setBottom(
        null
    );
  }


  private HBox createActionButtons(
      PlayerDecisionContext context)
  {
    HBox buttons =
        new HBox(
            10
        );

    buttons.setAlignment(
        Pos.CENTER
    );


    for (HandAction action :
         HandAction.values())
    {
      if (!context.isAllowed(
              action))
      {
        continue;
      }


      Button button =
          new Button(
              getActionText(
                  action
              )
          );


      styleActionButton(
          button
      );


      button.setOnAction(
          e ->
              answerQuestion(
                  action
              )
      );


      buttons.getChildren().add(
          button
      );
    }


    return buttons;
  }


  private void answerQuestion(
      HandAction action)
  {
    userAnswers.add(
        action
    );


    questionIndex++;


    if (questionIndex
        >= session.size())
    {
      finishTest();

      return;
    }


    showCurrentQuestion();
  }


  // ============================================================
  // RESULTS
  // ============================================================

    private void finishTest()
    {
      PracticeTestResult result =
          scorer.score(
              session,
              userAnswers
          );


      PracticeTestCostSummary costSummary =
          new PracticeTestCostAnalyzer()
              .analyze(
                  result
              );


      showResults(
          result,
          costSummary
      );
    }
    

    private void showResults(
        PracticeTestResult result,
        PracticeTestCostSummary costSummary)
   
  {
      double totalCost =
          costSummary.getTotalEstimatedCost();


      Label costHeading =
          new Label(
              "ESTIMATED EV COST OF MISTAKES"
          );

      costHeading.setStyle("""
          -fx-font-size: 11px;
          -fx-font-weight: bold;
          -fx-text-fill: #d4af37;
      """);


      Label costValue =
          new Label(
              String.format(
                  "$%.2f",
                  totalCost
              )
          );

      costValue.setStyle("""
          -fx-font-size: 26px;
          -fx-font-weight: bold;
          -fx-text-fill: white;
      """);


      Label costSubtitle =
          new Label(
              "at a $25 wager"
          );

      costSubtitle.setStyle("""
          -fx-font-size: 11px;
          -fx-text-fill: #888888;
      """);


      VBox costBox =
          new VBox(
              3,
              costHeading,
              costValue,
              costSubtitle
          );

      costBox.setAlignment(
          Pos.CENTER
      );

      PracticeTestMistakeCost mostExpensive =
          costSummary.getMostExpensiveMistake();


      VBox mostCostlyBox =
          new VBox(
              4
          );

      mostCostlyBox.setAlignment(
          Pos.CENTER
      );

      Button reviewButton =
          new Button(
              "REVIEW MISTAKES"
          );

      stylePrimaryButton(
          reviewButton
      );


      reviewButton.setOnAction(
          e ->
              showMistakeReview(
                  result,
                  costSummary,
                  0
              )
      );


      if (result.getIncorrectAnswers()
              .isEmpty())
      {
        reviewButton.setVisible(
            false
        );

        reviewButton.setManaged(
            false
        );
      }
      
      if (mostExpensive != null)
      {
        PracticeTestQuestion question =
            mostExpensive
                .getAnswer()
                .getQuestion();


        Label mostCostlyTitle =
            new Label(
                "Most costly mistake"
            );

        mostCostlyTitle.setStyle("""
            -fx-font-size: 11px;
            -fx-font-weight: bold;
            -fx-text-fill: #aaaaaa;
        """);


        Label mostCostlyHand =
            new Label(
                question.getPlayerHandText()
                + " vs "
                + question.getDealerText()
            );

        mostCostlyHand.setStyle("""
            -fx-font-size: 13px;
            -fx-font-weight: bold;
            -fx-text-fill: white;
        """);


        Label mostCostlyActions =
            new Label(
                "You: "
                + getActionText(
                    mostExpensive
                        .getAnswer()
                        .getUserAction()
                )
                + "   •   Basic: "
                + getActionText(
                    mostExpensive
                        .getBasicAction()
                )
            );

        mostCostlyActions.setStyle("""
            -fx-font-size: 11px;
            -fx-text-fill: #bbbbbb;
        """);


        Label mostCostlyValue =
            new Label(
                String.format(
                    "Estimated EV cost: $%.2f",
                    mostExpensive
                        .getEstimatedDollarCost()
                )
            );

        mostCostlyValue.setStyle("""
            -fx-font-size: 11px;
            -fx-font-weight: bold;
            -fx-text-fill: #d4af37;
        """);


        mostCostlyBox.getChildren().addAll(
            mostCostlyTitle,
            mostCostlyHand,
            mostCostlyActions,
            mostCostlyValue
        );
      }
      
    Label title =
        new Label(
            getDifficultyText(
                result.getDifficulty()
            )
            + " PRACTICE TEST"
        );

    title.setStyle("""
        -fx-font-size: 15px;
        -fx-font-weight: bold;
        -fx-text-fill: #d4af37;
    """);


    Label score =
        new Label(
            result.getCorrectCount()
            + " / "
            + result.getTotalQuestions()
        );

    score.setStyle("""
        -fx-font-size: 42px;
        -fx-font-weight: bold;
        -fx-text-fill: white;
    """);


    Label percent =
        new Label(
            String.format(
                "%.0f%%",
                result.getScorePercent()
            )
        );

    percent.setStyle("""
        -fx-font-size: 22px;
        -fx-font-weight: bold;
        -fx-text-fill: #339af0;
    """);

      Label basicScore =
          new Label(
              "BASIC STRATEGY\n"
              + result.getBasicCorrectCount()
              + " / "
              + result.getTotalQuestions()
          );

      basicScore.setAlignment(
          Pos.CENTER
      );

      basicScore.setStyle("""
          -fx-font-size: 12px;
          -fx-font-weight: bold;
          -fx-text-fill: #dddddd;
      """);


      Label socialScore =
          new Label(
              "CASINO READY\n"
              + result.getCasinoReadyCount()
              + " / "
              + result.getTotalQuestions()
          );

      socialScore.setAlignment(
          Pos.CENTER
      );

      socialScore.setStyle("""
          -fx-font-size: 12px;
          -fx-font-weight: bold;
          -fx-text-fill: #dddddd;
      """);


      HBox strategyScores =
          new HBox(
              55,
              basicScore,
              socialScore
          );

      strategyScores.setAlignment(
          Pos.CENTER
      );

      VBox categoryBox =
          new VBox(
              5
          );

      categoryBox.setAlignment(
          Pos.CENTER
      );


      for (PracticeTestCategory category :
           PracticeTestCategory.values())
      {
        int correct =
            result.getCorrectCount(
                category
            );

        int total =
            result.getQuestionCount(
                category
            );


        Label categoryLabel =
            new Label(
                getCategoryText(
                    category
                )
                + "   "
                + correct
                + " / "
                + total
            );

        categoryLabel.setStyle("""
            -fx-font-size: 12px;
            -fx-text-fill: #bbbbbb;
        """);


        categoryBox.getChildren().add(
            categoryLabel
        );
      }
      
    Label message =
        new Label(
            getResultMessage(
                result.getScorePercent()
            )
        );

    message.setStyle("""
        -fx-font-size: 13px;
        -fx-text-fill: #bbbbbb;
    """);


    Button newTestButton =
        new Button(
            "TAKE ANOTHER TEST"
        );

    stylePrimaryButton(
        newTestButton
    );

    newTestButton.setOnAction(
        e ->
            showStartScreen()
    );


    Button closeButton =
        new Button(
            "CLOSE"
        );

    styleSecondaryButton(
        closeButton
    );

    closeButton.setOnAction(
        e ->
            dialog.close()
    );


    HBox buttons =
        new HBox(
            12,
            newTestButton,
            closeButton
        );

    buttons.setAlignment(
        Pos.CENTER
    );

      VBox content =
          new VBox(
              9,
              title,
              spacer(6),
              score,
              percent,
              strategyScores,
              spacer(6),
              categoryBox,
              spacer(6),
              costBox,
              mostCostlyBox,
              spacer(8),
              reviewButton,
              spacer(8),
              message,
              buttons
          );
      

    content.setAlignment(
        Pos.CENTER
    );

    content.setPadding(
        new Insets(
            24
        )
    );


    root.setTop(
        null
    );

    root.setBottom(
        null
    );

    root.setCenter(
        content
    );
  }

    private String getCategoryText(
        PracticeTestCategory category)
    {
      switch (category)
      {
        case HARD:
          return "Hard";

        case SOFT:
          return "Soft";

        case PAIR:
          return "Pairs";

        case SURRENDER:
          return "Surrender";

        default:
          return category.toString();
      }
    }
    
  // ============================================================
  // HAND DISPLAY
  // ============================================================

  private VBox createDealerArea(
      Card dealerCard)
  {
    Label label =
        new Label(
            "DEALER"
        );

    label.setStyle("""
        -fx-font-size: 11px;
        -fx-font-weight: bold;
        -fx-text-fill: #aaaaaa;
    """);


    VBox area =
        new VBox(
            7,
            label,
            createMiniCard(
                dealerCard
            )
        );

    area.setAlignment(
        Pos.CENTER
    );


    return area;
  }


  private VBox createPlayerArea(
      Hand hand)
  {
    Label label =
        new Label(
            "PLAYER"
        );

    label.setStyle("""
        -fx-font-size: 11px;
        -fx-font-weight: bold;
        -fx-text-fill: #aaaaaa;
    """);


    HBox cards =
        new HBox(
            7
        );

    cards.setAlignment(
        Pos.CENTER
    );


    for (int i = 0;
         i < hand.getNumCards();
         i++)
    {
      cards.getChildren().add(
          createMiniCard(
              hand.getCard(i)
          )
      );
    }


    Label description =
        new Label(
            getHandDescription(
                hand
            )
        );

    description.setStyle("""
        -fx-font-size: 12px;
        -fx-text-fill: #dddddd;
    """);


    VBox area =
        new VBox(
            7,
            label,
            cards,
            description
        );

    area.setAlignment(
        Pos.CENTER
    );


    return area;
  }


  private Node createMiniCard(
      Card card)
  {
    CardView cardView =
        new CardView();


    cardView.showFront(
        card
    );


    double scale =
        0.62;


    cardView.getTransforms().add(
        new Scale(
            scale,
            scale,
            0,
            0
        )
    );


    return new Group(
        cardView
    );
  }


  private String getHandDescription(
      Hand hand)
  {
    if (hand.getNumCards() == 2
        && hand.getCard(0)
               .getPointValue()
           == hand.getCard(1)
               .getPointValue())
    {
      return "Pair of "
          + hand.getCard(0)
              .getRank()
              .getShortName()
          + "s";
    }


    if (hand.isSoft())
    {
      return "Soft "
          + hand.getScore();
    }


    return "Hard "
        + hand.getScore();
  }


  // ============================================================
  // STYLING
  // ============================================================

  private Button createLevelButton(
      String title,
      String subtitle)
  {
    Button button =
        new Button(
            title
            + "\n"
            + subtitle
        );


    button.setPrefWidth(
        390
    );

    button.setPrefHeight(
        78
    );
      button.setAlignment(
          Pos.CENTER
      );

      button.setTextAlignment(
          TextAlignment.CENTER
      );

      button.setWrapText(
          false
      );
      
      button.setStyle("""
          -fx-background-color: #363636;
          -fx-text-fill: white;
          -fx-font-size: 13px;
          -fx-font-weight: bold;
          -fx-background-radius: 8px;
          -fx-border-color: #555555;
          -fx-border-radius: 8px;
          -fx-padding: 12px;
      """);


    button.setOnMouseEntered(
        e ->
            button.setStyle("""
                -fx-background-color: #404040;
                -fx-text-fill: white;
                -fx-font-size: 13px;
                -fx-font-weight: bold;
                -fx-background-radius: 8px;
                -fx-border-color: #d4af37;
                -fx-border-radius: 8px;
                -fx-padding: 10px;
            """)
    );


    button.setOnMouseExited(
        e ->
            button.setStyle("""
                -fx-background-color: #363636;
                -fx-text-fill: white;
                -fx-font-size: 13px;
                -fx-font-weight: bold;
                -fx-background-radius: 8px;
                -fx-border-color: #555555;
                -fx-border-radius: 8px;
                -fx-padding: 10px;
            """)
    );


    return button;
  }


  private void styleActionButton(
      Button button)
  {
    button.setMinWidth(
        95
    );

    button.setPrefHeight(
        42
    );

    button.setStyle("""
        -fx-background-color: #3b4656;
        -fx-text-fill: white;
        -fx-font-size: 12px;
        -fx-font-weight: bold;
        -fx-background-radius: 5px;
    """);
  }


  private void stylePrimaryButton(
      Button button)
  {
    button.setStyle("""
        -fx-background-color: #007bff;
        -fx-text-fill: white;
        -fx-font-weight: bold;
        -fx-background-radius: 5px;
        -fx-padding: 9px 16px;
    """);
  }


  private void styleSecondaryButton(
      Button button)
  {
    button.setStyle("""
        -fx-background-color: #444444;
        -fx-text-fill: white;
        -fx-font-weight: bold;
        -fx-background-radius: 5px;
        -fx-padding: 9px 16px;
    """);
  }


  private String getActionText(
      HandAction action)
  {
    if (action
        == HandAction.SURRENDER)
    {
      return "SURRENDER";
    }


    return action
        .getDisplayText()
        .toUpperCase();
  }


  private String getDifficultyText(
      PracticeTestDifficulty difficulty)
  {
    if (difficulty
        == PracticeTestDifficulty.CP)
    {
      return "CP — CASINO READY";
    }


    return "HONORS — PERFECT STRATEGY";
  }


  private String getResultMessage(
      double score)
  {
    if (score >= 90.0)
    {
      return "Excellent strategy knowledge.";
    }


    if (score >= 80.0)
    {
      return "Strong result — a few decisions are worth reviewing.";
    }


    if (score >= 70.0)
    {
      return "Solid foundation, with some important gaps to review.";
    }


    return "Review your missed decisions and try again.";
  }


  private Region spacer(
      double height)
  {
    Region spacer =
        new Region();

    spacer.setMinHeight(
        height
    );

    spacer.setPrefHeight(
        height
    );


    return spacer;
  }


  private Region spacerHorizontal()
  {
    Region spacer =
        new Region();

    HBox.setHgrow(
        spacer,
        javafx.scene.layout.Priority.ALWAYS
    );


    return spacer;
  }

    private void showMistakeReview(
        PracticeTestResult result,
        PracticeTestCostSummary costSummary,
        int mistakeIndex)
    {
      List<PracticeTestMistakeCost> mistakes =
          costSummary.getMistakeCosts();


      PracticeTestMistakeCost mistake =
          mistakes.get(
              mistakeIndex
          );


      PracticeTestAnswer answer =
          mistake.getAnswer();


      PracticeTestQuestion question =
          answer.getQuestion();


      PlayerDecisionContext context =
          evaluator.createContext(
              question
          );


      Label title =
          new Label(
              "REVIEW MISTAKES"
          );

      title.setStyle("""
          -fx-font-size: 17px;
          -fx-font-weight: bold;
          -fx-text-fill: #d4af37;
      """);


      Label progress =
          new Label(
              (mistakeIndex + 1)
              + " of "
              + mistakes.size()
          );

      progress.setStyle("""
          -fx-font-size: 13px;
          -fx-font-weight: bold;
          -fx-text-fill: white;
      """);


      HBox top =
          new HBox(
              title,
              spacerHorizontal(),
              progress
          );

      top.setAlignment(
          Pos.CENTER
      );

      top.setPadding(
          new Insets(
              22,
              28,
              8,
              28
          )
      );


      VBox dealerArea =
          createDealerArea(
              context.getDealerUpCard()
          );


      VBox playerArea =
          createPlayerArea(
              context.getPlayerHand()
          );


      VBox decisionArea =
          createReviewDecisionArea(
              answer
          );


      Label costLabel =
          new Label(
              String.format(
                  "Estimated EV cost: $%.2f",
                  mistake.getEstimatedDollarCost()
              )
          );

      costLabel.setStyle("""
          -fx-font-size: 13px;
          -fx-font-weight: bold;
          -fx-text-fill: #d4af37;
      """);


      VBox explanationArea =
          createReviewExplanation(
              context,
              answer
          );


      Button previousButton =
          new Button(
              "PREVIOUS"
          );

      styleSecondaryButton(
          previousButton
      );


      previousButton.setDisable(
          mistakeIndex == 0
      );


      previousButton.setOnAction(
          e ->
              showMistakeReview(
                  result,
                  costSummary,
                  mistakeIndex - 1
              )
      );


      Button nextButton =
          new Button(
              "NEXT"
          );

      styleSecondaryButton(
          nextButton
      );


      nextButton.setDisable(
          mistakeIndex
          == mistakes.size() - 1
      );


      nextButton.setOnAction(
          e ->
              showMistakeReview(
                  result,
                  costSummary,
                  mistakeIndex + 1
              )
      );


      HBox navigation =
          new HBox(
              12,
              previousButton,
              nextButton
          );

      navigation.setAlignment(
          Pos.CENTER
      );


      Button backButton =
          new Button(
              "BACK TO RESULTS"
          );

      stylePrimaryButton(
          backButton
      );


      backButton.setOnAction(
          e ->
              showResults(
                  result,
                  costSummary
              )
      );


        VBox center =
            new VBox(
                12,
                dealerArea,
                playerArea,
                decisionArea,
                costLabel,
                explanationArea
            );

        center.setAlignment(
            Pos.CENTER
        );

        center.setPadding(
            new Insets(
                8,
                30,
                8,
                30
            )
        );

        VBox bottom =
            new VBox(
                8,
                navigation,
                backButton
            );

        bottom.setAlignment(
            Pos.CENTER
        );

        bottom.setPadding(
            new Insets(
                8,
                0,
                22,
                0
            )
        );
        

    root.setTop(
        top
    );

    root.setCenter(
        center
    );

    root.setBottom(
        bottom
    );
    
    }
    private VBox createReviewDecisionArea(
        PracticeTestAnswer answer)
    {
      PracticeTestEvaluator.Evaluation evaluation =
          answer.getEvaluation();


      Label userLabel =
          new Label(
              "You chose: "
              + getActionText(
                  answer.getUserAction()
              )
          );

      userLabel.setStyle("""
          -fx-font-size: 13px;
          -fx-font-weight: bold;
          -fx-text-fill: #ff7b7b;
      """);


      Label basicLabel =
          new Label(
              "Basic Strategy: "
              + getActionText(
                  evaluation.getBasicAction()
              )
          );

      basicLabel.setStyle("""
          -fx-font-size: 13px;
          -fx-font-weight: bold;
          -fx-text-fill: #339af0;
      """);


      Label socialLabel =
          new Label(
              "Social Strategy: "
              + getActionText(
                  evaluation.getSocialAction()
              )
          );

      socialLabel.setStyle("""
          -fx-font-size: 13px;
          -fx-font-weight: bold;
          -fx-text-fill: #dddddd;
      """);


      VBox area =
          new VBox(
              4,
              userLabel,
              basicLabel,
              socialLabel
          );

      area.setAlignment(
          Pos.CENTER
      );


      /*
       * Especially useful on Honors:
       * distinguish a bad blackjack decision from
       * a valid Social Strategy decision.
       */
      if (evaluation.isValidSocialAlternative())
      {
        Label socialNote =
            new Label(
                "✓ Valid Social Strategy play"
            );

        socialNote.setStyle("""
            -fx-font-size: 12px;
            -fx-font-weight: bold;
            -fx-text-fill: #7bd88f;
        """);


        Label honorsNote =
            new Label(
                "Honors requires Basic Strategy"
            );

        honorsNote.setStyle("""
            -fx-font-size: 11px;
            -fx-text-fill: #aaaaaa;
        """);


        area.getChildren().addAll(
            socialNote,
            honorsNote
        );
      }


      return area;
    }
    private VBox createReviewExplanation(
        PlayerDecisionContext context,
        PracticeTestAnswer answer)
    {
      VBox area =
          new VBox(
              5
          );

      area.setAlignment(
          Pos.CENTER
      );

      area.setMaxWidth(
          470
      );


      Label heading =
          new Label(
              "WHY?"
          );

      heading.setStyle("""
          -fx-font-size: 11px;
          -fx-font-weight: bold;
          -fx-text-fill: #d4af37;
      """);


      StrategyDeviation deviation =
          StrategyDeviationCatalog.find(
              context
          );


      if (deviation != null)
      {
        Label explanation =
            createReviewText(
                deviation.getExplanation(),
                "#cccccc",
                12
            );


        Label takeaway =
            createReviewText(
                deviation.getTakeaway(),
                "#d4af37",
                11
            );


        area.getChildren().addAll(
            heading,
            explanation,
            takeaway
        );
      }
      else
      {
        /*
         * For ordinary Basic Strategy mistakes,
         * explain the underlying strategy rule.
         */
        StrategyGuidanceProvider provider =
            StrategyGuidanceFactory.create(
                StrategyMode.BASIC
            );


        StrategyGuidance guidance =
            provider.getGuidance(
                context
            );


        if (guidance != null)
        {
          Label rule =
              createReviewText(
                  guidance.getRule(),
                  "white",
                  12
              );

          rule.setStyle(
              rule.getStyle()
              + "-fx-font-weight: bold;"
          );


          Label explanation =
              createReviewText(
                  guidance.getExplanation(),
                  "#bbbbbb",
                  11
              );


          area.getChildren().addAll(
              heading,
              rule,
              explanation
          );
        }
        else
        {
          Label fallback =
              createReviewText(
                  "Review the Basic Strategy rule for this hand.",
                  "#bbbbbb",
                  11
              );


          area.getChildren().addAll(
              heading,
              fallback
          );
        }
      }


      area.setPadding(
          new Insets(
              9,
              14,
              9,
              14
          )
      );

      area.setStyle("""
          -fx-background-color: rgba(255, 255, 255, 0.035);
          -fx-background-radius: 8px;
      """);


      return area;
    }
    private Label createReviewText(
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
          440
      );

      label.setMinHeight(
          Region.USE_PREF_SIZE
      );

      label.setAlignment(
          Pos.CENTER
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
    
}
