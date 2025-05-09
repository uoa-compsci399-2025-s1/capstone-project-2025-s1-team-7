package com.example.compsci399testproject
import com.example.compsci399testproject.utils.Node
import com.example.compsci399testproject.utils.NodeType
import com.example.compsci399testproject.utils.getPath
import com.example.compsci399testproject.utils.getRoomNodes
import com.example.compsci399testproject.utils.initialiseTestGraph
import org.junit.Test
import org.junit.Assert.*
class NavigationActivityTest {

class NavigationTests {
    @Test
    fun testGetRoomNodes() {

        val navigationGraph = initialiseTestGraph()

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
        val navigationGraph = initialiseTestGraph()

        // Create a starting location node
        val startNode = Node(
            id = "test_location",
            x = 1000,
            y = -968,
            floor = 2,
            type = NodeType.TRAVEL,
            edges = mutableListOf()
        )
        // Get the target node
        val targetNode = navigationGraph.findNode("302 280")

        // Get the path
        val startTime = System.currentTimeMillis()
        val path = getPath(startNode, targetNode, navigationGraph)
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        print("getPath took $duration ms")

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
    @Test
    fun testGetPathx10() {}
    @Test
    fun testIfOutside() {}
    @Test
    fun testDifferentFloors() {}
    @Test
    fun testElevatosr() {}
    @Test
    fun testStairs() {}
    @Test
    fun testGetClosestNodesAA() { // Annoying Areas eg. ping pong
    }
    @Test
    fun testRerouting() { // rerun from new location without changing destination)
    }

}