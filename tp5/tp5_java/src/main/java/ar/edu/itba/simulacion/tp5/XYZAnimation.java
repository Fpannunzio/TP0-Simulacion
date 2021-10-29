package ar.edu.itba.simulacion.tp5;

import static ar.edu.itba.simulacion.tp5.SimulationSettings.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import ar.edu.itba.simulacion.particle.Particle2D;
import ar.edu.itba.simulacion.particle.marshalling.XYZWritable;

public final class XYZAnimation {
    private XYZAnimation() {
        // static
    }

    public static final int OUTPUT_SAMPLE_RATE  = 1;

    public static void main(final String[] args) throws IOException {
        if(args.length < 1) {
            throw new IllegalArgumentException("First argument must be config path");
        }

        final ObjectMapper mapper = new ObjectMapper();

        final PedestrianDynamicsConfig config = mapper.readValue(new File(args[0]), PedestrianDynamicsConfig.class);

        System.out.println("Particle Generation Seed: " + config.particleGeneration.seed);
        System.out.println("Simulation Seed: " + config.seed);

        final List<Particle2D> initialState = config.generateInitialState();
        if(initialState.isEmpty()) {
            throw new IllegalArgumentException("Initial state was not generated");
        }

        final PedestrianDynamicsSimulation simulation = config.toSimulation();

        final int maxIterations = config.iterations;

        try(final BufferedWriter writer = new BufferedWriter(new FileWriter(config.outputFile))) {
            simulation.simulate(initialState, (i, locked, escaped) -> {
                if(i % OUTPUT_SAMPLE_RATE == 0) {
                    XYZWritable.xyzWrite(writer, locked, escaped);
                }

                return i < maxIterations;
            });
        }
    }
}
