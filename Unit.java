package potato;
import battlecode.common.*;

public strictfp abstract class Unit extends Robot {

    //BIG TODO: Run a unit away in update if they are about to die

    
    MapLocation destination;
    int state;
    /*
    General
    0 --> Nothing
    1 --> Exploring
    2 --> Attacking
    3 --> Defending

    Miner
    51 --> Mining
    52 --> Refining
    53 --> Building
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

                //If there is no straight path, we must start bugging the obstacle
                isBugging = true;
                clockwise = true;
                count = 0;
                startBugLocation = myMapLocation;
                gradient = calculateGradient(myMapLocation, target);
                obstacle = rc.adjacentLocation(myMapLocation.directionTo(target));
                return(bugNavigate(target));

            } else {

                //The robot is currently trying to bug
                
                //If the bot returns to the original location in the same nav multiple times, it probably is stuck.
                if (startBugLocation.equals(myMapLocation)) {
                    count += 1;
                    if (count >= 3) {
                        System.out.println("I don't think I can get there!");
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
                            System.out.println("There is no path there!");
                            return true;
                        }
                    }
                    if (targetDirection == obstacleDirection) {
                        System.out.printf("Well I'm stuck! %d\n",rc.getID());
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