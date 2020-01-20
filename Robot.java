package potato;
import java.util.*;
import java.util.stream.IntStream;

import battlecode.common.*;

public strictfp abstract class Robot {

    Robot(RobotController rc) throws GameActionException {
        Robot.rc = rc;
        team = rc.getTeam();
        // So I can run the robots against each other
        if (team == Team.B) {
            KEY += 1;
        }
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

            for (Transaction trans : latestCommunication) {
                int[] message = getInformation(trans);
                if (message[6] != KEY) {
                    continue;
                }
                if (message[0] == 0) {
                    MapLocation loc = new MapLocation((message[1]%10000-message[1]%100)/100, message[1]%100);
                    map[getMiniMapLocation(loc)][1] = message[3];
                }
            }
            
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
                // 5 --> Sucess
                // 6 --> Drone Wall
            //[flooded + x + y]
            //[Elevation]
            //[Soup]
            //[Robot Data (Team + Type)]
            //[Order ID]

    private boolean secureSend(int[] message, int cost) throws GameActionException {
        //TODO: Implement better encryption
        return(trySendBlockchain(new int[] {message[0],message[1],message[2],message[3],message[4],message[5],KEY}, cost));
    }

    int getRobotTypeID(RobotType type) {
        int robotype = 0;
        switch (type) {
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
        return robotype;
    }

    RobotType getRobotTypeFromID(int ID) {
        RobotType robotype = null;
        switch (ID) {
            case 1:                 robotype=RobotType.HQ;                           break;
            case 2:                 robotype=RobotType.MINER;                        break;
            case 3:                 robotype=RobotType.REFINERY;                     break;
            case 4:                 robotype=RobotType.VAPORATOR;                    break;
            case 5:                 robotype=RobotType.DESIGN_SCHOOL;                break;
            case 6:                 robotype=RobotType.FULFILLMENT_CENTER;           break;
            case 7:                 robotype=RobotType.LANDSCAPER;                   break;
            case 8:                 robotype=RobotType.DELIVERY_DRONE;               break;
            case 9:                 robotype=RobotType.NET_GUN;                      break;
        }
        return robotype;
    }

    private boolean trySendBlockchain(int[] message, int cost) throws GameActionException {
        if (rc.canSubmitTransaction(message, cost)) {
            rc.submitTransaction(message, cost);
            return true;
        }
        return false;
    }

    boolean tryBroadcastLocation(MapLocation loc, int cost) throws GameActionException {
        if (!rc.canSenseLocation(loc)) {
            return false;
        }
        //System.out.println("Trying to send a location");
        int soup = rc.senseSoup(loc);
        int elevation = rc.senseElevation(loc);
        int flooded = rc.senseFlooding(loc)?1:0;
        RobotInfo robot = rc.senseRobotAtLocation(loc);
        int roboteam = 0;
        int robotype = 0;
        if (robot!=null) {
            roboteam = robot.getTeam()==team?0:1;
            robotype = getRobotTypeID(robot.getType());
        }

        return secureSend(new int[] {
            0,
            flooded * 10000 + loc.x* 100 + loc.y,
            elevation,
            soup,
            roboteam*10+robotype, 
            0
        }, cost);

    }

    boolean tryBroadcastBuild(MapLocation loc, RobotType type, int orderID, int cost) throws GameActionException {
        System.out.println("Trying to send a building");
        int robotype = robotype = getRobotTypeID(type);
        
        return secureSend(new int[] {
            1,
            loc.x* 100 + loc.y,
            0,
            0,
            robotype, 
            orderID
        }, cost);
    }

    boolean tryBroadcastSuccess(int orderID, int cost) throws GameActionException {
        
        return secureSend(new int[] {
            5,
            0,
            0,
            0,
            0, 
            orderID
        }, cost);

    }
    

    int[] getInformation (Transaction trans) {

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

    //Hey I am doing polymorphism give me a medal <--- woc Mark NBBBB
    ArrayList<MapLocation> getAdjacent(MapLocation loc) throws GameActionException {
        ArrayList<MapLocation> res = new ArrayList<MapLocation>();
        for (Direction dir : directions) {
            if (rc.onTheMap(loc.add(dir))) {
                res.add(loc.add(dir));
            }
        }
        return res;
    }

    ArrayList<MapLocation> getAdjacent() throws GameActionException {
        ArrayList<MapLocation> res = new ArrayList<MapLocation>();
        for (Direction dir : directions) {
            if (rc.onTheMap(myMapLocation.add(dir))) {
                res.add(myMapLocation.add(dir));
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