package com.larkmusic.player.lyrics

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView

class FloatingLyricsService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var lyricTextView: TextView? = null

    companion object {
        const val ACTION_UPDATE_LYRIC = "com.larkmusic.player.action.UPDATE_LYRIC"
        const val EXTRA_LYRIC_TEXT = "extra_lyric_text"
        var isRunning = false
            private set
    }

    override fun onBind(intent: Intent?): IBinder? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()
        isRunning = true
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            x = 0
            y = 200
        }

        // Build floating capsule programmatically
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(32, 16, 24, 16)

            // Glassmorphic dark rounded pill background
            val shape = GradientDrawable().apply {
                setColor(0xCC1E202B.toInt()) // dark semi-transparent
                cornerRadius = 60f
                setStroke(2, 0x8800E5FF.toInt()) // cyan glow border
            }
            background = shape
        }

        lyricTextView = TextView(this).apply {
            text = "♪ Lark Music Floating Lyrics ♪"
            setTextColor(Color.WHITE)
            textSize = 15f
            setShadowLayer(4f, 0f, 0f, 0xFF00E5FF.toInt())
            maxLines = 2
            setPadding(0, 0, 24, 0)
        }

        val closeBtn = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            setBackgroundColor(Color.TRANSPARENT)
            setColorFilter(Color.WHITE)
            setOnClickListener {
                stopSelf()
            }
        }

        container.addView(lyricTextView)
        container.addView(closeBtn)

        // Make window draggable
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        container.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager?.updateViewLayout(container, params)
                    true
                }
                else -> false
            }
        }

        floatingView = container
        windowManager?.addView(container, params)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_UPDATE_LYRIC) {
            val lyric = intent.getStringExtra(EXTRA_LYRIC_TEXT) ?: ""
            if (lyric.isNotEmpty()) {
                lyricTextView?.text = lyric
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        isRunning = false
        floatingView?.let { windowManager?.removeView(it) }
        floatingView = null
        super.onDestroy()
    }
}
