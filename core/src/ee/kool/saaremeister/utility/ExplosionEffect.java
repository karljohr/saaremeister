package ee.kool.saaremeister.utility;

import lombok.Data;

@Data
public class ExplosionEffect {
    private float x;
    private float y;
    private float timeAlive;

    public ExplosionEffect(float x, float y) {
        this.x = x;
        this.y = y;
    }
}
