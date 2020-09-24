package com.kintex.check.bean

class KeyEventBean {

    var keyName = ""
    var keyEvent = 0

    constructor(keyName: String, keyEvent: Int) {
        this.keyName = keyName
        this.keyEvent = keyEvent
    }
}