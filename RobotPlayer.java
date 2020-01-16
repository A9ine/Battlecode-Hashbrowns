package potato;
import battlecode.common.*;

public strictfp class RobotPlayer {

    static RobotController rc;
    static Robot robot;

    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

       //Init
        RobotPlayer.rc = rc;

        switch (rc.getType()) {
            case HQ:                 robot = new HQ(rc);                 break;
            case MINER:              robot = new Miner(rc);              break;
            case REFINERY:           robot = new Refinery(rc);           break;
            case VAPORATOR:          robot = new Vaporator(rc);          break;
            case DESIGN_SCHOOL:      robot = new Design(rc);             break;
            case FULFILLMENT_CENTER: robot = new Fulfillment(rc);        break;
            case LANDSCAPER:         robot = new Landscaper(rc);         break;
            case DELIVERY_DRONE:     robot = new Drone(rc);              break;
            case NET_GUN:            robot = new Turret(rc);             break;
        }

        while (true) {
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                robot.run();
                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
                //TODO: DELETE THIS LINE BEFORE THE COMPETITION
                //rc.resign();
                //TODO: Issues with sensor range
            }
        }
    }
}
