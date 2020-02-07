package potato;
import battlecode.common.*;

public strictfp class Design extends Building {

    boolean hasEmpty = false;

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

    void designSchoolCommunication() throws GameActionException {

        for (Transaction trans : latestCommunication) {
            int[] message = getInformation(trans);
            if (message[6] != KEY) {
                continue;
            }

            if (message[0] == 6) {
                hasEmpty = false;
                int walls = message[2];
                for (int i = 0; i < getAdjacent(hqLoc).size(); i ++) {
                    if (walls%10 == 0) {
                        hasEmpty = true;
                    }
                    walls = walls/10;
                }
            }
        }
    }

    int landscaperNumber = 0;

    @Override
    public void run() throws GameActionException {

        update();
        designSchoolCommunication();
        System.out.println(hasEmpty);
        if ((rc.getTeamSoup() > 200 && (landscaperNumber < 10 || hasEmpty)) || (rc.getTeamSoup() > 550 && Math.random()<0.2)) {
            for (Direction dir : directions) {
                if(tryBuild(RobotType.LANDSCAPER, dir)) {
                    landscaperNumber += 1;
                };
            }
        }

    }

}