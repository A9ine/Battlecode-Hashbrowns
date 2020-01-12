package potato;
import battlecode.common.*;

public strictfp class Miner extends Unit {

    Miner(RobotController rc) {
        super(rc);
    }

    static boolean tryMine() throws GameActionException {
        for (Direction dir : directions) {
            if (rc.isReady() && rc.canMineSoup(dir)) {
                rc.mineSoup(dir);
                return true;
            }
        }
        return false;
    }

    static boolean tryRefine() throws GameActionException {
        for (Direction dir : directions) {
            if (rc.isReady() && rc.canDepositSoup(dir)) {
                rc.depositSoup(dir, rc.getSoupCarrying());
                return true;
            } 
        }
        return false;
    }


    @Override
    public void run() throws GameActionException{
        // TODO Auto-generated method stub
    }

}