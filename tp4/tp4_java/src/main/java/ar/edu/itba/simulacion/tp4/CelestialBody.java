package ar.edu.itba.simulacion.tp4;

import java.io.IOException;
import java.io.Writer;

import ar.edu.itba.simulacion.particle.marshalling.XYZWritable;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Jacksonized
@Builder(setterPrefix = "with")
public class CelestialBody implements XYZWritable {

    private double                  x;
    private double                  y;
    private double                  velocityX;
    private double                  velocityY;
    private final double            mass;
    private final double            radius;
    private MolecularDynamicSolver  solver;

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
            getMass()       + FIELD_SEPARATOR +
            getRadius()     + FIELD_SEPARATOR
        );
        XYZWritable.newLine(writer);
    }
}
