package ee.kool.saaremeister.server.game.object.player;

import com.badlogic.gdx.physics.box2d.World;
import com.esotericsoftware.kryonet.Connection;
import ee.kool.saaremeister.server.game.BuildHandler;
import ee.kool.saaremeister.server.game.GameInstance;
import message.dto.PlayerRole;

import static constant.Constants.*;

/**
 * Initializes a new server/side representation of the island master.
 * The island master is alone on their team, and their goal is to defend the island, aka keep all the capture zones
 * theirs until the game timer runs out. Island master has more lives and money than tax officers and is allowed
 * to build defensive structures.
 */
public class Master extends Player {

    public Master(
            Connection connection,
            GameInstance gameInstance,
            World world,
            BuildHandler buildHandler
    ) {
        super(connection, connection.getID(), gameInstance, world, buildHandler, MASTER_MONEY, MASTER_LIVES, PlayerRole.MASTER);
    }

    /**
     * Gives master money for killing the officer.
     */
    @Override
    public void earnFromKill() {
        setMoney(getMoney() + MASTER_KILL_PROFIT);
    }

    /**
     * Master shouldn't ever call this method.
     */
    @Override
    public void earnFromDestroying() {
        throw new UnsupportedOperationException(
                "Master somehow destroyed his own building "
                + "and is now trying to collect profits..."
        );
    }

    /**
     * Gives master income for every zone he holds.
     */
    @Override
    public void passiveIncome(int zoneCount) {
        setMoney(getMoney() + MASTER_ZONE_PROFIT * zoneCount);
    }
}
