package com.riffdeck.player.data.remote.listenbrainz

import com.riffdeck.player.BuildConfig
import com.riffdeck.player.data.remote.listenbrainz.model.ListenBrainzResponse
import com.riffdeck.player.data.remote.listenbrainz.model.ListenBrainzSubmission
import com.riffdeck.player.data.remote.listenbrainz.model.ListenBrainzTokenValidationResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.userAgent

class ListenBrainzService(private val client: HttpClient) {

    suspend fun validateToken(token: String): ListenBrainzTokenValidationResponse {
        return client.get("${BASE_URL}validate-token") {
            userAgent(USER_AGENT)
            parameter("token", token)
        }.body()
    }

    suspend fun submitListen(token: String, submission: ListenBrainzSubmission): ListenBrainzResponse {
        val response = client.post("${BASE_URL}submit-listens") {
            userAgent(USER_AGENT)
            header("Authorization", "Token $token")
            contentType(ContentType.Application.Json)
            setBody(submission)
        }
        return response.body()
    }

    companion object {
        private const val BASE_URL = "https://api.listenbrainz.org/1/"
        private const val USER_AGENT = "RiffDeck/${BuildConfig.VERSION_NAME} (https://github.com/Nazatric/Riff-Deck-)"
    }
}
