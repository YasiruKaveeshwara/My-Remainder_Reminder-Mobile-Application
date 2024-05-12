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
import android.view.View
import android.widget.AdapterView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class AddRemainderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddRemainderBinding
    private lateinit var db: RemainderDbHelper
    private var selectedDate: String = ""
    private var selectedTime: String = ""
    private val scope = CoroutineScope(Dispatchers.Main)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAddRemainderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val meridianValues = arrayOf("AM", "PM")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, meridianValues)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.meridianSpinner.adapter = adapter

        binding.meridianSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedMeridian = parent.getItemAtPosition(position).toString()
                val timeText = binding.timeEditText.text.toString()
                if (timeText.isNotEmpty()) {
                    val time = timeText.split(":")
                    val hour = Integer.parseInt(time[0])
                    val adjustedHour = if (selectedMeridian == "AM" && hour >= 12) hour - 12 else if (selectedMeridian == "PM" && hour < 12) hour + 12 else hour
                    binding.timeEditText.setText("$adjustedHour:${time[1]}")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        binding.repeatEditText.minValue = 1
        binding.repeatEditText.maxValue = 5

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

            // Adjust the hour based on the meridian selected in the spinner
            val adjustedHour = if (binding.meridianSpinner.selectedItem.toString() == "PM" && hour < 12) hour + 12 else hour

            TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
                val selectedTimeHour = if (selectedHour < 12) selectedHour else selectedHour - 12
                selectedTime = String.format("%02d:%02d", selectedTimeHour, selectedMinute)
                binding.timeEditText.text = selectedTime

                // Set the meridian based on the selected hour
                val meridian = if (selectedHour < 12) "AM" else "PM"
                binding.meridianSpinner.setSelection((binding.meridianSpinner.adapter as ArrayAdapter<String>).getPosition(meridian))
            }, adjustedHour, minute, false).show() // Set is24HourView to false
        }

        binding.saveButton.setOnClickListener {
            val title = binding.titleEditText.text?.toString() ?: ""
            val content = binding.contentEditText.text?.toString() ?: ""
            val time = binding.timeEditText.text?.toString() ?: ""
            val date = binding.dateEditText.text?.toString() ?: ""
            val meridian = binding.meridianSpinner.selectedItem?.toString() ?: ""
            val repeat = binding.repeatEditText.value?.toString() ?: ""
            val active = binding.activeEditStatus.isChecked?.toString() ?: ""

            scope.launch {
                val id = db.insertRemainder(title, content, time, date, meridian, repeat, active)
                setAlarm(id, title, content, date, time, meridian, active, repeat.toInt())
                finish()

                Toast.makeText(this@AddRemainderActivity, "Remainder added", Toast.LENGTH_SHORT).show()
            }


        }

        binding.addBackButton.setOnClickListener {
            finish()
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

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}