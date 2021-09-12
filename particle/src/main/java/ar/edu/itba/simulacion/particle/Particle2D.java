package ar.edu.itba.simulacion.particle;

import ar.edu.itba.simulacion.particle.marshalling.XYZWritable;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;

@Value
@Jacksonized
@Builder(setterPrefix = "with")
public class Particle2D implements XYZWritable {

    int     id;
    double  x;
    double  y;
    // Cartesian velocity
    double  velocityX;
    double  velocityY;
    // Polar velocity
    double  velocityMod;
    double  velocityDir;  // Radianes entre [-PI, PI)
    double  mass;
    double  radius;

    public static Particle2D randomParticle(
        final int id,
        final double minX,          final double maxX,
        final double minY,          final double maxY,
        final double minVelocity,   final double maxVelocity,
        final double minMass,       final double maxMass,
        final double minRadius,     final double maxRadius
    ) {
        final ThreadLocalRandom rand = ThreadLocalRandom.current();
        return Particle2D.builder()
            .withId         (id)
            .withX          (minX < maxX ? rand.nextDouble(minX, maxX) : minX)
            .withY          (minY < maxY ? rand.nextDouble(minY, maxY) : minY)
            .withVelocityMod(minVelocity < maxVelocity ? rand.nextDouble(minVelocity, maxVelocity) : minVelocity)
            .withVelocityDir(rand.nextDouble(-Math.PI, Math.PI))
            .withMass       (minMass < maxMass ? rand.nextDouble(minMass, maxMass) : minMass)
            .withRadius     (minRadius < maxRadius ? rand.nextDouble(minRadius, maxRadius) : minRadius)
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

        return Math.hypot(dx, dy) - radius - other.radius;
    }

    public double distanceTo(final Particle2D other) {
        return Math.hypot(x - other.x, y - other.y) - radius - other.radius;
    }

    public boolean collides(final Particle2D particle, final double spaceWidth, final boolean periodicBorder) {
        return distanceTo(particle, spaceWidth, periodicBorder) <= 0;
    }

    public boolean collides(final Particle2D particle) {
        return distanceTo(particle) <= 0;
    }

    public boolean collides(final Collection<Particle2D> particles, final double spaceWidth, final boolean periodicBorder) {
        return particles.stream().anyMatch(p -> collides(p, spaceWidth, periodicBorder));
    }

    public boolean collides(final Collection<Particle2D> particles) {
        return particles.stream().anyMatch(this::collides);
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

    public double getNextX(final double time, final double spaceWidth, final boolean periodicBorder) {
        return normalizeAxis(getNextX(time), spaceWidth, periodicBorder);
    }

    public double getNextY(final double time, final double spaceWidth, final boolean periodicBorder) {
        return normalizeAxis(getNextY(time), spaceWidth, periodicBorder);
    }

    public double getNextX(final double time) {
        return x + time * velocityX;
    }

    public double getNextY(final double time) {
        return y + time * velocityY;
    }

    public Particle2D move(final double time, final double spaceWidth, final boolean periodicBorder) {
        return Particle2D.builder()
            .withId         (id)
            .withX          (getNextX(time, spaceWidth, periodicBorder))
            .withY          (getNextY(time, spaceWidth, periodicBorder))
            .withVelocityMod(velocityMod)
            .withVelocityDir(velocityDir)
            .withMass       (mass)
            .withRadius     (radius)
            .build()
            ;
    }

    public Particle2D move(final double time) {
        return Particle2D.builder()
            .withId         (id)
            .withX          (getNextX(time))
            .withY          (getNextY(time))
            .withVelocityMod(velocityMod)
            .withVelocityDir(velocityDir)
            .withMass       (mass)
            .withRadius     (radius)
            .build()
            ;
    }

    public Particle2D movePolar(
        final double time, final double newVelocityMod, final double newVelocityDir, final double spaceWidth, final boolean periodicBorder
    ) {
        return Particle2D.builder()
            .withId         (id)
            .withX          (getNextX(time, spaceWidth, periodicBorder))
            .withY          (getNextY(time, spaceWidth, periodicBorder))
            .withVelocityMod(newVelocityMod)
            .withVelocityDir(newVelocityDir)
            .withMass       (mass)
            .withRadius     (radius)
            .build()
            ;
    }

    public Particle2D movePolar(final double time, final double newVelocityMod, final double newVelocityDir) {
        return Particle2D.builder()
            .withId         (id)
            .withX          (getNextX(time))
            .withY          (getNextY(time))
            .withVelocityMod(newVelocityMod)
            .withVelocityDir(newVelocityDir)
            .withMass       (mass)
            .withRadius     (radius)
            .build()
            ;
    }

    public Particle2D moveCartesian(
        final double time, final double velocityX, final double velocityY, final double spaceWidth, final boolean periodicBorder
    ) {
        return Particle2D.builder()
            .withId         (id)
            .withX          (getNextX(time, spaceWidth, periodicBorder))
            .withY          (getNextY(time, spaceWidth, periodicBorder))
            .withVelocityX  (velocityX)
            .withVelocityY  (velocityY)
            .withMass       (mass)
            .withRadius     (radius)
            .build()
            ;
    }

    public Particle2D moveCartesian(final double time, final double velocityX, final double velocityY) {
        return Particle2D.builder()
            .withId         (id)
            .withX          (getNextX(time))
            .withY          (getNextY(time))
            .withVelocityX  (velocityX)
            .withVelocityY  (velocityY)
            .withMass       (mass)
            .withRadius     (radius)
            .build()
            ;
    }

    @Override
    public void xyzWrite(Writer writer) throws IOException {
        writer.write(
            getX()          + FIELD_SEPARATOR +
                getY()          + FIELD_SEPARATOR +
                getVelocityX()  + FIELD_SEPARATOR +
                getVelocityY()  + FIELD_SEPARATOR +
                getMass()       + FIELD_SEPARATOR +
                getRadius()     + FIELD_SEPARATOR
        );
        XYZWritable.newLine(writer);
    }

    public static class Particle2DBuilder {
        private Double  velocityX;
        private Double  velocityY;
        private Double  velocityMod;
        private Double  velocityDir;  // Radianes entre [-PI, PI)

        public Particle2DBuilder withVelocityX(final double velocityX) {
            this.velocityX = velocityX;
            calculatePolarVelocity();
            return this;
        }

        public Particle2DBuilder withVelocityY(final double velocityY) {
            this.velocityY = velocityY;
            calculatePolarVelocity();
            return this;
        }

        public Particle2DBuilder withVelocityMod(final double velocityMod) {
            this.velocityMod = velocityMod;
            calculateCartesianVelocity();
            return this;
        }

        public Particle2DBuilder withVelocityDir(final double velocityDir) {
            this.velocityDir = velocityDir;
            calculateCartesianVelocity();
            return this;
        }

        private void calculatePolarVelocity() {
            if(velocityX != null && velocityY != null) {
                velocityMod = Math.hypot(velocityX, velocityY);
                velocityDir = Math.atan2(velocityY, velocityX);
            }
        }

        private void calculateCartesianVelocity() {
            if(velocityMod != null && velocityDir != null) {
                velocityX = Math.cos(velocityDir) * velocityMod;
                velocityY = Math.sin(velocityDir) * velocityMod;
            }
        }
    }
}