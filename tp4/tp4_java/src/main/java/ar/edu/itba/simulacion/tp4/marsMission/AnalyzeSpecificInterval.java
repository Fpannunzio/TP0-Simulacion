package ar.edu.itba.simulacion.tp4.marsMission;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import ar.edu.itba.simulacion.tp4.marsMission.AnalyzeInterval.IntervalAnalysis;
import ar.edu.itba.simulacion.tp4.marsMission.AnalyzeInterval.IterationMarsDistance;
import ar.edu.itba.simulacion.tp4.marsMission.MarsMissionSimulation.SpaceshipInitParams;
import ar.edu.itba.simulacion.tp4.marsMission.SimulationSettings.MarsMissionConfig;

public class AnalyzeSpecificInterval {
    private AnalyzeSpecificInterval() {
        // static
    }

    public static final double  MARS_ORBIT              = 2.2799e8;

    public static final int  INITIAL_ITERATIONS      = 360 * 144; //144 intervalos de 10 min tiene un dia
    
    public static final int  MARS_ORBIT_SECONDS         = 687 * 24 * 60 * 60;

    public static final double  MAX_MARS_ORBITS         = 0.5;

    public static final int ANALYZER_RESOLUTION_SECONDS = 10 * 60;     // 10 minutos

    public static final int INTERVAL_SECONDS            = 3 * 24 * 60 * 60;  // 3 dias

    public static final double DISTANCE_TOLERANCE       = MARS_ORBIT / 1_00;

    public static final int MAX_UNCHANGED_MIN_DIST      = 100_000;

    public static final int INITIAL_TOLERANCE           = 100_000;

    public static final LocalDateTime INITIAL_DATE_TIME = LocalDateTime.of(2021, 10, 24, 0, 0, 0).plus(INITIAL_ITERATIONS * ANALYZER_RESOLUTION_SECONDS, ChronoUnit.SECONDS);
    
    public static void main(String[] args) throws IOException {
        if(args.length < 1) {
            throw new IllegalArgumentException("First argument must be config path");
        }

        final ObjectMapper mapper = new ObjectMapper();

        final MarsMissionConfig config = mapper.readValue(new File(args[0]), MarsMissionConfig.class);

        final double dt = config.dt;
        final SpaceshipInitParams spaceshipParams = config.spaceship;

        final MarsMissionSimulation baseSimulation = config.toPlanetSimulation();

        baseSimulation.simulate((i, spaceship, earth, mars, sun) -> {    
            return  i < INITIAL_ITERATIONS;
        });

        final int maxIntervalIteration  = (int) (INTERVAL_SECONDS / dt) + 1;
        final int maxIterations         = (int) (MAX_MARS_ORBITS * MARS_ORBIT_SECONDS / dt) + 1;
        final int resolution            = (int) (ANALYZER_RESOLUTION_SECONDS / dt);
        final int totalSimulations      = maxIntervalIteration / resolution;

        final List<IterationMarsDistance> bestDistances = new LinkedList<>();
        IterationMarsDistance bestMarsDistance = null;

        int currentIter = 0;
        while(currentIter <= maxIntervalIteration) {
            final double[] bestDistancePtr = new double[]{Double.POSITIVE_INFINITY};

            final MarsMissionSimulation simulation = baseSimulation.buildNewMission(spaceshipParams);

            final int[] unchangedDistIters = new int[] {0};
            final int[] lastIter           = new int[] {0};

            simulation.simulate((i, spaceship, earth, mars, sun) -> {
                lastIter[0]++;

                double distance = spaceship.distanceTo(mars) - mars.getRadius();
                distance = distance <= 0 ? 0 : distance;
                if(distance < bestDistancePtr[0]) {
                    bestDistancePtr[0] = distance;
                    unchangedDistIters[0] = 0;
                } else {
                    // if(i > INITIAL_TOLERANCE) {
                    //     return false;
                    // }
                    unchangedDistIters[0]++;
                }

                return  i <= maxIterations
                    &&  unchangedDistIters[0] < MAX_UNCHANGED_MIN_DIST
                    &&  spaceship.distanceFrom0() <= MARS_ORBIT + mars.getRadius() + DISTANCE_TOLERANCE
                    ;
            });

            final IterationMarsDistance marsDistance = new IterationMarsDistance(currentIter, INITIAL_DATE_TIME.plus(Math.round(currentIter * dt), ChronoUnit.SECONDS).toEpochSecond(ZoneOffset.UTC),bestDistancePtr[0]);
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
            baseSimulation.simulate((i, spaceship, earth, mars, sun) -> i <= resolution);
            currentIter += resolution;
        }

        mapper.writeValue(new File("output/analyze_specific_interval.json"), new IntervalAnalysis(bestDistances, bestMarsDistance));
    }
}
