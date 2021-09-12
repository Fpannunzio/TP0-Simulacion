package ar.edu.itba.simulacion.tp3;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import ar.edu.itba.simulacion.particle.marshalling.XYZWritable;
import com.fasterxml.jackson.databind.ObjectMapper;

import ar.edu.itba.simulacion.particle.Particle2D;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

public class BrownianMotionSimulation {
    public static void main( String[] args ) throws IOException {
        if(args.length < 1) {
            throw new IllegalArgumentException("First argument must be config path");
        }
        final ObjectMapper mapper = new ObjectMapper();

        final BrownianMotionSimulationConfig config = mapper.readValue(new File(args[0]), BrownianMotionSimulationConfig.class);

        final List<Particle2D> initialState = List.of(mapper.readValue(new File(config.particlesFile), Particle2D[].class));
        if(initialState.isEmpty()) {
            throw new IllegalArgumentException("No small particles for initial state found");
        }

        final BrownianParticleSystem brownianSystem = new BrownianParticleSystem(config.spaceWidth, initialState);

        for(int i = 0; i < config.iterations; i++) {
            final BrownianParticleSystem.SimulationState state = brownianSystem.calculateNextCollision();
            final BrownianParticleSystem.Collision collision = state.getCollision();
            final List<Particle2D> particles = state.getParticles();

            if(i % 1000 == 0) {
                // Informamos que la simulacion avanza
                System.out.println("Total states processed so far: " + i);
            }
        }

        XYZWritable.exportToFile(config.outputFile, brownianSystem.getStates());
    }

    @Data
    @Jacksonized
    @Builder(setterPrefix = "with")
    public static class BrownianMotionSimulationConfig {
        public double                   spaceWidth;
        public int                      iterations;
        public String                   particlesFile;
        public String                   outputFile;
    }
}
