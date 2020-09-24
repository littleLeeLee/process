package com.kintex.check.view

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SmoothLinearLayoutManager(
    context: Context?,
    vertical: Int,
    b: Boolean
) : LinearLayoutManager(context) {

    override fun smoothScrollToPosition(
        recyclerView: RecyclerView?,
        state: RecyclerView.State?,
        position: Int
    ) {
        val smoothScroller = TopLinearSmoothScroller(recyclerView!!.context)
        smoothScroller.targetPosition = position
        startSmoothScroll(smoothScroller)
    }

}