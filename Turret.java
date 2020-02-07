package potato;
import battlecode.common.*;

public strictfp class Turret extends Building {

    Team enemy = team.opponent();

    Turret(RobotController rc) throws GameActionException{
        super(rc);
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
       findAndShoot();
    }
}
