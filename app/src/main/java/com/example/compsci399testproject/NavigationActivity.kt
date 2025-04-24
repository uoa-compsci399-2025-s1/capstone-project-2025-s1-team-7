package com.example.compsci399testproject

data class Node(
    val id: String,
    val x: Int,
    val y: Int,
    val floor: Int,
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

    fun addEdge(from: String, to: String, weight: Float) {
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
    path.reverse()

    if (shortestDistance[goal] != Int.MAX_VALUE) {
        println("Shortest path from ${start.id} to ${goal.id} is ${shortestDistance[goal]} with path: $path")
    }
    return (path)
}

fun main() {
    // Create an instance of NavigationGraph
    val navigationGraph = NavigationGraph()

    // Create nodes
    var nodes = mutableListOf<Node>()
    nodes.add(Node("a", mutableListOf()))
    nodes.add(Node("b", mutableListOf()))
    nodes.add(Node("c", mutableListOf()))
    nodes.add(Node("d", mutableListOf()))
    nodes.add(Node("e", mutableListOf()))


    // Add nodes to the graph
    for (node in nodes) {
        navigationGraph.addNodeToGraph(node)
    }

    // Add edges to the nodes
    var edges = mutableListOf<Edge>(

    )
    navigationGraph.addEdge("a", "b", 10.0f)
    navigationGraph.addEdge("a", "c", 3.0f)
    navigationGraph.addEdge("b", "c", 1.0f)
    navigationGraph.addEdge("b", "d", 2.0f)
    navigationGraph.addEdge("c", "b", 4.0f)
    navigationGraph.addEdge("c", "d", 8.0f)
    navigationGraph.addEdge("c", "e", 2.0f)
    navigationGraph.addEdge("d", "e", 7.0f)
    navigationGraph.addEdge("e", "d", 9.0f)




    // Print the graph to verify
    println("Nodes added to the graph successfully!")
    println(navigationGraph.graph)

    // Find a node
    val startNode = navigationGraph.findNode("a")
    val goalNode = navigationGraph.findNode("b")

    // Perform Dijkstra's algorithm
    val shortestPath = dijkstra(navigationGraph.graph, startNode, goalNode)
    println(shortestPath)
}