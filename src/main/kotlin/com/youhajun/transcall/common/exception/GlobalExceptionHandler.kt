package com.youhajun.transcall.common.exception

import com.youhajun.transcall.common.dto.ErrorResponse
import org.springframework.boot.autoconfigure.web.WebProperties
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler
import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.reactive.error.ErrorAttributes
import org.springframework.context.ApplicationContext
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import reactor.core.publisher.Mono

@Component
@Order(-2)
class GlobalExceptionHandler(
    errorAttributes: ErrorAttributes,
    applicationContext: ApplicationContext,
    codecConfigurer: ServerCodecConfigurer
) : AbstractErrorWebExceptionHandler(
    errorAttributes,
    WebProperties.Resources(),
    applicationContext
) {

    init {
        super.setMessageWriters(codecConfigurer.writers)
        super.setMessageReaders(codecConfigurer.readers)
    }

    override fun getRoutingFunction(errorAttributes: ErrorAttributes): RouterFunction<ServerResponse> {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse)
    }

    private fun renderErrorResponse(request: ServerRequest): Mono<ServerResponse> {
        val throwable = getError(request)
        val attrs = getErrorAttributes(request, ErrorAttributeOptions.defaults())

        val (status, body) = when (throwable) {
            is TransCallException -> {
                val errorType = throwable.errorType
                errorType.httpStatus to ErrorResponse(
                    status = errorType.status,
                    message = throwable.message,
                    data = throwable.data,
                )
            }

            else -> {
                val defaultStatus = (attrs["status"] as? Int)
                    ?.let(HttpStatus::resolve)
                    ?: HttpStatus.INTERNAL_SERVER_ERROR

                defaultStatus to ErrorResponse(
                    status = defaultStatus.name,
                    message = attrs["message"] as? String ?: defaultStatus.reasonPhrase,
                )
            }
        }

        return ServerResponse
            .status(status)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
    }
}
