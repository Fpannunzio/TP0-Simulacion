package ar.edu.itba.simulacion.tp2;

import ar.edu.itba.simulacion.particle.Particle2D;
import ar.edu.itba.simulacion.particle.neighbours.CellIndexMethod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.DoubleUnaryOperator;

import static ar.edu.itba.simulacion.particle.neighbours.CellIndexMethod.*;

public class OffLatticeAutomata {

    private final double            spaceWidth;
    private final boolean           periodicBorder;
    private final CellIndexMethod   cim;

    public OffLatticeAutomata(final double spaceWidth, final double actionRadius, final boolean periodicBorder, final double maxRadius) {
        this.spaceWidth     = spaceWidth;
        this.periodicBorder = periodicBorder;
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

    // Tobi: Dejo este metodo para testear, pero para mi no queremos correrlo una cantidad fija de veces,
    // sino usando alguna heuristica para determinar el corte
    public List<List<Particle2D>> doNSteps(final List<Particle2D> state, final int steps) {
        final List<List<Particle2D>> states = new ArrayList<>(steps);
        List<Particle2D> last = state;

        states.add(last);
        for(int i = 0; i < steps; i++) {
            last = step(last);
            states.add(last);
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
        return particle.doStep(
            particle.getVelocityMod(),
            Math.atan2(
                velocityDirAverage(neighbours, Math::sin),
                velocityDirAverage(neighbours, Math::cos)
            ),
            spaceWidth,
            periodicBorder
        );
    }
}