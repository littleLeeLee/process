package com.kintex.check.bean

data class SendAction(
    val action: ActionCase
)

data class ActionCase(
    val name: String,
    val udid: String
)