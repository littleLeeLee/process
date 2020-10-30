package com.kintex.check.bean

data class ReceiveAdbBean(
    val action: ReceiveAction
)

data class ReceiveAction(
    val name: String,
    val udid: String,
    val test_case_list: List<ReceiveTestCase>

)

data class ReceiveTestCase(
    val caseId: Int,
    val caseName: String,
    val enable: Int,
    val visible: Int
)