package ru.itmo.client

@OptIn(ExperimentalStdlibApi::class)
actual fun HttpClient(): HttpClient = JvmHttpClient()
