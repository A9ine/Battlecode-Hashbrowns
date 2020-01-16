package potato;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import battlecode.common.*;

public strictfp class Miner extends Unit {

    ArrayList<MapLocation> refinerySpots = new ArrayList<MapLocation>();
    MapLocation moveTarget;

    Miner(RobotController rc) throws GameActionException {
        super(rc);
    }


    //Refinery stuff
    MapLocation findClosestRefinery() {
        int distance = Integer.MAX_VALUE;
        MapLocation res = hqLoc;
        for (MapLocation refinery : refinerySpots) {
            if (refinery.distanceSquaredTo(myMapLocation) < distance) {
                distance = refinery.distanceSquaredTo(myMapLocation);
                res = refinery; 
            }
        }
        return res;
    }

    //The first function called when started to refine
    void goRefine() throws GameActionException {
        MapLocation closestRefinery = findClosestRefinery();
        if (closestRefinery.distanceSquaredTo(myMapLocation)>225) {
            for (Direction dir : directions) {
                if(tryBuild(RobotType.REFINERY, dir)) {
                    refinerySpots.add(rc.adjacentLocation(dir));
                }
            }
        }
        state = 52;
    }

    //General stuff
    void minerUpdate() throws GameActionException {
        update();
        for (MapLocation processingMapLocation : rc.senseNearbySoup()) {
            if (state == 0) {
                moveTarget = null;
            }
            int location = getMiniMapLocation(processingMapLocation); 
            map[location][0] = rc.senseElevation(processingMapLocation);
            map[location][1] = rc.senseSoup(processingMapLocation);
        }
        minerCommunication();
    }

    boolean tryMine() throws GameActionException {
        for (Direction dir : Direction.allDirections()){
            if (rc.isReady() && rc.canMineSoup(dir)) {
                rc.mineSoup(dir);
                //TODO: Send message once sector finished mining
                return true;
            }
        }
        return false;
    }

    boolean tryRefine() throws GameActionException {
        for (Direction dir : Direction.allDirections()){
            if (rc.isReady() && rc.canDepositSoup(dir)) {
                rc.depositSoup(dir, rc.getSoupCarrying());
                return true;
            }
        }
        return false;
    }

    void minerCommunication() {
        for (Transaction trans : latestCommunication) {
            int[] message = getInformation(trans);
            if (message[6] != 69420) {
                continue;
            }
            if (message[0] == 0) {
                MapLocation loc = new MapLocation(message[2]%10000-message[2]%100, message[2]%100);

                map[getMiniMapLocation(loc)][1] = message[3];
            }
        }
    }


    @Override
    public void run() throws GameActionException {

        System.out.println(state);

        minerUpdate();

        if (turn == 1) {
            System.out.println("Finding HQ");
            for (MapLocation loc : getAdjacent()) {
                if (rc.isLocationOccupied(loc) && rc.senseRobotAtLocation(loc).getType() == RobotType.HQ && rc.senseRobotAtLocation(loc).getTeam() == team) { 
                    hqLoc = loc;
                    refinerySpots.add(loc);
                }
            }
        }

        if (state == 0) {
            if (moveTarget == null) {
                //Find a mining spot with BFS
                System.out.println("Running BFS");
                Queue<Integer> queue = new LinkedList<Integer>();
                HashMap<Integer,Boolean> visited = new HashMap<Integer,Boolean>();
                queue.add(getMiniMapLocation(myMapLocation));
                while (queue.size() > 0) {
                    Integer current = queue.poll();
                    if (visited.containsKey(current)) {
                        continue;
                    }
                    visited.put(current,true);
                    if (map[current][1] > 0) {
                        moveTarget = locationInMiniMap(current);
                        state = 51;
                        break;
                    }
                    //Add nearby sectors
                    int x = current%miniMapWidth;
                    int y = current/miniMapWidth;
                    if (x > 0) {
                        queue.add((x-1)+y*miniMapWidth);
                    }
                    if (x < miniMapWidth-1) {
                        queue.add((x+1)+y*miniMapWidth);
                    }
                    if (y > 0) {
                        queue.add(x+(y-1)*miniMapWidth);
                    }
                    if (y < miniMapHeight-1) {
                        queue.add(x+(y+1)*miniMapWidth);
                    }
                }
            }
        }
            
        if (state == 0 && moveTarget == null) {
            //Nothing must have been found. Go to a random location and explore
            moveTarget = randomLocation(); 

        } else if (state == 0 && myMapLocation.equals(moveTarget)) {
            moveTarget = randomLocation();
        }

        //Mining
        if (state == 51) {
            //System.out.printf("Currently carrying %d soup",rc.getSoupCarrying());
            //So many edge cases I want to kill myself
            while(tryMine());
            if (rc.getSoupCarrying() == RobotType.MINER.soupLimit) {
                goRefine();
                Clock.yield();
            }
            if (rc.canSenseLocation(moveTarget) && rc.senseSoup(moveTarget)==0 && nearbySoup.length > 0) {
                moveTarget = nearbySoup[0];
            }
            if (getMiniMapLocation(myMapLocation) == getMiniMapLocation(moveTarget) && nearbySoup.length==0) {
                if (rc.getSoupCarrying()>0) {
                    goRefine();
                } else {
                    state = 0;
                    moveTarget = null;
                }
                Clock.yield();
            }
        }

        //Refining
        if (state == 52) {
            //TODO: Find nearest refinery
            moveTarget = findClosestRefinery();

            tryRefine();
            if (rc.getSoupCarrying() == 0) {
                state = 0;
                moveTarget = null;
                Clock.yield();
            }
        }
        if (rc.isReady()) {
            if (moveTarget != null && bugNavigate(moveTarget)) {
                if (!myMapLocation.equals(moveTarget)) {
                    //Not sure if this should be added
                    map[getMiniMapLocation(moveTarget)][1] = 0;
                    //Can't get to the target
                    System.out.println("Can't get there!");
                    state = 0;
                    moveTarget = randomLocation(); 
                    Clock.yield();
                }
            }
        }

    }

}