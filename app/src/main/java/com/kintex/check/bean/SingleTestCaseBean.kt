package com.kintex.check.bean

data class SingleTestCaseBean(
    val action: SingleAction,
    val params: List<Param>
)

data class SingleAction(
    val name: String,
    val udid: String
)

data class Param(
    val caseId: Int,
    val caseName: String,
    val result: Int
)