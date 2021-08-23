package ar.edu.itba.simulacion.tp2.endCondition;

import ar.edu.itba.simulacion.particle.Particle2D;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class StableVAEndCondition implements OffLatticeEndCondition {
    public static final String TYPE = "stableVa";

    @Getter private final double  targetSTD;
    @Getter private final int     window;

    // Mutable state
    private final Queue<Double> calculatedVAs;
    private       double        mean;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public StableVAEndCondition(
        @JsonProperty("targetSTD") final double targetSTD,
        @JsonProperty("window") final int window
    ) {
        if(targetSTD <= 0 || window <= 0) {
            throw new IllegalArgumentException("targetSTD and window cannot be negative");
        }
        this.targetSTD              = targetSTD;
        this.window                 = window;
        this.calculatedVAs          = new LinkedList<>();
        this.mean                   = 0;
    }

    @Override
    public boolean hasEnded() {
        if(calculatedVAs.size() < window) {
            return false;
        }

        final double intermediateStd = calculatedVAs.stream().mapToDouble(va -> Math.pow(va - mean, 2)).sum();
        final double std = Math.sqrt(intermediateStd / window);

        return std <= targetSTD;
    }

    @Override
    public void processNewState(final List<Particle2D> state) {
        if(calculatedVAs.size() == window) {
            // Eliminamos el valor que quedo stale
            final double removedValue = calculatedVAs.remove();
            mean -= removedValue / window;
        }
        // Agregamos nuevo valor
        final double newValue = calculateStableNormalizedVelocity(state);
        calculatedVAs.add(newValue);
        mean += newValue / window;
    }

    private static double calculateStableNormalizedVelocity(final List<Particle2D> state) {
        final double aggregateVelocityX = state.stream().mapToDouble(Particle2D::getVelocityX).sum();
        final double aggregateVelocityY = state.stream().mapToDouble(Particle2D::getVelocityY).sum();
        final double totalVelocityMod = state.stream().mapToDouble(Particle2D::getVelocityMod).sum();

        return Math.hypot(aggregateVelocityX, aggregateVelocityY) / totalVelocityMod;
    }
}
