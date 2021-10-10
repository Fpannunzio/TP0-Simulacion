package ar.edu.itba.simulacion.tp4.marsMission;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import ar.edu.itba.simulacion.particle.marshalling.XYZWritable;
import ar.edu.itba.simulacion.tp4.marsMission.MarsMissionSimulation.SpaceshipInitParams;
import ar.edu.itba.simulacion.tp4.marsMission.SimulationSettings.MarsMissionConfig;

public final class XYZAnimation {
    private XYZAnimation() {
        // static
    }

    public static final int INITIAL_ITERATIONS = 360 * 144 + 215 - 1; //intervalo de inicio + cantidad hasta la mejor iteracion - 1
    public static final int MAX_ITERATIONS = 1_000_000;
    public static final int OUTPUT_SAMPLE_RATE = MAX_ITERATIONS / 10_000;

    public static void main(String[] args) throws IOException {
        if(args.length < 1) {
            throw new IllegalArgumentException("First argument must be config path");
        }

        final ObjectMapper mapper = new ObjectMapper();

        final MarsMissionConfig config = mapper.readValue(new File(args[0]), MarsMissionConfig.class);

        final SpaceshipInitParams spaceshipParams = config.spaceship;

        final MarsMissionSimulation baseSimulation = config.toSimulation();

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

            String aux;

            aux = simulation.simulate((i, spaceship, earth, mars, sun) -> {
                // Imprimimos estado
                if(i % OUTPUT_SAMPLE_RATE == 0) {
                    XYZWritable.xyzWrite(writer, List.of(spaceship, earth, mars, sun));
                }
                if(i % (MAX_ITERATIONS / 10) == 0) {
                    // Informamos que la simulacion avanza
                    System.out.println("Total states processed so far: " + i);
                }
                return i <= MAX_ITERATIONS;
            });
            
            if (aux != null){
                System.out.println(aux);
            }
        }
    }
}
