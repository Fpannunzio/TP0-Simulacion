package ar.edu.itba.simulacion.tp2.endCondition;

import ar.edu.itba.simulacion.particle.Particle2D;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class StepEndCondition implements OffLatticeEndCondition {
    public static final String TYPE = "step";

    private final int endStep;
    private final int validRangeStart;

    // Mutable state
    private long currentState;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public StepEndCondition(
        @JsonProperty("id")                 final int endStep,
        @JsonProperty("validRangeStart")    final int validRangeStart
    ) {
        this.endStep            = endStep;
        this.validRangeStart    = validRangeStart;
        this.currentState       = 0;
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
    public int validRangeStart() throws IllegalStateException {
        OffLatticeEndCondition.super.validRangeStart();
        return validRangeStart;
    }

    @Override
    public void reset() {
        currentState = 0;
    }
}
