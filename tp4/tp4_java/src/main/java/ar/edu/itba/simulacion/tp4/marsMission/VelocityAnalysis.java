package ar.edu.itba.simulacion.tp4.marsMission;

import static ar.edu.itba.simulacion.tp4.marsMission.simulation.MarsMissionSimulation.*;
import static ar.edu.itba.simulacion.tp4.marsMission.SimulationSettings.*;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

import com.fasterxml.jackson.databind.ObjectMapper;

import ar.edu.itba.simulacion.tp4.marsMission.simulation.MarsMissionSimulation;
import lombok.Value;

public final class VelocityAnalysis {
    private VelocityAnalysis() {
        // static
    }

    public static final BigDecimal  INITIAL_VELOCITY    = BigDecimal.valueOf(RETURN_TRIP ? 7.9885 : 7.995);
    public static final BigDecimal  VELOCITY_STEP       = BigDecimal.valueOf(1L, 4);
    public static final int         ITERATION_COUNT     = RETURN_TRIP ? 140 : 70;

    public static void main(String[] args) throws IOException {
        if(args.length < 1) {
            throw new IllegalArgumentException("First argument must be config path");
        }

        final ObjectMapper mapper = new ObjectMapper();

        final MarsMissionConfig config = mapper.readValue(new File(args[0]), MarsMissionConfig.class);

        final SpaceshipInitParams spaceshipParams = config.spaceship.withReturnTrip(RETURN_TRIP);

        final MarsMissionSimulation baseSimulation = config.toPlanetSimulation();

        baseSimulation.simulate((i, spaceship, earth, mars, sun) -> i < COLLISION_ITERATION);

        final int[] time = new int[ITERATION_COUNT];

        // masajeo
        int startedZero = -1;
        boolean firstZeroBatchEnded = false;

        int currentIter = 0;
        BigDecimal currentVelocity = INITIAL_VELOCITY;

        while(currentIter < ITERATION_COUNT) {
            final MarsMissionSimulation simulation = baseSimulation.buildNewMission(spaceshipParams.withSpaceshipInitialVelocity(currentVelocity.doubleValue()));

            final int[] iterCountPtr = new int[] {0};

            final String collision = simulation.simulate((i, spaceship, earth, mars, sun) -> {
                iterCountPtr[0]++;
                return  i < MAX_COLLISION_TOLERANCE;
            });
            if(collision == null) {
                iterCountPtr[0] = 0;

                // No choco con marte!
                if(firstZeroBatchEnded && startedZero < 0) {
                    // Choco, pero no lo detectamos. Lo registramos para masajear los datos ;)
                    startedZero = currentIter;
                }
            } else {
                firstZeroBatchEnded = true;
                if(startedZero > 0) {
                    // Masajeamos los datos ;)
                    for(int j = startedZero; j < currentIter; j++) {
                        time[j] = time[startedZero - 1];
                    }
                    startedZero = -1;
                }
            }

            System.out.println("Iteration " + currentIter + "\t- Velocity " + currentVelocity + "\t- Iters " + iterCountPtr[0] + "\t- Time " + iterCountPtr[0] * config.dt);

            time[currentIter] = iterCountPtr[0] * config.dt;
            currentVelocity = currentVelocity.add(VELOCITY_STEP);
            currentIter++;
        }

        final String outputFile = "output/analyze_velocities" + (RETURN_TRIP ? "_return_trip" : "") + ".json";
        mapper.writeValue(new File(outputFile), new VelocityAnalysisInfo(INITIAL_VELOCITY.doubleValue(), VELOCITY_STEP.doubleValue(), time, RETURN_TRIP));
    }

    @Value
    public static class VelocityAnalysisInfo {
        double initialVelocity;
        double velocityStep;
        int[] tripDuration;
        boolean returnTrip;
    }
}
