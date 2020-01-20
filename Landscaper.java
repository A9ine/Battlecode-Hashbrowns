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
                    //System.out.println(staticWallsOrderID);
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
        landcaperUpdate();

        //System.out.println(state);
        //System.out.println(staticWallTarget);

        if (state == 0) {
            //Might as well check the HQ Walls
            state = 51;
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
                    if (isWall.containsKey(loc) && isWall.get(loc) && rc.senseElevation(loc) < minElevation && ((rc.senseRobotAtLocation(loc) != null && rc.senseRobotAtLocation(loc).getTeam()==team && rc.senseRobotAtLocation(loc).getType()==RobotType.LANDSCAPER) || rc.getRoundNum() > 600)) {
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
                if (staticWallsToBeBuilt.size()<=0) {
                    state = 0;
                    Clock.yield();
                }
                if (staticWallTarget == null && staticWallsToBeBuilt.size()>0) {
                    int index = 0;
                    Integer minimumDistance = Integer.MAX_VALUE;
                    
                    for (int i = 0; i < staticWallsToBeBuilt.size(); i ++) {
                        if (minimumDistance > myMapLocation.distanceSquaredTo(staticWallsToBeBuilt.get(i))) {
                            index = i;
                            minimumDistance = myMapLocation.distanceSquaredTo(staticWallsToBeBuilt.get(i)); 
                        }
                    }
                    staticWallTarget = staticWallsToBeBuilt.get(index);
                    orderID = staticWallsOrderID.get(index);
                    staticWallsOrderID.remove(index);
                    staticWallsToBeBuilt.remove(index);
                }
                if(staticWallTarget == null) {
                    state = 0;
                    return;
                }
                if (rc.canSenseLocation(staticWallTarget) && rc.senseRobotAtLocation(staticWallTarget) != null) {
                    if (rc.senseRobotAtLocation(staticWallTarget).getTeam() == team && rc.senseRobotAtLocation(staticWallTarget).getType()==RobotType.LANDSCAPER) {
                        staticWallTarget = null;
                        orderID = null;
                    }
                }
                if (staticWallTarget != null) {
                    if (myMapLocation.distanceSquaredTo(staticWallTarget) <= 2){
                        fuzzyNavigate(staticWallTarget);
                    }
                    if (bugNavigate(staticWallTarget) && !myMapLocation.equals(staticWallTarget)){
                        fuzzyNavigate(staticWallTarget);
                    }
                }
                
            }
            
        }
    }
}