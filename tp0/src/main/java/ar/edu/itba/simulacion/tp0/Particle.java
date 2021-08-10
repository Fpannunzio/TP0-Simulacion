package ar.edu.itba.simulacion.tp0;

import java.util.concurrent.ThreadLocalRandom;

public class Particle {

    private int id;
    private double x;
    private double y;
    private double radius;

    public static Particle randomParticle(final int id, final double L, final double maxRadius) {
        final ThreadLocalRandom rand = ThreadLocalRandom.current();
        return new Particle(id, rand.nextDouble(Double.MIN_VALUE, L), rand.nextDouble(Double.MIN_VALUE, L), rand.nextDouble(Double.MIN_VALUE, maxRadius));
    }

    private Particle() {
        // Serialization
    }

    public Particle(int id, double x, double y, double radius) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    public double distanceTo(Particle other) {
        double deltaX = this.x - other.x;
        double deltaY = this.y - other.y;
        return Math.sqrt(deltaX*deltaX + deltaY*deltaY) - this.radius - other.radius;
    }

    //////////////////////////// Burocracia //////////////////////////////////////

    public int getId() {
        return this.id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public double getX() {
        return this.x;
    }
    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return this.y;
    }
    public void setY(double y) {
        this.y = y;
    }

    public double getRadius() {
        return this.radius;
    }
    public void setRadius(double radius) {
        this.radius = radius;
    }
}