package potato;
import battlecode.common.*;

public strictfp class Design extends Building {

    Design(RobotController rc) throws GameActionException {
        super(rc);
    }

    int landscaperNumber = 0;

    @Override
    public void run() throws GameActionException {

        if (rc.getTeamSoup()>landscaperNumber * 150) {
            for (Direction dir : directions) {
                if(tryBuild(RobotType.LANDSCAPER, dir)) {
                    landscaperNumber += 1;
                };
            }
        }

    }

}