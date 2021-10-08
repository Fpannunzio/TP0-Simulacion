package ar.edu.itba.simulacion.tp4;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Jacksonized
@Builder(setterPrefix = "with")
public class CelestialBody {

    private double x;
    private double y;
    private double velocityX;
    private double velocityY;
    private final double mass;
    private final double radius;
    private MolecularDynamicSolver solver;

    public double calculateDistance(double otherX, double otherY) {
        return Math.hypot(x - otherX, y - otherY);
    }
    
}
