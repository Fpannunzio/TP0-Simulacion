package ar.edu.itba.simulacion.tp4.marsMission;

import static ar.edu.itba.simulacion.tp4.marsMission.SimulationSettings.EPSILON;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.math3.util.Precision;

import com.fasterxml.jackson.databind.ObjectMapper;

import ar.edu.itba.simulacion.particle.marshalling.XYZWritable;

public final class CyclicStateSearch {
    private CyclicStateSearch() {
        // static
    }

    public static final int MAX_ITERATIONS_SEC  = 5_500 * 24 * 60 * 60;  // 5480 dias con 20 de changui
    public static final int OUTPUT_SAMPLE_RATE  = 10 * 24 * 60 * 60;    // 10 dias

    public static void main(String[] args) throws IOException {
        if(args.length < 1) {
            throw new IllegalArgumentException("First argument must be config path");
        }

        final ObjectMapper mapper = new ObjectMapper();

        final SimulationSettings.MarsMissionConfig config = mapper.readValue(new File(args[0]), SimulationSettings.MarsMissionConfig.class);

        final MarsMissionSimulation simulation = config.toPlanetSimulation();

        final int maxIterations = (MAX_ITERATIONS_SEC / config.dt) + 1;

        final CelestialBody ogEarth = simulation.getEarth();
        final CelestialBody ogMars = simulation.getMars();

        final double ogEarthX = ogEarth.getX();
        final double ogEarthY = ogEarth.getY();
        final double ogEarthDist = ogEarth.distanceFrom0();

        final double ogMarsX = ogMars.getX();
        final double ogMarsY = ogMars.getY();
        final double ogMarsDist = simulation.getMars().distanceFrom0();

        final double ogAngle = Math.acos((ogEarthX * ogMarsX + ogEarthY * ogMarsY) / (ogEarthDist * ogMarsDist));

        final boolean[] globalAngleMatchPtr = {false};
        final int[]     angleMatchCount = {0};

        try(final BufferedWriter writer = new BufferedWriter(new FileWriter(config.outputFile))) {
            // Estado inicial
            XYZWritable.xyzWrite(
                writer,
                List.of(simulation.getEarth(), simulation.getMars(), simulation.getSun())
            );

            simulation.simulate((i, spaceship, earth, mars, sun) -> {
                final double earthX = earth.getX();
                final double earthY = earth.getY();
                final double earthDist = earth.distanceFrom0();

                final double marsX = mars.getX();
                final double marsY = mars.getY();
                final double marsDist = mars.distanceFrom0();

                final double angle = Math.acos((earthX * marsX + earthY * marsY) / (earthDist * marsDist));
                final boolean angleMatch = Precision.equalsWithRelativeTolerance(ogAngle, angle, EPSILON);

                if(angleMatch && !globalAngleMatchPtr[0]) {
                    globalAngleMatchPtr[0] = true;
                    angleMatchCount[0]++;
                    System.out.print("Angle match " + angleMatchCount[0] + " from iteration " + i);
                } else if(!angleMatch && globalAngleMatchPtr[0]) {
                    globalAngleMatchPtr[0] = false;
                    System.out.println(" to iteration " + i);
                }

                if(i % OUTPUT_SAMPLE_RATE == 0) {
                    XYZWritable.xyzWrite(writer, List.of(earth, mars, sun));
                }

                return i <= maxIterations;
            });
        }
    }
}
