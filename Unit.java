package potato;
import battlecode.common.*;

public strictfp abstract class Unit extends Robot {

    //BIG TODO: Run a unit away in update if they are about to die

    
    MapLocation destination;
    int state;
    /*
    General
    0 --> Nothing

    Miner
    51 --> Mining
    52 --> Refining
    53 --> Building

    Landscaper
    60 --> Repairing
    61 --> HQ Wall
    62 --> Latticing
    63 --> Path building
    64 --> Offensive
    */

    Unit(RobotController rc) throws GameActionException {
        super(rc);
        state = 0;
    }

    //Navigation

    boolean canMoveWithoutSuicide(Direction dir) throws GameActionException {
        if (rc.canMove(dir) && (myType == RobotType.DELIVERY_DRONE || !rc.senseFlooding(rc.adjacentLocation(dir)))) {
            return true;
        }
        return false;
    }

    double calculateGradient(MapLocation start, MapLocation end) {
        if (end.x-start.x == 0) {
            return -1;
        }
        //Rise over run
        return (end.y-start.y)/(end.x-start.x);
    }

    //General stuff
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

    /* fuzzynav

    Your go to landscaper nav

    */

    boolean buildStair(Direction dir) throws GameActionException {
        MapLocation nextLocation = rc.adjacentLocation(dir);
        Boolean isFlooded = rc.senseFlooding(nextLocation);
        Integer myElevation = rc.senseElevation(myMapLocation);
        Integer nextElevation = rc.senseElevation(nextLocation);
        if (rc.isLocationOccupied(nextLocation) || Math.abs(nextElevation-myElevation)<3) {
            return false;
        }
        //TODO: For now stairs only. We can try to build a bridge
        if (Math.abs(nextElevation-myElevation)<1000 && !isFlooded) {
            System.out.printf("I'm building stairs!%d %d\n",nextLocation.x,nextLocation.y);
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
        //rc.setIndicatorLine(myMapLocation, target, 255, 255, 255);
        if(target.equals(myMapLocation)){
            return true;
        }
        if(rc.isReady()){
            if (canMoveWithoutSuicide(myMapLocation.directionTo(target))) {
                rc.move(myMapLocation.directionTo(target));
                return false;
            }
            if (!rc.adjacentLocation(myMapLocation.directionTo(target)).equals(target)) {
                if (canMoveWithoutSuicide(myMapLocation.directionTo(target).rotateRight())) {
                    rc.move(myMapLocation.directionTo(target).rotateRight());
                    return false;
                }
                if (canMoveWithoutSuicide(myMapLocation.directionTo(target).rotateLeft())) {
                    rc.move(myMapLocation.directionTo(target).rotateLeft());
                    return false;
                }
            }

            if (!buildStair(myMapLocation.directionTo(target))) {
                if (!buildStair(myMapLocation.directionTo(target).rotateRight())) {
                    if (!buildStair(myMapLocation.directionTo(target).rotateLeft())) {
                        bugNavigate(target);
                    }
                }
            }


        }
        return false;
    }

    /*
    Bug Navigation
    
    This type of navigation will mostly be used with... um... everything....

    */

    boolean isBugging = false;
    boolean clockwise = true;
    int count = 0;
    MapLocation prevTarget;
    MapLocation startBugLocation;
    MapLocation obstacle;
    double gradient;
    int stuckTurns;

    //Stop the bug spiral
    boolean bugNavigate(MapLocation target) throws GameActionException {

        //Debugging
        //rc.setIndicatorLine(myMapLocation, target, 255, 0, 0);

        if (target != prevTarget) {
            isBugging = false;
        }
        prevTarget = target;

        if (target.equals(myMapLocation)) {
            isBugging = false;
            return true;
        }

        //If the robot is ready to move

        if (rc.isReady()) {

            if (!isBugging) {

                //If there is a straight path
                if (canMoveWithoutSuicide(myMapLocation.directionTo(target))) {
                    rc.move(myMapLocation.directionTo(target));
                    return false;
                }
                if (canMoveWithoutSuicide(myMapLocation.directionTo(target).rotateRight())) {
                    rc.move(myMapLocation.directionTo(target).rotateRight());
                    return false;
                }
                if (canMoveWithoutSuicide(myMapLocation.directionTo(target).rotateLeft())) {
                    rc.move(myMapLocation.directionTo(target).rotateLeft());
                    return false;
                }

                //If there is no straight path, we must start bugging the obstacle
                isBugging = true;
                clockwise = true;
                count = 0;
                startBugLocation = myMapLocation;
                gradient = calculateGradient(myMapLocation, target);
                obstacle = rc.adjacentLocation(myMapLocation.directionTo(target));
                stuckTurns = 0;
                return(bugNavigate(target));

            } else {

                //The robot is currently trying to bug
                
                //If the bot returns to the original location in the same nav multiple times, it probably is stuck.
                if (startBugLocation.equals(myMapLocation)) {
                    count += 1;
                    if (count >= 3) {
                        //Move in random direction
                        //System.out.println("I don't think I can get there!");
                        return true;
                    }
                }

                Direction obstacleDirection = myMapLocation.directionTo(obstacle);
                Direction targetDirection = obstacleDirection;

                if (canMoveWithoutSuicide(obstacleDirection)) {
                    //Edge case --> The obstacle is.... gone?
                    isBugging = false;
                    return bugNavigate(target);
                }

                if (clockwise) {
                    targetDirection = targetDirection.rotateRight();
                } else {
                    targetDirection = targetDirection.rotateLeft(); 
                }

                while(!canMoveWithoutSuicide(targetDirection)) {
                    if (clockwise) {
                        targetDirection = targetDirection.rotateRight();
                    } else {
                        targetDirection = targetDirection.rotateLeft(); 
                    }
                    //If on the edge of the map, switch bug directions
                    //Or, there is no way past
                    if (!rc.onTheMap(rc.adjacentLocation(targetDirection))) {
                        if (clockwise) {
                            clockwise = false;
                            targetDirection = targetDirection.rotateLeft();
                            return bugNavigate(target);
                        } else {
                            stuckTurns += 1;
                            if (getAdjacent(hqLoc).contains(myMapLocation) && myType != RobotType.LANDSCAPER && stuckTurns > 1 && rc.getRoundNum() > 200) {
                                rc.disintegrate();
                            }
                            System.out.println("There is no path there!");
                            return true;
                        }
                    }
                    if (targetDirection == obstacleDirection) {
                        stuckTurns += 1;
                        System.out.printf("Well I'm stuck! %d\n",rc.getID());
                        //Please don't get stuck near the HQ and kill me
                        //TODO: Make a better way to solve this
                        if (getAdjacent(hqLoc).contains(myMapLocation) && myType != RobotType.LANDSCAPER && stuckTurns > 1 && rc.getRoundNum() > 200) {
                            rc.disintegrate();
                        }
                        return false;
                    }
                }
                if (clockwise) {
                    obstacle = rc.adjacentLocation(targetDirection.rotateLeft());
                } else {
                    obstacle = rc.adjacentLocation(targetDirection.rotateRight());
                }
                
                MapLocation targetLoc = rc.adjacentLocation(targetDirection);
                //Check if it's passing the original line closer to the target
                if (myMapLocation.distanceSquaredTo(target) < startBugLocation.distanceSquaredTo(target)) {
                    if (calculateGradient(myMapLocation, target) > gradient && calculateGradient(targetLoc, target) <= gradient) {
                        isBugging = false;
                    } 
                    else if (calculateGradient(targetLoc, target) >= gradient) {
                        isBugging = false;
                    }
                }
                
                rc.move(targetDirection);
                return false;

            }
            
        }

        return false;

    }

    static Direction randomDirection() {
        return directions[(int) (Math.random() * directions.length)];
    }

    static MapLocation randomLocation() {
        return new MapLocation((int)(Math.random() * rc.getMapWidth()-1),(int)(Math.random() * rc.getMapHeight()-1));
    }

}