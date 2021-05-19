package com.kintex.check.bean

data class PrintBean(
    val action: PrintAction
)

data class PrintAction(
    val name: String,
    val print: String,
    val udid: String
)