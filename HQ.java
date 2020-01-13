package potato;
import battlecode.common.*;

public strictfp class HQ extends Building {
    boolean firstRun = false;

    HQ(RobotController rc) throws GameActionException {
        super(rc);
    }

    @Override
    public void run() throws GameActionException {
        for (Direction dir : directions){
            if(tryBuild(RobotType.MINER, dir) && firstRun == false) {
                rc.build(RobotType.MINER,dir);
                rc.build(RobotType.MINER,dir);
                firstRun = true;
                break;
            }
            if(tryBuild(RobotType.MINER, dir) && rc.getTeamSoup()>=70) {
                rc.build(RobotType.MINER,dir);
                break;
            }
        }
    }
}