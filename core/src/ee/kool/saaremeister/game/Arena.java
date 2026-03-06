package ee.kool.saaremeister.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import message.GameStateMessage;
import message.dto.BulletState;
import message.dto.PlayerState;

import java.util.ArrayList;
import java.util.List;

/**
 * Render the current arena of the game, based on the info from the server.
 */
public class Arena {

    private final List<Player> players = new ArrayList<>();
    private List<BulletState> bullets = new ArrayList<>();

    /**
     * Update the game state in the client based on the server's messages.
     */
    public void update(GameStateMessage gameStateMessage) {
        var currentPlayerIds = gameStateMessage.getPlayerStates()
                .stream()
                .map(PlayerState::getId)
                .toList();

        players.removeIf(player -> !currentPlayerIds.contains(player.getId()));

        gameStateMessage.getPlayerStates().forEach(playerState -> {
            var player = players
                    .stream()
                    .filter(p -> p.getId().equals(playerState.getId()))
                    .findFirst()
                    .orElseGet(() -> {
                        var newPlayer = new Player(playerState.getId());
                        players.add(newPlayer);
                        return newPlayer;
                    });
            player.setX(playerState.getX());
            player.setY(playerState.getY());
        });
        this.bullets = gameStateMessage.getBulletStates();
    }

    // public void render()

    // private void renderMap()

    // private void renderPlayers()

    //private void renderBullets()
}
