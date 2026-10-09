import javafx.scene.control.Button;
import javafx.scene.Cursor;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

public class DecisionHelpButton extends Button
{
    private HandAction acceptedAction;
    
    public DecisionHelpButton(
        Stage ownerStage,
        PlayerDecisionContext context)
    {
      super("?");
    
      setPadding(Insets.EMPTY);
      setAlignment(Pos.CENTER);
    
      setCursor(Cursor.HAND);
    
      setPrefSize(26, 26);
      setMinSize(26, 26);
      setMaxSize(26, 26);

      applyNormalStyle();
      setOnMouseEntered(e -> applyHoverStyle());
      setOnMouseExited(e -> applyNormalStyle());
        
    setOnAction(e ->
    {
      HandAction action =
          DecisionHelpDialog.show(
              ownerStage,
              context
          );

      if (action != null)
      {
        acceptedAction = action;

        Stage actionBarStage =
            (Stage) getScene()
                .getWindow();

        actionBarStage.close();
      }
    });
    }

    public HandAction getAcceptedAction()
    {
      return acceptedAction;
    }
    
    private void applyNormalStyle()
    {
      setStyle("""
          -fx-background-color: transparent;
          -fx-border-color: #9fa6ad;
          -fx-border-width: 1.5px;
          -fx-border-radius: 13px;
          -fx-background-radius: 13px;
          -fx-text-fill: #d8dde3;
          -fx-font-size: 14px;
          -fx-font-weight: bold;
      """);

      setOpacity(0.85);
    }

    private void applyHoverStyle()
    {
      setStyle("""
          -fx-background-color: transparent;
          -fx-border-color: #D4AF37;
          -fx-border-width: 1.5px;
          -fx-border-radius: 13px;
          -fx-background-radius: 13px;
          -fx-text-fill: #D4AF37;
          -fx-font-size: 14px;
          -fx-font-weight: bold;
      """);

      setOpacity(1.0);
    }
    
}
