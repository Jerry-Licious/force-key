package forcekey

import com.megacrit.cardcrawl.map.MapRoomNode

data class Point2D(val x: Int, val y: Int)

val MapRoomNode.coordinates
    get() = Point2D(this.x, this.y)