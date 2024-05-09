package com.example.myremainder

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myremainder.databinding.ActivityUpdateRemainderBinding

class UpdateRemainderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateRemainderBinding
    private lateinit var db: RemainderDbHelper
    private var id: Int = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_update_remainder)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding = ActivityUpdateRemainderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = RemainderDbHelper(this)

        id = intent.getIntExtra("id", -1)
        if (id == -1) {
            finish()
            return
        }

        val remainder = db.getRemainderById(id)
        binding.editTitleEditText.setText(remainder.title)
        binding.editContentEditText.setText(remainder.content)
        binding.editDateEditText.setText(remainder.date)
        binding.editTimeEditText.setText(remainder.time)

        binding.editSaveButton.setOnClickListener {
            val title = binding.editTitleEditText.text.toString()
            val content = binding.editContentEditText.text.toString()
            val time = binding.editTimeEditText.text.toString()
            val date = binding.editDateEditText.text.toString()
            val repeat = binding.editRepeatEditText.text.toString()
            val active = binding.activeEditStatus.isChecked.toString()
            val updateRemainder = Remainder(id, title, content, time, date, repeat, active)

            db.updateRemainder(updateRemainder)
            finish()

            Toast.makeText(this, "Remainder updated", Toast.LENGTH_SHORT).show()
        }
    }
}