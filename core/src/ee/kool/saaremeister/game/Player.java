package ee.kool.saaremeister.game;

import lombok.Setter;

public class Player {

    private final int id;
    @Setter
    private float x;
    @Setter
    private float y;

    public Player(int id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    // public void render()

}
