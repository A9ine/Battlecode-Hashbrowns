package potato;
import java.util.*;
import java.util.stream.IntStream;

import battlecode.common.*;

public strictfp abstract class Robot {

    Robot(RobotController rc) throws GameActionException {
        Robot.rc = rc;
        team = rc.getTeam();
        turn = 0;
        map = new int[rc.getMapWidth()][rc.getMapHeight()][3];
        myType = rc.getType(); 
        //[Elevation][Resources][Flooding = -1; Normal = 0; Building = 1; ]
    }

    protected static RobotController rc;

    /* Updated Information */
    int turn;
    MapLocation myMapLocation;
    int[][][] map;
    ArrayList<MapLocation> sensedMapLocations;
    RobotInfo[] nearbyRobots;
    //Communication
    Transaction[] latestCommunication;
    //TODO: Implement dynamic costs
    //Someone do this in local
    int fastSend = 30;
    int averageSend = 10;
    int cheapSend = 2;

    /* General Information */
    MapLocation hqLoc;
    int KEY = 69420;
    Team team;
    RobotType myType;
    int symmetry = 0; //0 is I don't know, 1 is hor, 2 is ver, 3 is 45 degrees

    //This method will run each turn, with the exception of turn 1
    void update() throws GameActionException {
        turn += 1;
        myMapLocation = rc.getLocation();
        sensedMapLocations = getNear();
        //Update local map
        for (MapLocation processingMapLocation : sensedMapLocations) {
            map[processingMapLocation.x][processingMapLocation.y][0] = rc.senseElevation(processingMapLocation);
            map[processingMapLocation.x][processingMapLocation.y][1] = rc.senseSoup(processingMapLocation);
            map[processingMapLocation.x][processingMapLocation.y][2] = 0;
            if (rc.senseFlooding(processingMapLocation)) {
                map[processingMapLocation.x][processingMapLocation.y][2] = -1;
            }
        }
        //Update Robots
        nearbyRobots = rc.senseNearbyRobots();
        for (RobotInfo robot : nearbyRobots) {
            if (Arrays.asList(BUILDINGS).contains(robot.type)) {
                map[robot.getLocation().x][robot.getLocation().y][2] = 1;
            }
        }
        //Communication
        latestCommunication = rc.getBlock(rc.getRoundNum()-1);
    }

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
    static RobotType[] BUILDINGS = new RobotType[] {
        RobotType.DESIGN_SCHOOL,
        RobotType.HQ,
        RobotType.NET_GUN,
        RobotType.REFINERY,
        RobotType.VAPORATOR,
        RobotType.FULFILLMENT_CENTER
    };


    //Communication Code

        //Broadcast info

            //TODO: Go compress the bits Richard
            
            //[Action] 
                // 0 --> Data
                // 1 --> Build
                // 2 --> Wall
                // 3 --> Attack
                // 4 --> Transport
            //[x + y]
            //[Elevation]
            //[Soup]
            //[Robot Data (Team + Type)]
            //[Extra Data]

    private boolean secureSend(int[] message, int cost) throws GameActionException {
        //TODO: Implement better encryption
        return(trySendBlockchain(new int[] {message[0],message[1],message[2],message[3],message[4],message[5],KEY}, cost));
    }

    private boolean trySendBlockchain(int[] message, int cost) throws GameActionException {
        if (rc.canSubmitTransaction(message, cost)) {
            rc.submitTransaction(message, cost);
            return true;
        }
        return false;
    }

    boolean tryBroadcastLocation(MapLocation loc, int cost) throws GameActionException {
        System.out.println("Trying to send a location");
        int soup = rc.senseSoup(loc);
        int elevation = rc.senseElevation(loc);
        int flooded = rc.senseFlooding(loc)?1:0;
        RobotInfo robot = rc.senseRobotAtLocation(loc);
        secureSend(new int[] {
            0,
            flooded * 10000 + loc.x* 100 + loc.y,
            elevation,
            soup,
            0, //TODO: Add robot Data
            0
        }, cost);
        return true;

    }

    int[] getInformation (Transaction trans) {
            //[Action] 
                // 0 --> Data
                // 1 --> Build
                // 2 --> Wall
                // 3 --> Attack
                // 4 --> Transport
            //[flooded + x + y]
            //[Elevation]
            //[Soup]
            //[Robot Data (Team + Type)]
            //[Extra Data]
        //TODO: Bit compression comprehension
        return 	trans.getMessage();
        
    }



    //Sensing Code

    //Return all map locations in sensing range
    ArrayList<MapLocation> getNear() throws GameActionException {
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

    ArrayList<MapLocation> getAdjacent() throws GameActionException {
        ArrayList<MapLocation> res = new ArrayList<MapLocation>();
        for (Direction dir : directions) {
            if (rc.onTheMap(rc.adjacentLocation(dir))) {
                res.add(rc.adjacentLocation(dir));
            }
        }
        return res;
    }



    public abstract void run() throws GameActionException;

}