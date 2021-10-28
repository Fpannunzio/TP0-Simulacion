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

public class XYZAnimation {
    private XYZAnimation() {
        // static
    }

    public static final int MAX_ITERATIONS      = 1_000_000;
    public static final int OUTPUT_SAMPLE_RATE  = 100;

    public static void main(final String[] args) throws IOException {
        if(args.length < 1) {
            throw new IllegalArgumentException("First argument must be config path");
        }

        final ObjectMapper mapper = new ObjectMapper();

        final PedestrianDynamicsConfig config = mapper.readValue(new File(args[0]), PedestrianDynamicsConfig.class);

        // TODO(tobi): La generacion de particulas tiene que recibir la seed
        final List<Particle2D> initialState = config.generateInitialState();
        if(initialState.isEmpty()) {
            throw new IllegalArgumentException("Initial state was not generated");
        }

        final PedestrianDynamicsSimulation simulation = config.toSimulation();

        System.out.println("Seed: " + simulation.getSeed());

        try(final BufferedWriter writer = new BufferedWriter(new FileWriter(config.outputFile))) {
            simulation.simulate(initialState, (i, state) -> {
                if(i % OUTPUT_SAMPLE_RATE == 0) {
                    XYZWritable.xyzWrite(writer, state);
                }

                return i < MAX_ITERATIONS;
            });
        }
    }
}
