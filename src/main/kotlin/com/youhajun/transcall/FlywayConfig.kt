package com.youhajun.transcall

import org.flywaydb.core.Flyway
import org.springframework.boot.autoconfigure.flyway.FlywayProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FlywayConfig {

    @Bean
    fun flyway(flywayProperties: FlywayProperties): Flyway {
        val flyway = Flyway.configure()
            .baselineOnMigrate(flywayProperties.isBaselineOnMigrate)
            .baselineVersion(flywayProperties.baselineVersion)
            .dataSource(flywayProperties.url, flywayProperties.user, flywayProperties.password)
            .load()

        flyway.migrate()
        return flyway
    }

    @Bean
    fun flywayProperties(): FlywayProperties {
        return FlywayProperties()
    }
}