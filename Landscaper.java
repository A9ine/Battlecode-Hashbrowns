package potato;
import battlecode.common.*;
import java.util.*;

public strictfp class Landscaper extends Unit {

    ArrayList<MapLocation> hqWall = new ArrayList<MapLocation>();
    MapLocation staticWallTarget;
    int wallIter;
    int storage = 0;

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

                    }
                    wallIter = hqWall.size()-1;
                }
            }
        }
    }
    
    void landscaperUpdate() throws GameActionException {
        update();
    }
    
    @Override
    boolean canMoveWithoutSuicide(Direction dir) throws GameActionException{
        if(!rc.senseFlooding(rc.adjacentLocation(dir)) && !((rc.senseElevation(dir)-3-rc.senseElevation(Direction.CENTER))>25)){
            if(abs(rc.senseElevation(dir)-rc.senseElevation(Direction.CENTER)) > 3){
                //up
                if(rc.senseElevation(dir)-3 > rc.senseElevation(Direction.CENTER)){
                    for(Direction mdir : Direction){
                        if(mdir == dir) continue;
                        while(rc.canDigDirt(mdir)){rc.digDirt(mdir); storage++;}
                        if(storage >= rc.senseElevation(dir)-3-rc.senseElevation(Direction.CENTER)){
                            break;
                        }
                    }
                    while(rc.senseElevation(Direction.CENTER) < rc.senseElevation(dir)-3){rc.depositDirt(Direction.CENTER);}
                    return true;
                }
                //down
                else{
                    while(rc.senseElevation(Direction.CENTER)-3 > rc.senseElevation(dir)){
                        if(!rc.canDigDirt(Direction.CENTER)){ //cant dig cuz reach limit 25. dispose some.
                            for(Direction mdir : Direction){
                                if(rc.canDepositDirt(mdir) && mdir!=dir){
                                    rc.depositDirt(mdir);
                                    break;
                                }
                            }
                        }
                        rc.digDirt(Direction.CENTER);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    boolean fuzzyNavigation(MapLocation target) throws GameActionException{
        rc.setIndicatorLine(myMapLocation, target, 0, 144, 144);
        if(target.equals(myMapLocation)){
            return true;
        }
        if(rc.isReady()){
            if(canMoveWithoutSuicide(myMapLocation.directionTo(target))){
                rc.move(myMapLocation.directionTo(target));
            }else{
                bugNavigate(target);
            }
        }
    }

    @Override
    public void run() throws GameActionException {
        landscaperUpdate();

        if (state == 0) {
            //I must be a defensive robot if I'm near the HQ.
            if (hqLoc.distanceSquaredTo(myMapLocation) < 16) {
                state = 51;
            }
        }
        if (state == 51) {
            if (staticWallTarget != null && staticWallTarget.equals(myMapLocation)) {
                if (rc.senseFlooding(rc.adjacentLocation(myMapLocation.directionTo(hqLoc)))) {
                    if (rc.canDepositDirt(myMapLocation.directionTo(hqLoc))) {
                        rc.depositDirt(myMapLocation.directionTo(hqLoc));
                        storage--;
                    }
                }
                if (rc.canDepositDirt(Direction.CENTER)) {
                    rc.depositDirt(Direction.CENTER);
                    storage--;
                }
                for (Direction dir : directions) {
                    if (rc.adjacentLocation(dir).distanceSquaredTo(hqLoc)<myMapLocation.distanceSquaredTo(hqLoc)) {
                        continue;
                    }
                    if (!hqWall.contains(rc.adjacentLocation(dir)) && rc.canDigDirt(dir)) {
                        rc.digDirt(dir);
                        storage++;
                    }
                }
            } else {
                if (staticWallTarget == null) {
                    staticWallTarget = hqWall.get(wallIter);
                    if (!rc.onTheMap(staticWallTarget)) {
                        staticWallTarget = null;
                    }
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
                                    storage--;
                                }
                                for (Direction dir : directions) {
                                    if (!hqWall.contains(rc.adjacentLocation(dir)) && rc.canDigDirt(dir)) {
                                        rc.digDirt(dir);
                                        storage++;
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