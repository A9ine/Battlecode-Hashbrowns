package potato;
import battlecode.common.*;

public strictfp class Building extends Robot {

    static boolean tryBuild(RobotType type, Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canBuildRobot(type, dir)) {
            rc.buildRobot(type, dir);
            return true;
        } else return false;
    }

    Building(RobotController rc) {
        super(rc);
    }

    @Override
    public void run() throws GameActionException {
        
    }

}