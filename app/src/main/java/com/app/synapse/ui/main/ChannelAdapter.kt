package com.app.synapse.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.synapse.data.model.Channel
import com.app.synapse.databinding.ItemChannelBinding
import java.text.SimpleDateFormat
import java.util.Locale

class ChannelAdapter(
    private val onChannelClickListener: (Channel) -> Unit
) : ListAdapter<Channel, ChannelAdapter.ChannelViewHolder>(ChannelDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        val binding = ItemChannelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChannelViewHolder(binding, onChannelClickListener)
    }

    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ChannelViewHolder(
        private val binding: ItemChannelBinding,
        private val onChannelClickListener: (Channel) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

        fun bind(channel: Channel) {
            binding.textViewChannelName.text = channel.name
            binding.textViewChannelDescription.text = channel.description ?: "No description"
            binding.textViewLastMessage.text = channel.lastMessageText ?: "No messages yet"

            channel.lastMessageTimestamp?.let {
                binding.textViewTimestamp.text = timeFormat.format(it)
                binding.textViewTimestamp.visibility = android.view.View.VISIBLE
            } ?: run {
                binding.textViewTimestamp.visibility = android.view.View.GONE
            }

            binding.root.setOnClickListener {
                onChannelClickListener(channel)
            }
        }
    }

    class ChannelDiffCallback : DiffUtil.ItemCallback<Channel>() {
        override fun areItemsTheSame(oldItem: Channel, newItem: Channel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Channel, newItem: Channel): Boolean {
            return oldItem == newItem // Data class implements equals
        }
    }
}
