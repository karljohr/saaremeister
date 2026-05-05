package ee.kool.saaremeister.screen.overlay.Components;

import com.badlogic.gdx.scenes.scene2d.Actor;
import message.GameStateMessage;
import message.dto.PlayerState;

public interface UIComponent<T> {
    Actor getActor();
    void update(T msg);
}
