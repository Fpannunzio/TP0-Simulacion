package ar.edu.itba.simulacion.tp2.endCondition;

import ar.edu.itba.simulacion.particle.Particle2D;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static ar.edu.itba.simulacion.tp2.OffLatticeAutomata.calculateStableNormalizedVelocity;

@Getter
public class StableVAEndCondition implements OffLatticeEndCondition {
    public static final String TYPE = "stableVa";

    private final double  targetSTD;
    private final int     window;

    // Mutable state
    private final Queue<Double> calculatedVAs;
    private       double        mean;
    private       int           iterationCount;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public StableVAEndCondition(
        @JsonProperty("targetSTD") final double targetSTD,
        @JsonProperty("window") final int window
    ) {
        if(targetSTD <= 0 || window <= 0) {
            throw new IllegalArgumentException("targetSTD and window must be positive");
        }
        this.targetSTD              = targetSTD;
        this.window                 = window;
        this.calculatedVAs          = new LinkedList<>();
        this.mean                   = 0;
        this.iterationCount         = 0;
    }

    @Override
    public boolean hasEnded() {
        if(calculatedVAs.size() < window) {
            return false;
        }

        final double intermediateStd = calculatedVAs.stream().mapToDouble(va -> Math.pow(va - mean, 2)).sum();
        final double std = Math.sqrt(intermediateStd / window);
        return std <= targetSTD / Math.exp(1 - iterationCount / 1000.0);
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
        
        iterationCount++;
        mean += newValue / window;
    }

    @Override
    public int validRangeStart() throws IllegalStateException {
        OffLatticeEndCondition.super.validRangeStart();
        return iterationCount - window;
    }

    @Override
    public void reset() {
        this.calculatedVAs.clear();
        this.mean           = 0;
        this.iterationCount = 0;
    }
}
