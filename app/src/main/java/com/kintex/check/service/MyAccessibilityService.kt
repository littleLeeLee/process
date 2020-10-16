package com.kintex.check.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.elvishew.xlog.XLog


class MyAccessibilityService : AccessibilityService() {
    override fun onInterrupt() {
        XLog.d("onInterrupt")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // 获取包名
/*
        val pkgName = event!!.packageName.toString()
        XLog.d("onAccessibilityEvent: $pkgName event :${event.toString()}")

                val text = event.text
                XLog.d("size${text.size}")
                for (charSequence in text) {
                    XLog.d("charSequence:$charSequence")
                }
                val findAccessibilityNodeInfosByText =
                    rootInActiveWindow.findAccessibilityNodeInfosByText("kintex")
                XLog.d("findSize${findAccessibilityNodeInfosByText.size}")
                for (nodeInfo in findAccessibilityNodeInfosByText) {
                    XLog.d("findInfo${nodeInfo}")
                    if(nodeInfo.className == "android.widget.TextView" ){
                        val performAction =
                            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        XLog.d("click :$performAction id:${nodeInfo.windowId}")
                    }
                }
*/

        if (event!!.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && event!!.packageName == "com.android.packageinstaller") {
            val findAccessibility =
                rootInActiveWindow.findAccessibilityNodeInfosByText("安装")
            if(findAccessibility.size > 0){
                XLog.d(findAccessibility[0])
                findAccessibility[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Log.d("click", "安装")
            }


        }


    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        XLog.d("onServiceConnected")

    }


    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
        XLog.d("onUnbind")
    }
}