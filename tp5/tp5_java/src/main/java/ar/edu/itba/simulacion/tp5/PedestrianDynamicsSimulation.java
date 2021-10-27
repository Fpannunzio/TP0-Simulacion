package ar.edu.itba.simulacion.tp5;

import java.util.List;

import ar.edu.itba.simulacion.particle.Particle2D;
import lombok.Builder;

public class PedestrianDynamicsSimulation {

    @Builder(setterPrefix = "with")
    public PedestrianDynamicsSimulation() {
        // TODO:
    }

    public void simulate(final SimulationStateNotifier notifier) {
        int iteration = 0;
        List<Particle2D> currentState = List.of(); // TODO: Calcular current state

        while(notifier.notify(iteration, currentState)) {
            // TODO: Calcular current state
            currentState = List.of();

            iteration++;
        }
    }

    /* ----------------------------------------- Clases Auxiliares ----------------------------------------------- */

    @FunctionalInterface
    public interface SimulationStateNotifier {
        boolean notify(
            final int               iteration,
            final List<Particle2D>  state);
    }
}
