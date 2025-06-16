import com.gigamind.cognify.util.UserFields;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

public class UserFieldsTest {

    @ParameterizedTest
    @ValueSource(strings = {"WordDash", "QuickMath", "Chess", "quick_math"})
    void testLastGameScoreField(String type) {
        String expected = "last" + toCamelCase(type) + "Score";
        assertEquals(expected, UserFields.lastGameScoreField(type));
    }

    @ParameterizedTest
    @ValueSource(strings = {"WordDash", "QuickMath", "Chess", "quick_math"})
    void testTotalGameXpField(String type) {
        String expected = "total" + toCamelCase(type) + "Xp";
        assertEquals(expected, UserFields.totalGameXpField(type));
    }

    private String toCamelCase(String input) {
        String[] parts = input.split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            sb.append(Character.toUpperCase(p.charAt(0)))
              .append(p.substring(1));
        }
        return sb.toString();
    }
}
