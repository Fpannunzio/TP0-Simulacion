package ar.edu.itba.simulacion.tp0;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class Particle {

    private int id;
    private double x;
    private double y;
    private double radius;

    public static Particle randomParticle(final int id, final double L, final double minRadius, final double maxRadius) {
        final ThreadLocalRandom rand = ThreadLocalRandom.current();
        return new Particle(id, rand.nextDouble(minRadius, L), rand.nextDouble(minRadius, L), rand.nextDouble(minRadius, maxRadius));
    }

    private Particle() {
        // Serialization
    }

    public Particle(final int id, final double x, final double y, final double radius) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    public double distanceTo(final Particle other) {
        double deltaX = this.x - other.x;
        double deltaY = this.y - other.y;
        return Math.sqrt(deltaX*deltaX + deltaY*deltaY) - this.radius - other.radius;
    }

    public boolean collides(final Particle particle) {
        return distanceTo(particle) <= 0;
    }

    public boolean collides(final Collection<Particle> particles) {
        return particles.stream().anyMatch(this::collides);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Particle)) return false;
        final Particle particle = (Particle) o;
        return id == particle.id &&
            Double.compare(particle.x, x) == 0 &&
            Double.compare(particle.y, y) == 0 &&
            Double.compare(particle.radius, radius) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, x, y, radius);
    }

    @Override
    public String toString() {
        return "Particle{" +
            "id=" + id +
            ", x=" + x +
            ", y=" + y +
            ", radius=" + radius +
            '}';
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