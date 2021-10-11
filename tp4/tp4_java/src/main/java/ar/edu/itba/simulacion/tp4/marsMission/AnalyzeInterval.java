package ar.edu.itba.simulacion.tp4.marsMission;

import static ar.edu.itba.simulacion.tp4.marsMission.SimulationSettings.*;
import static java.util.concurrent.TimeUnit.*;

import java.io.File;
import java.io.IOException;
import java.time.ZoneOffset;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import ar.edu.itba.simulacion.tp4.marsMission.simulation.MarsMissionSimulation;
import ar.edu.itba.simulacion.tp4.marsMission.simulation.MarsMissionSimulation.SpaceshipInitParams;
import ar.edu.itba.simulacion.tp4.marsMission.SimulationSettings.MarsMissionConfig;
import lombok.Value;

public final class AnalyzeInterval {
    private AnalyzeInterval() {
        // static
    }

    public static final boolean     ACCURATE = true;

    public static final double      MAX_MARS_ORBITS = 0.001;

    public static final int         ACCURATE_START = ACCURATE ? 86_400 : 0;

    public static final long        ANALYZER_RESOLUTION_SECONDS = ACCURATE ? MINUTES.toSeconds(5) : HOURS.toSeconds(6);

    public static final long        INTERVAL_SECONDS = ACCURATE ? DAYS.toSeconds(5) : DAYS.toSeconds(300);

    public static final double      DISTANCE_TOLERANCE = MARS_ORBIT / 1_00;

    public static final int         MAX_UNCHANGED_MIN_DIST = 100_000;

    public static final int         MAX_ITERATIONS_RETURN_TRIP = 30_000;

    public static void main(String[] args) throws IOException {
        if(args.length < 1) {
            throw new IllegalArgumentException("First argument must be config path");
        }

        final ObjectMapper mapper = new ObjectMapper();

        final MarsMissionConfig config = mapper.readValue(new File(args[0]), MarsMissionConfig.class);

        final double dt = config.dt;
        final SpaceshipInitParams spaceshipParams = config.spaceship.withReturnTrip(RETURN_TRIP);

        final int maxIntervalIteration  = (int) (INTERVAL_SECONDS / dt) + ACCURATE_START;
        final int maxIterations         = (int) (MAX_MARS_ORBITS * MARS_ORBIT_SECONDS / dt) + 1;
        final int resolution            = (int) (ANALYZER_RESOLUTION_SECONDS / dt);
        final int totalSimulations      = maxIntervalIteration / resolution;

        final List<IterationMarsDistance> bestDistances = new LinkedList<>();
        IterationMarsDistance bestMarsDistance = null;

        final MarsMissionSimulation baseSimulation = config.toPlanetSimulation();

        if(ACCURATE_START > 0) {
            baseSimulation.simulate((i, spaceship, earth, mars, sun) -> i < ACCURATE_START);
        }

        int currentIter = ACCURATE_START;
        while(currentIter <= maxIntervalIteration) {
            final double[] bestDistancePtr = new double[]{Double.POSITIVE_INFINITY};

            final MarsMissionSimulation simulation = baseSimulation.buildNewMission(spaceshipParams);

            final int[] unchangedDistIters = new int[] {0};
            final int[] lastIter           = new int[] {0};

            simulation.simulate((i, spaceship, earth, mars, sun) -> {
                lastIter[0]++;

                double distance = spaceship.distanceTo(RETURN_TRIP ? earth : mars);
                distance = distance <= 0 ? 0 : distance;
                if(distance < bestDistancePtr[0]) {
                    bestDistancePtr[0] = distance;
                    unchangedDistIters[0] = 0;
                } else {
                    unchangedDistIters[0]++;
                }

                return (RETURN_TRIP && i < MAX_ITERATIONS_RETURN_TRIP)
                    || (
                            !RETURN_TRIP
                        &&  i < maxIterations
                        &&  unchangedDistIters[0] < MAX_UNCHANGED_MIN_DIST
                        &&  spaceship.distanceFrom0() <= MARS_ORBIT + mars.getRadius() + DISTANCE_TOLERANCE
                        )
                    ;
            });

            final IterationMarsDistance marsDistance = new IterationMarsDistance(currentIter, Math.round(currentIter * dt), bestDistancePtr[0]);
            bestDistances.add(marsDistance);
            if(marsDistance.compareTo(bestMarsDistance) < 0) {
                bestMarsDistance = marsDistance;
            }
            System.out.println(
                "Iteracion " + currentIter / resolution + " de " + totalSimulations +
                " \t- Inicio en iteracion " + currentIter + " de " + maxIntervalIteration +
                " \t- Mejor distancia: " + bestDistancePtr[0] +
                " \t- Cortamos en la iteracion " + lastIter[0]
            );

            // Movemos la base simulation
            baseSimulation.simulate((i, spaceship, earth, mars, sun) -> i < resolution);
            currentIter += resolution;
        }

        final String outputFile = "output/analyze"
            + (ACCURATE ? "_accurate" : "")
            + "_interval"
            + (RETURN_TRIP ? "_return_trip" : "")
            + ".json"
            ;
        mapper.writeValue(new File(outputFile), new IntervalAnalysis(bestDistances, bestMarsDistance));
    }

    @Value
    public static class IterationMarsDistance implements Comparable<IterationMarsDistance> {
        int     startIteration;
        long    startTimeEpoch;
        double  distance;

        public IterationMarsDistance(final int startIteration, final long relativeStartTimeSeconds, final double distance) {
            this.startIteration = startIteration;
            this.startTimeEpoch = INITIAL_DATE_TIME.plusSeconds(relativeStartTimeSeconds).toEpochSecond(ZoneOffset.UTC);
            this.distance = distance;
        }

        @Override
        public int compareTo(final IterationMarsDistance o) {
            return o == null ? -1 : Double.compare(distance, o.distance);
        }
    }

    @Value
    public static class IntervalAnalysis {
        List<IterationMarsDistance> distances;
        IterationMarsDistance       bestDistance;
    }
}
