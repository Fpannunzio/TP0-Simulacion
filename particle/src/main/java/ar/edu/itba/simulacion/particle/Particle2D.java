package ar.edu.itba.simulacion.particle;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@JsonDeserialize(builder = Particle2D.Builder.class)
public class Particle2D {

    private final int     id;
    private final double  x;
    private final double  y;
    private final double  velocityMod;
    private final double  velocityDir;
    private final double  radius;

    public static Builder builder() {
        return new Builder();
    }

    public static Particle2D randomParticle(final int id, final double spaceWidth, final double velocityMod, final double minRadius, final double maxRadius) {
        final ThreadLocalRandom rand = ThreadLocalRandom.current();
        return Particle2D.builder()
            .withId(id)
            .withX(rand.nextDouble(minRadius, spaceWidth))
            .withY(rand.nextDouble(minRadius, spaceWidth))
            .withVelocityMod(velocityMod)
            .withVelocityDir(rand.nextDouble(-Math.PI, -Math.PI))
            .withRadius(rand.nextDouble(minRadius, maxRadius))
            .build()
            ;
    }

    private Particle2D(final Builder builder) {
        this.id             = builder.id;
        this.x              = builder.x;
        this.y              = builder.y;
        this.velocityMod    = builder.velocityMod;
        this.velocityDir    = builder.velocityDir;
        this.radius         = builder.radius;
    }

    private static double axisDistance(final double ax1, final double ax2, final double spaceWidth, final boolean periodicBorder) {
        double dist = Math.abs(ax1 - ax2);
        if(periodicBorder) {
            if(dist > spaceWidth/2) {
                // Si espacio toroidal, la distancia no puede ser mayor a la mitad del largo total del espacio
                dist = spaceWidth - dist;
            }
        }
        return dist;
    }

    public double distanceTo(final Particle2D other, final double L, final boolean periodicOutline) {
        final double dx = axisDistance(x, other.x, L, periodicOutline);
        final double dy = axisDistance(y, other.y, L, periodicOutline);

        return Math.sqrt(dx*dx + dy*dy) - radius - other.radius;
    }

    public boolean collides(final Particle2D particle, final double L, final boolean periodicOutline) {
        return distanceTo(particle, L, periodicOutline) <= 0;
    }

    public boolean collides(final Collection<Particle2D> particles, final double L, final boolean periodicOutline) {
        return particles.stream().anyMatch(p -> collides(p, L, periodicOutline));
    }

    public double getXShift(int time) {
        return Math.cos(velocityDir) * velocityMod * time;
    }

    public double getYShift(int time) {
        return Math.sin(velocityDir) * velocityMod * time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Particle2D)) return false;
        final Particle2D particle = (Particle2D) o;
        return  id == particle.id                                       &&
                Double.compare(particle.x, x) == 0                      &&
                Double.compare(particle.y, y) == 0                      &&
                Double.compare(particle.velocityMod, velocityMod) == 0  &&
                Double.compare(particle.velocityDir, velocityDir) == 0  &&
                Double.compare(particle.radius, radius) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, x, y, velocityMod, velocityDir, radius);
    }

    @Override
    public String toString() {
        return "Particle2D{" +
            "id=" + id +
            ", x=" + x +
            ", y=" + y +
            ", velocityMod=" + velocityMod +
            ", velocityDir=" + velocityDir +
            ", radius=" + radius +
            '}';
    }

    //////////////////////////// Burocracia //////////////////////////////////////

    @JsonPOJOBuilder
    protected static class Builder {
        private int     id;
        private double  x;
        private double  y;
        private double  velocityMod;
        private double  velocityDir;
        private double  radius;

        public Particle2D build() {
            return new Particle2D(this);
        }

        public Builder withId(final int id) {
            this.id = id;
            return this;
        }
        public Builder withX(final double x) {
            this.x = x;
            return this;
        }
        public Builder withY(final double y) {
            this.y = y;
            return this;
        }
        public Builder withVelocityMod(final double velocityMod) {
            this.velocityMod = velocityMod;
            return this;
        }
        public Builder withVelocityDir(final double velocityDir) {
            this.velocityDir = velocityDir;
            return this;
        }
        public Builder withRadius(final double radius) {
            this.radius = radius;
            return this;
        }
    }

    public int getId() {
        return this.id;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getVelocityMod() {
        return velocityMod;
    }

    public double getVelocityDir() {
        return velocityDir;
    }
    

    public double getRadius() {
        return this.radius;
    }
}