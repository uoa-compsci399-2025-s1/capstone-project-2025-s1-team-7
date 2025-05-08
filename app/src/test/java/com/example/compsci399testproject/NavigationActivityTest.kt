package com.example.compsci399testproject
import com.example.compsci399testproject.utils.Edge
import com.example.compsci399testproject.utils.Node
import com.example.compsci399testproject.utils.NodeType
import com.example.compsci399testproject.utils.getPath
import com.example.compsci399testproject.utils.getRoomNodes
import com.example.compsci399testproject.utils.initialiseGraph
import org.junit.Test
import org.junit.Assert.*
class NavigationActivityTest {
    @Test
    fun testGetRoomNodes() {

        val navigationGraph = initialiseGraph()

        // Add test nodes with different types
        val roomNode = Node("room1", 0, 0, 1, NodeType.ROOM, mutableListOf())
        val travelNode = Node("travel1", 1, 1, 1, NodeType.TRAVEL, mutableListOf())
        val stairsNode = Node("stairs1", 2, 2, 1, NodeType.STAIRS, mutableListOf())

        navigationGraph.addNodeToGraph(roomNode)
        navigationGraph.addNodeToGraph(travelNode)
        navigationGraph.addNodeToGraph(stairsNode)

        // Test the function
        val roomNodes = getRoomNodes(navigationGraph)

        // Assert results
        assertEquals(1, roomNodes.size)
        assertEquals(NodeType.ROOM, roomNodes[0].type)
        assertEquals("room1", roomNodes[0].id)
    }

    @Test
    fun testGetPath() {
        // Create test navigation graph with test data
        val navigationGraph = initialiseGraph(useTestData = true)

        // Create a starting location node
        val startNode = Node(
            id = "test_location",
            x = 1000,
            y = -968,
            floor = 2,
            type = NodeType.TRAVEL,
            edges = mutableListOf()
        )
        navigationGraph.addNodeToGraph(startNode)

        // Connect start node to nearest node
        val travel1 = navigationGraph.findNode("travel1")
        val distance = calculateDistance(startNode, travel1)
        startNode.edges.add(Edge(travel1, distance))
        travel1.edges.add(Edge(startNode, distance))

        // Get the target node
        val targetNode = navigationGraph.findNode("302 280")

        // Get the path
        val path = getPath(startNode, targetNode, navigationGraph)

        // Assertions
        assertNotNull(path)
        assertTrue(path.isNotEmpty())
        assertEquals("test_location", path.first().id)
        assertEquals("302 280", path.last().id)

        // Verify path connectivity
        for (i in 0 until path.size - 1) {
            val currentNode = path[i]
            val nextNode = path[i + 1]
            assertTrue(currentNode.edges.any { it.to == nextNode })
        }
    }

    private fun calculateDistance(node1: Node, node2: Node): Float {
        val dx = node1.x - node2.x
        val dy = node1.y - node2.y
        return kotlin.math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }
}