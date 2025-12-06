package com.example.humour

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.humour.databinding.ItemHistoryCardBinding

class HistoryAdapter(private val entries: List<JournalEntry>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(val binding: ItemHistoryCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val entry = entries[position]

        with(holder.binding) {
            tvDate.text = entry.date
            tvMood.text = entry.mood
            tvNotes.text = if (entry.notes.isNotEmpty()) entry.notes else "No notes provided"

            val iconRes = when (entry.mood) {
                "Great" -> R.drawable.star
                "Good" -> R.drawable.smiling
                "Meh" -> R.drawable.neutral
                "Bad" -> R.drawable.confused
                "Terrible" -> R.drawable.disappointed
                else -> R.drawable.smiling
            }
            ivEmoji.setImageResource(iconRes)

            // --- AJOUT DU CLIC ICI ---
            root.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, HistoryDetailsActivity::class.java)

                // On passe les infos à la page de détails
                intent.putExtra("MOOD", entry.mood)
                intent.putExtra("DATE", entry.date)
                intent.putExtra("NOTES", entry.notes)

                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount() = entries.size
}