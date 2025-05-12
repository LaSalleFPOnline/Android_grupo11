package com.example.educontrol.api

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private const val BASE_URL = "http://192.168.1.133:8080/" // 🔹 Reemplaza con la URL real de la API
    //private const val BASE_URL = "http://79.143.91.200:8080/" // 🔹 Reemplaza con la URL real de la API

    init {
        Log.d("educontrol", "📡 Inicializando Retrofit con URL base: $BASE_URL")
    }

    private val client = OkHttpClient.Builder().apply {
        addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        addInterceptor { chain ->
            val request = chain.request()
            Log.d("educontrol", "➡️ Enviando petición: ${request.method} ${request.url}")
            val response = chain.proceed(request)
            Log.d("educontrol", "⬅️ Respuesta recibida: ${response.code}")
            response
        }
    }.build()

    val api: ApiService by lazy {
        Log.d("educontrol", "🔹 Creando instancia de APIService")
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(ApiService::class.java)
    }
}
