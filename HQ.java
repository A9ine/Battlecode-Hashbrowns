package potato;
import battlecode.common.*;

public strictfp class HQ extends Building {

    HQ(RobotController rc) throws GameActionException {
        super(rc);
    }

    //Builds two miners and sends communication identifier
    private void turnOne() throws GameActionException {
        System.out.println("HQ Init");
        
        for (Direction dir : directions) {
            tryBuild(RobotType.MINER, dir);
        }
        
        //Gut feeling tells me sprint tournament lots of people will be sending cost 0 transactions
        int random = (int)(Math.random() * (Integer.MAX_VALUE-KEY*3));

        trySendBlockchain(new int[]{0,rc.getLocation().x * 100 + rc.getLocation().y,0,0,0,0,KEY}, 2);

        for (MapLocation loc : getLineOfSense()) {
            if (rc.senseSoup(loc) > 0) {
                System.out.printf("I found SOUP at %d %d \n",loc.x,loc.y);
                trySendBlockchain(new int[]{1,loc.x*100+loc.y,0,rc.senseSoup(loc),0,0,KEY},1);
            }
        }
    }

    @Override
    public void run() throws GameActionException {

        if (rc.getRoundNum() == 1) {
            turnOne();
        }

        /*if (rc.getRoundNum() < 500) {
            for (Direction dir : directions) {
                tryBuild(RobotType.MINER, dir);
            }
        } */

    }

}