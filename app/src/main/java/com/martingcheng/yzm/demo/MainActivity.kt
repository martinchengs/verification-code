package com.martingcheng.yzm.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import com.martingcheng.yzm.VerificationCodeView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val view = findViewById<VerificationCodeView>(R.id.codeView)
        val view2 = findViewById<VerificationCodeView>(R.id.password_view)
        view2.setPasswordTxt("*")
        view2.addOnVerificationCodeChangeListener(object :
            VerificationCodeView.OnVerificationCodeWatcher {
            override fun onTextChanged(
                text: CharSequence?,
                start: Int,
                lengthBefore: Int,
                lengthAfter: Int
            ) = Unit

            override fun onTextCompleted(text: CharSequence?) {
                view2.setPasswordStyle(false)
            }

        })
        view.addOnVerificationCodeChangeListener(object :
            VerificationCodeView.OnVerificationCodeWatcher {
            override fun onTextChanged(
                text: CharSequence?,
                start: Int,
                lengthBefore: Int,
                lengthAfter: Int
            ) {
                Log.d(
                    "VerificationCodeView",
                    "onTextChanged() called with: text = $text, start = $start, lengthBefore = $lengthBefore, lengthAfter = $lengthAfter"
                )
            }

            override fun onTextCompleted(text: CharSequence?) {
                text?.let { Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT).show() }
            }

        })

    }
}