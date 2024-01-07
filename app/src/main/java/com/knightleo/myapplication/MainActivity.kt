package com.knightleo.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.knightleo.loadingButton.LoadingButton
import java.util.Timer
import java.util.TimerTask

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val loadingButton = findViewById<LoadingButton>(R.id.loadingButton)

        val d = ResourcesCompat.getDrawable(resources, R.drawable.ic_launcher_foreground, null)

        loadingButton.setOnClickListener {
            loadingButton.loading = true
            loadingButton.isEnabled = false
            Timer().schedule(
                object : TimerTask() {
                    override fun run() =
                        runOnUiThread {
                            loadingButton.isEnabled = true
                            loadingButton.loading = false
                        }
                },
                7000
            )
        }
    }
}