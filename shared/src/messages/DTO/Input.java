package messages.DTO;

public class Input {
    public int playerId;

    public boolean up;
    public boolean down;
    public boolean left;
    public boolean right;

    public Input() {
    }

    public Input(int playerId, boolean up, boolean down, boolean left, boolean right) {
        this.playerId = playerId;
        this.up = up;
        this.down = down;
        this.left = left;
        this.right = right;
    }
}
