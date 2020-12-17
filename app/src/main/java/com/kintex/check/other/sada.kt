package com.kintex.check.other

data class sada(
    val action: Action
)

data class Action(
    val name: String,
    val operations: List<Operation>,
    val udid: String
)

data class Operation(
    val testTypeName: String,
    val types: List<Type>
)

data class Type(
    val name: String,
    val typeItems: List<TypeItem>
)

data class TypeItem(
    val caseId: Int,
    val caseName: String,
    val enable: Int,
    val visible: Int
)