package potato;
import battlecode.common.*;

public strictfp class Turret extends Building {

    Turret(RobotController rc) {
        super(rc);
    }

    @Override
    public void run() {
        Team enemy = rc.getTeam().opponent();
        //See if there are any enemy robots within striking range
        RobotInfo[] robots = rc.senseNearbyRobots(GameConstants.NET_GUN_SHOOT_RADIUS_SQUARED,enemy);
        if (robots.length==1) {
            // if one robot in range, shoot that robot
            rc.shootUnit(robots[0].getID());
            System.out.println("I shot" + robots[0].getID() + "!");
        }
        if (robots.length>1) {
            // if multiple robots in range, check for which ones you can shoot down and shoot
            for (int i=0;i<robots.length;i++) {
                if (rc.canShootUnit(robots[i].getID()) == true) {
                    rc.shootUnit(robots[i].getID());
                    System.out.println("I shot" + robots[i].getID() + "!");
                    break;
                }
            }
        }
    }
}
