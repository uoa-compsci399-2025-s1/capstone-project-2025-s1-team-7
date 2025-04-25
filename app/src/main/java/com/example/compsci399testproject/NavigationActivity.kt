package com.example.compsci399testproject
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// Data classes for JSON parsing
private data class NodeJson(
    val id: String,
    val x: Int,
    val y: Int,
    val floor: Int,
    val type: String,
    val edges: List<EdgeJson>?
)
private data class EdgeJson(
    val to: String,
    val weight: Float
)

enum class NodeType {
    ROOM,
    TRAVEL,
    STAIRS,
    ELEVATOR
}

data class Node(
    val id: String,
    val x: Int,
    val y: Int,
    val floor: Int,
    val type: NodeType,
    var edges: MutableList<Edge>
    ) {
    override fun toString(): String {
        return "Node(id='$id', edges=${edges.map { it.to.id }})"
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Node) return false
        return id == other.id
    }
}

data class Edge(
    val to: Node,
    val weight: Float
) {
    override fun toString(): String {
        return "Edge(to=${to.id}, weight=$weight)"
    }

    override fun hashCode(): Int {
        return to.id.hashCode() + weight.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Edge) return false
        return to.id == other.to.id && weight == other.weight
    }
}

class NavigationGraph {
    internal val graph = mutableMapOf<String, Node>()

    fun addNodeToGraph(node: Node) {
        graph[node.id] = node
    }

    fun findNode(id: String): Node {
        var retNode: Node? = null

        for (node in graph) {
            if (node.key == id) {
                retNode = node.value
                break
            }
        }
        return retNode ?: throw NoSuchElementException("Node with id $id not found")
    }
}

// Function to Load nodes from JSON
private fun loadNodesFromJson(context: Context): List<NodeJson> {
    val jsonString = context.assets.open("Nodes/nodes.json").bufferedReader().use { it.readText() }
    val type = object : TypeToken<List<NodeJson>>() {}.type
    return Gson().fromJson(jsonString, type)
}

// Function to initialise the graph
fun initialiseGraph(context: Context): NavigationGraph {
    val navigationGraph = NavigationGraph()
    val nodesJson = loadNodesFromJson(context)

    // Create nodes
    val nodes = nodesJson.map { nodeJson ->
        Node(
            id = nodeJson.id,
            x = nodeJson.x,
            y = nodeJson.y,
            floor = nodeJson.floor,
            type = when (nodeJson.type) {
                "ROOM" -> NodeType.ROOM
                "TRAVEL" -> NodeType.TRAVEL
                "STAIRS" -> NodeType.STAIRS
                "ELEVATOR" -> NodeType.ELEVATOR
                else -> throw IllegalArgumentException("Unknown node type: ${nodeJson.type}")
            },
            edges = mutableListOf()
        )
    }

    // Add nodes to the graph and edges to the nodes
    for (node in nodes) {
        navigationGraph.addNodeToGraph(node)

        val nodeJson = nodesJson.find { it.id == node.id }
        nodeJson?.edges?.forEach { edgeJson ->
            val toNode = nodes.find { it.id == edgeJson.to }
            if (toNode != null) {
                val edge = Edge(toNode, edgeJson.weight)
                node.edges.add(edge)
            }
        }
    }
    return navigationGraph
}

// Dijkstra's algorithm implementation
private fun dijkstra(graph: MutableMap<String, Node>, start: Node, goal: Node): MutableList<Node> {
    val unseenNodes = graph
    val predecessor = mutableMapOf<String, Node?>()
    val shortestDistance = mutableMapOf<Node, Int>()
    val path = mutableListOf<Node>()

    for ((_, node) in unseenNodes) {
        shortestDistance[node] = Int.MAX_VALUE
    }
    shortestDistance[start] = 0

    while (unseenNodes.isNotEmpty()) {
        var minNode: Node? = null
        for ((_, node) in unseenNodes) {
            if (minNode == null){
                minNode = node
            }else if ((shortestDistance[node] ?: Int.MAX_VALUE) < (shortestDistance[minNode] ?: Int.MAX_VALUE)) {
                minNode = node
            }
        }
        for (edge in minNode?.edges!!) {
            val newDistance = (shortestDistance[minNode] ?: Int.MAX_VALUE) + edge.weight
            if (newDistance < (shortestDistance[edge.to] ?: Int.MAX_VALUE)) {
                shortestDistance[edge.to] = newDistance.toInt()
                predecessor[edge.to.id] = minNode
            }
            unseenNodes.remove(minNode.id)
        }
    }
    var currentNode: Node? = goal
    while (currentNode != start){
        try {
            path.add(currentNode!!)
            currentNode = predecessor[currentNode.id]
        } catch (e: NoSuchElementException) {
            println("No path found")
            break
        }
    }

    path.add(start)
    path.reverse()

    if (shortestDistance[goal] != Int.MAX_VALUE) {
        println("Shortest path from ${start.id} to ${goal.id} is ${shortestDistance[goal]} with path: $path")
    }
    return (path)
}

private fun calculateDistance(x1: Int, y1: Int, x2: Int, y2: Int): Float {
    return kotlin.math.sqrt(((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)).toFloat())
}

private fun getClosestNodes(location: Node, navigationGraph: NavigationGraph, n: Int = 3) : List<Pair<Node, Float>> {
    return navigationGraph.graph.values
        .filter {it.floor == location.floor} // Only nodes on the same floor
        .map { node ->
            Pair(node, calculateDistance(location.x, location.y, node.x, node.y))
        }
        .sortedBy { it.second } // Sort by distance
        .take(n) // Take the closest n nodes
}

// Function to get the path between two nodes
fun getPath(startNode: Node, goal: Node, navigationGraph: NavigationGraph): MutableList<Node> {

    // link location to the graph
    val closestNodes = getClosestNodes(startNode, navigationGraph)
    for (edge in closestNodes) {
        startNode.edges.add(Edge(edge.first, edge.second.toFloat()))
    }

    // Find goal node in the graph
    val goalNode = navigationGraph.findNode(goal.id)

    // Get the shortest path
    val shortestPath = dijkstra(navigationGraph.graph, startNode, goalNode)

    return (shortestPath)
}
