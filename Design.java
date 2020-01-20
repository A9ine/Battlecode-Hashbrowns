package potato;
import battlecode.common.*;

public strictfp class Design extends Building {

    Design(RobotController rc) throws GameActionException {
        super(rc);
        int round = 1;
        while (hqLoc == null) {
            Transaction[] block = rc.getBlock(round);
            round += 1;
            for (Transaction trans : block) {
                int[] message = getInformation(trans);
                if (message[6] != KEY) {
                    continue;
                }
                if (message[0] == 0) {
                    MapLocation loc = new MapLocation((message[1]%10000-message[1]%100)/100, message[1]%100);
                    if (message[4]  == 1) {
                        hqLoc = loc;
                    }
                }
            }
        }
    }

    int landscaperNumber = 0;

    @Override
    public void run() throws GameActionException {

        if ((rc.getTeamSoup()>150 && landscaperNumber < 10) || rc.getTeamSoup()>landscaperNumber*150) {
            for (Direction dir : directions) {
                if(tryBuild(RobotType.LANDSCAPER, dir)) {
                    landscaperNumber += 1;
                };
            }
        }

    }

}