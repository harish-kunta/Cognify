import com.gigamind.cognify.util.UserFields;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

public class UserFieldsTest {

    @ParameterizedTest
    @ValueSource(strings = {"WordDash", "QuickMath", "Chess"})
    void testLastGameScoreField(String type) {
        String expected = "last" + type + "Score";
        assertEquals(expected, UserFields.lastGameScoreField(type));
    }
}
