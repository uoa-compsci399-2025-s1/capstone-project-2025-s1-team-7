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
fun loadNodesFromJson(context: Context): List<NodeJson> {
    val jsonString = context.assets.open("Nodes/nodes.json").bufferedReader().use { it.readText() }
    val type = object : TypeToken<List<NodeJson>>() {}.type
    return Gson().fromJson(jsonString, type)
}
