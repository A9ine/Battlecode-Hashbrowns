package potato;
import battlecode.common.*;

public strictfp class Landscaper extends Unit {

    Landscaper(RobotController rc) throws GameActionException {
        super(rc);
        //Have to find the hqLoc
        int round = 1;
        while (hqLoc == null) {
            Transaction[] block = rc.getBlock(round);
            round += 1;
            for (Transaction trans : block) {
                if (message[0] == 0) {
                    MapLocation loc = new MapLocation((message[1]%10000-message[1]%100)/100, message[1]%100);
                    if (message[4]  == 1) {
                        hqLoc = loc;
                    }
                }
            }
        }
    }
    
    void landcaperUpdate() throws GameActionException {
        update();
    }

    @Override
    public void run() throws GameActionException {
        landscaperUpdate();
        
    }

}