package potato;
import battlecode.common.*;
import java.util.*;

public strictfp class HQ extends Building {

    Team enemy;
    int minerNum = 0;
    int designSchoolNum = 0;
    int fulfillmentCenterNum = 0;
    int orderID = 0;
    boolean stageOne = false; //Turn 50 or enemy rush
    ArrayList<MapLocation> buildingLocations = new ArrayList<MapLocation>(); 

    HQ(RobotController rc) throws GameActionException {
        super(rc);
        enemy = team.opponent();
    }

    void findAndShoot() throws GameActionException {
        if (rc.isReady()) {
           //See if there are any enemy drones within striking range
           RobotInfo[] robots = rc.senseNearbyRobots(GameConstants.NET_GUN_SHOOT_RADIUS_SQUARED,enemy);
           //Now shoot them
           for (RobotInfo robot : robots) {
               if (robot.getType() == RobotType.DELIVERY_DRONE) {
                   rc.shootUnit(robot.getID());
                   System.out.println("I shot" + robot.getID() + "!");
                   Clock.yield();
               }
           }
        }
    }

    @Override
    public void run() throws GameActionException {
        update();
        findAndShoot();
        //First Turn
        //Find nearby soup
        if (turn == 1) {
            System.out.println(miniMapWidth);
            System.out.println(miniMapHeight);
            for (Direction dir : directions) {
                tryBuild(RobotType.MINER, dir);
            }
            tryBroadcastLocation(myMapLocation, averageSend);
        }

        //Start building the base
        if (turn == 100) {
            //Make sure there is a miner nearby
            for (Direction dir : directions) {
                if(tryBuild(RobotType.MINER, dir)) {
                    minerNum += 1;
                };
            }
            //If someone rushes us
            //We are fucked
            //TODO: Unfuck us with defense code
            //TODO: Potential fuck up point : No Building loc found
            //Someone should do a BFS or something
            MapLocation target = myMapLocation.translate(0,2);
            for (int i = -2; i <= 2; i ++) {
                for (int j = -2; j <= 2; j ++) {
                    MapLocation temp = myMapLocation.translate(i,j);
                    System.out.println(temp.distanceSquaredTo(myMapLocation));
                    if (rc.onTheMap(temp) && temp.distanceSquaredTo(myMapLocation)>2 && !rc.senseFlooding(temp) && Math.abs(rc.senseElevation(temp) - rc.senseElevation(myMapLocation)) <= 3 && rc.isLocationOccupied(temp) && !buildingLocations.contains(temp)) {
                        target = temp;
                    }
                }
            }

            buildingLocations.add(target);
            orderID+=1;
            while (!tryBroadcastBuild(target, RobotType.DESIGN_SCHOOL, rc.getID()+orderID, fastSend));
        }

        if (turn == 60) {
            //Make sure there is a miner nearby
            for (Direction dir : directions) {
                if(tryBuild(RobotType.MINER, dir)) {
                    minerNum += 1;
                };
            }
            //If someone rushes us
            //We are fucked
            //TODO: Unfuck us with defense code
            MapLocation target = myMapLocation.translate(0,2);
            for (int i = -2; i <= 2; i ++) {
                for (int j = -2; j <= 2; j ++) {
                    MapLocation temp = myMapLocation.translate(i,j);
                    System.out.println(temp.distanceSquaredTo(myMapLocation));
                    if (rc.onTheMap(temp) && temp.distanceSquaredTo(myMapLocation)>2 && !rc.senseFlooding(temp) && Math.abs(rc.senseElevation(temp) - rc.senseElevation(myMapLocation)) <= 3 && rc.isLocationOccupied(temp) && !buildingLocations.contains(temp)) {
                        target = temp;
                    }
                }
            }
            buildingLocations.add(target);
            orderID+=1;
            while (!tryBroadcastBuild(target, RobotType.FULFILLMENT_CENTER, rc.getID()+orderID, fastSend));
        }


        if (minerNum < 20) {
            if  (turn < 10) {
                for (Direction dir : directions) {
                    if(tryBuild(RobotType.MINER, dir)) {
                        minerNum += 1;
                    };
                }
            }
            else if (turn < 50) {
                if (rc.getTeamSoup()>140) { //In case an idiot decides to attack
                    for (Direction dir : directions) {
                        if(tryBuild(RobotType.MINER, dir)) {
                            minerNum += 1;
                        };
                    }
                }
            }
    
            else {  
                if (rc.getTeamSoup()>70+minerNum*20) {
                    for (Direction dir : directions) {
                        if(tryBuild(RobotType.MINER, dir)) {
                            minerNum += 1;
                        };
                    }
                }
            }
        }
        
    }
}

