package com.app.synapse.ui.main

import android.os.Bundle
import androidx.activity.viewModels
import com.app.synapse.R
import com.app.synapse.core.base.BaseActivity
import com.app.synapse.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(
    ActivityMainBinding::inflate
) {
    // ViewModel can be shared if MainActivity needs to interact with it,
    // or if other fragments in MainActivity need to.
    // If only ChannelListFragment uses it, it can be scoped to the fragment.
    private val viewModel: ChannelListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar) // Ensure you have a Toolbar with id 'toolbar' in activity_main.xml

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, ChannelListFragment())
                .commitNow()
        }

        // Example: Observe something from ViewModel if MainActivity needs to react directly
        // viewModel.navigateToVerification.observe(this, EventObserver {
        //     // Logic handled in fragment, but MainActivity could also observe
        // })
    }

    // If MainActivity needs to handle options menu itself, or delegate to fragment
    // override fun onCreateOptionsMenu(menu: Menu): Boolean {
    //     menuInflater.inflate(R.menu.menu_channel_list, menu)
    //     return true
    // }
    //
    // override fun onOptionsItemSelected(item: MenuItem): Boolean {
    //     return when (item.itemId) {
    //         R.id.action_sign_out -> {
    //             viewModel.signOut()
    //             true
    //         }
    //         else -> super.onOptionsItemSelected(item)
    //     }
    // }
}
