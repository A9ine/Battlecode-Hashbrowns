package potato;
import battlecode.common.*;

public strictfp abstract class Unit extends Robot {

    static Direction randomDirection() {
        return directions[(int) (Math.random() * directions.length)];
    }

}