package potato;
import battlecode.common.*;

public strictfp class HQ extends Building {
    boolean firstRun = false;

    HQ(RobotController rc) throws GameActionException {
        super(rc);
    }

    @Override
    public void run() throws GameActionException {
        // if HQ is running for the first time, HQ produces 2 miners
        if(firstRun == false) {
            for (Direction dir : directions) {
                if (tryBuild(RobotType.MINER, dir)) {
                    rc.build(RobotType.MINER,dir);
                    rc.build(RobotType.MINER,dir);
                    firstRun = true;
                    break;  
                }
            }        
        }
        else {
            if (rc.getTeamSoup()<=70) {
                for (Direction dir : directions) {
                    if (tryBuild(RobotType.MINER, dir)){
                        rc.build(RobotType.MINER,dir);
                        break;
                    }
                }
            }
        }
    }
}