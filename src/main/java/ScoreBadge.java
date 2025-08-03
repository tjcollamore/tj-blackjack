import javafx.animation.ScaleTransition;
import javafx.animation.FillTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.paint.Color;
import javafx.animation.PauseTransition;

public class ScoreBadge extends StackPane
{
    private final Label label;
    private final Circle background;
    private final Label softIndicator;
    
    public ScoreBadge()
    {
        setPrefSize(30, 30);
        setMaxSize(30, 30);
        setMinSize(30, 30);

        // Main circular background
        background = new Circle(15);
        background.setFill(Color.DARKGREEN);
        background.setStroke(Color.GOLD);
        background.setStrokeWidth(2);

        // Score label
        label = new Label("0");
        label.setFont(Font.font("Arial", 14));
        label.setTextFill(Color.WHITE);

        // Star indicator for soft hand
        softIndicator = new Label("✦");
        softIndicator.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        softIndicator.setTextFill(Color.LIGHTYELLOW);
        softIndicator.setVisible(false);
        softIndicator.setMouseTransparent(true);

        // Inner layout (confined to 30x30)
        StackPane inner = new StackPane(background, label, softIndicator);
        inner.setPrefSize(30, 30);
        inner.setMaxSize(30, 30);
        inner.setMinSize(30, 30);

        StackPane.setAlignment(softIndicator, Pos.TOP_CENTER);
        StackPane.setMargin(softIndicator, new Insets(-2, 0, 0, 0));

        getChildren().add(inner);
    }

 
    public void setScore(Hand hand, int score)
    {
        if (hand.isSoft())
            softIndicator.setVisible(true);
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
