package com.sergeenko.alexey.globars.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import com.sergeenko.alexey.globars.R
import com.sergeenko.alexey.globars.api.GlobarsApiService
import com.sergeenko.alexey.globars.dataClasses.LogResult
import com.sergeenko.alexey.globars.dataClasses.User
import kotlinx.android.synthetic.main.auth_layout.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.auth_layout)
        password_text.setOnEditorActionListener(OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                enter_button.isFocusableInTouchMode = true
                enter_button.requestFocus()
                enter_button.isFocusableInTouchMode = false
                return@OnEditorActionListener true
            }
            false
        })
    }

    fun goToSecondScreen(view: View){
        CoroutineScope(IO).launch {
            val call = retrofit.create(GlobarsApiService::class.java).auth(User(name_text.text.toString().trim(), password_text.text.toString().trim()))
            call.enqueue(object : Callback<LogResult> {
                override fun onResponse(call: Call<LogResult>, response: Response<LogResult>) {
                    response.body()?.let { result ->
                        if (response.code() == 200) {
                            if (result.success) {
                                manageSuccess(result.data)
                            } else {
                                manageError(result.data)
                            }
                        } else {
                            manageError(response.code().toString())
                        }
                    }
                }

                override fun onFailure(call: Call<LogResult>, t: Throwable) {
                    manageError("не удалось подключиться")
                }
            })

        }
    }

    private fun manageError(success: String) {
        Toast.makeText(
            this@MainActivity,
            "Ошибка: $success",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun manageSuccess(data: String) {
        serviceInterceptor.token = data
        startActivity(
            Intent(this@MainActivity, MapActivity::class.java)
        )
        finish()
    }
}