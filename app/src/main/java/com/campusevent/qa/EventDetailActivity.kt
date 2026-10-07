package com.campusevent.qa

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class EventDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_EVENT_ID = "extra_event_id"
        const val EXTRA_EVENT_TITLE = "extra_event_title"
        const val EXTRA_EVENT_CATEGORY = "extra_event_category"
        const val EXTRA_EVENT_SPEAKER = "extra_event_speaker"
        const val EXTRA_EVENT_LOCATION = "extra_event_location"
        const val EXTRA_EVENT_DATE = "extra_event_date"
        const val EXTRA_EVENT_QUOTA = "extra_event_quota"
        const val EXTRA_EVENT_REGISTERED = "extra_event_registered"
    }

    private lateinit var tvEventTitle: TextView
    private lateinit var tvEventCategory: TextView
    private lateinit var tvEventSpeaker: TextView
    private lateinit var tvEventLocation: TextView
    private lateinit var tvEventDate: TextView
    private lateinit var tvEventQuota: TextView

    private lateinit var etParticipantName: EditText
    private lateinit var etParticipantEmail: EditText
    private lateinit var etParticipantCount: EditText

    private lateinit var btnRegister: Button
    private lateinit var btnBack: Button
    private lateinit var progressRegistration: ProgressBar

    private lateinit var cardRegistrationResult: MaterialCardView
    private lateinit var tvRegistrationStatus: TextView
    private lateinit var tvRegistrationMessage: TextView

    private var eventId: String = "EVT-001"
    private var eventTitle: String = "Workshop UI/UX Design"
    private var eventCategory: String = "Design"
    private var eventSpeaker: String = "Sabrina Putri"
    private var eventLocation: String = "Lab Komputer 2"
    private var eventDate: String = "2026-10-15"
    private var quota: Int = 50
    private var registeredCount: Int = 32

    private val validator = EventValidator()
    private val repository: EventRepository by lazy {
        (application as CampusEventApplication).repository
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_detail)

        extractIntentData()
        initViews()
        populateEventData()
        setupListeners()
    }

    private fun extractIntentData() {
        intent?.let {
            eventId = it.getStringExtra(EXTRA_EVENT_ID) ?: eventId
            eventTitle = it.getStringExtra(EXTRA_EVENT_TITLE) ?: eventTitle
            eventCategory = it.getStringExtra(EXTRA_EVENT_CATEGORY) ?: eventCategory
            eventSpeaker = it.getStringExtra(EXTRA_EVENT_SPEAKER) ?: eventSpeaker
            eventLocation = it.getStringExtra(EXTRA_EVENT_LOCATION) ?: eventLocation
            eventDate = it.getStringExtra(EXTRA_EVENT_DATE) ?: eventDate
            quota = it.getIntExtra(EXTRA_EVENT_QUOTA, quota)
            registeredCount = it.getIntExtra(EXTRA_EVENT_REGISTERED, registeredCount)
        }

        // Cross-check with repository cache if available
        repository.getLocalEventById(eventId)?.let {
            quota = it.quota
            registeredCount = it.registeredCount
        }
    }

    private fun initViews() {
        tvEventTitle = findViewById(R.id.tvEventTitle)
        tvEventCategory = findViewById(R.id.tvEventCategory)
        tvEventSpeaker = findViewById(R.id.tvEventSpeaker)
        tvEventLocation = findViewById(R.id.tvEventLocation)
        tvEventDate = findViewById(R.id.tvEventDate)
        tvEventQuota = findViewById(R.id.tvEventQuota)

        etParticipantName = findViewById(R.id.etParticipantName)
        etParticipantEmail = findViewById(R.id.etParticipantEmail)
        etParticipantCount = findViewById(R.id.etParticipantCount)

        btnRegister = findViewById(R.id.btnRegister)
        btnBack = findViewById(R.id.btnBack)
        progressRegistration = findViewById(R.id.progressRegistration)

        cardRegistrationResult = findViewById(R.id.cardRegistrationResult)
        tvRegistrationStatus = findViewById(R.id.tvRegistrationStatus)
        tvRegistrationMessage = findViewById(R.id.tvRegistrationMessage)
    }

    private fun populateEventData() {
        tvEventTitle.text = eventTitle
        tvEventCategory.text = eventCategory
        tvEventSpeaker.text = "Pembicara: $eventSpeaker"
        tvEventLocation.text = "Lokasi: $eventLocation"
        tvEventDate.text = "Tanggal: $eventDate"

        updateQuotaBadge()
    }

    private fun updateQuotaBadge() {
        val remaining = (quota - registeredCount).coerceAtLeast(0)
        if (registeredCount >= quota) {
            tvEventQuota.text = getString(R.string.error_event_full)
            tvEventQuota.setBackgroundResource(R.drawable.bg_badge_full)
            tvEventQuota.setTextColor(getColor(R.color.error_text))
        } else {
            tvEventQuota.text = "$remaining kursi tersisa"
            tvEventQuota.setBackgroundResource(R.drawable.bg_badge_quota)
            tvEventQuota.setTextColor(getColor(R.color.secondary_variant))
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnRegister.setOnClickListener {
            hideKeyboard()
            handleRegistration()
        }
    }

    private fun handleRegistration() {
        val name = etParticipantName.text?.toString() ?: ""
        val email = etParticipantEmail.text?.toString() ?: ""
        val countStr = etParticipantCount.text?.toString() ?: ""

        // Clear previous field errors
        etParticipantName.error = null
        etParticipantEmail.error = null
        etParticipantCount.error = null

        // 1. Validation for Participant Name
        if (!validator.isValidParticipantName(name)) {
            val errorMsg = getString(R.string.error_name_empty)
            etParticipantName.error = errorMsg
            showErrorResult(errorMsg)
            return
        }

        // 2. Validation for Email
        if (!validator.isValidEmail(email)) {
            val errorMsg = getString(R.string.error_email_invalid)
            etParticipantEmail.error = errorMsg
            showErrorResult(errorMsg)
            return
        }

        // 3. Validation for Participant Count
        val count = countStr.toIntOrNull()
        if (count == null || !validator.isValidParticipantCount(count)) {
            val errorMsg = getString(R.string.error_count_invalid)
            etParticipantCount.error = errorMsg
            showErrorResult(errorMsg)
            return
        }

        // 4. Validation for Quota Availability
        if (!validator.isQuotaAvailable(quota, registeredCount, count)) {
            val errorMsg = getString(R.string.error_quota_exceeded)
            etParticipantCount.error = errorMsg
            showErrorResult(errorMsg)
            return
        }

        // Submit registration
        setLoadingState(true)

        lifecycleScope.launch {
            val result = repository.registerParticipant(
                eventId = eventId,
                participantName = name,
                email = email,
                participantCount = count,
                currentQuota = quota,
                currentRegistered = registeredCount
            )

            setLoadingState(false)

            result.onSuccess {
                registeredCount += count
                updateQuotaBadge()
                showSuccessResult(name, count)
            }.onFailure { error ->
                showErrorResult(error.message ?: getString(R.string.status_error))
            }
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        btnRegister.isEnabled = !isLoading
        progressRegistration.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showSuccessResult(name: String, count: Int) {
        cardRegistrationResult.setCardBackgroundColor(ContextCompat.getColor(this, R.color.success_bg))
        cardRegistrationResult.strokeColor = ContextCompat.getColor(this, R.color.success_stroke)
        tvRegistrationStatus.setTextColor(ContextCompat.getColor(this, R.color.success_text))
        tvRegistrationStatus.text = getString(R.string.status_success)
        tvRegistrationMessage.text = getString(R.string.success_registered_format, name, count)
        cardRegistrationResult.visibility = View.VISIBLE
    }

    private fun showErrorResult(message: String) {
        cardRegistrationResult.setCardBackgroundColor(ContextCompat.getColor(this, R.color.error_bg))
        cardRegistrationResult.strokeColor = ContextCompat.getColor(this, R.color.error_stroke)
        tvRegistrationStatus.setTextColor(ContextCompat.getColor(this, R.color.error_text))
        tvRegistrationStatus.text = getString(R.string.status_error)
        tvRegistrationMessage.text = message
        cardRegistrationResult.visibility = View.VISIBLE
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        currentFocus?.let {
            imm?.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }
}
