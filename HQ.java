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
           if (robots.length > 0) {
               rc.shootUnit(robots[0].getID());
               System.out.println("I shot" + robots[0].getID() + "!");
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
            for (Direction dir : directions) {
                tryBuild(RobotType.MINER, dir);
            }
            tryBroadcastLocation(myMapLocation, averageSend);
        }

        if (turn < 50) {
            if (rc.getTeamSoup()>270) { //In case an idiot decides to attack
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

