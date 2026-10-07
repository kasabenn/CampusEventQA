package com.campusevent.qa

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var rvEvents: RecyclerView
    private lateinit var tvEmptyEvents: TextView
    private lateinit var progressEvents: ProgressBar
    private lateinit var tvEventsError: TextView
    private lateinit var adapter: EventAdapter

    private val repository: EventRepository by lazy {
        (application as CampusEventApplication).repository
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupRecyclerView()
        loadEvents()
    }

    override fun onResume() {
        super.onResume()
        // Refresh when returning from EventDetailActivity
        loadEvents()
    }

    private fun initViews() {
        rvEvents = findViewById(R.id.rvEvents)
        tvEmptyEvents = findViewById(R.id.tvEmptyEvents)
        progressEvents = findViewById(R.id.progressEvents)
        tvEventsError = findViewById(R.id.tvEventsError)
    }

    private fun setupRecyclerView() {
        adapter = EventAdapter(emptyList()) { event ->
            val intent = Intent(this, EventDetailActivity::class.java).apply {
                putExtra(EventDetailActivity.EXTRA_EVENT_ID, event.eventId)
                putExtra(EventDetailActivity.EXTRA_EVENT_TITLE, event.title)
                putExtra(EventDetailActivity.EXTRA_EVENT_CATEGORY, event.category)
                putExtra(EventDetailActivity.EXTRA_EVENT_SPEAKER, event.speaker)
                putExtra(EventDetailActivity.EXTRA_EVENT_LOCATION, event.location)
                putExtra(EventDetailActivity.EXTRA_EVENT_DATE, event.date)
                putExtra(EventDetailActivity.EXTRA_EVENT_QUOTA, event.quota)
                putExtra(EventDetailActivity.EXTRA_EVENT_REGISTERED, event.registeredCount)
            }
            startActivity(intent)
        }
        rvEvents.layoutManager = LinearLayoutManager(this)
        rvEvents.adapter = adapter
    }

    private fun loadEvents() {
        showLoading()
        lifecycleScope.launch {
            val result = repository.getEvents()
            result.onSuccess { events ->
                if (events.isEmpty()) {
                    showEmpty()
                } else {
                    showContent(events)
                }
            }.onFailure {
                showError()
            }
        }
    }

    private fun showLoading() {
        progressEvents.visibility = View.VISIBLE
        rvEvents.visibility = View.GONE
        tvEmptyEvents.visibility = View.GONE
        tvEventsError.visibility = View.GONE
    }

    private fun showContent(events: List<Event>) {
        progressEvents.visibility = View.GONE
        rvEvents.visibility = View.VISIBLE
        tvEmptyEvents.visibility = View.GONE
        tvEventsError.visibility = View.GONE
        adapter.updateData(events)
    }

    private fun showEmpty() {
        progressEvents.visibility = View.GONE
        rvEvents.visibility = View.GONE
        tvEmptyEvents.visibility = View.VISIBLE
        tvEventsError.visibility = View.GONE
    }

    private fun showError() {
        progressEvents.visibility = View.GONE
        rvEvents.visibility = View.GONE
        tvEmptyEvents.visibility = View.GONE
        tvEventsError.visibility = View.VISIBLE
    }
}
