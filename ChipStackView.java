import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import java.util.List;

public class ChipStackView extends VBox 
{

    public ChipStackView(List<WagerChip> chips)
    {
        HBox stackRow = new HBox();
        double spacing = 8.0;

        for (WagerChip chip : chips)
        {
            for (int i = 0; i < chip.getCount(); i++) 
            {
                ImageView chipView = new ImageView(new Image(chip.getImagePath()));
                    //getClass().getResource(chip.getImagePath()).toExternalForm()));
//                chipView.setFitWidth(40);
//                chipView.setFitHeight(40);
                Pane wrapper = new Pane(chipView);
                wrapper.setTranslateX(-i * (spacing / 2)); // slight fan effect for same type
                stackRow.getChildren().add(wrapper);
            }
        }

        // Add total label
        double total = chips.stream()
                .mapToDouble(c -> c.getCount() * (c.getValue() / 100.0))
                .sum();
        Label totalLabel = new Label(String.format("Total: $%.2f", total));
        totalLabel.setFont(Font.font("Arial", 14));

        this.getChildren().addAll(stackRow, totalLabel);
        this.setSpacing(5);
    }
}
