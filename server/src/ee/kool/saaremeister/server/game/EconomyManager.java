package ee.kool.saaremeister.server.game;

import ee.kool.saaremeister.server.game.object.CaptureZone;
import ee.kool.saaremeister.server.game.object.player.Master;
import ee.kool.saaremeister.server.game.object.player.Player;

import java.util.List;

import static constant.Constants.GAME_TICK_RATE;

public class EconomyManager {

    private float zoneProfitTimer;

    private static final float ZONE_PROFIT_INTERVAL = 6f;

    public EconomyManager() {

    }

    public void distributeZoneProfits(List<CaptureZone> captureZones, List<Player> players) {

        float deltaTime = 1f / GAME_TICK_RATE;
        zoneProfitTimer += deltaTime;

        if (zoneProfitTimer < ZONE_PROFIT_INTERVAL) return;

        long masterZones = captureZones.stream()
                .filter(z -> z.getOwnerEnum() == CaptureZone.ZoneOwner.MASTER)
                .count();

        long officerZones = captureZones.size() - masterZones;

        players.forEach(player -> {
            if (player instanceof Master) player.passiveIncome((int) masterZones);
            else player.passiveIncome((int) officerZones);
        });

        zoneProfitTimer -= ZONE_PROFIT_INTERVAL;
    }
}
