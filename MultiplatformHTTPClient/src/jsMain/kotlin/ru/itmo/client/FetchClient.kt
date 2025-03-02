package ru.itmo.client


import kotlinx.browser.window
import kotlinx.coroutines.await
import org.khronos.webgl.Int8Array
import org.w3c.fetch.Headers
import org.w3c.fetch.RequestInit
import org.w3c.fetch.Response
import kotlin.js.Promise

class JsHttpClient : HttpClient {
    override fun close() {
        return
    }

    override suspend fun request(method: HttpMethod, request: HttpRequest): HttpResponse {
        val jsMethod = getMethod(method)

        val headers = Headers().also { header ->
            request.headers.value.forEach {
                header.append(it.key, it.value)
            }
        }

        val response = getResponse(request.url, jsMethod, headers, request.body)
        val resHeaders = response.headers.unsafeCast<Array<Array<String>>>()

        val headersMap = mutableMapOf<String, String>()

        for (entry in resHeaders) {
            headersMap[entry[0]] = entry[1]
        }

        return HttpResponse(
            HttpStatus(response.status.toInt()),
            HttpHeaders(headersMap),
            response.text().await().encodeToByteArray(),
        )
    }

}

private fun getMethod(method: HttpMethod): String {
    return when (method) {
        HttpMethod.GET -> "GET"
        HttpMethod.POST -> "POST"
        HttpMethod.PUT -> "PUT"
        HttpMethod.DELETE -> "DELETE"
    }
}

private suspend fun getResponse(url: String, method: String, headers: Headers, body: ByteArray?): Response {
    val requestInit = RequestInit(
        method,
        headers,
        body?.let { Int8Array(it.toTypedArray()) }
    )
    return when (platform) {
        Platform.Node -> fetch(url, requestInit
            .asNodeOptions()
            .unsafeCast<RequestInit>()
        )
        Platform.Browser -> fetch(url, requestInit)
    }
}

private suspend fun fetch(url: String, req: RequestInit): Response {
    return when (platform) {
        Platform.Node -> nodeFetch(url, req).unsafeCast<Promise<Response>>().await()
        Platform.Browser -> window.fetch(url, req).await()
    }
}

private enum class Platform { Node, Browser }

private val platform: Platform
    get() {
        val hasNodeApi = js(
            """
            (typeof process !== 'undefined' 
                && process.versions != null 
                && process.versions.node != null) ||
            (typeof window !== 'undefined' 
                && typeof window.process !== 'undefined' 
                && window.process.versions != null 
                && window.process.versions.node != null)
            """
        ) as Boolean
        return if (hasNodeApi) Platform.Node else Platform.Browser
    }

private val nodeFetch: dynamic
    get() = js("eval('require')('node-fetch')")

private fun RequestInit.asNodeOptions(): dynamic =
    js("Object").assign(js("Object").create(null), this)
