package com.sergeenko.alexey.globars.dagger

import android.app.Application
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.sergeenko.alexey.globars.activities.BaseActivity
import dagger.Component
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


 const val baseUrl = "https://test.globars.ru/api/"

@Module
class AppModule(private var mApplication: Application) {
    @Provides
    @Singleton
    fun provideApplication(): Application {
        return mApplication
    }
}

@Module
class NetModule(private var mBaseUrl: String) {

    @Provides
    @Singleton
    fun provideHttpCache(application: Application): Cache {
        val cacheSize: Long = 10 * 1024 * 1024
        return Cache(application.cacheDir, cacheSize)
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        return gsonBuilder.create()
    }

    @Provides
    @Singleton
    fun provideServiceInterceptor(): ServiceInterceptor {
        return ServiceInterceptor()
    }


    @Provides
    @Singleton
    fun provideOkhttpClient(cache: Cache?, serviceInterceptor: ServiceInterceptor): OkHttpClient {
        val clientBuilder = OkHttpClient.Builder()
        clientBuilder.cache(cache)
        clientBuilder.readTimeout(60, TimeUnit.SECONDS)
        clientBuilder.writeTimeout(60, TimeUnit.SECONDS)
        clientBuilder.connectTimeout(60, TimeUnit.SECONDS)
        clientBuilder.addInterceptor(serviceInterceptor)


        val logging = HttpLoggingInterceptor()
        logging.level = (HttpLoggingInterceptor.Level.BODY)

        clientBuilder.addInterceptor(logging)
        return clientBuilder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(gson: Gson, okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(mBaseUrl)
            .client(okHttpClient)
            .build()
    }
}

@Singleton
@Component(modules = [AppModule::class, NetModule::class])
interface NetComponent {
    fun inject(mainActivity: BaseActivity?)

    fun retrofit(): Retrofit

}

class App : Application() {
    var netComponent: NetComponent? = null

    override fun onCreate() {
        super.onCreate()
        netComponent = DaggerNetComponent.builder()
            .appModule(AppModule(this))
            .netModule(NetModule(baseUrl))
            .build()
    }
}

class ServiceInterceptor : Interceptor{

    var token : String = ""

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        if(request.header("No-Authentication") == null){
            if(token.isNotEmpty()) {
                val finalToken = "Bearer $token"
                request = request.newBuilder()
                    .addHeader("Authorization", finalToken)
                    .build()
            }
        }

        return chain.proceed(request)
    }

}
