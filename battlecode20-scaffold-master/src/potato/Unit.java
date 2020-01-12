package potato;
import battlecode.common.*;

public strictfp abstract class Unit extends Robot {

    static boolean tryNavigate(MapLocation target) throws GameActionException {
        Direction dir = rc.getLocation().directionTo(target);
        if (rc.canMove(dir)) {
            if (rc.senseFlooding(rc.adjacentLocation(dir)) && rc.getType() != RobotType.DELIVERY_DRONE) {
                return false;
            }
            rc.move(dir);
            return true;
        } 
        return false;
    }

    int elevationMuliplier; //Movement costs for moving over elevation

    Unit(RobotController rc) {
        super(rc);
    }

}