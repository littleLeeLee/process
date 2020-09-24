package com.kintex.check.utils

object ResultCode {

    //测试状态码
    var DEFAULT = 0
    var PASSED = 1
    var FAILED = 2
    var RESET = -1

    //测试项对应的position
    var WIFI_POSITION = 0
    var BLUETOOTH_POSITION = 1
    var GPS_POSITION = 2
    var PROXIMITY_POSITION = 3
    var BUTTON_POSITION = 4
    var VIBRATION_POSITION = 5
    var ACCELEROMETER_POSITION = 6
    var FRONT_CAM_POSITION = 7
    var NEAR_CAM_POSITION = 8
    var MIC_LOUD_POSITION = 9
    var MIC_EAR_POSITION = 10
    var HEADSET_POSITION = 11
    var DIGITIZER_POSITION = 12
    var TEST_CALL_POSITION = 12
    var DEVICE_LOCK_POSITION = 12


}