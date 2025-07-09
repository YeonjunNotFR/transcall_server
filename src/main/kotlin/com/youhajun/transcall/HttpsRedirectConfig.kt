package com.youhajun.transcall

import org.apache.catalina.Context
import org.apache.catalina.connector.Connector
import org.apache.tomcat.util.descriptor.web.SecurityCollection
import org.apache.tomcat.util.descriptor.web.SecurityConstraint
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory
import org.springframework.boot.web.servlet.server.ServletWebServerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class HttpsRedirectConfig {

    @Bean
    fun servletContainer(): ServletWebServerFactory {
        return object : TomcatServletWebServerFactory() {
            override fun postProcessContext(context: Context) {
                val collection = SecurityCollection().apply {
                    addPattern("/*")
                }

                val securityConstraint = SecurityConstraint().apply {
                    userConstraint = "CONFIDENTIAL"
                    addCollection(collection)
                }

                context.addConstraint(securityConstraint)
            }
        }.apply {
            addAdditionalTomcatConnectors(httpToHttpsRedirectConnector())
        }
    }

    private fun httpToHttpsRedirectConnector(): Connector {
        return Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL).apply {
            scheme = "http"
            port = 8080
            secure = false
            redirectPort = 8443
        }
    }
}