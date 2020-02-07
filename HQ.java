package potato;
import battlecode.common.*;
import java.util.*;

public strictfp class HQ extends Building {

    Team enemy;
    int minerNum = 0;
    int designSchoolNum = 0;
    int fulfillmentCenterNum = 0;
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
        if (turn == 60) {
            System.out.println(orderID);
            tryBroadcastBuild(myMapLocation, RobotType.FULFILLMENT_CENTER, 3, 16, rc.getID()+orderID, averageSend);
            System.out.println(orderID);
            tryBroadcastBuild(myMapLocation, RobotType.DESIGN_SCHOOL, 3, 16, rc.getID()+orderID, averageSend);
        }

        //If base appears to be destroyed, try replacing
        if (turn % 100 == 0) {
            boolean hasDesign = false;
            boolean hasFulfillment = false;
            for (RobotInfo robot : nearbyRobots) {
                if (robot.getTeam() == team && robot.getType() == RobotType.DESIGN_SCHOOL) {
                    hasDesign = true;
                }
                if (robot.getTeam() == team && robot.getType() == RobotType.FULFILLMENT_CENTER) {
                    hasFulfillment = true;
                }
            }
            if (!hasDesign) {
                tryBroadcastBuild(myMapLocation, RobotType.DESIGN_SCHOOL, 3, 16, rc.getID()+orderID, averageSend);
            }
            if (!hasFulfillment) {
                tryBroadcastBuild(myMapLocation, RobotType.FULFILLMENT_CENTER, 3, 16, rc.getID()+orderID, averageSend);
            }
            if (rc.getTeamSoup() > 600) {
                tryBroadcastBuild(myMapLocation, RobotType.DESIGN_SCHOOL, 8, 50, rc.getID()+orderID, averageSend);
                tryBroadcastBuild(myMapLocation, RobotType.FULFILLMENT_CENTER, 8, 50, rc.getID()+orderID, averageSend);
            }
            tryBroadcastBuild(myMapLocation, RobotType.VAPORATOR, 8, 40, rc.getID()+orderID, cheapSend);
        }

        if (turn > 60) {
            ArrayList<MapLocation> walls = getAdjacent();
            int sendWall = 0;
            for (int i = 0; i < walls.size(); i ++) {
                if (rc.isLocationOccupied(walls.get(i)) && rc.senseRobotAtLocation(walls.get(i)).getTeam()==team && rc.senseRobotAtLocation(walls.get(i)).getType()==RobotType.LANDSCAPER){
                    sendWall *= 10;
                    sendWall += 1;
                } else{
                    sendWall *= 10;
                }
            }
            if (turn % 50 == 0) {
                tryBroadcastImportant(symmetry, sendWall, averageSend); 
            }
            else if (turn % 10 == 0) {
                tryBroadcastImportant(symmetry, sendWall, cheapSend);
            } 
        }

        //Miner construction

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

