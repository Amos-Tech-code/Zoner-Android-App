package com.zoner.data.remote

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import retrofit2.Invocation
import java.util.concurrent.TimeUnit

class TimeoutInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val timeout = getTimeoutFromAnnotation(request)

        return chain.withTimeouts(timeout).proceed(request)
    }

    private fun getTimeoutFromAnnotation(request: Request): TimeoutConfig {
        // Get the method annotation from the request tag
        val annotation = request.tag(Invocation::class.java)
            ?.method()
            ?.getAnnotation(DynamicTimeout::class.java)

        return if (annotation != null) {
            TimeoutConfig(annotation.connect, annotation.read, annotation.write)
        } else {
            TimeoutConfig(60, 60, 60) // Default timeouts
        }
    }

    private fun Interceptor.Chain.withTimeouts(config: TimeoutConfig): Interceptor.Chain {
        return this.withConnectTimeout(config.connect, TimeUnit.SECONDS)
            .withReadTimeout(config.read, TimeUnit.SECONDS)
            .withWriteTimeout(config.write, TimeUnit.SECONDS)
    }

    data class TimeoutConfig(
        val connect: Int,
        val read: Int,
        val write: Int
    )
}