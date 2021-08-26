package ar.edu.itba.simulacion.tp2.endCondition;

import ar.edu.itba.simulacion.particle.Particle2D;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

public class StepEndCondition implements OffLatticeEndCondition {
    public static final String TYPE = "step";

    @Getter private final long endStep;

    // Mutable state
    private long currentState;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public StepEndCondition(@JsonProperty("id") final long endStep) {
        this.endStep = endStep;
        currentState = 0;
    }

    @Override
    public boolean hasEnded() {
        return currentState >= endStep;
    }

    @Override
    public void processNewState(final List<Particle2D> state) {
        currentState++;
    }

    @Override
    public void reset() {
        currentState = 0;
    }
}
