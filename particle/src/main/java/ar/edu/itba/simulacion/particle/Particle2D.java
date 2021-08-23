package ar.edu.itba.simulacion.particle;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;

@Value
@Jacksonized
@Builder(setterPrefix = "with")
public class Particle2D {

    int     id;
    double  x;
    double  y;
    double  velocityMod;
    double  velocityDir;  // Radianes entre [0, 2*PI)
    double  radius;

    public static Particle2D randomParticle(
        final int id, final double spaceWidth, final double minVelocity,
        final double maxVelocity, final double minRadius, final double maxRadius) {

        final ThreadLocalRandom rand = ThreadLocalRandom.current();
        return Particle2D.builder()
            .withId(id)
            .withX(rand.nextDouble(minRadius, spaceWidth))
            .withY(rand.nextDouble(minRadius, spaceWidth))
            .withVelocityMod(minVelocity < maxVelocity ? rand.nextDouble(minVelocity, maxVelocity) : minVelocity)
            .withVelocityDir(rand.nextDouble(0, 2 * Math.PI))
            .withRadius(minRadius < maxRadius ? rand.nextDouble(minRadius, maxRadius) : minRadius)
            .build()
            ;
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

    public double distanceTo(final Particle2D other, final double spaceWidth, final boolean periodicBorder) {
        final double dx = axisDistance(x, other.x, spaceWidth, periodicBorder);
        final double dy = axisDistance(y, other.y, spaceWidth, periodicBorder);

        return Math.sqrt(dx*dx + dy*dy) - radius - other.radius;
    }

    public boolean collides(final Particle2D particle, final double spaceWidth, final boolean periodicBorder) {
        return distanceTo(particle, spaceWidth, periodicBorder) <= 0;
    }

    public boolean collides(final Collection<Particle2D> particles, final double spaceWidth, final boolean periodicBorder) {
        return particles.stream().anyMatch(p -> collides(p, spaceWidth, periodicBorder));
    }

    private static double normalizeAxis(final double axis, final double spaceWidth, final boolean periodicBorder) {
        double ret = axis;

        if(periodicBorder) {
            if(axis >= spaceWidth) {
                ret = axis - spaceWidth;
            } else if(axis < 0) {
                ret = axis + spaceWidth;
            }
        }
        return ret;
    }

    @JsonIgnore
    public double getVelocityX() {
        return Math.cos(velocityDir) * velocityMod;
    }

    @JsonIgnore
    public double getVelocityY() {
        return Math.sin(velocityDir) * velocityMod;
    }

    public double getNextX(final double spaceWidth, final boolean periodicBorder) {
        return normalizeAxis(x + getVelocityX(), spaceWidth, periodicBorder);
    }

    public double getNextY(final double spaceWidth, final boolean periodicBorder) {
        return normalizeAxis(y + getVelocityY(), spaceWidth, periodicBorder);
    }

    public Particle2D doStep(
        final double newVelocityMod, final double newVelocityDir, final double spaceWidth, final boolean periodicBorder
    ) {
        return Particle2D.builder()
            .withId(id)
            .withX(getNextX(spaceWidth, periodicBorder))
            .withY(getNextY(spaceWidth, periodicBorder))
            .withVelocityMod(newVelocityMod)
            .withVelocityDir(newVelocityDir < 0 ? newVelocityDir + Math.PI : newVelocityDir)
            .withRadius(radius)
            .build()
            ;
    }
}