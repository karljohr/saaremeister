package ee.kool.saaremeister.server.game;

import com.badlogic.gdx.maps.tiled.TiledMap;
import

public class Box2dWorldGenerator {

    public void initalizeWorld(World world) {

    }

    public TiledMap getMap() {
        return new HijackedTmxLoader(new MyServer.MyFileHandleResolver())
                .load("shared/src/TMXAssets/map.tmx");
    }
}
