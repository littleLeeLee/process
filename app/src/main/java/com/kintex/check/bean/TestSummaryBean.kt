package com.kintex.check.bean

data class TestSummaryBean(
    val action: Action,
    val params: ArrayList<TestCase>
)

data class Action(
    val name: String,
    val udid:String


)

data class TestCase(
    val caseId: Int,
    val caseName: String,
    var description: String,
    val enable: Int,
    var result: Int
)