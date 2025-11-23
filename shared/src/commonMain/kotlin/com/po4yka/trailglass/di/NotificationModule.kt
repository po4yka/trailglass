package com.po4yka.trailglass.di

import com.po4yka.trailglass.data.repository.PushNotificationRepository
import com.po4yka.trailglass.data.repository.impl.PushNotificationRepositoryImpl
import me.tatarka.inject.annotations.Provides

/**
 * Notification dependency injection module.
 * Provides push notification repositories and related dependencies.
 */
interface NotificationModule {
    /**
     * Provides PushNotificationRepository implementation.
     */
    @AppScope
    @Provides
    fun providePushNotificationRepository(impl: PushNotificationRepositoryImpl): PushNotificationRepository = impl
}
