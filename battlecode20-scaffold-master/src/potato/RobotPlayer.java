package potato;
import battlecode.common.*;

public strictfp class RobotPlayer {

    static RobotController rc;
    static int turnCount;

    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

       //Init
        RobotPlayer.rc = rc;
        turnCount = 0;

        switch (rcgetType()) {
            case value:
                
                break;
        
            default:
                break;
        }

        while (true) {
            turnCount += 1;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }
}
