package com.kintex.check.bean

class CaseResultBean {

    var result =0
    var caseId = -1
    var type = 0
    var dis = ""
    constructor(caseId: Int,result: Int,type:Int) {
        this.result = result
        this.caseId = caseId
        this.type = type
    }
}