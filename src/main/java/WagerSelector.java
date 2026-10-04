import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class WagerSelector extends VBox
{
    private static final int STEP = 5;

    private int wager;
    private int minWager;
    private int maxWager;
    
    private final Label amountLabel;
    private final Button minusButton;
    private final Button plusButton;

    private Timeline repeatTimeline;
    private Timeline repeatDelay;
    
    public WagerSelector(int minWager, int maxWager)
    {
        this.minWager = minWager;
        this.maxWager = normalizeMax(maxWager);
        wager = minWager;

        setAlignment(Pos.CENTER);
        setSpacing(1);

        Label title = new Label("WAGER");
        title.setStyle("""
            -fx-text-fill: white;
            -fx-font-size: 10px;
            -fx-font-weight: bold;
        """);

        minusButton = new Button("−");
        plusButton = new Button("+");
        amountLabel = new Label();

        amountLabel.setMinWidth(52);
        amountLabel.setAlignment(Pos.CENTER);

        amountLabel.setStyle("""
            -fx-text-fill: #FFD700;
            -fx-font-size: 18px;
            -fx-font-weight: bold;
        """);

        styleAdjustmentButton(minusButton);
        styleAdjustmentButton(plusButton);

        HBox controls = new HBox(
            0,
            minusButton,
            amountLabel,
            plusButton
        );

        controls.setAlignment(Pos.CENTER);

        getChildren().addAll(
            title,
            controls
        );

        installAutoRepeat(minusButton, -1);
        installAutoRepeat(plusButton, 1);
        
        updateDisplay();
    }

    public int getWager()
    {
        return wager;
    }

    public void setMaxWager(int maxWager)
    {
        this.maxWager = normalizeMax(maxWager);

        if (wager > this.maxWager)
            wager = this.maxWager;

        updateDisplay();
    }

    private void updateDisplay()
    {
        updateAmountLabel();
        minusButton.setDisable(wager <= minWager);
        plusButton.setDisable(wager >= maxWager);
    }
    
    private void updateAmountLabel()
    {
        amountLabel.setText("$" + wager);
    }
    
    private int normalizeMax(int maxWager)
    {
        return maxWager - (maxWager % STEP);
    }

    private void styleAdjustmentButton(Button button)
    {
        button.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: white;
            -fx-font-size: 18px;
            -fx-font-weight: bold;
            -fx-padding: 1px 4px 1px 4px;
            -fx-cursor: hand;
        """);

        button.setFocusTraversable(false);
    }

    private void installAutoRepeat(Button button, int direction)
    {
        button.setOnMousePressed(e -> {
            // One immediate step
            changeWager(direction * STEP);

            // Wait briefly before rapid repeat starts
            repeatDelay = new Timeline(
                new KeyFrame(Duration.millis(400), event -> {
                    repeatTimeline = new Timeline(
                        new KeyFrame(
                            Duration.millis(80),
                            repeatEvent -> changeWager(direction * STEP)
                        )
                    );

                    repeatTimeline.setCycleCount(Timeline.INDEFINITE);
                    repeatTimeline.play();
                })
            );

            repeatDelay.play();
        });

        button.setOnMouseReleased(e -> stopAutoRepeat());
        button.setOnMouseExited(e -> stopAutoRepeat());
    }

    private void stopAutoRepeat()
    {
        if (repeatDelay != null)
            repeatDelay.stop();

        if (repeatTimeline != null)
            repeatTimeline.stop();
    }

    private void changeWager(int amount)
    {
        int newWager = wager + amount;

        if (newWager < minWager)
            newWager = minWager;

        if (newWager > maxWager)
            newWager = maxWager;

        wager = newWager;
        updateDisplay();

        if (wager == minWager || wager == maxWager)
            stopAutoRepeat();
    }
    
}
