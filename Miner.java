package potato;

import battlecode.common.*;

public strictfp class Miner extends Unit {

    Miner(RobotController rc) throws GameActionException {
        super(rc);
    }


    @Override
    public void run() throws GameActionException {
        update();
        bugNavigate(new MapLocation(rc.getMapWidth()-1,rc.getMapHeight()-1));
        
    }

}