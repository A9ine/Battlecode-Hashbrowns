package potato;
import battlecode.common.*;

public strictfp class Fulfillment extends Building {

    int droneCount;
    double multiplier;

    Fulfillment(RobotController rc) throws GameActionException {
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

    @Override
    public void run() throws GameActionException {
        update();
        multiplier = 10;
        if (nearbyRobots.length > 0) {
            for (RobotInfo robot : nearbyRobots) {
                if (robot.getTeam() != team && robot.getType() != RobotType.DELIVERY_DRONE && !robot.getType().isBuilding()) {
                    multiplier = 0;
                }
            }
        }
        System.out.println(multiplier);
        
        if (droneCount < 2 && rc.getTeamSoup() > 200 + 200 * multiplier || rc.getTeamSoup() > 550 && Math.random()<0.2) {
            for (Direction dir : directions) {
                if(tryBuild(RobotType.DELIVERY_DRONE, dir)) {
                    droneCount += 1;
                };
            }
        }
    }

}