import com.gigamind.cognify.util.GameConfig;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GameConfigTest {

    @Test
    void testWordGameSettings() {
        assertEquals(4, GameConfig.GRID_SIZE);
        assertEquals(16, GameConfig.TOTAL_LETTERS);
        assertTrue(GameConfig.MIN_WORD_LENGTH >= 3);
    }

    @Test
    void testScoringConstants() {
        assertTrue(GameConfig.BASE_SCORE > 0);
        assertTrue(GameConfig.LENGTH_BONUS > 0);
        assertTrue(GameConfig.COMPLEXITY_BONUS > 0);
    }
}
