package ar.edu.itba.simulacion.tp2;

import ar.edu.itba.simulacion.particle.Particle2D;
import ar.edu.itba.simulacion.particle.neighbours.CellIndexMethod;
import ar.edu.itba.simulacion.tp2.endCondition.OffLatticeEndCondition;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.DoubleUnaryOperator;

import static ar.edu.itba.simulacion.particle.neighbours.CellIndexMethod.*;

public class OffLatticeAutomata {

    private final double            spaceWidth;
    private final double            eta;
    private final boolean           periodicBorder;
    private final CellIndexMethod   cim;

    public OffLatticeAutomata(final double spaceWidth, final double actionRadius, final double eta, final boolean periodicBorder, final double maxRadius) {
        this.spaceWidth     = spaceWidth;
        this.periodicBorder = periodicBorder;
        this.eta            = eta;
        this.cim            = new CellIndexMethod(
            optimalM(spaceWidth, actionRadius, maxRadius), spaceWidth, actionRadius, periodicBorder
        );
    }

    public List<Particle2D> step(final List<Particle2D> state) {
        final List<Particle2D> newState = new LinkedList<>();

        final Map<Integer, Set<Particle2D>> neighboursMap = cim.calculateNeighbours(state);

        for(final Particle2D particle : state) {
            final Set<Particle2D> neighbours = neighboursMap.get(particle.getId());

            // Consideramos que la particula tambien es su neighbour
            neighbours.add(particle);

            // Calculamos la nueva posicion de la particula y la agregamos al estado del automata
            newState.add(particleNextState(particle, neighbours));
        }

        return newState;
    }

    public List<List<Particle2D>> run(final List<Particle2D> initialState, final OffLatticeEndCondition endCondition) {
        final List<List<Particle2D>> states = new LinkedList<>();

        List<Particle2D> last = initialState;
        states.add(last);
        endCondition.processNewState(last);

        while(!endCondition.hasEnded()) {
            last = step(last);
            states.add(last);
            endCondition.processNewState(last);
        }

        return states;
    }

    private static double velocityDirAverage(final Collection<Particle2D> particles, final DoubleUnaryOperator projection) {
        return particles.stream()
            .mapToDouble(Particle2D::getVelocityDir)
            .map(projection)
            .sorted()       // El average da mejor si ordenamos los valores
            .average()
            .orElseThrow()  // Nunca deberia estar vacio, pues siempre esta la particle
            ;
    }

    // Se asume que la particula esta dentro de los vecinos (uno es su propio vecino)
    private Particle2D particleNextState(final Particle2D particle, final Set<Particle2D> neighbours) {
        final ThreadLocalRandom rand = ThreadLocalRandom.current();
        return particle.doStep(
            particle.getVelocityMod(),
            Math.atan2(
                velocityDirAverage(neighbours, Math::sin),
                velocityDirAverage(neighbours, Math::cos)
            ) + rand.nextDouble(-eta/2, eta/2),
            spaceWidth,
            periodicBorder
        );
    }
}