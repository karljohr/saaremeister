package ee.kool.saaremeister.server.game.object.player;

import com.badlogic.gdx.physics.box2d.World;
import com.esotericsoftware.kryonet.Connection;
import ee.kool.saaremeister.server.game.BuildHandler;
import ee.kool.saaremeister.server.game.GameInstance;
import message.dto.PlayerRole;

import static constant.Constants.OFFICER_LIVES;
import static constant.Constants.OFFICER_MONEY;

/**
 * Initializes a new server/side representation of a tax officer.
 * The tax officer is part of a team of tax officers, and their goal is to capture the island from the Island Master.
 * Tax officers have fewer lives and money than the island master and are not allowed to build defensive structures.
 * They win if either they have captured all capture zones or they possess the majority of them when the game timer
 * runs out.
 */
public class Officer extends Player {

    public Officer(
            Connection connection,
            GameInstance gameInstance,
            World world,
            BuildHandler buildHandler
    ) {
        super(connection, connection.getID(), gameInstance, world, buildHandler, OFFICER_MONEY, OFFICER_LIVES, PlayerRole.OFFICER);
    }
}
