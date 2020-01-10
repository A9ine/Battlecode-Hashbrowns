package potato;
import battlecode.common.*;

public strictfp abstract class Robot {

    static int turnCount;

    protected static RobotController rc;

    Robot(RobotController rc) {
        Robot.rc = rc;
    }

    public abstract void run();

}