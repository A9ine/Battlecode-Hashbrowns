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

    boolean tryMine() throws GameActionException {
        for (Direction dir : Direction.allDirections()){
            if (rc.isReady() && rc.canMineSoup(dir)) {
                rc.mineSoup(dir);
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
        update();
        sensoryUpdate();

        if (turn == 1) {
            for (MapLocation loc : sensedMapLocations) {
                if (rc.isLocationOccupied(loc) && rc.senseRobotAtLocation(loc).getType() == RobotType.HQ && rc.senseRobotAtLocation(loc).getTeam() == team) { 
                    hqLoc = loc;
                    refinerySpots.add(loc);
                }
            }
        }

        if (state == 0) {
            //If the robot is not ready, try running a BFS over the local map to find soup spots
            Queue<MapLocation> queue = new LinkedList<MapLocation>();
            HashMap<MapLocation,Boolean> visited = new HashMap<MapLocation,Boolean>();
            queue.add(myMapLocation);
            while (queue.size() > 0) {
                MapLocation current = queue.poll();
                if (visited.containsKey(current)) {
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
            System.out.printf("Currently carrying %d soup",rc.getSoupCarrying());
            tryMine();
            if (rc.getSoupCarrying() == RobotType.MINER.soupLimit) {
                state = 52;
            }
        }

        //Refining
        if (state == 52) {
            //TODO: Find nearest refinery
            moveTarget = refinerySpots.get(0);
            tryRefine();
            if (rc.getSoupCarrying() == 0) {
                state = 0;
            }
        }

        if (bugNavigate(moveTarget)) {
            if (!myMapLocation.equals(moveTarget)) {
                //Can't get to the target
                System.out.println("Can't get there!");
                state = 0;
                moveTarget = null;
            }
        }

    }

}