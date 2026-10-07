package com.campusevent.qa

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EventAdapter(
    private var events: List<Event>,
    private val onItemClick: (Event) -> Unit
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCategory: TextView = itemView.findViewById(R.id.tvItemCategory)
        val tvQuota: TextView = itemView.findViewById(R.id.tvItemQuota)
        val tvTitle: TextView = itemView.findViewById(R.id.tvItemTitle)
        val tvSpeaker: TextView = itemView.findViewById(R.id.tvItemSpeaker)
        val tvLocation: TextView = itemView.findViewById(R.id.tvItemLocation)
        val tvDate: TextView = itemView.findViewById(R.id.tvItemDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        holder.tvTitle.text = event.title
        holder.tvCategory.text = event.category
        holder.tvSpeaker.text = event.speaker
        holder.tvLocation.text = event.location
        holder.tvDate.text = event.date

        if (event.isFull) {
            holder.tvQuota.text = "Penuh"
            holder.tvQuota.setBackgroundResource(R.drawable.bg_badge_full)
            holder.tvQuota.setTextColor(holder.itemView.context.getColor(R.color.error_text))
        } else {
            holder.tvQuota.text = "${event.remainingQuota} kursi tersisa"
            holder.tvQuota.setBackgroundResource(R.drawable.bg_badge_quota)
            holder.tvQuota.setTextColor(holder.itemView.context.getColor(R.color.secondary_variant))
        }

        holder.itemView.setOnClickListener {
            onItemClick(event)
        }
    }

    override fun getItemCount(): Int = events.size

    fun updateData(newEvents: List<Event>) {
        events = newEvents
        notifyDataSetChanged()
    }
}
