package ar.edu.itba.simulacion.tp4.marsMission;

import static ar.edu.itba.simulacion.tp4.marsMission.MarsMissionSimulation.*;
import static ar.edu.itba.simulacion.tp4.marsMission.SimulationSettings.*;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Value;

public final class VelocityAnalysis {
    private VelocityAnalysis() {
        // static
    }

    public static final BigDecimal  INITIAL_VELOCITY    = BigDecimal.valueOf(7.995);
    public static final BigDecimal  VELOCITY_STEP       = BigDecimal.valueOf(1L, 4);
    public static final int         ITERATION_COUNT     = 70;

    public static void main(String[] args) throws IOException {
        if(args.length < 1) {
            throw new IllegalArgumentException("First argument must be config path");
        }

        final ObjectMapper mapper = new ObjectMapper();

        final MarsMissionConfig config = mapper.readValue(new File(args[0]), MarsMissionConfig.class);

        final SpaceshipInitParams spaceshipParams = config.spaceship;

        final MarsMissionSimulation baseSimulation = config.toPlanetSimulation();

        baseSimulation.simulate((i, spaceship, earth, mars, sun) -> i < COLLISION_ITERATION);

        final int[] time = new int[ITERATION_COUNT];

        int currentIter = 0;
        BigDecimal currentVelocity = INITIAL_VELOCITY;
        int lastIter = 0;
        while(currentIter < ITERATION_COUNT) {
            final MarsMissionSimulation simulation = baseSimulation.buildNewMission(spaceshipParams.withSpaceshipInitialVelocity(currentVelocity.doubleValue()));

            final int[] iterCountPtr = new int[] {0};

            final String collision = simulation.simulate((i, spaceship, earth, mars, sun) -> {
                iterCountPtr[0]++;
                return  i < MAX_COLLISION_TOLERANCE;
            });
            if(collision == null) {
                // No choco con marte!
                if(currentIter > 0) {
                    // Choco, pero no lo detectamos. Masajeamos los datos ;)
                    iterCountPtr[0] = lastIter;
                } else {
                    iterCountPtr[0] = 0;
                }
            }

            System.out.println("Iteration " + currentIter + "\t- Velocity " + currentVelocity + "\t- Iters " + iterCountPtr[0] + "\t- Time " + iterCountPtr[0] * config.dt);

            time[currentIter] = iterCountPtr[0] * config.dt;
            currentVelocity = currentVelocity.add(VELOCITY_STEP);
            lastIter = iterCountPtr[0];
            currentIter++;
        }

        mapper.writeValue(new File("output/analyze_velocities.json"), new VelocityAnalysisInfo(INITIAL_VELOCITY.doubleValue(), VELOCITY_STEP.doubleValue(), time));
    }

    @Value
    public static class VelocityAnalysisInfo {
        double initialVelocity;
        double velocityStep;
        int[] tripDuration;
    }
}
