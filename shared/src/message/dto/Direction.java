package message.dto;

public enum Direction {
    UP(Axis.Y),
    DOWN(Axis.Y),
    LEFT(Axis.X),
    RIGHT(Axis.X);

    public final Axis axis;

    Direction(Axis axis) {
        this.axis = axis;
    }
}

enum Axis {
    X, Y
}
