package potato;
import battlecode.common.*;

public strictfp class Unit extends Robot {

    static Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST
    };

    Unit(RobotController rc) {
        super(rc);
    }

}