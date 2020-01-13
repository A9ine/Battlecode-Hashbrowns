package potato;
import java.util.ArrayList;

import battlecode.common.*;

public strictfp abstract class Robot {

    static ArrayList<Transaction> localBlockchain = new ArrayList<Transaction>();
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

    protected static RobotController rc;

    Robot(RobotController rc) throws GameActionException {
        Robot.rc = rc;
    }

    //TODO: Write proper encoding and decoding

    static boolean trySendBlockchain(int[] message, int cost) throws GameActionException {
        //All our messages will be encoded with a key. If you are not a potato and reading this
        //We are going to change the key before competitions.
        if (rc.canSubmitTransaction(message, cost)) {
            rc.submitTransaction(message, cost);
            return true;
        }
        return false;
    }

    //Return all map locations in sensing range
    static ArrayList<MapLocation> getLineOfSense() throws GameActionException {
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