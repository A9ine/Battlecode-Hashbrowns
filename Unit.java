package potato;
import battlecode.common.*;

public strictfp abstract class Unit extends Robot {

    boolean isMoving;
    MapLocation destination;
    int state;
    /*
    General
    0 --> Nothing
    1 --> Exploring
    2 --> Attacking
    3 --> Defending

    Miner
    51 --> Mining
    52 --> Refining
    53 --> Building
    */

    Unit(RobotController rc) throws GameActionException {
        super(rc);
    }

    /*
    Bug Navigation
    
    This type of navigation will mostly be used with miners, 
    and also landscapers who are near friendly buildings so 
    they don't try to tear down everything.

    */

    boolean canMoveWithoutSuicide(Direction dir) {
        if (rc.canMove(dir) && !rc.senseFlooding(rc.adjacentLocation(dir))) {
            return true;
        }
        return false;
    }

    void bugNavigate(MapLocation target) {

        if (rc.isReady()) {

            if (rc.canMoveWithoutSuicide(myMapLocation.directionTo(target))) {
                rc.move(myMapLocation.directionTo(target));
                return;
            }
            if (myMapLocation.y < target.y && rc.canMoveWithoutSuicide(Direction.NORTH)) {
                rc.move(directionTo);
                return;
            }
            
        }

    }



    static Direction randomDirection() {
        return directions[(int) (Math.random() * directions.length)];
    }

}