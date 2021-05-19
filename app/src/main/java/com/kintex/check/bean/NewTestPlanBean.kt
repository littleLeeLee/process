package com.kintex.check.bean

data class NewTestPlanBean(
    val action: StartAction
)

data class StartAction(
    val name: String,
    val operations: List<Operation>,
    val udid: String
)

data class Operation(
    val testTypeName: String,
    val types: List<CaseType>
)

data class CaseType(
        val typeId :Int =0,
        val name: String,
        val typeItems: List<TypeItem>,
        var state : Int ? = 2
)

data class TypeItem(
        val caseId: Int,
        val caseName: String,
        val enable: Int,
        var visible: Int,
        var result : Int ?= 2,
        var description : String ?= ""
)