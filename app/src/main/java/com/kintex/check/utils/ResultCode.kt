package com.kintex.check.utils

import com.kintex.check.activity.BaseActivity

object ResultCode {

    //测试状态码
    var DEFAULT = 0
    var PASSED = 1
    var FAILED = 2
    var RESET = -1
    var START = 10086
    var STOP = 10087
    var SENDSUMMARY = 10088

    //测试项对应的position
    var WIFI_POSITION = 0
    var BLUETOOTH_POSITION = 1
    var GPS_POSITION = 2
    var VIBRATION_POSITION = 3
    var MIC_LOUD_POSITION = 4
    var MIC_EAR_POSITION = 999
    var HEADSET_POSITION = 888
    var PROXIMITY_POSITION = 5
    var BUTTON_POSITION = 6
    var ACCELEROMETER_POSITION = 7
    var CAM_POSITION = 8
    var LCD_POSITION = 9
    var DIGITIZER_POSITION = 1000
    var TEST_CALL_POSITION = 10
  //  var DEVICE_LOCK_POSITION = 14
    var BATTERY_POSITION = 11
    var NFC_POSITION = 12
    var TOUCH_POSITION = 10010
    var FINGER_POSITION = 13

    var currentActivity : BaseActivity ?= null
}