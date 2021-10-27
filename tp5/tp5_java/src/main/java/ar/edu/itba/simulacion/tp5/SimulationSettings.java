package ar.edu.itba.simulacion.tp5;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

public class SimulationSettings {
    private SimulationSettings() {
        // static
    }

    @Data
    @Jacksonized
    @Builder(setterPrefix = "with")
    public static class PedestrianDynamicsConfig {
        public final String outputFile;

        public PedestrianDynamicsSimulation toSimulation() {
            return PedestrianDynamicsSimulation.builder()
                .build()
                ;
        }
    }
}
