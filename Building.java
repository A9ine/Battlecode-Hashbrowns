package potato;
import battlecode.common.*;

public strictfp class Building extends Robot {

    Building(RobotController rc) throws GameActionException {
        super(rc);
    }

    static boolean tryBuild(RobotType type, Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canBuildRobot(type, dir)) {
            rc.buildRobot(type, dir);
            return true;
        } else return false;
    }

    @Override
    public void run() throws GameActionException {
        update();
    }

}