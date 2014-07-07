import io.dropwizard.testing.junit.DatabaseMigrationRule;
import org.junit.Rule;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.fail;

public class DatabaseMigrationRuleTests {

    private static final String DB_URL = "jdbc:h2:mem:DbTest-" + System.currentTimeMillis();
    private static final String DB_USER = "sa";
    private static final String DB_PASS = "";

    @Rule
    public DatabaseMigrationRule databaseMigrationRule = new DatabaseMigrationRule(DB_URL, DB_USER, DB_PASS);

    @Test
    public void migrationsShouldRunBeforeTest() throws Exception {
        try {
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER,  DB_PASS);
            Statement statement = connection.createStatement();
            statement.executeQuery("SELECT 1 FROM People LIMIT 1;");
        } catch (SQLException e) {
            fail("No exception should be thrown if migrations executed.");
        }
    }
}
