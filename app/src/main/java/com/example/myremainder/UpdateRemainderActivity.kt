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
import android.view.View
import android.widget.AdapterView

class UpdateRemainderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateRemainderBinding
    private lateinit var db: RemainderDbHelper
    private var id: Long = -1
    private var selectedDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityUpdateRemainderBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.editMeridianSpinner.adapter = adapter

        // Check if remainder is not null before accessing its properties
        if (remainder != null) {
            val meridianValues = arrayOf("AM", "PM")
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, meridianValues)
            val meridianIndex = adapter.getPosition(remainder.meridian)

            binding.editTitleEditText.setText(remainder.title)
            binding.editContentEditText.setText(remainder.content)
            binding.editDateEditText.setText(remainder.date)
            binding.editTimeEditText.setText(remainder.time)
            binding.editRepeatNumberPicker.minValue = 1
            binding.editRepeatNumberPicker.maxValue = 5
            binding.editRepeatNumberPicker.value = remainder.repeat.toInt()
            binding.editMeridianSpinner.adapter = adapter
            binding.editMeridianSpinner.setSelection(meridianIndex)
            binding.activeEditStatus.isChecked = remainder.active == "true"
        } else {
            // Handle the case where remainder is null
            // For example, you could show an error message and finish the activity
            Toast.makeText(this, "Error: Remainder not found", Toast.LENGTH_SHORT).show()
            finish()
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.editRepeatNumberPicker.setOnValueChangedListener { _, _, newVal ->
            // Handle value change
        }

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

            // Adjust the hour based on the meridian selected in the spinner
            val adjustedHour = if (binding.editMeridianSpinner.selectedItem.toString() == "PM" && hour < 12) hour + 12 else hour

            TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
                val selectedTimeHour = if (selectedHour < 12) selectedHour else selectedHour - 12
                val selectedTime = String.format("%02d:%02d", selectedTimeHour, selectedMinute)
                binding.editTimeEditText.text = selectedTime

                // Set the meridian based on the selected hour
                val meridian = if (selectedHour < 12) "AM" else "PM"
                binding.editMeridianSpinner.setSelection((binding.editMeridianSpinner.adapter as ArrayAdapter<String>).getPosition(meridian))
            }, adjustedHour, minute, false).show() // Set is24HourView to false
        }

        binding.editSaveButton.setOnClickListener {
            val title = binding.editTitleEditText.text?.toString() ?: ""
            val content = binding.editContentEditText.text?.toString() ?: ""
            val time = binding.editTimeEditText.text?.toString() ?: ""
            val date = binding.editDateEditText.text?.toString() ?: ""
            val meridian = binding.editMeridianSpinner.selectedItem?.toString() ?: ""
            val repeat = binding.editRepeatNumberPicker.value?.toString() ?: ""
            val active = binding.activeEditStatus.isChecked?.toString() ?: ""
            val updatedRemainder = Remainder(id.toInt(), title, content, time, date, meridian, repeat, active)
            setAlarm(id, title, content, date, time, meridian, active, repeat.toInt())
            db.updateRemainder(updatedRemainder)
            finish()

            Toast.makeText(this, "Remainder updated", Toast.LENGTH_SHORT).show()
        }

        binding.updateBackButton.setOnClickListener {
            finish()
        }
    }

    private fun setAlarm(id: Long, title: String, content: String, date: String, time: String, meridian: String, active: String, repeat: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Cancel the old alarms
        for (i in 0 until repeat) {
            val intent = Intent(this, RemainderAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(this, id.toInt() + i, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            alarmManager.cancel(pendingIntent)
        }

        // Set new alarms
        if (active == "true") {
            val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
            val dateInString = "$date $time $meridian"
            val date = sdf.parse(dateInString)
            val calendar = Calendar.getInstance()
            calendar.time = date!!

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName"))
                startActivity(intent)
            } else {
                for (i in 0 until repeat) {
                    val intent = Intent(this, RemainderAlarmReceiver::class.java).apply {
                        putExtra("title", title)
                        putExtra("content", content)
                        putExtra("id", id.toInt())
                    }

                    // Add 'i' to the request code to make it unique for each alarm
                    val pendingIntent = PendingIntent.getBroadcast(this, id.toInt() + i, intent, PendingIntent.FLAG_UPDATE_CURRENT)

                    // Set the alarm to go off after i * 5 seconds
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis + i * 3000, pendingIntent)
                }
            }
        }
    }
}