package com.kintex.check.bean

class CameraBean {

    var cameraId = ""
    var cameraState = "Pass"
    var cameraName = ""

    constructor(cameraId: String, cameraState: String, cameraName: String) {
        this.cameraId = cameraId
        this.cameraState = cameraState
        this.cameraName = cameraName
    }
}