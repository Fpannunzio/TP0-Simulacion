package ar.edu.itba.simulacion.tp2.endCondition;

import ar.edu.itba.simulacion.particle.Particle2D;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;

@JsonTypeInfo(property = OffLatticeEndCondition.FIELD_TYPE, use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
    @JsonSubTypes.Type(name = StepEndCondition.TYPE, value = StepEndCondition .class),
})
public interface OffLatticeEndCondition {
    String FIELD_TYPE = "type";

    boolean hasEnded(final List<Particle2D> state, final long step);

}
