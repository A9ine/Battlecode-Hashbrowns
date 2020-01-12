package potato;
import battlecode.common.*;

public strictfp class HQ extends Building {

    HQ(RobotController rc) {
        super(rc);
    }

    //Builds two miners and sends communication identifier
    private void turnOne() throws GameActionException {
        for (Direction dir : directions) {
            tryBuild(RobotType.MINER, dir);
        }
        
        //Gut feeling tells me sprint tournament lots of people will be sending cost 10 transactions
        int random = (int)(Math.random() * (Integer.MAX_VALUE-KEY*3));
        trySendBlockchain(new int[] {random,0,0,0,0,0,0},1);
        trySendBlockchain(new int[] {random + KEY,0,0,0,0,0,0},1);
        trySendBlockchain(new int[] {random + KEY + KEY ,0,0,0,0,0,0},1);

    }

    //This is the only robot that will always know what turn it is
    @Override
    public void run() throws GameActionException {
        turnCount += 1;

        if (turnCount == 1) {
            turnOne();
        }

        if (turnCount < 500) {
            for (Direction dir : directions) {
                tryBuild(RobotType.MINER, dir);
            }
        } 

    }

}