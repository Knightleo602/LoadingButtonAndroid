package com.knightleo.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.knightleo.loadingButton.LoadingButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val loadingButton = findViewById<LoadingButton>(R.id.loadingButton)

        loadingButton.setOnClickListener {
            loadingButton.loading = true
            loadingButton.isEnabled = false
//            Timer().schedule(
//                object : TimerTask() {
//                    override fun run() =
//                        runOnUiThread {
//                            loadingButton.isEnabled = true
//                            loadingButton.loading = false
//                        }
//                },
//                10_000
//            )
        }
    }
}