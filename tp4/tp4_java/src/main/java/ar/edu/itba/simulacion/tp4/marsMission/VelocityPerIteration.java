package ar.edu.itba.simulacion.tp4.marsMission;

import static ar.edu.itba.simulacion.tp4.marsMission.SimulationSettings.*;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import ar.edu.itba.simulacion.tp4.marsMission.simulation.MarsMissionSimulation;
import lombok.Value;

public final class VelocityPerIteration {
    private VelocityPerIteration() {
        // static
    }

    public static final int ITERATION_STEP = 10;

    public static void main(String[] args) throws IOException {
        if(args.length < 1) {
            throw new IllegalArgumentException("First argument must be config path");
        }

        final ObjectMapper mapper = new ObjectMapper();

        final MarsMissionConfig config = mapper.readValue(new File(args[0]), MarsMissionConfig.class);

        final MarsMissionSimulation planetSimulation = config.toPlanetSimulation();

        planetSimulation.simulate((i, spaceship, earth, mars, sun) -> i < COLLISION_ITERATION);

        final List<Double> velocities = new LinkedList<>();
        final MarsMissionSimulation simulation = planetSimulation.buildNewMission(config.spaceship.withReturnTrip(RETURN_TRIP));

        final int[] totalIterationsPtr = new int[]{0};
        final String collision = simulation.simulate((i, spaceship, earth, mars, sun) -> {
            if(i % ITERATION_STEP == 0) {
                velocities.add(spaceship.getVelocityModule());
            }
            totalIterationsPtr[0]++;
            return  i < MAX_COLLISION_TOLERANCE;
        });
        if(collision != null) {
            System.out.println("Velocity before collision: " + simulation.getSpaceship().getVelocityModule());
            System.out.println("Mars velocity: " + simulation.getMars().getVelocityModule());
            System.out.println("Relative speed to Mars: " + simulation.getSpaceship().getRelativeVelocityModule(simulation.getMars()));
            System.out.println("Total travel: " + totalIterationsPtr[0] * config.dt + " seconds");
        } else {
            System.out.println("No collision :(");
        }

        final String outputFile = "output/velocities_by_time" + (RETURN_TRIP ? "_return_trip" : "") + ".json";
        mapper.writeValue(new File(outputFile), new VelocityPerIterationInfo(ITERATION_STEP * config.dt, velocities, RETURN_TRIP));
    }

    @Value
    public static class VelocityPerIterationInfo {
        int             secondsStep;
        List<Double>    velocities;
        boolean         returnTrip;
    }
}
