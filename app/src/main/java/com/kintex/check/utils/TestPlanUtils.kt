package com.kintex.check.utils

import com.kintex.check.bean.TestCase
import com.kintex.check.bean.TestPlanBean

object TestPlanUtils {

    open fun handleTestPlan(planList : ArrayList<TestPlanBean>) : ArrayList<TestPlanBean> {

        for (k in planList.indices) {
            if( planList[k].resultItemList.size>0){
                planList[k].resultItemList.clear()
            }
            when (k) {
                ResultCode.WIFI_POSITION -> {
                    planList[k].resultItemList.add(TestCase("WiFi", 3, "WiFi", "", 1, 0))
                }
                ResultCode.BLUETOOTH_POSITION -> {

                    planList[k].resultItemList.add(TestCase("BlueTooth", 5, "BlueTooth", "", 1, 0))

                }
                ResultCode.GPS_POSITION -> {
                    planList[k].resultItemList.add(TestCase("GPS", 4, "GPS", "", 1, 0))
                }

                ResultCode.PROXIMITY_POSITION -> {

                    planList[k].resultItemList.add( TestCase("Proximity Sensor",8,"Proximity Sensor","",1,0))
                }

                ResultCode.BUTTON_POSITION -> {

                    planList[k].resultItemList.add( TestCase("Buttons",30,"Power Button","",1,0))
                    planList[k].resultItemList.add( TestCase("Buttons",31,"Home Button","",1,0))
                    planList[k].resultItemList.add( TestCase("Buttons",32,"Volume Down Button","",1,0))
                    planList[k].resultItemList.add( TestCase("Buttons",33,"Volume Up Button","",1,0))
                    planList[k].resultItemList.add( TestCase("Buttons",35,"Back Button","",1,0))
                    planList[k].resultItemList.add( TestCase("Buttons",36,"Menu Button","",1,0))
                    planList[k].resultItemList.add( TestCase("Buttons",34,"Flip Switch","",0,0))

                }

                ResultCode.VIBRATION_POSITION -> {

                    planList[k].resultItemList.add( TestCase("Vibration", 45, "Vibration", "", 1, 0))

                }

                ResultCode.ACCELEROMETER_POSITION -> {

                    planList[k].resultItemList.add( TestCase("Accelerometer", 9, "Accelerometer", "", 1, 0))
                    planList[k].resultItemList.add( TestCase("Accelerometer", 50, "Light sensor", "", 1, 0))
                    planList[k].resultItemList.add( TestCase("Accelerometer", 6, "Gyroscope", "", 1, 0))
                    planList[k].resultItemList.add( TestCase("Accelerometer", 51, "Screen Rotation", "", 1, 0))

                }

                ResultCode.CAM_POSITION -> {

                    planList[k].resultItemList.add( TestCase("Camera",23,"Front Camera","",1,0))
                    planList[k].resultItemList.add( TestCase("Camera",24,"Rear Camera","",1,0))
                    planList[k].resultItemList.add( TestCase("Camera",25,"Flash","",1,0))

                }

                ResultCode.MIC_LOUD_POSITION -> {

                    planList[k].resultItemList.add( TestCase("Loud Speaker",11,"Loud Speaker","",1,0))
                    planList[k].resultItemList.add( TestCase("Loud Speaker",15,"Microphone","",1,0))
                    planList[k].resultItemList.add( TestCase("Loud Speaker",16,"Video Microphone","",1,0))

                }

                ResultCode.MIC_EAR_POSITION -> {

                    planList[k].resultItemList.add( TestCase("Earpiece",12,"Earpiece","",1,0))

                }
                ResultCode.HEADSET_POSITION -> {

                    planList[k].resultItemList.add( TestCase("Headset Port",20,"Headset Port","",1,0))
                    planList[k].resultItemList.add( TestCase("Headset Port",18,"Headset-Left","",1,0))
                    planList[k].resultItemList.add( TestCase("Headset Port",19,"Headset-Right","",1,0))

                }

                ResultCode.LCD_POSITION -> {

                    planList[k].resultItemList.add( TestCase("LCD",37,"LCD","",1,0))

                }

                ResultCode.DIGITIZER_POSITION -> {

                    planList[k].resultItemList.add( TestCase("Digitizer",38,"Touch Screen","",1,0))

                }

                ResultCode.TEST_CALL_POSITION -> {

                    planList[k].resultItemList.add( TestCase("Dial",1,"SimReader","",1,0))
                    planList[k].resultItemList.add( TestCase("Dial",53,"Dial","",1,0))

                }

                ResultCode.BATTERY_POSITION -> {

                    planList[k].resultItemList.add( TestCase("BatteryHealth",52,"BatteryHealth","",1,0))

                }

                ResultCode.NFC_POSITION -> {

                    planList[k].resultItemList.add( TestCase("NFC",48,"NFC","",1,0))

                }

                ResultCode.TOUCH_POSITION -> {

                    planList[k].resultItemList.add( TestCase("Multi Touch",49,"Multi Touch","",1,0))

                }

                ResultCode.FINGER_POSITION -> {

                    planList[k].resultItemList.add( TestCase("Fingerprint Sensor",27,"Fingerprint","",1,0))

                }
            }
        }
        return planList
    }

}