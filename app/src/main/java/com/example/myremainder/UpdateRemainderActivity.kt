package com.example.myremainder

import android.app.*
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myremainder.databinding.ActivityUpdateRemainderBinding
import java.text.SimpleDateFormat
import java.util.*
import android.app.AlarmManager
import android.app.PendingIntent
import android.os.Build
import android.provider.Settings
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher

class UpdateRemainderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateRemainderBinding
    private lateinit var db: RemainderDbHelper
    private var id: Long = -1
    private var selectedDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_update_remainder)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = RemainderDbHelper(this)
        id = intent.getIntExtra("id", -1).toLong() // Retrieve the ID as an Int

        if (id == -1L) {
            finish()
            return
        }

        val remainder = db.getRemainderById(id.toInt())
        val meridianValues = arrayOf("AM", "PM")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, meridianValues)
        val meridianIndex = adapter.getPosition(remainder.meridian)

        binding = ActivityUpdateRemainderBinding.inflate(layoutInflater)
        binding.editTitleEditText.setText(remainder.title)
        binding.editContentEditText.setText(remainder.content)
        binding.editDateEditText.setText(remainder.date)
        binding.editTimeEditText.setText(remainder.time)
        binding.editRepeatEditText.setText(remainder.repeat)
        binding.editMeridianSpinner.adapter = adapter
        binding.editMeridianSpinner.setSelection(meridianIndex)
        binding.activeEditStatus.isChecked = remainder.active == "true"

        setContentView(binding.root)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.editRepeatEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString()
                if (input.isNotEmpty()) {
                    try {
                        val value = input.toInt()
                        if (value < 1 || value > 5) {
                            // If the value is less than 1, reset it to 1
                            binding.editRepeatEditText.setText("1")
                        }
                    } catch (e: NumberFormatException) {
                        // If the input is not a number, reset it to 1
                        binding.editRepeatEditText.setText("1")
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.editDateEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                selectedDate = "${selectedDay}/${selectedMonth + 1}/$selectedYear"
                binding.editDateEditText.setText(selectedDate)
            }, year, month, day)

            datePickerDialog.datePicker.minDate = System.currentTimeMillis()
            datePickerDialog.show()
        }

        binding.editTimeEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                val selectedTime = "$selectedHour:$selectedMinute"
                binding.editTimeEditText.text = selectedTime
            }, hour, minute, true).show()
        }

        binding.editSaveButton.setOnClickListener {
            val title = binding.editTitleEditText.text.toString()
            val content = binding.editContentEditText.text.toString()
            val time = binding.editTimeEditText.text.toString()
            val date = binding.editDateEditText.text.toString()
            val meridian = binding.editMeridianSpinner.selectedItem.toString()
            val repeat = binding.editRepeatEditText.text.toString()
            val active = binding.activeEditStatus.isChecked.toString()
            val updateRemainder = Remainder(id.toInt(), title, content, time, date, meridian, repeat, active)
            setAlarm(id, title, content, date, time, meridian, active, repeat.toInt())
            db.updateRemainder(updateRemainder)
            finish()

            Toast.makeText(this, "Remainder updated", Toast.LENGTH_SHORT).show()
        }

        binding.updateBackButton.setOnClickListener {
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

    private fun setAlarm(id: Long, title: String, content: String, date: String, time: String, meridian: String, active: String, repeat: Int) {
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
        val remainder = db.getRemainderById(id.toInt())
        return remainder.title != binding.editTitleEditText.text.toString() ||
                remainder.content != binding.editContentEditText.text.toString() ||
                remainder.time != binding.editTimeEditText.text.toString() ||
                remainder.date != binding.editDateEditText.text.toString() ||
                remainder.meridian != binding.editMeridianSpinner.selectedItem.toString() ||
                remainder.repeat != binding.editRepeatEditText.text.toString() ||
                remainder.active != binding.activeEditStatus.isChecked.toString()
    }

    private fun saveDetails() {
        val title = binding.editTitleEditText.text.toString()
        val content = binding.editContentEditText.text.toString()
        val time = binding.editTimeEditText.text.toString()
        val date = binding.editDateEditText.text.toString()
        val meridian = binding.editMeridianSpinner.selectedItem.toString()
        val repeat = binding.editRepeatEditText.text.toString()
        val active = binding.activeEditStatus.isChecked.toString()
        val updateRemainder = Remainder(id.toInt(), title, content, time, date, meridian, repeat, active)
        db.updateRemainder(updateRemainder)
    }
}