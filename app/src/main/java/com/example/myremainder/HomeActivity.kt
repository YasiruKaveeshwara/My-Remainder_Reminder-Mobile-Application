package com.example.myremainder

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myremainder.databinding.ActivityHomeBinding
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var db: RemainderDbHelper
    private lateinit var remainderAdapter: RemaindersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = RemainderDbHelper(this)

        remainderAdapter = RemaindersAdapter(listOf(), this@HomeActivity) // Initialize with an empty list

        lifecycleScope.launch {
            val remainders = db.getAllRemainders()
            remainderAdapter.refreshData(remainders)

            binding.remainderRecyclerView.layoutManager = LinearLayoutManager(this@HomeActivity)
            binding.remainderRecyclerView.adapter = remainderAdapter
        }


        binding.addButton.setOnClickListener {
            val intent = Intent(this, AddRemainderActivity::class.java)
            startActivity(intent)
        }
    }
    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            remainderAdapter.refreshData(db.getAllRemainders())
        }
    }
}