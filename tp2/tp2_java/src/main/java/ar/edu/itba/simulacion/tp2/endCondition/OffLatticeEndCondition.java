package ar.edu.itba.simulacion.tp2.endCondition;

import ar.edu.itba.simulacion.particle.Particle2D;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;

@JsonTypeInfo(property = OffLatticeEndCondition.FIELD_TYPE, use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
    @JsonSubTypes.Type(name = StepEndCondition.TYPE, value = StepEndCondition .class),
    @JsonSubTypes.Type(name = StableVAEndCondition.TYPE, value = StableVAEndCondition.class),
})
public interface OffLatticeEndCondition {
    String FIELD_TYPE = "type";

    boolean hasEnded();

    void processNewState(final List<Particle2D> state);

    /**
     * Retorna el inicio del rango de valores validos de la simulacion
     * Esto permite descartar valores iniciales de la simulacion que consideramos invalidos
     * Solo es valido llamar a este metodo una vez que {@link #hasEnded()} retorna true
     * @throws IllegalStateException si {@link #hasEnded()} retorna false
     */
    default int validRangeStart() throws IllegalStateException {
        if(!hasEnded()) {
            throw new IllegalStateException("La simulacion todavia no termino. No es posible determinar el rango valido.");
        }
        return 0;
    }

    void reset();

}
