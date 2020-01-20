package potato;
import battlecode.common.*;
import java.util.*;
import java.math.*;

public strictfp class Landscaper extends Unit {

    ArrayList<MapLocation> staticWallsToBeBuilt = new ArrayList<MapLocation>();
    ArrayList<Integer> staticWallsOrderID = new ArrayList<Integer>(); 
    HashMap<MapLocation, Boolean> isWall = new HashMap<MapLocation, Boolean>(); 
    MapLocation staticWallTarget;
    Integer orderID;
    Boolean fulfilled = false;

    boolean buildStair(Direction dir) throws GameActionException {
        MapLocation nextLocation = rc.adjacentLocation(dir);
        Boolean isFlooded = rc.senseFlooding(nextDirection);
        Integer myElevation = rc.senseElevation(myMapLocation);
        Integer nextElevation = rc.senseElevation(nextLocation);
        //TODO: For now stairs only. We can try to build a bridge
        if (Math.abs(nextElevation-myElevation)<15 && !isFlooded) {
            if (nextElevation > myElevation) {
                tryDepositDirt(Direction.CENTER);
                tryDigDirt(dir);
            } else {
                tryDepositDirt(dir);
                tryDigDirt(Direction.CENTER);
            }
            return true;
        }
        return false;
    }

    boolean fuzzyNavigate(MapLocation target) throws GameActionException{
        rc.setIndicatorLine(myMapLocation, target, 0, 144, 144);
        if(target.equals(myMapLocation)){
            return true;
        }
        if(rc.isReady()){
            if (canMoveWithoutSuicide(myMapLocation.directionTo(target))) {
                rc.move(myMapLocation.directionTo(target));
                return false;
            }
            if (myMapLocation.y < target.y && canMoveWithoutSuicide(Direction.NORTH)) {
                rc.move(Direction.NORTH);
                return false;
            }
            if (myMapLocation.y > target.y && canMoveWithoutSuicide(Direction.SOUTH)) {
                rc.move(Direction.SOUTH);
                return false;
            }
            if (myMapLocation.x < target.x && canMoveWithoutSuicide(Direction.EAST)) {
                rc.move(Direction.EAST);
                return false;
            }
            if (myMapLocation.x > target.x && canMoveWithoutSuicide(Direction.WEST)) {
                rc.move(Direction.WEST);
                return false;
            }

            if (!buildStair(myMapLocation.directionTo(target))) {
                bugNavigate(target);
            };


        }
        return false;
    }

    boolean tryDepositDirt(Direction dir) throws GameActionException {
        if (rc.canDepositDirt(dir)) {
            rc.depositDirt(dir);
        }
        return false;
    }

    boolean tryDigDirt(Direction dir) throws GameActionException {
        if (rc.canDigDirt(dir)) {
            rc.digDirt(dir);
        }
        return false;
    }

    Landscaper(RobotController rc) throws GameActionException {
        super(rc);
        //Have to find the hqLoc
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
                    for (MapLocation nearLoc : getAdjacent(hqLoc)) {
                        staticWallsToBeBuilt.add(nearLoc);
                        isWall.put(nearLoc,true);
                        staticWallsOrderID.add(nearLoc.x*100+nearLoc.y);
                    }
                }
            }
        }
    }

    void landcaperUpdate() throws GameActionException {
        update();
        landscaperCommunications();
    }

    void landscaperCommunications() throws GameActionException {

        for (Transaction trans : latestCommunication) {
            int[] message = getInformation(trans);
            if (message[6] != KEY) {
                continue;
            }

            if (message[0] == 5) {
                for (int i = 0; i < staticWallsOrderID.size(); i++) {
                    if (staticWallsOrderID.get(i) == message[5]) {
                        staticWallsOrderID.remove(i);
                        if (staticWallTarget == staticWallsToBeBuilt.get(i)) {
                            staticWallTarget = null;
                        }
                        staticWallsToBeBuilt.remove(i);
                        break;
                    }
                }
            }
        }

    }
    


    @Override
    public void run() throws GameActionException {
        System.out.println(staticWallTarget);
        landcaperUpdate();

        if (state == 0) {
            //Might as well check the HQ Walls
            if (hqLoc.distanceSquaredTo(myMapLocation) < 16) {
                state = 51;
            }
        }

        if (state == 51) {
            if (staticWallTarget != null && staticWallTarget.equals(myMapLocation)) {

                if (!fulfilled) {
                    fulfilled = true;
                    while(!tryBroadcastSuccess(orderID, averageSend));
                }
                ArrayList<MapLocation> adjacent = getAdjacent();
                MapLocation minElevationTarget = myMapLocation;
                Integer minElevation = rc.senseElevation(myMapLocation);
                for (MapLocation loc : adjacent) {
                    if (isWall.containsKey(loc) && isWall.get(loc) && rc.senseElevation(loc) < minElevation && rc.senseRobotAtLocation(loc) != null && rc.senseRobotAtLocation(loc).getTeam()==team && rc.senseRobotAtLocation(loc).getType()==RobotType.LANDSCAPER) {
                        minElevation = rc.senseElevation(loc);
                        minElevationTarget = loc;
                    }
                }
                if (tryDepositDirt(myMapLocation.directionTo(minElevationTarget))) {
                    Clock.yield();
                };
                for (Direction dir : directions) {
                    if (!isWall.containsKey(rc.adjacentLocation(dir)) || !isWall.get(rc.adjacentLocation(dir))) {
                        if (tryDigDirt(dir)) {
                            Clock.yield();
                        }
                    }
                }
            } else {
                if (staticWallTarget == null) {
                    if (staticWallsToBeBuilt.size() > 0) {
                        staticWallTarget = staticWallsToBeBuilt.get(0);
                        orderID = staticWallsOrderID.get(0);
                        staticWallsOrderID.remove(0);
                        staticWallsToBeBuilt.remove(0);
                    }
                }
                if (rc.canSenseLocation(staticWallTarget) && rc.senseRobotAtLocation(staticWallTarget) != null) {
                    if (rc.senseRobotAtLocation(staticWallTarget).getTeam() == team && rc.senseRobotAtLocation(staticWallTarget).getType()==RobotType.LANDSCAPER) {
                        System.out.println("OK?");
                        staticWallTarget = null;
                        orderID = null;
                    }
                }
                fuzzyNavigation(staticWallTarget);
            }
            
        }
    }
}