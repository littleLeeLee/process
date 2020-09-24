package com.kintex.check.bean

import com.kintex.check.utils.ResultCode.FAILED

class TestResultBean {

    var position = 0
    var result = FAILED

    constructor(position: Int, result: Int) {
        this.position = position
        this.result = result
    }
}