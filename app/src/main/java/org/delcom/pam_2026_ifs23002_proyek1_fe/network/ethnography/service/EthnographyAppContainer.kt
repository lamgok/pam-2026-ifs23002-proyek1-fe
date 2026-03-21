package org.delcom.pam_2026_ifs23002_proyek1_fe.network.ethnography.service

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import org.delcom.pam_2026_ifs23002_proyek1_fe.BuildConfig
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class EthnographyAppContainer: IEthnographyAppContainer {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private fun getUnsafeOkHttpClient(): OkHttpClient {
        return try {
            // Create a trust manager that does not validate certificate chains
            val trustAllCerts = arrayOf<TrustManager>(
                object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                }
            )

            // Install the all-trusting trust manager
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())
            
            // Create an ssl socket factory with our all-trusting manager
            val sslSocketFactory = sslContext.socketFactory

            OkHttpClient.Builder().apply {
                sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                hostnameVerifier { _, _ -> true }
                
                if (BuildConfig.DEBUG) {
                    addInterceptor(loggingInterceptor)
                }

                connectTimeout(2, TimeUnit.MINUTES)
                readTimeout(2, TimeUnit.MINUTES)
                writeTimeout(2, TimeUnit.MINUTES)
            }.build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    val okHttpClient = getUnsafeOkHttpClient()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .build()

    private val retrofitService: EthnographyApiService by lazy {
        retrofit.create(EthnographyApiService::class.java)
    }

    override val repository: IEthnographyRepository by lazy {
        EthnographyRepository(retrofitService)
    }
}
