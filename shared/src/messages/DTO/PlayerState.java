package messages.DTO;

public class PlayerState {
    public int playerId;

    public float x;
    public float y;

    public PlayerState() {}

    public PlayerState(int playerId, float x, float y) {
        this.playerId = playerId;
        this.x = x;
        this.y = y;
    }
}
