package com.app.synapse.core.di

// Example: If you create interfaces like this:
// import com.app.synapse.data.source.remote.ChatDataSource
// import com.app.synapse.data.source.remote.FirebaseChatDataSource
// import dagger.Binds

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    // If FirebaseChatDataSource implemented an interface ChatDataSource, you'd bind it like this:
    // @Binds
    // abstract fun bindChatDataSource(
    //     firebaseChatDataSource: FirebaseChatDataSource // Implementation
    // ): ChatDataSource // Interface

    // Similarly for other data sources if they implement interfaces and repositories depend on those interfaces.
}