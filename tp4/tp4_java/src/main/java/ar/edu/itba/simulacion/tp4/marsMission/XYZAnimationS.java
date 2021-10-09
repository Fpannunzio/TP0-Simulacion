package ar.edu.itba.simulacion.tp4.marsMission;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import ar.edu.itba.simulacion.particle.marshalling.XYZWritable;
import ar.edu.itba.simulacion.tp4.marsMission.SimulationSettings.CelestialBodyData;
import ar.edu.itba.simulacion.tp4.marsMission.SimulationSettings.MarsMissionConfig;
import ar.edu.itba.simulacion.tp4.marsMission.SimulationSettings.SolverStrategy;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

public final class XYZAnimationS {
    private XYZAnimationS() {
        // static
    }

    public static final int MAX_ITERATIONS = 1_000_000_000; // 30 años
    public static final int LAUNCH_COUNT = 30;
    public static final int LAUNCH_ITERATIONS = MAX_ITERATIONS / LAUNCH_COUNT;  // 1 año
    public static final int OUTPUT_SAMPLE_RATE = MAX_ITERATIONS / 10_000;

    public static void main(String[] args) throws IOException {
        if(args.length < 1) {
            throw new IllegalArgumentException("First argument must be config path");
        }

        final ObjectMapper mapper = new ObjectMapper();

        final XYZAnimationSCon config = mapper.readValue(new File(args[0]), XYZAnimationSCon.class);
        
        MarsMissionSimulation simulation = config.toSimulation();
        String aux;

        for (int launchCoun = 0; launchCoun < LAUNCH_COUNT; launchCoun++) {
        
            try(final BufferedWriter writer = new BufferedWriter(new FileWriter(config.outputFile.replace(".exyz", launchCoun + ".exyz")))) {

                    aux = simulation.simulate((i, spaceship, earth, mars, sun) -> {
                        // Imprimimos estado
                        if(i % OUTPUT_SAMPLE_RATE == 0) {
                            XYZWritable.xyzWrite(writer, List.of(spaceship, earth, mars, sun));
                        }
                        if(i % (LAUNCH_ITERATIONS / 10) == 0) {
                            // Informamos que la simulacion avanza
                            System.out.println("Total states processed so far: " + i);
                        }
        
                        return i <= LAUNCH_ITERATIONS;
                    });
                    
                    System.out.println(aux);
            }

            simulation = config.toSimulation(simulation.getSun(), simulation.getMars(), simulation.getEarth());
        }
    }

    @Data
    @Jacksonized
    @Builder(setterPrefix = "with")
    public static class XYZAnimationSCon {
        public double               dt;
        public double               gravitationalConstant;
        public MarsMissionSimulation.SpaceshipInitParams spaceship;
        public CelestialBodyData    sun;
        public CelestialBodyData    earth;
        public CelestialBodyData    mars;
        public SolverStrategy       solver;
        public String               outputFile;

        public MarsMissionSimulation toSimulation() {
            return MarsMissionSimulation.builder()
                .withDt                     (dt)
                .withGravitationalConstant  (gravitationalConstant)
                .withSun                    (sun.toCelestialBody("sun"))
                .withMars                   (mars.toCelestialBody("mars"))
                .withEarth                  (earth.toCelestialBody("earth"))
                .withSpaceship              (spaceship)
                .withSolverSupplier         (solver.getSolverSupplier())
                .build()
                ;
        }

        public MarsMissionSimulation toSimulation(CelestialBody sun, CelestialBody mars, CelestialBody earth) {
            return MarsMissionSimulation.builder()
                .withDt                     (dt)
                .withGravitationalConstant  (gravitationalConstant)
                .withSun                    (sun)
                .withMars                   (mars)
                .withEarth                  (earth)
                .withSpaceship              (spaceship)
                .withSolverSupplier         (solver.getSolverSupplier())
                .build()
                ;
        }
    }
}
