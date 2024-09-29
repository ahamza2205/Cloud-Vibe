package com.example.cloudvibe.utils


import com.example.cloudvibe.model.network.WeatherApiService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"
    private const val API_KEY =  "7af08d0e1d543aea9b340405ceed1c3d" // Replace with your actual API key

    private val interceptor = Interceptor { chain ->
        val original = chain.request()
        val originalUrl = original.url

        // Add API key to every request
        val url = originalUrl.newBuilder()
            .addQueryParameter("appid", API_KEY)
            .build()

        val requestBuilder = original.newBuilder().url(url)
        val request = requestBuilder.build()
        chain.proceed(request)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .build()

    val api: WeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)  // Attach the client with interceptor
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }
}
