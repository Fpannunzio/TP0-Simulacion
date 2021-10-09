package ar.edu.itba.simulacion.tp4.marsMission;

import static ar.edu.itba.simulacion.tp4.MolecularDynamicSolver.*;

import java.io.IOException;
import java.io.Writer;

import ar.edu.itba.simulacion.particle.marshalling.XYZWritable;
import ar.edu.itba.simulacion.tp4.MolecularDynamicSolver;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Jacksonized
@Builder(setterPrefix = "with")
public class CelestialBody implements XYZWritable {

    private         String                  name;
    private         double                  x;
    private         double                  y;
    private         double                  velocityX;
    private         double                  velocityY;
    private final   double                  mass;
    private final   double                  radius;
    private MolecularDynamicSolver solver;

    public void update() {
        final MoleculeStateAxis[] newState = solver.nextStep();

        x           = newState[0].getPosition();
        velocityX   = newState[0].getVelocity();
        y           = newState[1].getPosition();
        velocityY   = newState[1].getVelocity();
    }

    public double distanceTo(double otherX, double otherY) {
        return Math.hypot(x - otherX, y - otherY);
    }

    @Override
    public void xyzWrite(Writer writer) throws IOException {
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
