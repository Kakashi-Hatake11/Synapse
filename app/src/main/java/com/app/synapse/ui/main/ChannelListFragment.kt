package com.app.synapse.ui.main

import android.app.AlertDialog
import android.content.Intent
import android.view.*
import android.widget.EditText
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.synapse.R
import com.app.synapse.core.base.BaseFragment
import com.app.synapse.core.utils.Constants
import com.app.synapse.core.utils.EventObserver
import com.app.synapse.databinding.FragmentChannelListBinding
import com.app.synapse.ui.chat.ChatActivity
import com.app.synapse.ui.verification.VerificationActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlin.text.ifEmpty
import kotlin.text.trim

@AndroidEntryPoint
class ChannelListFragment : BaseFragment<FragmentChannelListBinding>(
    FragmentChannelListBinding::inflate
) {

    // Use activityViewModels if ViewModel is shared with MainActivity or other fragments
    // Use viewModels() if ViewModel is scoped only to this fragment
    private val viewModel: ChannelListViewModel by activityViewModels()
    private lateinit var channelAdapter: ChannelAdapter

    override fun setupViews() {
        setupRecyclerView()
        setupMenu()

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshChannels()
        }

        binding.fabAddChannel.setOnClickListener {
            showCreateChannelDialog()
        }
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_channel_list, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_sign_out -> {
                        viewModel.signOut()
                        true
                    }
                    // Handle other menu items if any
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupRecyclerView() {
        channelAdapter = ChannelAdapter { channel ->
            viewModel.onChannelClicked(channel)
        }
        binding.recyclerViewChannels.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = channelAdapter
            // addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL)) // Optional
        }
    }

    override fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            binding.swipeRefreshLayout.isRefreshing = state is ChannelListUiState.Loading
            when (state) {
                is ChannelListUiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.recyclerViewChannels.visibility = View.GONE
                    binding.textViewEmptyChannels.visibility = View.GONE
                    binding.textViewError.visibility = View.GONE
                }
                is ChannelListUiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.textViewEmptyChannels.visibility = View.GONE
                    binding.textViewError.visibility = View.GONE
                    binding.recyclerViewChannels.visibility = View.VISIBLE
                    channelAdapter.submitList(state.channels)
                }
                is ChannelListUiState.Empty -> {
                    binding.progressBar.visibility = View.GONE
                    binding.recyclerViewChannels.visibility = View.GONE
                    binding.textViewError.visibility = View.GONE
                    binding.textViewEmptyChannels.visibility = View.VISIBLE
                    binding.textViewEmptyChannels.text = getString(R.string.message_no_channels_found)
                    channelAdapter.submitList(emptyList()) // Clear adapter
                }
                is ChannelListUiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.recyclerViewChannels.visibility = View.GONE
                    binding.textViewEmptyChannels.visibility = View.GONE
                    binding.textViewError.visibility = View.VISIBLE
                    binding.textViewError.text = state.message
                    // showToast(state.message)
                }
            }
        }

        viewModel.navigateToChat.observe(viewLifecycleOwner, EventObserver { channel ->
            val intent = Intent(activity, ChatActivity::class.java).apply {
                putExtra(Constants.EXTRA_CHANNEL_ID, channel.id)
                putExtra(Constants.EXTRA_CHANNEL_NAME, channel.name)
            }
            startActivity(intent)
        })

        viewModel.navigateToVerification.observe(viewLifecycleOwner, EventObserver {
            val intent = Intent(activity, VerificationActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            activity?.finish()
        })
    }

    private fun showCreateChannelDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_create_channel, null)
        val editTextChannelName = dialogView.findViewById<EditText>(R.id.editText_channel_name)
        val editTextChannelDescription = dialogView.findViewById<EditText>(R.id.editText_channel_description)

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.dialog_title_create_channel)
            .setView(dialogView)
            .setPositiveButton(R.string.button_create) { dialog, _ ->
                val name = editTextChannelName.text.toString().trim()
                val description = editTextChannelDescription.text.toString().trim()
                if (name.isNotEmpty()) {
                    viewModel.createNewChannel(name, description.ifEmpty { null })
                } else {
                    showToast(getString(R.string.error_channel_name_empty))
                }
                dialog.dismiss()
            }
            .setNegativeButton(R.string.button_cancel) { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    override fun onResume() {
        super.onResume()
        // Optionally, refresh channels if needed when fragment resumes
        // viewModel.refreshChannels()
    }
}
