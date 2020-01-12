package potato;
import battlecode.common.*;

public strictfp class Drone extends Unit {

    Drone(RobotController rc) {
        super(rc);
    }

    @Override
    public void run() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        MapLocation water = null;
        // if drone is not holding unit or found water
        if (!rc.isCurrentlyHoldingUnit() || water==null) {
            // moves in a random direction
            for (int i=0;i<8;i++) {
                if (rc.isReady() && rc.canMove(directions[i])) {
                    rc.move(directions[i]);
                    // searches for water
                    if (rc.senseFlooding(rc.getLocation()))
                    {
                        water = rc.getLocation();
                    }
                    break;
                }
            }
            RobotInfo[] robots = rc.senseNearbyRobots(GameConstants.DELIVERY_DRONE_PICKUP_RADIUS_SQUARED, enemy);
            if (robots.length > 0) {
                // Pick up a first robot within range
                rc.pickUpUnit(robots[0].getID());
                System.out.println("I picked up " + robots[0].getID() + "!");
            }                  
        }
        else {
            // if drone is one step away from water
            if (MapLocation.distanceSquaredTo(water)==1){
                // drone drops robot into water
                rc.dropUnit(MapLocation.directionTo(water));
            }
            else
            {
                // if not near water, drone moves towards water
                rc.move(MapLocation.directionTo(water));
            } 
        }        
    }        
}
