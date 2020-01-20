package potato;
import battlecode.common.*;

public strictfp class Drone extends Unit {

    RobotInfo[] enemyRobots;

    boolean tryPickup(int ID) throws GameActionException {
        if (rc.canPickUpUnit(ID)) {
            rc.pickUpUnit(ID);
            return true;
        }
        return false;
    }

    Drone(RobotController rc) throws GameActionException {
        super(rc);
        int round = 1;
        while (hqLoc == null) {
            Transaction[] block = rc.getBlock(round);
            round += 1;
            for (Transaction trans : block) {
                int[] message = getInformation(trans);
                if (message[6] != KEY) {
                    continue;
                }
                if (message[0] == 0) {
                    MapLocation loc = new MapLocation((message[1]%10000-message[1]%100)/100, message[1]%100);
                    if (message[4]  == 1) {
                        hqLoc = loc;
                    }
                }
            }
        }
    }

    boolean checkedHorizontal = false;
    boolean checkedAngled = false;
    int target = -1;
    MapLocation moveTarget;

    void droneUpdate() throws GameActionException {
        update();
        enemyRobots = rc.senseNearbyRobots(GameConstants.NET_GUN_SHOOT_RADIUS_SQUARED,team.opponent());
    }


    @Override
    public void run() throws GameActionException {

        droneUpdate();

        if (!rc.isCurrentlyHoldingUnit()) {
            if (enemyRobots.length > 0) {
                int minimum = Integer.MAX_VALUE;
                RobotInfo potential = null;
                for (RobotInfo robot : enemyRobots) {
                    if (minimum > robot.location.distanceSquaredTo(myMapLocation) && !robot.getType().isBuilding() && robot.getType()!=RobotType.DELIVERY_DRONE) {
                        potential = robot;
                        minimum = robot.location.distanceSquaredTo(myMapLocation); 
                    }
                }
                if (potential != null) {
                    target = potential.getID();
                    state = 42;
                }
            }
        }

        if (state == 0) {

            /*if (symmetry == 0) {
                state = 41;
            }

            else {

            }*/

            if (moveTarget == null || myMapLocation.equals(moveTarget)) {
                moveTarget = randomLocation();
            }
        }

        /*if (state = 41) {

           if (symmetry != 0) {
               state = 0;
           }

           for (RobotInfo robot : enemyRobots) {
               if (robot.getType() == RobotType.HQ) {
                   if (!checkedHorizontal) {
                       symmetry = 1;
                   }
                   if (!checkedAngled) {
                       symmetry = 3;
                   } else {
                       symmetry = 2;
                   }
                   tryBroadcastLocation(robot.loc, fastSend);
               }
           }

           if (!checkedHorizontal) {
               bugNavigate(hqLoc.translate((rc.getMapWidth()/2-hqLoc.x)*2,0));
           }
        } */

        if (state == 42) {
            if (rc.canSenseRobot(target) || rc.isCurrentlyHoldingUnit()) {
                if (rc.isCurrentlyHoldingUnit()) {
                    if (moveTarget == null || myMapLocation.equals(moveTarget)) {
                        moveTarget = randomLocation();
                    }
                    for (MapLocation loc : getAdjacent()) {
                        if (rc.senseFlooding(loc)) {
                            if (rc.canDropUnit(myMapLocation.directionTo(loc))) {
                                rc.dropUnit(myMapLocation.directionTo(loc));
                                state = 0;
                            }
                        }
                    }
                } else {
                    if (tryPickup(target)) {
                        moveTarget = randomLocation();
                    } else {
                        moveTarget = rc.senseRobot(target).location;
                    }
                }
                
                
            } else {
                state = 0;
            }
        }

        if (rc.isReady()) {
            if (moveTarget != null && bugNavigate(moveTarget)) {
                if (!myMapLocation.equals(moveTarget)) {
                    state = 0;
                    moveTarget = randomLocation();
                    System.out.println("Can't get there!");
                    
                }
            }
        }

         
    }        
}
