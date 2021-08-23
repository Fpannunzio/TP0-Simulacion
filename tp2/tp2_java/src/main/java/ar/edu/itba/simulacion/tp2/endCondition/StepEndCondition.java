package ar.edu.itba.simulacion.tp2.endCondition;

import ar.edu.itba.simulacion.particle.Particle2D;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Value
@Jacksonized
@Builder(setterPrefix = "with")
public class StepEndCondition implements OffLatticeEndCondition {
    public static final String TYPE = "step";

    long endStep;

    @Override
    public boolean hasEnded(final List<Particle2D> state, final long step) {
        return step >= endStep;
    }
}
