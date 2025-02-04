package io.micronaut.configuration.clickhouse;

import io.micronaut.context.ApplicationContext;
import io.micronaut.runtime.exceptions.ApplicationStartupException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.ClickHouseContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.yandex.clickhouse.ClickHouseConnection;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 23.3.2020
 */
@Testcontainers
class ClickHouseDatabaseInitializationTests extends ClickhouseRunner {

    @Container
    private final ClickHouseContainer container = getContainer();

    @Test
    void databaseInitializedWhenContextCreated() throws Exception {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("clickhouse.port", container.getMappedPort(ClickHouseContainer.HTTP_PORT));
        properties.put("clickhouse.database", "custom");
        properties.put("clickhouse.create-database-if-not-exist", true);

        final ApplicationContext context = ApplicationContext.run(properties);
        final ClickHouseConnection connection = context.getBean(ClickHouseConnection.class);

        final String version = connection.getServerVersion();
        assertEquals(getClickhouseVersion(), version);

        assertTrue(connection.createStatement().execute(container.getTestQueryString()));

        connection.createStatement().execute("CREATE TABLE custom.example(" +
                " name String," +
                " registered DateTime " +
                ") ENGINE = MergeTree() " +
                " ORDER BY registered;");

        assertTrue(connection.createStatement().execute("SELECT * FROM custom.example"));
    }

    @Test
    void databaseDefaultInitializeSkip() throws Exception {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("clickhouse.port", container.getMappedPort(ClickHouseContainer.HTTP_PORT));
        properties.put("clickhouse.database", ClickHouseSettings.DEFAULT_DATABASE);
        properties.put("clickhouse.create-database-if-not-exist", true);

        final ApplicationContext context = ApplicationContext.run(properties);
        final ClickHouseConnection connection = context.getBean(ClickHouseConnection.class);

        final String version = connection.getServerVersion();
        assertEquals(getClickhouseVersion(), version);

        assertTrue(connection.createStatement().execute(container.getTestQueryString()));

        connection.createStatement().execute("CREATE TABLE default.example(" +
                " name String," +
                " registered DateTime " +
                ") ENGINE = MergeTree() " +
                " ORDER BY registered;");

        assertTrue(connection.createStatement().execute("SELECT * FROM default.example"));
    }

    @Test
    void databaseCreationIsOff() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("clickhouse.port", container.getMappedPort(ClickHouseContainer.HTTP_PORT));
        properties.put("clickhouse.database", ClickHouseSettings.DEFAULT_DATABASE);
        properties.put("clickhouse.create-database-if-not-exist", false);

        final ApplicationContext context = ApplicationContext.run(properties);
        final ClickHouseConnection connection = context.getBean(ClickHouseConnection.class);

        try {
            connection.createStatement().execute(container.getTestQueryString());
        } catch (Exception e) {
            assertTrue(e.getCause().getCause() instanceof SQLException);
            assertEquals(81, ((SQLException) e.getCause().getCause()).getErrorCode());
        }
    }

    @Test
    void startUpForContextFailsOnTimeout() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("clickhouse.port", 7457);
        properties.put("clickhouse.database", "customos");
        properties.put("clickhouse.create-database-if-not-exist", true);
        properties.put("clickhouse.create-database-timeout", "1ms");

        try {
            ApplicationContext.run(properties);
            fail("Should not happen!");
        } catch (Exception e) {
            assertTrue(e.getCause().getCause() instanceof ApplicationStartupException);
        }
    }

    @Test
    void startUpForContextFailsOnConnection() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("clickhouse.port", 7459);
        properties.put("clickhouse.socket-timeout", 1000);
        properties.put("clickhouse.connection-timeout", 1000);
        properties.put("clickhouse.heath.retry", 1);
        properties.put("clickhouse.database", "customos");
        properties.put("clickhouse.create-database-if-not-exist", true);
        properties.put("clickhouse.create-database-timeout", "10000ms");

        try {
            ApplicationContext.run(properties);
            fail("Should not happen!");
        } catch (Exception e) {
            assertTrue(e.getCause().getCause() instanceof ApplicationStartupException);
        }
    }
}
