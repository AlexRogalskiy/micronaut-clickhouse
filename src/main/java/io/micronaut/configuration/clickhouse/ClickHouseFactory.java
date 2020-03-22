package io.micronaut.configuration.clickhouse;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Requires;
import io.micronaut.runtime.context.scope.Refreshable;
import ru.yandex.clickhouse.ClickHouseConnection;

import javax.inject.Singleton;

/**
 * Default factory for creating Official ClickHouse client {@link ClickHouseConnection}.
 *
 * @author Anton Kurako (GoodforGod)
 * @since 11.3.2020
 */
@Requires(beans = ClickHouseConfiguration.class)
@Factory
public class ClickHouseFactory {

    @Refreshable(ClickHouseSettings.PREFIX)
    @Bean(preDestroy = "close")
    @Singleton
    @Primary
    public ClickHouseConnection getConnection(ClickHouseConfiguration configuration) {
        return null;
    }
}
