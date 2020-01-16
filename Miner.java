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

    void minerUpdate() throws GameActionException {
        update();
        for (MapLocation processingMapLocation : rc.senseNearbySoup()) {
            map[processingMapLocation.x][processingMapLocation.y][0] = rc.senseElevation(processingMapLocation);
            map[processingMapLocation.x][processingMapLocation.y][1] = rc.senseSoup(processingMapLocation);
            map[processingMapLocation.x][processingMapLocation.y][2] = 0;
            if (rc.senseFlooding(processingMapLocation)) {
                map[processingMapLocation.x][processingMapLocation.y][2] = -1;
            }
        }
        minerCommunication();
    }

    boolean tryMine() throws GameActionException {
        for (Direction dir : Direction.allDirections()){
            if (rc.isReady() && rc.canMineSoup(dir)) {
                rc.mineSoup(dir);
                if (rc.senseSoup(rc.adjacentLocation(dir))==0) {
                    System.out.println("I finished mining a spot!");
                    tryBroadcastLocation(rc.adjacentLocation(dir), cheapSend);
                }
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
                map[loc.x][loc.y][0] = message[2];
                map[loc.x][loc.y][1] = message[3];
                map[loc.x][loc.y][2] = message[2]-message[2]%10000-message[2]%100 == 1 ? -1 : 0;
            }
        }
    }


    @Override
    public void run() throws GameActionException {

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
            //If the robot is not ready, try running a BFS over the local map to find soup spots
            System.out.println("Running BFS");
            Queue<MapLocation> queue = new LinkedList<MapLocation>();
            HashMap<MapLocation,Boolean> visited = new HashMap<MapLocation,Boolean>();
            queue.add(myMapLocation);
            while (queue.size() > 0) {
                MapLocation current = queue.poll();
                if (visited.containsKey(current) || !rc.onTheMap(current)) {
                    continue;
                }
                visited.put(current,true);
                if (map[current.x][current.y][1] > 0) {
                    moveTarget = current;
                    state = 51;
                    break;
                }
                if (current.distanceSquaredTo(myMapLocation) > 400) {
                    continue;
                }
                for (Direction dir : directions) {
                    queue.add(current.add(dir));
                }
            }

            if (state == 0 && moveTarget == null) {
                //Nothing must have been found. Go to a random location and explore
                moveTarget = randomLocation();
            }

        }

        //Mining
        if (state == 51) {
            //System.out.printf("Currently carrying %d soup",rc.getSoupCarrying());
            while(tryMine());
            if (rc.canSenseLocation(moveTarget) && rc.senseSoup(moveTarget) == 0) {
                state = 0;
                moveTarget = null;
                Clock.yield();
            }
            if (rc.getSoupCarrying() == RobotType.MINER.soupLimit) {
                state = 52;
                Clock.yield();
            }
        }

        //Refining
        if (state == 52) {
            //TODO: Find nearest refinery
            moveTarget = refinerySpots.get(0);
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
                    //Can't get to the target
                    System.out.println("Can't get there!");
                    state = 0;
                    moveTarget = null;
                }
            }
        }

    }

}