package com.app.synapse.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.semantics.text
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.synapse.core.utils.Constants
import com.app.synapse.data.model.Message
import com.app.synapse.databinding.ItemMessageReceivedBinding
import com.app.synapse.databinding.ItemMessageSentBinding
import java.text.SimpleDateFormat
import java.util.Locale

class MessageAdapter(
    private val currentUserId: String?
) : ListAdapter<Message, RecyclerView.ViewHolder>(MessageDiffCallback()) {

    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        return if (message.senderId == currentUserId) {
            Constants.VIEW_TYPE_MESSAGE_SENT
        } else {
            Constants.VIEW_TYPE_MESSAGE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            Constants.VIEW_TYPE_MESSAGE_SENT -> {
                val binding = ItemMessageSentBinding.inflate(inflater, parent, false)
                SentMessageViewHolder(binding)
            }
            Constants.VIEW_TYPE_MESSAGE_RECEIVED -> {
                val binding = ItemMessageReceivedBinding.inflate(inflater, parent, false)
                ReceivedMessageViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        when (holder) {
            is SentMessageViewHolder -> holder.bind(message, timeFormat)
            is ReceivedMessageViewHolder -> holder.bind(message, timeFormat)
        }
    }

    class SentMessageViewHolder(private val binding: ItemMessageSentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message, timeFormat: SimpleDateFormat) {
            binding.textViewMessageText.text = message.text
            message.timestamp?.let {
                binding.textViewTimestamp.text = timeFormat.format(it)
            } ?: run {
                binding.textViewTimestamp.text = "" // Or some placeholder for sending state
            }
            // Add image loading logic here if supporting image messages
        }
    }

    class ReceivedMessageViewHolder(private val binding: ItemMessageReceivedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message, timeFormat: SimpleDateFormat) {
            binding.textViewSenderName.text = message.senderName ?: "Anonymous"
            binding.textViewMessageText.text = message.text
            message.timestamp?.let {
                binding.textViewTimestamp.text = timeFormat.format(it)
            } ?: run {
                binding.textViewTimestamp.text = ""
            }
            // Add image loading logic here
        }
    }

    class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem // Data class implements equals
        }
    }
}
