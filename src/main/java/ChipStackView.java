import javafx.animation.TranslateTransition;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
import javafx.geometry.Pos;
import javafx.scene.shape.Circle;
import java.util.Map;

public class ChipStackView extends VBox {

    private static final double STACK_OFFSET_Y = 4;

    public ChipStackView(/*Map<WagerChip, Integer> chips,*/ boolean showTotal) {
        long stackCount = 1;//chips.entrySet().stream().filter(e -> e.getValue() > 0).count();

        double chipSize = (stackCount > 2) ? 40 : 50;
        double chipWidth = chipSize;
        double chipHeight = chipSize;

        StackPane chipArea = new StackPane();
        chipArea.setPrefWidth(200);
        chipArea.setPrefHeight(110);

        Circle betCircle = new Circle(65, Color.rgb(200, 200, 200, 0.1));
        betCircle.setStroke(Color.WHITE);
        betCircle.setStrokeWidth(3);
        betCircle.setEffect(new DropShadow(10, Color.gray(0.4)));

        Region chipLayout;
/*
        if (stackCount == 4) {
            GridPane grid = new GridPane();
            grid.setHgap(6);
            grid.setVgap(6);
            grid.setAlignment(Pos.CENTER);

            int col = 0;
            int row = 0;

            for (Map.Entry<WagerChip, Integer> entry : chips.entrySet()) {
                WagerChip chip = entry.getKey();
                int count = entry.getValue();
                if (count <= 0) continue;

                StackPane typeStack = createChipStack(chip, count, chipWidth, chipHeight);
                grid.add(typeStack, col, row);

                col++;
                if (col >= 2) {
                    col = 0;
                    row++;
                }
            }
            chipLayout = grid;

        } else {
            HBox chipRow = new HBox(1);
            chipRow.setSpacing(stackCount > 3 ? 0 : 2);
            chipRow.setAlignment(Pos.BOTTOM_CENTER);

            for (Map.Entry<WagerChip, Integer> entry : chips.entrySet()) {
                WagerChip chip = entry.getKey();
                int count = entry.getValue();
                if (count <= 0) continue;

                StackPane typeStack = createChipStack(chip, count, chipWidth, chipHeight);
                chipRow.getChildren().add(typeStack);
            }

            chipLayout = chipRow;
        }

        chipArea.getChildren().addAll(betCircle, chipLayout);

        this.setSpacing(2);
        this.setAlignment(Pos.CENTER);
        this.getChildren().add(chipArea);

        if (showTotal) {
            double total = chips.entrySet().stream()
                    .mapToDouble(e -> e.getKey().getCentValue() * e.getValue())
                    .sum() / 100.0;

            Label totalLabel = new Label(String.format("Total: $%.2f", total));
            totalLabel.setFont(Font.font("Arial", 14));
            this.getChildren().add(totalLabel);
        }
        */
    }
/*
    private StackPane createChipStack(WagerChip chip, int count, double chipWidth, double chipHeight) {
        StackPane typeStack = new StackPane();
        typeStack.setPrefWidth(chipWidth + 2);

        Image image = loadChipImage(chip);
        DropShadow shadow = chip == WagerChip.WHITE
                ? new DropShadow(6, Color.GRAY)
                : new DropShadow(4, Color.gray(0.3, 0.6));

        for (int i = 0; i < count; i++) {
            ImageView chipView = new ImageView(image);
            chipView.setFitWidth(chipWidth);
            chipView.setFitHeight(chipHeight);
            chipView.setTranslateY(-i * STACK_OFFSET_Y);
            chipView.setEffect(shadow);

            // Animate drop
            TranslateTransition drop = new TranslateTransition(Duration.millis(250), chipView);
            drop.setFromY(-30 - i * STACK_OFFSET_Y);
            drop.setToY(-i * STACK_OFFSET_Y);
            drop.play();

            typeStack.getChildren().add(chipView);
        }

        return typeStack;
    }

    private Image loadChipImage(WagerChip chip) {
        try {
            return new Image("file:" + chip.getImagePath());
        } catch (Exception e) {
            System.err.println("Failed to load chip image: " + chip.getImagePath());
            return null;
        }
    }
*/}
