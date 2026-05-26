package com.example.util

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.roundToInt

class PdfReaderEngine(private val file: File) {
    private var pfd: ParcelFileDescriptor? = null
    private var renderer: PdfRenderer? = null
    
    // Cache for bitmaps to avoid OutOfMemoryErrors
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    // Use 1/4th of the available memory for this memory cache.
    private val cacheSize = maxMemory / 4
    private val bitmapCache = object : LruCache<Int, Bitmap>(cacheSize) {
        override fun sizeOf(key: Int, bitmap: Bitmap): Int {
            // The cache size will be measured in kilobytes rather than number of items.
            return bitmap.byteCount / 1024
        }
    }

    private fun ensureRendererOpen() {
        if (pfd == null || renderer == null) {
            try {
                pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                renderer = pfd?.let { PdfRenderer(it) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val pageCount: Int
        get() {
            ensureRendererOpen()
            return renderer?.pageCount ?: 0
        }

    init {
        ensureRendererOpen()
    }

    suspend fun renderPage(pageIndex: Int, scale: Float = 1.5f): Bitmap? = withContext(Dispatchers.IO) {
        ensureRendererOpen()
        
        if (pageIndex < 0 || pageIndex >= pageCount) return@withContext null
        val r = renderer ?: return@withContext null

        val cachedBitmap = bitmapCache.get(pageIndex)
        if (cachedBitmap != null) {
            return@withContext cachedBitmap
        }

        synchronized(r) {
            if (!isActive) return@synchronized null
            try {
                val page = r.openPage(pageIndex)
                try {
                    val width = (page.width * scale).roundToInt().coerceAtLeast(1)
                    val height = (page.height * scale).roundToInt().coerceAtLeast(1)
                    
                    if (!isActive) return@synchronized null
                    
                    var bitmap: Bitmap? = null
                    try {
                        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    } catch (e: OutOfMemoryError) {
                        try {
                            bitmapCache.evictAll()
                            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
                        } catch (e2: OutOfMemoryError) {
                            return@synchronized null
                        }
                    }

                    if (bitmap == null || !isActive) return@synchronized null

                    val canvas = android.graphics.Canvas(bitmap)
                    canvas.drawColor(android.graphics.Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    
                    if (isActive) {
                        bitmapCache.put(pageIndex, bitmap)
                        return@synchronized bitmap
                    } else {
                        bitmap.recycle()
                        return@synchronized null
                    }
                } finally {
                    page.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return@synchronized null
            }
        }
    }

    fun close() {
        try {
            bitmapCache.evictAll()
            renderer?.close()
            pfd?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            renderer = null
            pfd = null
        }
    }
}
