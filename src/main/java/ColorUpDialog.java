import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.application.Platform;

public class ColorUpDialog
{
    public enum Choice
    {
      CANCEL,
      CHANGE_TABLE,
      EXIT
    }
    
  public static Choice show(
      Stage ownerStage,
      double amount)
  {
    final Choice[] choice =
        { Choice.CANCEL };

    Label title =
        new Label(
            "COLOR UP?"
        );

    title.setStyle("""
        -fx-font-size: 18px;
        -fx-font-weight: bold;
        -fx-text-fill: #d4af37;
    """);

    ChipStackView currentChips =
        new ChipStackView();

    currentChips.setAmount(
        amount,
        ChipDisplayMode.BANKROLL
    );

    ChipStackView coloredUpChips =
        new ChipStackView();

    coloredUpChips.setAmount(
        amount,
        ChipDisplayMode.COLOR_UP
    );

    VBox currentBox =
        new VBox(
            8,
            currentChips
        );

    currentBox.setAlignment(
        Pos.CENTER
    );

    VBox coloredBox =
        new VBox(
            8,
            coloredUpChips
        );

    coloredBox.setAlignment(
        Pos.CENTER
    );

    Label arrow =
        new Label("→");

    arrow.setStyle(
        "-fx-text-fill: #d4af37;"
        + "-fx-font-size: 26px;"
        + "-fx-font-weight: bold;"
    );

    HBox chipComparison =
        new HBox(
            18,
            currentBox,
            arrow,
            coloredBox
        );

    chipComparison.setAlignment(
        Pos.CENTER
    );

    Label total =
        new Label(
            Bankroll.getFormattedCurrency(
                amount
            )
        );

    total.setStyle(
        "-fx-text-fill: #FFD700;"
        + "-fx-font-size: 16px;"
        + "-fx-font-weight: bold;"
    );

    Button cancel =
        new Button(
            "CANCEL"
        );

      Button changeTable =
          new Button(
              "CHANGE TABLE"
          );
      changeTable.setStyle("""
          -fx-background-color: transparent;
          -fx-border-color: #d4af37;
          -fx-border-width: 1px;
          -fx-border-radius: 5px;
          -fx-text-fill: #d4af37;
          -fx-font-weight: bold;
      """);
      
    Button confirm =
        new Button(
            "EXIT"
        );

    confirm.setStyle("""
        -fx-background-color: #007bff;
        -fx-text-fill: white;
        -fx-font-weight: bold;
    """);

      HBox buttons =
          new HBox(
              12,
              cancel,
              changeTable,
              confirm
          );
      
    buttons.setAlignment(
        Pos.CENTER
    );

    VBox root =
        new VBox(
            16,
            title,
            chipComparison,
            total,
            buttons
        );

    root.setAlignment(
        Pos.CENTER
    );

    root.setPadding(
        new Insets(18, 24, 20, 24)
    );

    root.setStyle(
        "-fx-background-color: #282828;"
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
        "Color Up"
    );

    cancel.setOnAction(
        e ->
            dialog.close()
    );

        changeTable.setOnAction(
            e ->
            {
              choice[0] =
                  Choice.CHANGE_TABLE;

              dialog.close();
            }
        );

      
    confirm.setOnAction(
        e ->
        {
          choice[0] = Choice.EXIT;
          dialog.close();
        }
    );

      dialog.setScene(
          new Scene(root)
      );

      dialog.setResizable(
          false
      );

      dialog.setOnShown(e ->
          Platform.runLater(() ->
          {
            dialog.sizeToScene();

            dialog.setX(
                ownerStage.getX()
                + (ownerStage.getWidth()
                   - dialog.getWidth()) / 2
            );

            dialog.setY(
                ownerStage.getY()
                + (ownerStage.getHeight()
                   - dialog.getHeight()) / 2
            );
          })
      );

      dialog.showAndWait();
    return choice[0];
  }
}
