package potato;
import battlecode.common.*;

public strictfp abstract class Unit extends Robot {

    static Direction randomDirection() {
        return directions[(int) (Math.random() * directions.length)];
    }

    static MapLocation hqLoc;

    static void communicationsInit() {
        for (int i = 0; i < Math.min(10,rc.getRoundNum()); i ++) {
            Transaction[] recentBlock = rc.getBlock(i);
        }
    }

    static boolean tryNavigate(MapLocation target) throws GameActionException {
        Direction dir = rc.getLocation().directionTo(target);
        if (rc.getLocation().isWithinDistanceSquared(target,2)) {
            return false;
        }
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

    Unit(RobotController rc) throws GameActionException {
        super(rc);
    }

}