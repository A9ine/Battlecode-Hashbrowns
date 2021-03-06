package potato;
import battlecode.common.*;
import java.util.*;
import java.math.*;

public strictfp class Landscaper extends Unit {

    ArrayList<MapLocation> staticWallsToBeBuilt = new ArrayList<MapLocation>();
    ArrayList<Integer> staticWallsOrderID = new ArrayList<Integer>(); 
    HashMap<MapLocation, Boolean> isWall = new HashMap<MapLocation, Boolean>(); 
    MapLocation moveTarget;
    MapLocation staticWallTarget;
    Integer orderID;
    Boolean fulfilled = false;
    int latticeElevation = 10;


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
                        isWall.put(nearLoc,true);
                    }
                    //System.out.println(staticWallsOrderID);
                }
            }
        }
    }

    void landcaperUpdate() throws GameActionException {
        if (turn > 1500) {
            latticeElevation = 20;
        }
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

            if (message[0] == 6) {
                int walls = message[2];
                ArrayList<MapLocation> potentialWalls = getAdjacent(hqLoc);
                //System.out.println(potentialWalls);
                Collections.reverse(potentialWalls);
                for (int i = 0; i < potentialWalls.size(); i ++) {
                    if (walls%10 == 0) {
                        if (!staticWallsToBeBuilt.contains(potentialWalls.get(i))) {
                            staticWallsToBeBuilt.add(potentialWalls.get(i));
                            staticWallsOrderID.add(potentialWalls.get(i).x*100+potentialWalls.get(i).y);
                        }
                    }
                    walls = walls/10;
                }
            }
        }

    }

    @Override
    public void run() throws GameActionException {
        System.out.println(state);
        landcaperUpdate();

        //System.out.println(staticWallTarget);

        //Always prioritize building repair and nearby attack
        for (MapLocation loc : getAdjacent()) {
            if (rc.senseRobotAtLocation(loc) != null && rc.senseRobotAtLocation(loc).getTeam()==team &&rc.senseRobotAtLocation(loc).getType().isBuilding() && rc.senseRobotAtLocation(loc).dirtCarrying > 0){
                System.out.println("Repairing!");
                tryDigDirt(myMapLocation.directionTo(loc));
                if (rc.getDirtCarrying() > 20) {
                    for (Direction dir : directions) {
                        if (!rc.isLocationOccupied(rc.adjacentLocation(dir))){
                            tryDepositDirt(dir);
                        }
                    }
                }
            }

            if (rc.senseRobotAtLocation(loc) != null && rc.senseRobotAtLocation(loc).getTeam()!=team &&rc.senseRobotAtLocation(loc).getType().isBuilding()){
                System.out.println("Enemy spotted!");
                tryDepositDirt(myMapLocation.directionTo(loc));

                for (MapLocation locloc : getAdjacent()) {
                    if (rc.isLocationOccupied(locloc) && rc.senseRobotAtLocation(loc).getType().isBuilding() || rc.senseElevation(locloc) < -50) {
                        continue;
                    }
                    if (!rc.isLocationOccupied(locloc) && !(isWall.containsKey(loc) && isWall.get(loc))) {

                        if (rc.getDirtCarrying() == 0) {
                            tryDigDirt(myMapLocation.directionTo(locloc));
                        }
                        continue;
                    } 
                }

                return;
            }
        }

        if (state == 0) {
            System.out.println(moveTarget);
            //HQ Walls are always top priority
            if (staticWallsToBeBuilt.size()>0) {
                state = 51;
            }
            if (moveTarget == null || myMapLocation.equals(moveTarget)) {
                moveTarget = randomLocation();
            }
            boolean done = true;
            ArrayList<MapLocation> adjacentLoc = getAdjacent();
            adjacentLoc.add(myMapLocation);
            for (MapLocation loc : getAdjacent()) {
                if (rc.isLocationOccupied(loc) && rc.senseRobotAtLocation(loc).getType().isBuilding() || rc.senseElevation(loc) < -50) {
                    continue;
                }
                if (isLatticeHole(myMapLocation)) {
                    fuzzyNavigate(loc);
                    done = false;
                    break;
                }
                if (isLatticeHole(loc) && !loc.equals(myMapLocation)) {
                    if (rc.getDirtCarrying() == 0) {
                        tryDigDirt(myMapLocation.directionTo(loc));
                    }
                    continue;
                } 
                if (rc.senseElevation(loc) < latticeElevation) {
                    done = false;
                    tryDepositDirt(myMapLocation.directionTo(loc));
                }
                
            }
            //System.out.println(done);
            if (done) {
                if (bugNavigate(moveTarget)) {
                    moveTarget = randomLocation();
                    while (isLatticeHole(moveTarget)) {
                        moveTarget = randomLocation();
                    }
                }
                
            }
        }

        if (state == 51) {
            if (staticWallTarget != null && staticWallTarget.equals(myMapLocation)) {

                /*if (!fulfilled) {
                    fulfilled = true;
                    tryBroadcastSuccess(orderID, averageSend);
                }*/
                ArrayList<MapLocation> adjacent = getAdjacent();
                MapLocation minElevationTarget = myMapLocation;
                Integer minElevation = rc.senseElevation(myMapLocation);

                for (MapLocation wall : staticWallsToBeBuilt) {
                    if (rc.canSenseLocation(wall) && rc.senseElevation(wall) + 100 < rc.senseElevation(myMapLocation)) {
                        fuzzyNavigate(wall);
                        staticWallTarget = rc.getLocation();
                        break; 
                    }
                }

                for (MapLocation loc : adjacent) {
                    if (isWall.containsKey(loc) && isWall.get(loc) && rc.senseElevation(loc) < minElevation && ((rc.senseRobotAtLocation(loc) != null && rc.senseRobotAtLocation(loc).getTeam()==team && rc.senseRobotAtLocation(loc).getType()==RobotType.LANDSCAPER) || rc.getRoundNum() > 600)) {
                        minElevation = rc.senseElevation(loc);
                        minElevationTarget = loc;
                    }
                }
                if (!tryDepositDirt(myMapLocation.directionTo(minElevationTarget))) {
                    // Prioritize digging in the grid format
                    for (Direction dir : directions) {
                        MapLocation loc = rc.adjacentLocation(dir);
                        if ((!isWall.containsKey(loc) || !isWall.get(loc)) && isLatticeHole(loc)) {
                            if (tryDigDirt(dir)) {
                            }
                        }
                    }
                }
            } else {
                if (staticWallsToBeBuilt.size()<=0) {
                    state = 0;
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

                if (staticWallTarget == null) {
                    state = 0;
                }
            }
            
        }
    }
}