package com.joseluiscruz.capturainventario.ui.common

import android.view.MotionEvent
import android.view.View
import android.view.WindowManager

class BubbleTouchListener(
    private val params: WindowManager.LayoutParams,
    private val windowManager: WindowManager,
    private val bubbleView: View
) : View.OnTouchListener {
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = params.x
                initialY = params.y
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                params.x = initialX + (event.rawX - initialTouchX).toInt()
                params.y = initialY + (event.rawY - initialTouchY).toInt()
                windowManager.updateViewLayout(bubbleView, params)
                return true
            }
        }
        return false
    }
}

