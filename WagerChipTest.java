import org.junit.jupiter.api.Test;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WagerChipTest {

    @Test
    public void test400() {
        assertEquals(
            Map.of(WagerChip.WHITE, 4),
            WagerChip.getChipsByValue(4.00)
        );
    }

    @Test
    public void test450() {
        assertEquals(
            Map.of(WagerChip.PINK, 1, WagerChip.WHITE, 2),
            WagerChip.getChipsByValue(4.50)
        );
    }

    @Test
    public void test750() {
        assertEquals(
            Map.of(WagerChip.RED, 1, WagerChip.PINK, 1),
            WagerChip.getChipsByValue(7.50)
        );
    }

    @Test
    public void test800() {
        assertEquals(
            Map.of(WagerChip.RED, 1, WagerChip.WHITE, 3),
            WagerChip.getChipsByValue(8.00)
        );
    }

    @Test
    public void test16500() {
        assertEquals(
            Map.of(WagerChip.BLACK, 1, WagerChip.GREEN, 2, WagerChip.RED, 3),
            WagerChip.getChipsByValue(165.00)
        );
    }

    @Test
    public void test16750() {
        assertEquals(
            Map.of(WagerChip.BLACK, 1, WagerChip.GREEN, 2, WagerChip.RED, 3, WagerChip.PINK, 1),
            WagerChip.getChipsByValue(167.50)
        );
    }
}
