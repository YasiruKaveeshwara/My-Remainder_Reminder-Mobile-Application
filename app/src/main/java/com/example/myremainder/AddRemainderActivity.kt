package com.example.myremainder

import android.app.*
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myremainder.databinding.ActivityAddRemainderBinding
import java.text.SimpleDateFormat
import java.util.*
import android.app.AlarmManager
import android.app.PendingIntent
import android.os.Build
import android.provider.Settings
import android.net.Uri

class AddRemainderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddRemainderBinding
    private lateinit var db: RemainderDbHelper
    private var selectedDate: String = ""
    private var selectedTime: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_remainder)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val meridianValues = arrayOf("AM", "PM")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, meridianValues)

        binding = ActivityAddRemainderBinding.inflate(layoutInflater)
        binding.meridianSpinner.adapter = adapter


        setContentView(binding.root)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        db = RemainderDbHelper(this)

        binding.dateEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                selectedDate = "${selectedDay}/${selectedMonth + 1}/$selectedYear"
                binding.dateEditText.setText(selectedDate)
            }, year, month, day)

            datePickerDialog.datePicker.minDate = System.currentTimeMillis()
            datePickerDialog.show()
        }


        binding.timeEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                selectedTime = "$selectedHour:$selectedMinute"
                binding.timeEditText.setText(selectedTime)
            }, hour, minute, true).show()
        }


        binding.repeatEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString()
                if (input.isNotEmpty()) {
                    try {
                        val value = input.toInt()
                        if (value < 1 || value > 5) {
                            // If the value is less than 1, reset it to 1
                            binding.repeatEditText.setText("1")
                        }
                    } catch (e: NumberFormatException) {
                        // If the input is not a number, reset it to 1
                        binding.repeatEditText.setText("1")
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed here
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No action needed here
            }
        })


        binding.saveButton.setOnClickListener {
            val title = binding.titleEditText.text.toString()
            val content = binding.contentEditText.text.toString()
            val time = binding.timeEditText.text.toString()
            val date = binding.dateEditText.text.toString()
            val meridian = binding.meridianSpinner.selectedItem.toString()
            val repeat = binding.repeatEditText.text.toString()
            val active = binding.activeEditStatus.isChecked.toString()
            val remainderId = db.insertRemainder(title, content, time, date, meridian, repeat, active)
            setAlarm(remainderId, title, content, date, time, meridian, active, repeat.toInt())
            finish()
            Toast.makeText(this, "Remainder Saved", Toast.LENGTH_SHORT).show()
        }

        binding.addBackButton.setOnClickListener {
            if (hasChanges()) {
                AlertDialog.Builder(this)
                    .setTitle("Save changes?")
                    .setMessage("You have unsaved changes. Do you want to save them?")
                    .setPositiveButton("OK") { _, _ ->
                        // Save current details
                        saveDetails()
                        // Navigate back
                        finish()
                    }
                    .setNegativeButton("Cancel") { _, _ ->
                        // Navigate back without saving
                        finish()
                    }
                    .show()
            } else {
                // Navigate back without saving
                finish()
            }
        }
    }
    private fun setAlarm(id: Long, title: String, content: String, date: String, time: String, meridian: String, active: String, repeat:Int) {
        if (active == "true") {
            val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
            val dateInString = "$date $time $meridian"
            val date = sdf.parse(dateInString)
            val calendar = Calendar.getInstance()
            calendar.time = date!!

            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                // If the app cannot schedule exact alarms, show the system settings screen where the user can enable this option
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName"))
                startActivity(intent)
            } else {
                for (i in 0 until repeat) {
                    val intent = Intent(this, RemainderAlarmReceiver::class.java).apply {
                        putExtra("title", title)
                        putExtra("content", content)
                        putExtra("id", id.toInt())
                    }

                    val pendingIntent = PendingIntent.getBroadcast(this, id.toInt() + i, intent, PendingIntent.FLAG_UPDATE_CURRENT)

                    // Set the alarm to go off after i * 5 seconds
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis + i * 5000, pendingIntent)
                }
            }
        }
    }

    private fun hasChanges(): Boolean {
        // Check if any field has been modified
        return binding.titleEditText.text.isNotEmpty() ||
                binding.contentEditText.text.isNotEmpty() ||
                binding.timeEditText.text.isNotEmpty() ||
                binding.dateEditText.text.isNotEmpty() ||
                binding.repeatEditText.text.isNotEmpty() ||
                binding.activeEditStatus.isChecked
    }

    private fun saveDetails() {
        val title = binding.titleEditText.text.toString()
        val content = binding.contentEditText.text.toString()
        val time = binding.timeEditText.text.toString()
        val date = binding.dateEditText.text.toString()
        val meridian = binding.meridianSpinner.selectedItem.toString()
        val repeat = binding.repeatEditText.text.toString()
        val active = binding.activeEditStatus.isChecked.toString()
        val remainderId = db.insertRemainder(title, content, time, date, meridian, repeat, active)
        setAlarm(remainderId, title, content, date, time, meridian, active, repeat.toInt())
    }

}