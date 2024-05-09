package com.example.myremainder

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myremainder.databinding.ActivityRemainderBinding

class RemainderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRemainderBinding
    private lateinit var db: RemainderDbHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_remainder)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding = ActivityRemainderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = RemainderDbHelper(this)

        binding.saveButton.setOnClickListener {
        val title = binding.titleEditText.text.toString()
        val content = binding.contentEditText.text.toString()
        val time = binding.timeEditText.text.toString()
        val date = binding.dateEditText.text.toString()
        val repeat = binding.repeatEditText.text.toString()
        val active = binding.activeEditStatus.isChecked.toString()
        db.insertRemainder(title, content, time, date, repeat, active)
        finish()
        Toast.makeText(this, "Remainder Saved", Toast.LENGTH_SHORT).show()
    }
    }
}