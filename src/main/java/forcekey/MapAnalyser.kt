package forcekey

import com.megacrit.cardcrawl.dungeons.AbstractDungeon
import com.megacrit.cardcrawl.map.MapRoomNode

class MapAnalyser(val map: ArrayList<ArrayList<MapRoomNode>>) {
    companion object {
        @JvmStatic
        fun nodeReachableFromCurrentNode(predicate: (MapRoomNode) -> Boolean) =
            MapAnalyser(AbstractDungeon.map).nodeReachableFrom(AbstractDungeon.currMapNode.coordinates, predicate)
    }

    fun mapHasEmeraldKey() = map.any { list -> list.any { it.hasEmeraldKey } }

    fun getMapRoomNode(coordinate: Point2D) = map[coordinate.y][coordinate.x]

    // Returns the node coordinates that this node can reach in one floor.
    fun getNodesCoordinatesOneFloorFrom(nodeCoordinates: Point2D): ArrayList<Point2D> = getMapRoomNode(nodeCoordinates).edges
        .map { edge -> Point2D(edge.dstX, edge.dstY) }
        // At the end of each map, the last floor of rest sites have edges pointed towards the boss room, which is not
        // on the map. Remove nodes that are not in the map.
        .filter { coordinates -> coordinates.y < map.size }
        .toCollection(arrayListOf())

    // Returns the node coordinates that this set of node can reach in one floor.
    fun getNodesCoordinatesOneFloorFrom(nodesCoordinates: ArrayList<Point2D>) = ArrayList(nodesCoordinates
        // Find all reachable nodes from each node.
        .map { nodeCoordinates -> getNodesCoordinatesOneFloorFrom(nodeCoordinates) }
        // Collect them into one list.
        .fold(listOf()) { acc: List<Point2D>, nodes: ArrayList<Point2D> -> acc + nodes }
        // Remove duplicates.
        .distinct())

    fun getAllReachableNodesCoordinatesFrom(nodeCoordinates: Point2D): ArrayList<Point2D> {
        val allReachableNodes = arrayListOf<Point2D>()

        var reachableNodesNextFloor = getNodesCoordinatesOneFloorFrom(nodeCoordinates)
        do {
            allReachableNodes.addAll(reachableNodesNextFloor)
            reachableNodesNextFloor = getNodesCoordinatesOneFloorFrom(reachableNodesNextFloor)
        } while (reachableNodesNextFloor.isNotEmpty())

        return allReachableNodes
    }

    fun getAllReachableNodesFrom(nodeCoordinates: Point2D) =
        getAllReachableNodesCoordinatesFrom(nodeCoordinates).map { coordinates -> getMapRoomNode(coordinates) }

    fun nodeReachableFrom(nodeCoordinates: Point2D, predicate: (MapRoomNode) -> Boolean) =
        getAllReachableNodesFrom(nodeCoordinates).any(predicate)
}