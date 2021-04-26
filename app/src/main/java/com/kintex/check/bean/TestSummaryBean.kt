package com.kintex.check.bean

data class TestSummaryBean(
    val action: Action
)

data class Action(
    val name: String,
    val udid:String,
    val test_case__list: ArrayList<TestCase>

)

data class TestCase(
    var rootName : String,
    val caseId: Int,
    val caseName: String,
    var description: String,
    val enable: Int,
    var result: Int
)