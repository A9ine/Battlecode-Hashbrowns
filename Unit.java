package potato;
import battlecode.common.*;

public strictfp abstract class Unit extends Robot {

    
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
    }

    //Navigation

    boolean canMoveWithoutSuicide(Direction dir) throws GameActionException {
        if (rc.canMove(dir) && !rc.senseFlooding(rc.adjacentLocation(dir))) {
            return true;
        }
        return false;
    }

    double calculateGradient(MapLocation start, MapLocation end) {
        //Rise over run
        return (end.y-start.y)/(end.x-start.x);
    }

    /*
    Bug Navigation
    
    This type of navigation will mostly be used with miners, 
    and maybe landscapers who don't want to destroy everything

    */

    boolean isBugging = false;
    MapLocation startBugLocation;
    MapLocation obstacle;
    double gradient;

    boolean bugNavigate(MapLocation target) throws GameActionException {

        if (target == myMapLocation) {
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
                startBugLocation = myMapLocation;
                gradient = calculateGradient(myMapLocation, target);
                obstacle = rc.adjacentLocation(myMapLocation.directionTo(target));
                return(bugNavigate(target));

            } else {
                //The robot is currently trying to bug
                //TODO: Implement counterclockwise if hit map boundary
                Direction obstacleDirection = myMapLocation.directionTo(obstacle);
                Direction targetDirection = obstacleDirection;

                while(!canMoveWithoutSuicide(targetDirection)) {
                    targetDirection = targetDirection.rotateRight();
                    if (targetDirection == obstacleDirection) {
                        System.out.printf("Well I'm stuck! %d\n",rc.getID());
                        return false;
                    }
                }

                obstacle = rc.adjacentLocation(targetDirection.rotateLeft());
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

}