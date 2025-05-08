package com.example.compsci399testproject.utils

fun getClosestNodes(location: Node, navigationGraph: NavigationGraph, n: Int = 3) : List<Pair<Node, Float>> {
    return navigationGraph.graph.values
        .filter {it.floor == location.floor} // Only nodes on the same floor
        .map { node ->
            Pair(node, calculateDistance(location.x, location.y, node.x, node.y))
        }
        .sortedBy { it.second } // Sort by distance
        .take(n) // Take the closest n nodes
}
private fun calculateDistance(x1: Int, y1: Int, x2: Int, y2: Int): Float {
    return kotlin.math.sqrt(((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)).toFloat())
}
fun getRoomNodes(navigationGraph: NavigationGraph): List<Node> {
    return navigationGraph.graph.values
        .filter { it.type == NodeType.ROOM }
        .toList()
}
fun getPath(startNode: Node, goal: Node, navigationGraph: NavigationGraph): MutableList<Node> {

    // link location to the graph
    val closestNodes = getClosestNodes(startNode, navigationGraph)
    for (edge in closestNodes) {
        startNode.edges.add(Edge(edge.first, edge.second))
    }

    // Find goal node in the graph
    val goalNode = navigationGraph.findNode(goal.id)

    // Get the shortest path
    val shortestPath = dijkstra(navigationGraph.graph, startNode, goalNode)

    return (shortestPath)
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
        unseenNodes.remove(minNode?.id)

        for (edge in minNode?.edges!!) {
            val newDistance = (shortestDistance[minNode] ?: Int.MAX_VALUE) + edge.weight
            if (newDistance < (shortestDistance[edge.to] ?: Int.MAX_VALUE)) {
                shortestDistance[edge.to] = newDistance.toInt()
                predecessor[edge.to.id] = minNode
            }

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