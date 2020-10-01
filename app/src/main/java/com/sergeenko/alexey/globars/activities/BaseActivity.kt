package com.sergeenko.alexey.globars.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sergeenko.alexey.globars.dagger.App
import com.sergeenko.alexey.globars.dagger.ServiceInterceptor
import retrofit2.Retrofit
import javax.inject.Inject

open class BaseActivity: AppCompatActivity() {

    @Inject
    lateinit var retrofit: Retrofit
    @Inject
    lateinit var serviceInterceptor: ServiceInterceptor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as App).netComponent!!.inject(this)
    }

}
