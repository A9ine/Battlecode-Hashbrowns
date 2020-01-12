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
        
        trySendBlockchain(new int[] {(int)(Math.random() * (Integer.MAX_VALUE-KEY)),0,0,0,0,0,0},10);
        trySendBlockchain(new int[] {(int)(Math.random() * (Integer.MAX_VALUE-KEY)),0,0,0,0,0,0},10);
        trySendBlockchain(new int[] {(int)(Math.random() * (Integer.MAX_VALUE-KEY)),0,0,0,0,0,0},10);

    }

    //This is the only robot that will always know what turn it is
    @Override
    public void run() {
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