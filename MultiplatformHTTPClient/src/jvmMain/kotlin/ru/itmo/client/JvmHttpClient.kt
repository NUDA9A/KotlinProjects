package ru.itmo.client


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URI
import java.net.http.HttpResponse as JHttpResponse
import java.net.http.HttpClient as JHttpClient
import java.net.http.HttpRequest as JHttpRequest

class JvmHttpClient : HttpClient {
    private val client = JHttpClient.newBuilder().build()
    override suspend fun request(method: HttpMethod, request: HttpRequest): HttpResponse {
        return withContext(Dispatchers.IO) {
            val requestBuilder = JHttpRequest.newBuilder().uri(URI.create(request.url))

            request.headers.value.forEach { requestBuilder.headers(it.key, it.value) }

            when (method) {
                HttpMethod.GET -> requestBuilder.GET()
                HttpMethod.POST -> requestBuilder.POST(
                        request.body?.let { JHttpRequest.BodyPublishers.ofByteArray(it) }
                )
                HttpMethod.PUT -> requestBuilder.PUT(
                        request.body?.let { JHttpRequest.BodyPublishers.ofByteArray(it) }
                    )
                HttpMethod.DELETE -> requestBuilder.DELETE()
            }

            val jRequest = requestBuilder.build()

            val response = client.send(jRequest, JHttpResponse.BodyHandlers.ofString())

            HttpResponse(
                HttpStatus(response.statusCode()),
                HttpHeaders(convertHeaders(response.headers().map())),
                response.body().toByteArray()
            )
        }
    }

    override fun close() {
        return
    }
}

private fun convertHeaders(originalHeaders: Map<String, List<String>>): Map<String, String> {
    return originalHeaders.mapValues { (_, value) ->
        value.joinToString(", ")
    }
}
