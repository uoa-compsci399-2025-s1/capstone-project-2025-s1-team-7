package com.example.compsci399testproject.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class NodeJson(
    val id: String,
    val x: Int,
    val y: Int,
    val floor: Int,
    val type: String,
    val edges: List<EdgeJson>?
)
data class EdgeJson(
    val to: String,
    val weight: Float
)
fun loadNodesFromJson(): List<NodeJson> {
    val jsonString = object {}.javaClass.getResource("/Nodes/nodes.json")?.readText()
        ?: throw IllegalStateException("Could not load nodes.json")
    val type = object : TypeToken<List<NodeJson>>() {}.type
    return Gson().fromJson(jsonString, type)
}
fun loadTestNodes(): List<NodeJson> {
    return listOf(
        NodeJson(
            id = "302 280",
            x = 280,
            y = 302,
            floor = 2,
            type = "ROOM",
            edges = listOf(
                EdgeJson("travel1", 1.0f)
            )
        ),
        NodeJson(
            id = "travel1",
            x = 400,
            y = 0,
            floor = 2,
            type = "TRAVEL",
            edges = listOf(
                EdgeJson("302 280", 1.0f)
            )
        )
    )
}