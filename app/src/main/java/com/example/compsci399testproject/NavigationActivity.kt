package com.example.compsci399testproject

import androidx.annotation.Nullable

data class Node(
    val id: String,
    // val latitude: Double,
    // val longitude: Double,
    // val floor: Int,
    var edges: MutableList<Edge>
    // val type: NodeType // ROOM, HALLWAY, STAIRS, ELEVATOR                    Add later
)

data class Edge(
    val to: Node,
    val weight: Double
)

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

    fun addEdge(from: String, to: String, weight: Double) {
        val toNode = findNode(to)
        val edge = Edge(toNode, weight)
        findNode(from).edges.add(edge)
    }
}




fun dijkstra(graph: MutableMap<String, Node>, start: Node, goal: Node): MutableList<Node> {
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
    if (shortestDistance[goal] != Int.MAX_VALUE) {
        println("Shortest path from ${start.id} to ${goal.id} is ${shortestDistance[goal]} with path: $path")
    }
    return (path)
}

val graph = mutableMapOf(
    'a' to mapOf('b' to 10, 'c' to 3),
    'b' to mapOf('c' to 1, 'd' to 2),
    'c' to mapOf('b' to 4, 'd' to 8, 'e' to 2),
    'd' to mapOf('e' to 7),
    'e' to mapOf('d' to 9)
)
fun main() {
    // Create an instance of NavigationGraph
    val navigationGraph = NavigationGraph()

    // Create nodes
    val nodeA = Node("a", mutableListOf())
    val nodeB = Node("b", mutableListOf())
    val nodeC = Node("c", mutableListOf())
    val nodeD = Node("d", mutableListOf())
    val nodeE = Node("e", mutableListOf())

    // Add edges to the nodes
    navigationGraph.addEdge("a", "b", 10.0)
    navigationGraph.addEdge("a", "c", 3.0)
    navigationGraph.addEdge("b", "c", 1.0)
    navigationGraph.addEdge("b", "d", 2.0)
    navigationGraph.addEdge("c", "b", 4.0)
    navigationGraph.addEdge("c", "d", 8.0)
    navigationGraph.addEdge("c", "e", 2.0)
    navigationGraph.addEdge("d", "e", 7.0)
    navigationGraph.addEdge("e", "d", 9.0)


    // Add nodes to the graph
    navigationGraph.addNodeToGraph(nodeA)
    navigationGraph.addNodeToGraph(nodeB)
    navigationGraph.addNodeToGraph(nodeC)
    navigationGraph.addNodeToGraph(nodeD)
    navigationGraph.addNodeToGraph(nodeE)

    // Print the graph to verify
    println("Nodes added to the graph successfully!")

    // Find a node
    val startNode = navigationGraph.findNode("a")
    val goalNode = navigationGraph.findNode("b")

    // Perform Dijkstra's algorithm
    val shortestPath = dijkstra(navigationGraph.graph, startNode, goalNode)
}