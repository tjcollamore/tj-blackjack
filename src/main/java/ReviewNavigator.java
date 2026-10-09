import java.util.function.IntConsumer;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ReviewNavigator extends VBox
{
  private int selectedIndex;
  private int total;

  private final Button previousButton;
  private final Button nextButton;
  private final Label positionLabel;

  private IntConsumer onSelectionChanged;

  public ReviewNavigator(int total)
  {
    this.total = total;
    this.selectedIndex = 0;

    setAlignment(Pos.CENTER);
    setSpacing(1);

    Label title = new Label("REVIEW");
    title.setStyle("""
        -fx-text-fill: white;
        -fx-font-size: 10px;
        -fx-font-weight: bold;
    """);

    previousButton = new Button("‹");
    nextButton = new Button("›");
    positionLabel = new Label();

    positionLabel.setMinWidth(52);
    positionLabel.setAlignment(Pos.CENTER);

    positionLabel.setStyle("""
        -fx-text-fill: #FFD700;
        -fx-font-size: 15px;
        -fx-font-weight: bold;
    """);

    styleNavigationButton(previousButton);
    styleNavigationButton(nextButton);

    previousButton.setOnAction(e ->
        changeSelection(-1)
    );

    nextButton.setOnAction(e ->
        changeSelection(1)
    );

    HBox controls = new HBox(
        0,
        previousButton,
        positionLabel,
        nextButton
    );

    controls.setAlignment(Pos.CENTER);

    getChildren().addAll(
        title,
        controls
    );

    updateDisplay();
  }

  public void setOnSelectionChanged(
      IntConsumer onSelectionChanged)
  {
    this.onSelectionChanged = onSelectionChanged;
  }

  public int getSelectedIndex()
  {
    return selectedIndex;
  }

  private void changeSelection(int direction)
  {
    int newIndex = selectedIndex + direction;

    if (newIndex < 0 || newIndex >= total)
      return;

    selectedIndex = newIndex;

    updateDisplay();

    if (onSelectionChanged != null)
      onSelectionChanged.accept(selectedIndex);
  }

  private void updateDisplay()
  {
    positionLabel.setText(
        (selectedIndex + 1) + " of " + total
    );

    previousButton.setDisable(
        selectedIndex == 0
    );

    nextButton.setDisable(
        selectedIndex == total - 1
    );
  }

  private void styleNavigationButton(Button button)
  {
    button.setStyle("""
        -fx-background-color: transparent;
        -fx-text-fill: white;
        -fx-font-size: 20px;
        -fx-font-weight: bold;
        -fx-padding: 0px 6px 0px 6px;
        -fx-cursor: hand;
    """);

    button.setFocusTraversable(false);
  }
}
