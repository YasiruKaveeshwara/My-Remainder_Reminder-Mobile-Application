package com.example.myremainder

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myremainder.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val screenWidth = resources.displayMetrics.widthPixels

        val pic1Animator = ObjectAnimator.ofFloat(binding.pic1, "translationX", screenWidth.toFloat(), 0f)
        val pic2Animator = ObjectAnimator.ofFloat(binding.pic2, "translationX", screenWidth.toFloat(), 0f)
        val pic3Animator = ObjectAnimator.ofFloat(binding.pic3, "translationX", -screenWidth.toFloat(), 0f)

        pic1Animator.duration = 1000
        pic2Animator.duration = 1000
        pic3Animator.duration = 1000

        pic1Animator.start()
        pic2Animator.start()
        pic3Animator.start()

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000)
    }
}