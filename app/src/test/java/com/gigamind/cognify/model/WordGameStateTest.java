import com.gigamind.cognify.model.WordGameState;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WordGameStateTest {

    @Test
    void testBuilderSetsAllFieldsAndIsImmutable() {
        List<String> words = new ArrayList<>(Arrays.asList("HELLO", "WORLD"));
        char[] letters = new char[] {'A','B','C','D'};

        WordGameState state = new WordGameState.Builder()
                .score(42)
                .timeRemaining(1234L)
                .currentWord("TEST")
                .foundWords(words)
                .letters(letters)
                .isGameActive(true)
                .build();

        // modify originals after building
        words.add("MUTATE");
        letters[0] = 'Z';

        // verify stored values remain unchanged
        assertEquals(42, state.getScore());
        assertEquals(1234L, state.getTimeRemaining());
        assertEquals("TEST", state.getCurrentWord());
        assertEquals(Arrays.asList("HELLO", "WORLD"), state.getFoundWords());
        assertArrayEquals(new char[] {'A','B','C','D'}, state.getLetters());
        assertTrue(state.isGameActive());
    }

    @Test
    void testBuilderDefaultValues() {
        WordGameState state = new WordGameState.Builder().build();
        assertEquals(0, state.getScore());
        assertEquals(0L, state.getTimeRemaining());
        assertEquals("", state.getCurrentWord());
        assertTrue(state.getFoundWords().isEmpty());
        assertEquals(16, state.getLetters().length);
        assertFalse(state.isGameActive());
    }
}
