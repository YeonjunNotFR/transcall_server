package com.youhajun.transcall.auth.google

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient

@Service
class GoogleService(
    private val googleAuthConfig: GoogleAuthConfig,
    webClientBuilder: WebClient.Builder
) {
    companion object {
        private const val TOKEN_URL = "https://oauth2.googleapis.com/token"
        private const val USER_INFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo"
    }

    private val webClient: WebClient = webClientBuilder.build()

    suspend fun fetchToken(authCode: String): GoogleTokenResponse {
        val body = BodyInserters.fromFormData("code", authCode)
            .with("client_id", googleAuthConfig.clientId)
            .with("client_secret", googleAuthConfig.clientSecret)
            .with("redirect_uri", googleAuthConfig.redirectUri)
            .with("grant_type", "authorization_code")

        return webClient.post()
            .uri(TOKEN_URL)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(body)
            .retrieve()
            .bodyToMono(GoogleTokenResponse::class.java)
            .awaitSingle()
    }

    suspend fun fetchUserInfo(accessToken: String): GoogleUserInfo {
        return webClient.get()
            .uri(USER_INFO_URL)
            .headers { it.setBearerAuth(accessToken) }
            .retrieve()
            .bodyToMono(GoogleUserInfo::class.java)
            .awaitSingle()
    }

    fun verifyClientToken(idToken: String, nonce: String): GoogleIdToken? {
        val transport = GoogleNetHttpTransport.newTrustedTransport()
        val jsonFactory = GsonFactory()
        val verifier = GoogleIdTokenVerifier.Builder(transport, jsonFactory)
            .setAudience(listOf(googleAuthConfig.clientId))
            .build()

        val verifiedIdToken = verifier.verify(idToken) ?: return null
        val payloadNonce = verifiedIdToken.payload.nonce
        return if(payloadNonce == nonce) verifiedIdToken else null
    }
}