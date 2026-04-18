package ee.kool.saaremeister.server.game;

import com.badlogic.gdx.physics.box2d.World;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.minlog.Log;
import ee.kool.saaremeister.server.game.object.Bullet;
import ee.kool.saaremeister.server.game.object.CaptureZone;
import ee.kool.saaremeister.server.game.object.Player;
import ee.kool.saaremeister.server.lobby.LobbyManager;
import message.dto.CaptureZoneState;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import static constant.Constants.GAME_TICK_RATE;

/**
 * Processes the game state and updates the game objects per tick.
 */
public class GameTickProcessor {

    private final World world;
    private final GameStateHandler gameStateHandler;
    private final BuildHandler buildHandler;
    private final BulletCollisionHandler bulletCollisionHandler;
    private final MatchRules matchRules;

    private final List<Player> players;
    private final Queue<Bullet> pendingBullets;
    private final List<Bullet> bullets;
    private final List<CaptureZone> captureZones;
    private final Set<Connection> connections;

    private final GameDisposer gameDisposer;
    private final LobbyManager lobbyManager;
    private final int lobbyId;

    public GameTickProcessor(
            World world,
            GameStateHandler gameStateHandler,
            BuildHandler buildHandler,
            BulletCollisionHandler bulletCollisionHandler,
            MatchRules matchRules,
            List<Player> players,
            Queue<Bullet> pendingBullets,
            List<Bullet> bullets,
            List<CaptureZone> captureZones,
            Set<Connection> connections,
            GameDisposer gameDisposer,
            LobbyManager lobbyManager,
            int lobbyId
    ) {
        this.world = world;
        this.gameStateHandler = gameStateHandler;
        this.buildHandler = buildHandler;
        this.bulletCollisionHandler = bulletCollisionHandler;
        this.matchRules = matchRules;
        this.players = players;
        this.pendingBullets = pendingBullets;
        this.bullets = bullets;
        this.captureZones = captureZones;
        this.connections = connections;
        this.gameDisposer = gameDisposer;
        this.lobbyManager = lobbyManager;
        this.lobbyId = lobbyId;
    }

    /**
     * Processes the game state and updates the game objects per tick.
     */
    public boolean tick() {
        float deltaTime = 1f / GAME_TICK_RATE;
        gameStateHandler.incrementGameTimeIfPlayersPresent();

        // Step Box2D simulation so bodies move.
        // Timestep based on tick rate; velocity/position iterations are standard defaults.
        world.step(deltaTime, 6, 2);

        // Buildings shoot
        buildHandler.shoot();
        buildHandler.buildFromQueue();

        // Update game objects.
        bullets.forEach(Bullet::update);
        // Log.info("bullets in game state: " + bullets.size());
        Bullet bullet;
        while ((bullet = pendingBullets.poll()) != null) {
            bullets.add(bullet);
        }
        bulletCollisionHandler.handleBulletCollisions(bullets, players, buildHandler.getBuildings());

        // Check if any players are dead and need to be given the respawn timer.
        matchRules.checkRespawn(players);

        // Check if any buildings have been broken and need to be deleted.
        matchRules.checkBrokenBuildings(buildHandler.getBuildings());

        // Update player positions.
        players.forEach(Player::updateFromPhysics);
        var gameStateMessage = gameStateHandler.getGameStateMessage(players, bullets, buildHandler.getBuildings());

        // Capture Zone updates according to the players (not) inside it.
        captureZones.forEach(zone -> zone.update(players, deltaTime));
        ArrayList<CaptureZoneState> zoneStates = captureZones.stream()
                .map(CaptureZone::toState)
                .collect(Collectors.toCollection(ArrayList::new));
        gameStateHandler.setCaptureZoneStates(zoneStates);

        // Send the game state to all clients.
        connections.forEach(connection -> connection.sendUDP(gameStateMessage));

        // If there is a match end reason, stop the game.
        MatchRules.MatchEndReason endReason = matchRules.determineEndReason(
                captureZones, connections, players, gameStateHandler.getGameTime()
        );
        if (endReason != MatchRules.MatchEndReason.NONE) {
            int winnerPlayerId = matchRules.getGameWinnerId();
            gameStateHandler.setWinnerPlayerId(winnerPlayerId);
            gameStateHandler.setGameOver(true);
            var finalGameStateMessage = gameStateHandler.getGameStateMessage(players, bullets, buildHandler.getBuildings());
            switch (endReason) {
                case NO_PLAYERS_CONNECTED -> Log.info("No players connected, stopping game.");
                case ALL_ZONES_CAPTURED -> Log.info("All zones captured by the officers, stopping game.");
                case TIME_LIMIT_REACHED_WITH_RESULT -> Log.info("Time limit reached, someone won, stopping game.");
                case TIME_LIMIT_REACHED_WITH_DRAW -> Log.info("Time limit reached, no one won, stopping game.");
            }
            connections.forEach(connection -> connection.sendTCP(finalGameStateMessage));
            gameDisposer.dispose();
            lobbyManager.disposeLobby(lobbyId);
            return false;
        }
        return true;
    }
}
