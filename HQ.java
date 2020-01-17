package potato;
import battlecode.common.*;

public strictfp class HQ extends Building {

    Team enemy;
    int minerNum = 0;
    int designSchoolNum = 0;
    int fulfillmentCenterNum = 0;
    int orderID = 0;
    boolean stageOne = false; //Turn 50 or enemy rush

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
        if (turn == 50) {
            System.out.println(turn=50);
            //Make sure there is a miner nearby
            for (Direction dir : directions) {
                if(tryBuild(RobotType.MINER, dir)) {
                    minerNum += 1;
                };
            }
            //If someone rushes us
            //We are fucked
            //TODO: Unfuck us with defense code
            MapLocation target = null;
            while (target == null) {
                for (MapLocation potential : getAdjacent()) {
                    if (rc.onTheMap(potential)) {
                        target = potential;
                        break;
                    }
                }
            }
            while (!tryBroadcastBuild(target, RobotType.DESIGN_SCHOOL, turn + KEY, fastSend));
        }
        
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
            System.out.println(rc.getTeamSoup());
            if (rc.getTeamSoup()>minerNum * 150) {
                for (Direction dir : directions) {
                    if(tryBuild(RobotType.MINER, dir)) {
                        minerNum += 1;
                    };
                }
            }
        }
    }
}

