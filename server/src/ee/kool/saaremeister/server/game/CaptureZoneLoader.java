package ee.kool.saaremeister.server.game;

import ee.kool.saaremeister.server.game.object.CaptureZone;

import java.util.ArrayList;
import java.util.List;

/**
 * Loads the capture zones.
 */
public class CaptureZoneLoader {

    public List<CaptureZone> loadCaptureZones() {
        List<CaptureZone> zones = new ArrayList<>();

        // Hardcoded rectangular capture zones.
        zones.add(new CaptureZone(1, 0f, 0f, 500f, 500f));
        zones.add(new CaptureZone(2, 500f, 0f, 500f, 500f));
        zones.add(new CaptureZone(3, 0f, 500f, 500f, 500f));
        zones.add(new CaptureZone(4, 500f, 500f, 500f, 500f));

        return zones;
    }

    // TODO: Load zones from TMX file.
}
