package com.kintex.check.bean

import com.kintex.check.utils.ResultCode.FAILED

class TestResultBean {

    var position = 0
    var result = FAILED
    var description : String = ""
    var itemCaseList : ArrayList<TestCase>?=null
    constructor(position: Int, result: Int,itemCaseList : ArrayList<TestCase>?) {
        this.position = position
        this.result = result
        this.itemCaseList = itemCaseList
    }
}