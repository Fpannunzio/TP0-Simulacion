package ar.edu.itba.simulacion.tp4.marsMission;

import static ar.edu.itba.simulacion.tp4.MolecularDynamicSolver.*;

import java.io.IOException;
import java.io.Writer;

import ar.edu.itba.simulacion.particle.marshalling.XYZWritable;
import ar.edu.itba.simulacion.tp4.MolecularDynamicSolver;
import lombok.Builder;
import lombok.Data;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

@Data
public class CelestialBody implements XYZWritable {

    private         String                  name;
    private         double                  x;
    private         double                  y;
    private         double                  velocityX;
    private         double                  velocityY;
    private final   double                  mass;
    private final   double                  radius;
    @With
    private MolecularDynamicSolver          solver;

    @Jacksonized
    @Builder(setterPrefix = "with")
    public CelestialBody(
        final String    name,
        final double    x,          final double    y,
        final double    velocityX,  final double    velocityY,
        final double    mass,       final double    radius,
        final MolecularDynamicSolver                solver) {

        this.name       = name;
        this.x          = x;
        this.y          = y;
        this.velocityX  = velocityX;
        this.velocityY  = velocityY;
        this.mass       = mass;
        this.radius     = radius;
        this.solver     = solver;
    }

    public CelestialBody(final CelestialBody other) {
        this(other.name, other.x, other.y, other.velocityX, other.velocityY, other.mass, other.radius, other.solver);
    }

    public void update() {
        final MoleculeStateAxis[] newState = solver.nextStep();

        x           = newState[0].getPosition();
        velocityX   = newState[0].getVelocity();
        y           = newState[1].getPosition();
        velocityY   = newState[1].getVelocity();
    }

    public double distanceFrom0() {
        return Math.hypot(x, y);
    }

    public double distanceTo(final double otherX, final double otherY) {
        return Math.hypot(x - otherX, y - otherY);
    }

    public double distanceTo(final CelestialBody body) {
        return distanceTo(body.x, body.y);
    }

    public boolean hasCollided(final CelestialBody body) {
        return distanceTo(body) <= radius + body.radius;
    }

    public double getVelocityModule() {
        return Math.hypot(velocityX, velocityY);
    }

    @Override
    public void xyzWrite(final Writer writer) throws IOException {
        writer.write(
            getX()          + FIELD_SEPARATOR +
            getY()          + FIELD_SEPARATOR +
            getVelocityX()  + FIELD_SEPARATOR +
            getVelocityY()  + FIELD_SEPARATOR +
            getRadius()     + FIELD_SEPARATOR
        );
        XYZWritable.newLine(writer);
    }
}
