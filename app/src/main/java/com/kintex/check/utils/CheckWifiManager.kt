package com.kintex.check.utils

import com.blankj.utilcode.util.NetworkUtils

object CheckWifiManager {

    fun checkWifiIsConnect() : Boolean{

        return NetworkUtils.isWifiConnected()
    }

}