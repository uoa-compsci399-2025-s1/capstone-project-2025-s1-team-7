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
fun getPath(startNode: Node, goalNode: Node, navigationGraph: NavigationGraph): MutableList<Node> {

    // link location to the graph
    val closestNodes = getClosestNodes(startNode, navigationGraph)
    print(closestNodes)
    for (edge in closestNodes) {
        startNode.edges.add(Edge(edge.first, edge.second))
    }

    // Get the shortest path
    val shortestPath = dijkstra(navigationGraph.graph, startNode, goalNode)

    return (shortestPath)
}
fun dijkstra(graph: MutableMap<String, Node>, start: Node, goal: Node): MutableList<Node> {
    val startNode = Pair(start, 0f)
    var unvisitedNodes = graph.values.map { Pair(it, Float.POSITIVE_INFINITY) }.toMutableList()
    val pred = mutableMapOf<String, Node>()
    val path = mutableListOf<Node>()

    print("start: ${start.id} goal: ${goal.id}")
    print("unvisitedNodes: $unvisitedNodes")

    pred[start.id] = start
    while (unvisitedNodes.isNotEmpty()) {
        var minNode: Node = start.edges[0].to

        print ("minNode: ${minNode.id}")

        for (edge in minNode.edges) {
            if (edge.weight < minNode.

            }
        }
        unseenNodes.remove(minNode?.id)
        print(unseenNodes)

        for (edge in minNode?.edges!!) {
            val newDistance = shortestDistance[minNode]?.plus(edge.weight)
            if (newDistance != null) {
                if (newDistance < (shortestDistance[edge.to] ?: Float.POSITIVE_INFINITY)) {
                    shortestDistance[edge.to] = newDistance
                    predecessor[edge.to.id] = minNode
                }
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

    if (shortestDistance[goal] != Float.POSITIVE_INFINITY) {
        println("Shortest path from ${start.id} to ${goal.id} is ${shortestDistance[goal]} with path: $path")
    }
    return (path)
}