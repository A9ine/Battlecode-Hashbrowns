package potato;
import battlecode.common.*;
import java.util.*;

public strictfp class Landscaper extends Unit {

    ArrayList<MapLocation> hqWall = new ArrayList<MapLocation>();
    MapLocation staticWallTarget;
    int wallIter;

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
                        //From bottom to top
                        hqWall.add(loc.translate(0,2));
                        hqWall.add(loc.translate(1,2));
                        hqWall.add(loc.translate(-1,2));
                        hqWall.add(loc.translate(-2,2));
                        hqWall.add(loc.translate(2,2));
                        hqWall.add(loc.translate(-2,1));
                        hqWall.add(loc.translate(2,1));
                        hqWall.add(loc.translate(-2,0));
                        hqWall.add(loc.translate(2,0));
                        hqWall.add(loc.translate(2,-1));
                        hqWall.add(loc.translate(-2,-1));
                        hqWall.add(loc.translate(2,-2));
                        hqWall.add(loc.translate(-2,-2));
                        hqWall.add(loc.translate(-1,-2));
                        hqWall.add(loc.translate(1,-2));
                        hqWall.add(loc.translate(0,-2));
                        for (MapLocation wall : hqWall) {
                            if (!rc.onTheMap(wall)) {
                                hqWall.remove(wall);
                            }
                        }
                    }
                    wallIter = hqWall.size()-1;
                }
            }
        }
    }
    
    void landcaperUpdate() throws GameActionException {
        update();
    }

    @Override
    public void run() throws GameActionException {
        landcaperUpdate();

        if (state == 0) {
            //I must be a defensive robot
            if (hqLoc.distanceSquaredTo(myMapLocation) < 16) {
                state = 51;
            }
        }
        if (state == 51) {
            if (staticWallTarget != null && staticWallTarget.equals(myMapLocation)) {
                if (rc.senseFlooding(rc.adjacentLocation(myMapLocation.directionTo(hqLoc)))) {
                    if (rc.canDepositDirt(myMapLocation.directionTo(hqLoc))) {
                        rc.depositDirt(myMapLocation.directionTo(hqLoc));
                    }
                }
                if (rc.canDepositDirt(Direction.CENTER)) {
                    rc.depositDirt(Direction.CENTER);
                }
                for (Direction dir : directions) {
                    if (rc.adjacentLocation(dir).distanceSquaredTo(hqLoc)<myMapLocation.distanceSquaredTo(hqLoc)) {
                        continue;
                    }
                    if (!hqWall.contains(rc.adjacentLocation(dir)) && rc.canDigDirt(dir)) {
                        rc.digDirt(dir);
                    }
                }
            } else {
                if (staticWallTarget == null) {
                    staticWallTarget = hqWall.get(wallIter);
                    wallIter -= 1;
                    if (wallIter == -1) {
                        wallIter = hqWall.size()-1; 
                    }
                } else {
                    if (rc.canSenseLocation(staticWallTarget))  {
                        //If flooded, try to block it up 
                        if (rc.senseFlooding(staticWallTarget)) {
                            if (getAdjacent().contains(staticWallTarget)){
                                if (rc.canDepositDirt(myMapLocation.directionTo(staticWallTarget))) {
                                    rc.depositDirt(myMapLocation.directionTo(staticWallTarget));
                                }
                                for (Direction dir : directions) {
                                    if (!hqWall.contains(rc.adjacentLocation(dir)) && rc.canDigDirt(dir)) {
                                        rc.digDirt(dir);
                                    }
                                }
                            }
                        }
                        RobotInfo robot = rc.senseRobotAtLocation(staticWallTarget);
                        if (robot != null) {
                            if (robot.getTeam() == team && robot.getType() == RobotType.LANDSCAPER) {
                                staticWallTarget = null;
                            }
                        }
                    }
                    if (staticWallTarget != null) {
                        bugNavigate(staticWallTarget);
                    }
                }
            }
            
        }
    }
}