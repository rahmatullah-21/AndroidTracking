/*
 * DROP-IN REFERENCE — not compiled by default.
 *
 * Copy to app/src/main/java/com/deviceinsight/pro/di/ and:
 *   - REMOVE the `bindCloudSyncRepository(NoOpCloudSyncRepository)` binding in RepositoryModule.kt
 *     (you can only bind CloudSyncRepository once).
 *   - Keep this module to provide Firebase singletons + bind the Firestore implementation.
 */
package com.deviceinsight.pro.di

import com.deviceinsight.pro.data.repository.FirestoreCloudSyncRepository
import com.deviceinsight.pro.domain.repository.CloudSyncRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    @Provides @Singleton fun firestore(): FirebaseFirestore = Firebase.firestore
    @Provides @Singleton fun auth(): FirebaseAuth = Firebase.auth
}

@Module
@InstallIn(SingletonComponent::class)
abstract class CloudSyncModule {
    @Binds @Singleton
    abstract fun bindCloudSyncRepository(impl: FirestoreCloudSyncRepository): CloudSyncRepository
}
