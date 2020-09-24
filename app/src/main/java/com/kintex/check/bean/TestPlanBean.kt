package com.kintex.check.bean

import com.kintex.check.R

class TestPlanBean {

     var planName = ""
     var planDescription=""
     var planPic = R.mipmap.wifi
     var planResult = 0
     var clickState = false

    constructor(planName: String, planDescription: String, planPic: Int, planResult: Int) {
        this.planName = planName
        this.planDescription = planDescription
        this.planPic = planPic
        this.planResult = planResult
    }




}