package ar.edu.itba.simulacion.tp4.marsMission;

import static ar.edu.itba.simulacion.tp4.marsMission.SimulationSettings.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import ar.edu.itba.simulacion.particle.marshalling.XYZWritable;
import ar.edu.itba.simulacion.tp4.marsMission.simulation.MarsMissionSimulation;
import ar.edu.itba.simulacion.tp4.marsMission.simulation.MarsMissionSimulation.SpaceshipInitParams;
import ar.edu.itba.simulacion.tp4.marsMission.SimulationSettings.MarsMissionConfig;

public final class XYZAnimation {
    private XYZAnimation() {
        // static
    }

    public static final int MAX_ITERATIONS      = MAX_COLLISION_TOLERANCE;
    public static final int OUTPUT_SAMPLE_RATE  = 100;
    public static final int INITIAL_ITERATIONS  = COLLISION_ITERATION;

    public static void main(String[] args) throws IOException {
        if(args.length < 1) {
            throw new IllegalArgumentException("First argument must be config path");
        }

        final ObjectMapper mapper = new ObjectMapper();

        final MarsMissionConfig config = mapper.readValue(new File(args[0]), MarsMissionConfig.class);

        final SpaceshipInitParams spaceshipParams = config.spaceship.withReturnTrip(RETURN_TRIP);

        final MarsMissionSimulation baseSimulation = config.toPlanetSimulation();

        try(final BufferedWriter writer = new BufferedWriter(new FileWriter(config.outputFile))) {
            
            XYZWritable.xyzWrite(
                writer,
                List.of(baseSimulation.getEarth(), baseSimulation.getEarth(), baseSimulation.getMars(), baseSimulation.getSun())
            );

            baseSimulation.simulate((i, spaceship, earth, mars, sun) -> {    
                if(i % OUTPUT_SAMPLE_RATE == 0) {
                    XYZWritable.xyzWrite(writer, List.of(earth, earth, mars, sun));
                }
                if(i % (MAX_ITERATIONS / 10) == 0) {
                    // Informamos que la simulacion avanza
                    System.out.println("Total states processed so far: " + i);
                }
                
                return  i < INITIAL_ITERATIONS;
            });

            final MarsMissionSimulation simulation = baseSimulation.buildNewMission(spaceshipParams);
            // Estado inicial
            XYZWritable.xyzWrite(
                writer,
                List.of(simulation.getSpaceship(), simulation.getEarth(), simulation.getMars(), simulation.getSun())
            );

            final int[] iterationPtr = new int[]{0};
            final double[] minDist = new double[]{Double.POSITIVE_INFINITY};
            final int[] totalIterPtr = new int[] {0};

            simulation.simulate((i, spaceship, earth, mars, sun) -> {
                // Imprimimos estado
                if(i % OUTPUT_SAMPLE_RATE == 0) {
                    XYZWritable.xyzWrite(writer, List.of(spaceship, earth, mars, sun));
                }
                if(i % (MAX_ITERATIONS / 10) == 0) {
                    // Informamos que la simulacion avanza
                    System.out.println("Total states processed so far: " + i);
                }
                final double dist = spaceship.distanceTo(RETURN_TRIP ? earth : mars);
                if(minDist[0] > dist) {
                    if(dist <= 0) {
                        System.out.println("Choque! " + i);
                    }
                    minDist[0] = dist;
                    iterationPtr[0] = i;
                }

                totalIterPtr[0]++;
                return i < MAX_ITERATIONS;
            });

            System.out.println("Min dist " + minDist[0] + " - Iteration " + iterationPtr[0] + " - Total Iterations: " + totalIterPtr[0]);
        }
    }
}
