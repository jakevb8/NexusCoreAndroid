package me.jakev.nexuscore.di

import com.google.firebase.auth.FirebaseAuth
import me.jakev.nexuscore.data.api.BackendPreference
import me.jakev.nexuscore.data.api.NexusApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideOkHttp(
        auth: FirebaseAuth,
        backendPreference: BackendPreference
    ): OkHttpClient = OkHttpClient.Builder()
        // Dynamically rewrite the base URL on every request so that changing
        // the backend chip takes effect immediately without recreating Retrofit.
        .addInterceptor { chain ->
            val baseUrl = runBlocking { backendPreference.get().baseUrl }.toHttpUrl()
            val original = chain.request()
            // Only rewrite scheme, host, and port — the path (including /api/v1/...)
            // is already correct because the placeholder base URL has the same
            // /api/v1/ prefix as the real backends.
            val newUrl = original.url.newBuilder()
                .scheme(baseUrl.scheme)
                .host(baseUrl.host)
                .port(baseUrl.port)
                .build()
            val newRequest = original.newBuilder().url(newUrl).build()
            chain.proceed(newRequest)
        }
        .addInterceptor { chain ->
            val token = runBlocking {
                auth.currentUser?.getIdToken(false)?.result?.token
            }
            val request = chain.request().newBuilder().apply {
                if (token != null) addHeader("Authorization", "Bearer $token")
            }.build()
            chain.proceed(request)
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        client: OkHttpClient,
        moshi: Moshi
    ): Retrofit {
        // Base URL is a placeholder — the OkHttp interceptor rewrites the host
        // and path dynamically on every request based on BackendPreference.
        // This allows the backend chip to take effect immediately without
        // recreating the Retrofit singleton.
        return Retrofit.Builder()
            .baseUrl("https://placeholder.invalid/api/v1/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideApi(retrofit: Retrofit): NexusApi = retrofit.create(NexusApi::class.java)
}
