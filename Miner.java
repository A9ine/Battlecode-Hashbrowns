package potato;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Queue;

import battlecode.common.*;

public strictfp class Miner extends Unit {

    static boolean tryMine() throws GameActionException {
        for (Direction dir : directions) {
            if (rc.isReady() && rc.canMineSoup(dir)) {
                MapLocation loc = rc.adjacentLocation(dir);
                System.out.println("HEY! I'm mining!");
                System.out.printf("%d %d\n",loc.x,loc.y);
                rc.mineSoup(dir);
                if(rc.senseSoup(rc.adjacentLocation(dir)) <= 0) {
                    miningSpots.remove(loc);
                    trySendBlockchain(new int[]{1,loc.x*100+loc.y,1,0,0,0,KEY},1);
                }
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
    public void run() throws GameActionException {
        
    }

}