package potato;
import battlecode.common.*;

public strictfp abstract class Robot {

    static int turnCount;
    static int KEY = 69420;

    protected static RobotController rc;

    Robot(RobotController rc) {
        Robot.rc = rc;
    }

    static int[] DecodeBlock(int[] message) {
        for (int i = 0; i < message.length; i ++) {
            message[i] -= KEY;
        }
        return message;
    }

    static boolean trySendBlockchain(int[] message, int cost) throws GameActionException {
        //All our messages will be encoded with the key below. If you are not a potato and reading this
        //We are going to change the key before competitions.
        for (int i = 0; i < message.length; i ++) {
            message[i] += KEY;
        }
        if (rc.canSubmitTransaction(message, cost)) {
            rc.submitTransaction(message, cost);
            return true;
        }
        return false;
    }

    public abstract void run();

}