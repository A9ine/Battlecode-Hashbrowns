package potato;
import battlecode.common.*;

public strictfp abstract class Unit extends Robot {

    Unit(RobotController rc) throws GameActionException {
        super(rc);
    }

    static Direction randomDirection() {
        return directions[(int) (Math.random() * directions.length)];
    }

}