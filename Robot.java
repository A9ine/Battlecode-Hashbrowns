package potato;
import java.util.ArrayList;

import battlecode.common.*;

public strictfp abstract class Robot {

    Robot(RobotController rc) throws GameActionException {
        Robot.rc = rc;
    }

    protected static RobotController rc;
    static int KEY = 69420;
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

    //Communication Code

    static boolean trySendBlockchain(int[] message, int cost) throws GameActionException {
        if (rc.canSubmitTransaction(message, cost)) {
            rc.submitTransaction(message, cost);
            return true;
        }
        return false;
    }

    //Sensing Code

    //Return all map locations in sensing range
    static ArrayList<MapLocation> getNear() throws GameActionException {
        ArrayList<MapLocation> res = new ArrayList<MapLocation>();
        MapLocation currentLoc = rc.getLocation();
        int maxDistance = (int)Math.floor(Math.sqrt(rc.getCurrentSensorRadiusSquared()));
        for (int x = Math.max(currentLoc.x-maxDistance,0); x < Math.min(currentLoc.x + maxDistance,rc.getMapWidth() - 1);x++) {
            for (int y = Math.max(currentLoc.x-maxDistance,0); y < Math.min(currentLoc.y + maxDistance,rc.getMapHeight() - 1);y++) {
                MapLocation newLoc = new MapLocation(x,y);
                if (rc.canSenseLocation(newLoc)) {
                    res.add(newLoc);
                }
            }
        }
        return res;
    }

    public abstract void run() throws GameActionException;

}