package com.tvvideoplayer.app.cache

import android.content.Context
import android.util.Log
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

/**
 * Advanced video cache manager inspired by CloudStream's CS3IPlayer cache system.
 *
 * Manages ExoPlayer's SimpleCache with configurable size limits and LRU eviction.
 * Cache is stored in the app's cache directory under "exoplayer/".
 *
 * Usage:
 *   val cacheManager = VideoCacheManager(context)
 *   cacheManager.initialize(cacheSizeBytes)
 *   val dataSourceFactory = cacheManager.createCacheDataSourceFactory(httpDataSourceFactory)
 */
class VideoCacheManager(private val context: Context) {

    companion object {
        private const val TAG = "VideoCacheManager"
        private const val CACHE_DIR_NAME = "exoplayer"

        // Preset cache sizes (in bytes)
        val CACHE_SIZE_100MB = 100L * 1024 * 1024
        val CACHE_SIZE_250MB = 250L * 1024 * 1024
        val CACHE_SIZE_500MB = 500L * 1024 * 1024
        val CACHE_SIZE_1GB = 1024L * 1024 * 1024
        val CACHE_SIZE_2GB = 2048L * 1024 * 1024
        val CACHE_SIZE_UNLIMITED = Long.MAX_VALUE

        @Volatile
        private var instance: VideoCacheManager? = null

        fun getInstance(context: Context): VideoCacheManager {
            return instance ?: synchronized(this) {
                instance ?: VideoCacheManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    private var simpleCache: SimpleCache? = null
    private var currentCacheSize: Long = 0L
    private val cacheDir: File
        get() = File(context.cacheDir, CACHE_DIR_NAME)

    /**
     * Initialize or reinitialize the cache with the given size.
     * If the cache already exists with the same size, it will be reused.
     * If the size changes, the old cache is released and a new one is created.
     */
    @Synchronized
    fun initialize(cacheSizeBytes: Long) {
        if (simpleCache != null && currentCacheSize == cacheSizeBytes) {
            Log.d(TAG, "Cache already initialized with size: ${cacheSizeBytes / 1024 / 1024}MB")
            return
        }

        // Release existing cache if size changed
        if (simpleCache != null) {
            release()
        }

        try {
            val databaseProvider = StandaloneDatabaseProvider(context)
            simpleCache = SimpleCache(
                cacheDir,
                LeastRecentlyUsedCacheEvictor(cacheSizeBytes),
                databaseProvider
            )
            currentCacheSize = cacheSizeBytes
            Log.d(TAG, "Cache initialized: dir=${cacheDir.absolutePath}, size=${cacheSizeBytes / 1024 / 1024}MB")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize cache", e)
        }
    }

    /**
     * Get the current SimpleCache instance, or null if not initialized.
     */
    fun getCache(): SimpleCache? = simpleCache

    /**
     * Check if cache is enabled (initialized with size > 0).
     */
    fun isCacheEnabled(): Boolean = simpleCache != null && currentCacheSize > 0

    /**
     * Get the current cache size limit in bytes.
     */
    fun getCacheSizeLimit(): Long = currentCacheSize

    /**
     * Get the current cache usage in bytes.
     */
    fun getCacheSize(): Long {
        return try {
            simpleCache?.cacheSpace ?: 0L
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get cache size", e)
            0L
        }
    }

    /**
     * Get the cache directory size on disk (including all files).
     */
    fun getCacheDirSize(): Long {
        return try {
            getFolderSize(cacheDir)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get cache dir size", e)
            0L
        }
    }

    /**
     * Create a CacheDataSource.Factory that wraps the given upstream factory.
     * If cache is not enabled, returns null.
     */
    fun createCacheDataSourceFactory(
        upstreamFactory: androidx.media3.datasource.HttpDataSource.Factory
    ): CacheDataSource.Factory? {
        val cache = simpleCache ?: return null
        return CacheDataSource.Factory().apply {
            setCache(cache)
            setUpstreamDataSourceFactory(upstreamFactory)
            setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        }
    }

    /**
     * Release the cache. Call this when the player is done.
     */
    @Synchronized
    fun release() {
        try {
            simpleCache?.release()
            Log.d(TAG, "Cache released")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to release cache", e)
        } finally {
            simpleCache = null
            currentCacheSize = 0L
        }
    }

    /**
     * Clear all cached data by deleting the cache directory.
     */
    @Synchronized
    fun clearCache() {
        try {
            simpleCache?.release()
            simpleCache = null
            cacheDir.deleteRecursively()
            Log.d(TAG, "Cache cleared: ${cacheDir.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear cache", e)
        }
    }

    private fun getFolderSize(dir: File): Long {
        var size = 0L
        dir.listFiles()?.forEach { file ->
            size += if (file.isDirectory) getFolderSize(file) else file.length()
        }
        return size
    }
}
