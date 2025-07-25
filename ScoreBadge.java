import javafx.animation.ScaleTransition;
import javafx.animation.FillTransition;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.util.Duration;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.paint.Color;
import javafx.animation.PauseTransition;

public class ScoreBadge extends StackPane
{
    private final Label label;
    private final Circle background;
    
    public ScoreBadge()
    {
        label = new Label("0");
        label.setFont(Font.font("Arial", 14));
        label.setTextFill(Color.WHITE);
    
        background = new Circle(15);
        background.setFill(Color.DARKGREEN);
        background.setStroke(Color.GOLD);
        background.setStrokeWidth(2);
    
        this.getChildren().addAll(background, label);
        this.setPrefSize(30, 30);
    }
    
    public void setScore(Hand hand, int score)
    {
        if (hand.isBusted())
        {
            setText(String.valueOf(score), Color.DARKRED);
        } else if (hand.isBlackjack())
        {
            setText(String.valueOf(score), Color.BLACK);
            playGlow();
        } else
        {
            setText(String.valueOf(score), Color.DARKSLATEBLUE);
        }
    }
    public void setText(String text, Color bgColor)
    {
        label.setText(text);
        animate(bgColor);
    }
    
    private void animate(Color newColor)
    {
        // Animate fill color
        FillTransition fill = new FillTransition(Duration.millis(300), background, (Color) background.getFill(), newColor);
        fill.play();
    
        // Animate bounce
        ScaleTransition scale = new ScaleTransition(Duration.millis(200), this);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(1.2);
        scale.setToY(1.2);
        scale.setAutoReverse(true);
        scale.setCycleCount(2);
        scale.play();
    }
    
    private void playGlow()
    {
        DropShadow glow = new DropShadow();
        glow.setColor(Color.WHITE);
        glow.setRadius(12);
        glow.setSpread(0.5);
        glow.setOffsetX(0);
        glow.setOffsetY(0);
    
        // Set the glow
        this.setEffect(glow);
    /*
        Optional: remove glow after a moment (e.g., 2 seconds)
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(e -> this.setEffect(null));
        pause.play();     */
    }
}
