package potato;
import java.util.*;
import java.util.stream.IntStream;

import battlecode.common.*;

public strictfp abstract class Robot {

    Robot(RobotController rc) throws GameActionException {
        Robot.rc = rc;
        team = rc.getTeam();
        turn = 0;
        miniMapWidth = (rc.getMapWidth()+rc.getMapWidth()%4)/4; 
        miniMapHeight = (rc.getMapHeight()+rc.getMapHeight()%4)/4;
        map = new int[miniMapWidth*(miniMapHeight+1)][4];

        myType = rc.getType(); 
        //[Elevation][Resources][Enemy][Friendly]
    }

    protected static RobotController rc;

    /* Updated Information */
    int turn;
    MapLocation myMapLocation;
    //Map is the big map split into 4 by 4 squares
    int[][] map;
    int miniMapWidth;
    int miniMapHeight;

    RobotInfo[] nearbyRobots;
    MapLocation[] nearbySoup;
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
        //Update map
        nearbyRobots = rc.senseNearbyRobots();
        nearbySoup = rc.senseNearbySoup();
        for (MapLocation loc : nearbySoup) {
            if (map[getMiniMapLocation(loc)][1] == 0) {
                tryBroadcastLocation(loc, cheapSend);
            }
            map[getMiniMapLocation(loc)][1] = rc.senseSoup(loc);
        }
        
        if (nearbySoup.length == 0) {
            map[getMiniMapLocation(myMapLocation)][1] = 0;
        }
        
        //Communication
        if (rc.getRoundNum() > 1) {
            latestCommunication = rc.getBlock(rc.getRoundNum()-1);
        }
        
    }

    //Navigation and minimap

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

    int getMiniMapLocation (MapLocation loc) {
        return loc.x/4 + loc.y/4*(miniMapWidth);
    }

    boolean hasSoup (int miniMapLoc) {
        return map[miniMapLoc][1]>0?true:false;
    }

    MapLocation locationInMiniMap(int miniMapLoc) {
        return new MapLocation((miniMapLoc%(miniMapWidth))*4+2,miniMapLoc/miniMapWidth*4+2);
    }

    //Communication Code

        //Broadcast info

            //TODO: Go compress the bits Richard
            
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
        int roboteam = 0;
        int robotype = 0;
        if (robot!=null) {
            roboteam = robot.getTeam()==team?0:1;
            switch (robot.getType()) {
                case HQ:                 robotype=1;                 break;
                case MINER:              robotype=2;                 break;
                case REFINERY:           robotype=3;                 break;
                case VAPORATOR:          robotype=4;                 break;
                case DESIGN_SCHOOL:      robotype=5;                 break;
                case FULFILLMENT_CENTER: robotype=6;                 break;
                case LANDSCAPER:         robotype=7;                 break;
                case DELIVERY_DRONE:     robotype=8;                 break;
                case NET_GUN:            robotype=9;                 break;
            }
        }

        secureSend(new int[] {
            0,
            flooded * 10000 + loc.x* 100 + loc.y,
            elevation,
            soup,
            roboteam*10+robotype, 
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
    /* !!! Don't use this function, its too expensive */
    ArrayList<MapLocation> getNear() throws GameActionException {
        System.out.println(Clock.getBytecodeNum());
        ArrayList<MapLocation> res = new ArrayList<MapLocation>();
        System.out.println(Clock.getBytecodeNum());
        int maxDistance = (int)Math.floor(Math.sqrt(rc.getCurrentSensorRadiusSquared()));
        System.out.println(Clock.getBytecodeNum());
        for (int x = -maxDistance; x < maxDistance;x++) {
            for (int y = -maxDistance; y < maxDistance; y++) {
                if (rc.canSenseLocation(myMapLocation.translate(x,y))) {
                    res.add(myMapLocation.translate(x,y));
                }
            }
        }
        System.out.println(Clock.getBytecodeNum());
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

    //Helper code

    static boolean tryBuild(RobotType type, Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canBuildRobot(type, dir)) {
            rc.buildRobot(type, dir);
            return true;
        } else return false;
    }



    public abstract void run() throws GameActionException;

}