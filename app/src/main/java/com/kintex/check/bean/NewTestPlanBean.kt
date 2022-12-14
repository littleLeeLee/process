package com.kintex.check.bean

data class NewTestPlanBean(
    val action: StartAction
)

data class StartAction(
    val name: String,
    val operations: ArrayList<Operation>,
    val udid: String
)

data class Operation(
    val testTypeName: String,
    var types: ArrayList<CaseType>
)

data class CaseType(
        val typeId :Int =0,
        var name: String,
        var typeItems: ArrayList<TypeItem>,
        var state : Int ? = 2
)

data class TypeItem(
        val caseId: Int,
        var caseName: String,
        val enable: Int,
        var visible: Int,
        var result : Int ?= 2,
        var desName :String,
        var description : String ?= ""
)