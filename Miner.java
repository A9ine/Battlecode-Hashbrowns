package potato;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Queue;

import battlecode.common.*;

public strictfp class Miner extends Unit {

    static ArrayList<MapLocation> miningSpots = new ArrayList<MapLocation>();

    Miner(RobotController rc) throws GameActionException {
        super(rc);
    }

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

    static void goRefine() throws GameActionException {
        
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

        System.out.printf("I currently have %d soup! \n",rc.getSoupCarrying());

        Transaction[] recentBlock = rc.getBlock(rc.getRoundNum()-1);
        for (Transaction trans : recentBlock) {
            System.out.println(trans);
            int[] t = trans.getMessage();
            if (t[6] == KEY) {
                if (t[0] == 1 && t[2] == 0) {
                    MapLocation loc = new MapLocation(t[1]/100,t[1]%100);
                    if (!miningSpots.contains(loc)) {
                        miningSpots.add(loc);
                    }
                }
                if (t[0] == 1 && t[2] == 1) {
                    MapLocation loc = new MapLocation(t[1]/100,t[1]%100);
                    if (miningSpots.contains(loc)) {
                        miningSpots.remove(loc);
                    }
                }
            }
        }


        for (MapLocation loc : getLineOfSense()) {
            if (rc.senseSoup(loc) > 0 && !miningSpots.contains(loc)) {
                miningSpots.add(loc);
                trySendBlockchain(new int[]{1,loc.x*100+loc.y,0,rc.senseSoup(loc),0,0,KEY},1);
            }
        }

        if (rc.isReady()) {
            if (!tryMine()) {
                if (miningSpots.size()>0) {
                    tryNavigate(miningSpots.get(0));
                }
            }
            if (rc.getSoupCarrying()==RobotType.MINER.soupLimit) {
                tryRefine();
                tryNavigate(hqLoc);
            }
        } 
        
    }

}