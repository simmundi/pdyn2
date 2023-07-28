package net.snowyhollows.epi.spatial.model;


import pl.edu.icm.trurl.ecs.annotation.WithMapper;

@WithMapper(namespace = "position")
public class Position {
    private float x;

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public static Position of(float x) {
        Position p = new Position();
        p.setX(x);
        return p;
    }

    public float distance(Position other) {
        return Math.abs(x - other.x);
    }
}
