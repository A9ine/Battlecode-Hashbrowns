package potato;
import battlecode.common.*;

public strictfp class HQ extends Building {
    boolean firstRun = false;
    Team enemy;

    HQ(RobotController rc) throws GameActionException {
        super(rc);
        enemy = team.opponent();
    }

    void findAndShoot() throws GameActionException {
        if (rc.isReady()) {
           //See if there are any enemy drones within striking range
           RobotInfo[] robots = rc.senseNearbyRobots(GameConstants.NET_GUN_SHOOT_RADIUS_SQUARED,enemy);
           //Now shoot them
           if (robots.length > 0) {
               rc.shootUnit(robots[0].getID());
               System.out.println("I shot" + robots[0].getID() + "!");
           }
        }
   }

    @Override
    public void run() throws GameActionException {
        findAndShoot();
        for (Direction dir : directions){
            tryBuild(RobotType.MINER, dir);
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