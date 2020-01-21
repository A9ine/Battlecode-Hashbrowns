package potato;

import java.util.*;
import battlecode.common.*;

public strictfp class Miner extends Unit {

    Miner(RobotController rc) throws GameActionException {
        super(rc);
    }

    //General
    MapLocation moveTarget;

    //Constants
    final int REFINERY_DISTANCE = 250;
    final int BUILD_DISTANCE = 100;

    //Building stuff
    ArrayList<RobotType> buildTarget = new ArrayList<RobotType>();
    ArrayList<MapLocation> buildLocation = new ArrayList<MapLocation>();
    ArrayList<Integer> buildMinRange = new ArrayList<Integer>();
    ArrayList<Integer> buildMaxRange = new ArrayList<Integer>();
    ArrayList<Integer> buildID = new ArrayList<Integer>();
    
    MapLocation currentBuildLocation;
    Integer currentMinRange;
    Integer currentMaxRange;
    Integer currentBuildID = -1;
    RobotType currentBuildTarget;

    //Refinery stuff
    ArrayList<MapLocation> refinerySpots = new ArrayList<MapLocation>();
    boolean wantToBuildRefinery = false;
    MapLocation leftMineLocation;

    MapLocation findClosestRefinery(MapLocation loc) {
        if (refinerySpots.size() == 0) {
            return null;
        }
        int distance = Integer.MAX_VALUE;
        MapLocation res = null;
        for (MapLocation refinery : refinerySpots) {
            if (refinery.distanceSquaredTo(loc) < distance) {
                distance = refinery.distanceSquaredTo(loc);
                res = refinery; 
            }
        }
        return res;
    }

    //Refining stuff
    void goRefine() throws GameActionException {
        MapLocation closestRefinery = findClosestRefinery(myMapLocation);
        leftMineLocation = myMapLocation;
        if (closestRefinery == null || closestRefinery.distanceSquaredTo(myMapLocation)>REFINERY_DISTANCE) {
            wantToBuildRefinery = true;
        }
        state = 52;
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

    //Mining Stuff
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

    //General stuff
    int lastCommunicationRound = 3;

    void reset() {
        System.out.println("RESET!");
        //Resets state to 0
        state = 0;
        moveTarget = null;
        currentBuildID = -1;
        currentBuildLocation = null;
        currentBuildTarget = null;
        currentMaxRange = -1;
        currentMinRange = -1;
        leftMineLocation = null;
        wantToBuildRefinery = false;

    }

    void minerUpdate() throws GameActionException {
        update();
        for (MapLocation processingMapLocation : nearbySoup) {
            if (state == 0) {
                moveTarget = null;
            }
        }

        for (RobotInfo robot : nearbyRobots) {
            if (robot.getType() == RobotType.REFINERY && robot.getTeam() == team && !refinerySpots.contains(robot.getLocation())) {
                refinerySpots.add(robot.getLocation());
            }
        }
        if (rc.getRoundNum() > 5 && rc.getRoundNum() - lastCommunicationRound > 2) {
            for (int i = lastCommunicationRound; i < rc.getRoundNum()-1; i ++) {
                minerCommunication(rc.getBlock(i));
            }
        }
        minerCommunication(latestCommunication);
        lastCommunicationRound = rc.getRoundNum();
    }

    void minerCommunication(Transaction[] incoming) {
        for (Transaction trans : incoming) {
            int[] message = getInformation(trans);
            if (message[6] != KEY) {
                continue;
            }

            //Potential mining data
            if (message[0] == 0) {
                MapLocation loc = new MapLocation((message[1]%10000-message[1]%100)/100, message[1]%100);
                if (message[3] > 0 && state == 0) {
                    moveTarget = null;
                }
                //Someone built a refinery
                if (message[4] != 0 && message[4]/10==0 && message[4]%10 ==3) {
                    if (!refinerySpots.contains(loc)) {
                        refinerySpots.add(loc);
                    }
                }
            }
            //Building data
            if (message[0] == 1) {
                MapLocation loc = new MapLocation((message[1]%10000-message[1]%100)/100, message[1]%100);
                buildLocation.add(loc);
                buildTarget.add(getRobotTypeFromID(message[4]));
                buildID.add(message[5]);
                buildMinRange.add(message[2]);
                buildMaxRange.add(message[3]);
            }
            //Completed Building
            if (message[0] == 5) {
                //System.out.println(message[5]);
                //System.out.println(buildID);
                //System.out.println(currentBuildID);
                for (int i = 0; i < buildID.size(); i++) {
                    if (buildID.get(i) == message[5]) {
                        buildID.remove(i);
                        buildMinRange.remove(i);
                        buildMaxRange.remove(i);
                        buildTarget.remove(i);
                        buildLocation.remove(i);
                        break;
                    }
                }
                if (currentBuildID == message[5]) {
                    reset();
                }
            }

            //Check last communications

            
        }
    }

    //Run function

    @Override
    public void run() throws GameActionException {
        System.out.println(state);
        minerUpdate();
        System.out.println(buildTarget);
        //System.out.println(currentBuildID);
        //System.out.println(currentBuildTarget);
        //System.out.println(buildTarget);
        //System.out.println(buildID);

        if (turn == 1) {
            for (MapLocation loc : getAdjacent()) {
                if (rc.isLocationOccupied(loc) && rc.senseRobotAtLocation(loc).getType() == RobotType.HQ && rc.senseRobotAtLocation(loc).getTeam() == team) { 
                    hqLoc = loc;
                    refinerySpots.add(loc);
                }
            }
        }

        //Doing nothing
        if (state == 0) {
            if (moveTarget == null) {
                //Find a mining spot with BFS
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
                        if (state != 53) {
                            state = 51;
                        }
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
            
        if (state == 0 && (moveTarget == null || myMapLocation.equals(moveTarget))) {
            //Nothing must have been found. Go to a random location and explore
            moveTarget = randomLocation(); 
        }

        //Building on orders
        if (state != 53) {
            for (int i = 0; i < buildLocation.size(); i ++) {
                if (buildLocation.get(i).distanceSquaredTo(myMapLocation) < BUILD_DISTANCE && rc.getTeamSoup()>buildTarget.get(i).cost+30) {
                    System.out.println("I found a building location");
                    state = 53;
                    currentBuildID = buildID.get(i);
                    System.out.println("Current build ID");
                    System.out.println(currentBuildID);
                    currentBuildTarget = buildTarget.get(i);
                    currentBuildLocation = buildLocation.get(i);
                    currentMinRange = buildMinRange.get(i);
                    currentMaxRange = buildMaxRange.get(i);
                    break;
                }
            }
        }

        if (state == 53) {
            System.out.println("Current Build Location");
            System.out.println(currentBuildLocation);
            if (currentBuildLocation == null) {
                reset();
            } else {
                if (myMapLocation.distanceSquaredTo(currentBuildLocation) > currentMaxRange) {
                    //System.out.println("a");
                    moveTarget = currentBuildLocation;
                } else if ((myMapLocation.distanceSquaredTo(currentBuildLocation) < currentMinRange)){
                    moveTarget = randomLocation();
                    //System.out.println("aa");
                } else {
                    moveTarget = myMapLocation;
                    //System.out.println("aaa");
                    for (Direction dir : directions) {
                        if (rc.adjacentLocation(dir).distanceSquaredTo(currentBuildLocation)>=currentMinRange && rc.adjacentLocation(dir).distanceSquaredTo(currentBuildLocation)<=currentMaxRange) {
                            if (tryBuild(currentBuildTarget, dir)) {
                                tryBroadcastSuccess(currentBuildID, averageSend);
                                reset();
                                break;
                            }
                        }
                    }
                }
            }
        }

        //Mining
        if (state == 51) {
            //System.out.printf("Currently carrying %d soup",rc.getSoupCarrying());
            //So many edge cases I want to kill myself
            if(!tryMine()) {
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
        }

        //Refining
        if (state == 52) {
            moveTarget = findClosestRefinery(myMapLocation);
            //System.out.println(moveTarget);
            if (moveTarget == null) {
                wantToBuildRefinery = true;
                leftMineLocation = myMapLocation;
            } else {
                if (myMapLocation.distanceSquaredTo(moveTarget)>400 && nearbySoup.length>0) {
                    wantToBuildRefinery = true;
                    leftMineLocation = myMapLocation;
                }
                if (leftMineLocation.distanceSquaredTo(findClosestRefinery(leftMineLocation)) < REFINERY_DISTANCE) {
                    wantToBuildRefinery = false;
                }
            }
            if (wantToBuildRefinery) {
                for (Direction dir : directions) {
                    if (rc.adjacentLocation(dir).distanceSquaredTo(hqLoc) <= 2) {
                        continue;
                    }
                    if (tryBuild(RobotType.REFINERY, dir)) {
                        wantToBuildRefinery = false;
                        tryBroadcastLocation(rc.adjacentLocation(dir), fastSend);
                        refinerySpots.add(rc.adjacentLocation(dir));
                    }
                }
            }

            tryRefine();
            if (rc.getSoupCarrying() == 0) {
                reset();
            }
        }

        //Movement

        if (rc.isReady()) {
            if (moveTarget != null && bugNavigate(moveTarget)) {
                if (!myMapLocation.equals(moveTarget)) {
                    //Not sure if this should be added
                    map[getMiniMapLocation(moveTarget)][1] = 0;
                    //Can't get to the target
                    if (state == 53) {
                        for (int i = 0; i < buildID.size(); i++) {
                            if (buildID.get(i) == currentBuildID) {
                                buildID.remove(i);
                                buildTarget.remove(i);
                                buildLocation.remove(i);
                                break;
                            }
                        }
                    }
                    if (state == 52) {
                        for (int i = 0; i < refinerySpots.size(); i ++) {
                            if (refinerySpots.get(i) == moveTarget) {
                                refinerySpots.remove(i);
                                moveTarget = null;
                            }
                        }
                    }
                    System.out.println("Can't get there!");
                    reset();
                    
                }
            }
        }
    }
}