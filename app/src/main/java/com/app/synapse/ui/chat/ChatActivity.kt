package com.app.synapse.ui.chat

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.synapse.R
import com.app.synapse.core.base.BaseActivity
import com.app.synapse.core.utils.Constants
import com.app.synapse.core.utils.EventObserver
import com.app.synapse.core.utils.hideKeyboard
import com.app.synapse.databinding.ActivityChatBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlin.text.clear
import kotlin.text.isNotEmpty
import kotlin.text.trim

@AndroidEntryPoint
class ChatActivity : BaseActivity<ActivityChatBinding>(
    ActivityChatBinding::inflate
) {
    private val viewModel: ChatViewModel by viewModels()
    private lateinit var messageAdapter: MessageAdapter

    private var channelId: String? = null
    private var channelName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        channelId = intent.getStringExtra(Constants.EXTRA_CHANNEL_ID)
        channelName = intent.getStringExtra(Constants.EXTRA_CHANNEL_NAME)

        if (channelId == null) {
            showToast(getString(R.string.error_channel_not_found))
            finish()
            return
        }

        setupToolbar(binding.toolbar, showUpButton = true)
        supportActionBar?.title = channelName ?: getString(R.string.title_chat)

        // Initialize currentUserId in adapter after viewModel is available
        messageAdapter = MessageAdapter(viewModel.getCurrentUserId())
        setupRecyclerView()
        setupViews()
        observeViewModel()

        channelId?.let {
            viewModel.loadMessages(it)
        }
    }

    private fun setupViews() {
        binding.buttonSendMessage.setOnClickListener {
            val messageText = binding.editTextMessage.text.toString().trim()
            if (messageText.isNotEmpty()) {
                viewModel.sendMessage(messageText)
            }
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerViewMessages.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true // New messages appear at the bottom
            }
            adapter = messageAdapter
            // Optimization: if message sizes are fixed
            // setHasFixedSize(true)

            // Scroll to bottom when keyboard opens or new items are added
            addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
                if (bottom < oldBottom) {
                    postDelayed({ scrollToPosition(messageAdapter.itemCount - 1) }, 100)
                }
            }
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(this) { state ->
            binding.progressBar.visibility = if (state is ChatUiState.Loading) View.VISIBLE else View.GONE
            binding.recyclerViewMessages.visibility = if (state is ChatUiState.Success || state is ChatUiState.Empty) View.VISIBLE else View.GONE
            binding.textViewEmptyChat.visibility = if (state is ChatUiState.Empty) View.VISIBLE else View.GONE
            binding.textViewError.visibility = if (state is ChatUiState.Error) View.VISIBLE else View.GONE

            when (state) {
                is ChatUiState.Success -> {
                    messageAdapter.submitList(state.messages) {
                        // Scroll to bottom only if new messages were added at the end
                        // or if it's the initial load.
                        val currentItemCount = messageAdapter.itemCount
                        if (currentItemCount > 0) {
                            binding.recyclerViewMessages.smoothScrollToPosition(currentItemCount - 1)
                        }
                    }
                    binding.textViewError.visibility = View.GONE
                    binding.textViewEmptyChat.visibility = View.GONE
                }
                is ChatUiState.Empty -> {
                    messageAdapter.submitList(emptyList())
                    binding.textViewEmptyChat.text = getString(R.string.message_no_messages_yet)
                    binding.textViewError.visibility = View.GONE
                }
                is ChatUiState.Error -> {
                    binding.textViewError.text = state.message
                    // showToast(state.message) // Can be too intrusive for continuous errors
                }
                is ChatUiState.Loading -> {
                    // Handled by progress bar visibility
                }
            }
        }

        viewModel.messageSentStatus.observe(this, EventObserver { success ->
            if (success) {
                binding.editTextMessage.text?.clear()
                // Keyboard should ideally hide automatically or smoothly adjust.
                // If not, consider manually hiding it:
                // binding.editTextMessage.hideKeyboard()
                // RecyclerView will auto-scroll due to submitList and adapter changes
            } else {
                showToast(getString(R.string.error_sending_message))
                // Optionally, re-enable send button or give other feedback
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed() // Or finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
