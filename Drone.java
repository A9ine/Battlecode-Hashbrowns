package potato;
import java.util.ArrayList;

import battlecode.common.*;

public strictfp class Drone extends Unit {

    RobotInfo[] enemyRobots;
    ArrayList<Integer> waterLocations = new ArrayList<Integer>();

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

        System.out.println(state);

        droneUpdate();

        //Prioritize enemy killing if near base
        if (!rc.isCurrentlyHoldingUnit() && (state != 41 || myMapLocation.distanceSquaredTo(hqLoc)<=100)) {
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

            if (symmetry == 0) {
                state = 41;
            }

            if (moveTarget == null || myMapLocation.equals(moveTarget)) {
                moveTarget = randomLocation();
            }
        }

        if (state == 41) {

           if (symmetry != 0) {
               state = 0;
           }

           MapLocation horizontal = hqLoc.translate((rc.getMapWidth()/2-hqLoc.x)*2,0);
           MapLocation angled = hqLoc.translate((rc.getMapWidth()/2-hqLoc.x)*2,(rc.getMapHeight()/2-hqLoc.y)*2);
           if (!checkedHorizontal) {
               rc.setIndicatorDot(horizontal,255,255,255);
               if (rc.canSenseLocation(horizontal)) {
                    if (rc.isLocationOccupied(horizontal) && rc.senseRobotAtLocation(horizontal).getTeam()!=team && rc.senseRobotAtLocation(horizontal).getType()==RobotType.HQ) {
                        symmetry = 1;
                        System.out.println("FOUND IT!");
                        tryBroadcastImportant(symmetry, 11111111, averageSend);
                    } else {
                        checkedHorizontal = true;
                        System.out.println("Nothing horizontal");
                    }
               }
               bugNavigate(horizontal);
           } else {
                rc.setIndicatorDot(angled,255,255,255);
                if (rc.canSenseLocation(angled)) {
                    System.out.println("FOUND IT!");
                    if (rc.isLocationOccupied(angled) && rc.senseRobotAtLocation(angled).getTeam()!=team && rc.senseRobotAtLocation(angled).getType()==RobotType.HQ) {
                        symmetry = 3;
                        tryBroadcastImportant(symmetry, 11111111, averageSend);
                    } else {
                        checkedAngled = true;
                        System.out.println("Nothing angled");
                        symmetry = 2;
                        tryBroadcastImportant(symmetry, 11111111, averageSend);
                    }
                }
                bugNavigate(angled);
            }
        }

        if (state == 42) {
            if (rc.canSenseRobot(target) || rc.isCurrentlyHoldingUnit()) {
                if (rc.isCurrentlyHoldingUnit()) {
                    for (MapLocation loc : getAdjacent()) {
                        if (rc.senseFlooding(loc)) {
                            if (!waterLocations.contains(getMiniMapLocation(loc))) {
                                waterLocations.add(getMiniMapLocation(loc));
                            }
                            if (rc.canDropUnit(myMapLocation.directionTo(loc))) {
                                rc.dropUnit(myMapLocation.directionTo(loc));
                                state = 0;
                            }
                        }
                    }

                    if (moveTarget == null || myMapLocation.equals(moveTarget)) {
                        boolean temp = true;
                        for (MapLocation loc : getNear()) {
                            if (rc.senseFlooding(loc)) {
                                moveTarget = loc;
                                temp = false;
                                break;
                            }
                        }

                        if (temp && waterLocations.contains(getMiniMapLocation(myMapLocation))) {
                            for (int i = 0; i < waterLocations.size(); i ++) {
                                if (waterLocations.get(i) == getMiniMapLocation(myMapLocation)) {
                                    waterLocations.remove(i);
                                    break;
                                }
                            }
                        }

                        if (waterLocations.size()>0) {
                            int minDistance = Integer.MAX_VALUE;
                            moveTarget = locationInMiniMap(waterLocations.get(0));
                            for (int maybeLoc : waterLocations) {
                                if (locationInMiniMap(maybeLoc).distanceSquaredTo(myMapLocation) < minDistance) {
                                    minDistance = locationInMiniMap(maybeLoc).distanceSquaredTo(myMapLocation); 
                                    moveTarget = locationInMiniMap(maybeLoc);
                                }
                            }
                        } else {
                            moveTarget = randomLocation();
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
