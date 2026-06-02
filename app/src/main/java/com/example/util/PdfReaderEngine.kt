package com.example.util

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.roundToInt

class PdfReaderEngine(private val file: File) {
    private var pfd: ParcelFileDescriptor? = null
    private var renderer: PdfRenderer? = null
    
    // Cache for bitmaps to avoid memory spikes
    private val bitmapCache = object : LruCache<Int, Bitmap>(5) {
        override fun entryRemoved(evicted: Boolean, key: Int, oldValue: Bitmap, newValue: Bitmap?) {
            // Let the Garbage Collector handle the bitmap memory.
            // Explicitly calling recycle() can crash Compose RenderNode if it's still drawing.
        }
    }

    private val mutex = kotlinx.coroutines.sync.Mutex()

    private var isClosed = false

    private var _cachedPageCount: Int = 0

    private var rendererOpenLog = "Not attempted"

    private fun ensureRendererOpen() {
        if (isClosed) {
            rendererOpenLog = "Engine is closed"
            return
        }
        if (pfd == null || renderer == null) {
            try {
                rendererOpenLog = "Opening file: ${file.absolutePath}, exists: ${file.exists()}"
                android.util.Log.e("PdfDebug", rendererOpenLog)
                if (!file.exists()) {
                    rendererOpenLog += "\nFile not found!"
                    return
                }
                pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                rendererOpenLog += "\nPFD opened: ${pfd != null}"
                renderer = pfd?.let { PdfRenderer(it) }
                rendererOpenLog += "\nRenderer opened: ${renderer != null}"
                _cachedPageCount = renderer?.pageCount ?: 0
            } catch (e: Exception) {
                rendererOpenLog += "\nException opening: ${e.stackTraceToString()}"
                android.util.Log.e("PdfDebug", "ensureRendererOpen failed", e)
                e.printStackTrace()
            }
        }
    }

    val pageCount: Int
        get() = _cachedPageCount

    init {
        ensureRendererOpen()
    }

    suspend fun renderPage(pageIndex: Int, scale: Float = 1.5f): Bitmap? = withContext(Dispatchers.IO) {
        val cachedBitmap = bitmapCache.get(pageIndex)
        if (cachedBitmap != null) {
            return@withContext cachedBitmap
        }

        mutex.withLock {
            val sb = java.lang.StringBuilder()
            sb.append("=== ERRO REPORT ===\n")
            sb.append("1. renderer abriu? $rendererOpenLog\n")
            sb.append("2. arquivo foi encontrado? ${file.exists()} (Path: ${file.absolutePath})\n")
            sb.append("3. número de páginas? $_cachedPageCount\n")

            if (isClosed) {
                throw RuntimeException(sb.append("Status: Engine is closed.\n").toString())
            }
            if (pageIndex < 0 || pageIndex >= pageCount) {
                throw RuntimeException(sb.append("Status: Index out of bounds (req: $pageIndex, count: $pageCount).\n").toString())
            }
            val r = renderer ?: throw RuntimeException(sb.append("Status: Renderer is NULL.\n").toString())
            
            if (!isActive) throw RuntimeException(sb.append("Status: Coroutine inactive before openPage.\n").toString())
            
            try {
                android.util.Log.e("PdfDebug", "Opening page $pageIndex")
                val page = r.openPage(pageIndex)
                sb.append("4. página abriu? SIM (${page.width}x${page.height})\n")
                
                try {
                    val metrics = android.content.res.Resources.getSystem().displayMetrics
                    val smallestWidthDp = Math.min(metrics.widthPixels, metrics.heightPixels) / metrics.density
                    val isTablet = smallestWidthDp >= 600f
                    val adaptiveScale = if (isTablet) 2.5f else 2.0f
                    
                    var width = (page.width * adaptiveScale).roundToInt().coerceAtLeast(1)
                    var height = (page.height * adaptiveScale).roundToInt().coerceAtLeast(1)
                    
                    val MAX_WIDTH = 3000
                    val MAX_HEIGHT = 4000
                    
                    if (width > MAX_WIDTH) {
                        val ratio = height.toFloat() / width.toFloat()
                        width = MAX_WIDTH
                        height = (MAX_WIDTH * ratio).roundToInt()
                    }
                    if (height > MAX_HEIGHT) {
                        val ratio = width.toFloat() / height.toFloat()
                        height = MAX_HEIGHT
                        width = (MAX_HEIGHT * ratio).roundToInt()
                    }
                    
                    val memoryEstimate = (width * height * 4) / (1024 * 1024)
                    android.util.Log.d("PdfDebug", "RENDER_SCALE: $adaptiveScale")
                    android.util.Log.d("PdfDebug", "BITMAP_SIZE: ${width}x${height}")
                    android.util.Log.d("PdfDebug", "MEMORY_ESTIMATE: $memoryEstimate MB")
                    
                    if (!isActive) throw RuntimeException(sb.append("Status: Inactive before createBitmap.\n").toString())
                    
                    var bitmap: Bitmap? = null
                    try {
                        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                        sb.append("5. bitmap criado? SIM (${width}x${height})\n")
                    } catch (e: OutOfMemoryError) {
                        try {
                            bitmapCache.evictAll()
                            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                            sb.append("5. bitmap criado? SIM (após evictAll)\n")
                        } catch (e2: OutOfMemoryError) {
                            throw RuntimeException(sb.append("5. bitmap criado? NÃO (OOM: ${e2.message})\n").toString(), e2)
                        }
                    }

                    if (!isActive) {
                        bitmap?.recycle()
                        throw RuntimeException(sb.append("Status: Inactive before rendering to canvas.\n").toString())
                    } else {
                        android.util.Log.e("PdfDebug", "Rendering page $pageIndex to bitmap")
                        val canvas = android.graphics.Canvas(bitmap!!)
                        canvas.drawColor(android.graphics.Color.WHITE)
                        require(bitmap.config == Bitmap.Config.ARGB_8888) { "Bitmap config must be ARGB_8888" }
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        sb.append("6. bitmap renderizado? SIM\n")
                    }
                    
                    if (isActive) {
                        bitmapCache.put(pageIndex, bitmap)
                        return@withLock bitmap
                    } else {
                        bitmap?.recycle()
                        throw RuntimeException(sb.append("Status: Inactive after rendering.\n").toString())
                    }
                } finally {
                    page.close()
                }
            } catch (e: Exception) {
                val element = e.stackTrace.firstOrNull()
                sb.append("7. onde exatamente ocorre a exceção?\n")
                sb.append("Exceção: ${e.javaClass.simpleName}: ${e.message}\n")
                sb.append("Arquivo: ${element?.fileName ?: "Desconhecido"}\n")
                sb.append("Linha: ${element?.lineNumber ?: -1}\n")
                sb.append("Método: ${element?.methodName ?: "Desconhecido"}\n")
                sb.append("Stacktrace Completo:\n${e.stackTraceToString()}\n")
                
                android.util.Log.e("PdfDebug", "Exception generating page report", e)
                throw RuntimeException(sb.toString(), e)
            }
        }
    }

    fun close() {
        isClosed = true
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob() + Dispatchers.IO).launch {
            mutex.withLock {
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
    }
}
