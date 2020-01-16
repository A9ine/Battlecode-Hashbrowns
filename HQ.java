package potato;
import battlecode.common.*;

public strictfp class HQ extends Building {
    Team enemy;

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

        if (turn < 50) {
            if (rc.getTeamSoup()>100) { //In case an idiot decides to attack
                for (Direction dir : directions) {
                    tryBuild(RobotType.MINER, dir);
                }
            }
        } else {
            if (rc.getTeamSoup()>1500) {
                for (Direction dir : directions) {
                    tryBuild(RobotType.MINER, dir);
                }
            }
        }
    }
}

