package ar.edu.itba.simulacion.tp4;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

public class Ej2 {
    public static void main(String[] args) throws IOException {
         
        if(args.length < 1) {
            throw new IllegalArgumentException("First argument must be config path");
        }

        final ObjectMapper mapper = new ObjectMapper();

        final MarsMissionConfig config = mapper.readValue(new File(args[0]), MarsMissionConfig.class);

        final MarsMissionSimulation marsMissionSimulation = new MarsMissionSimulation(config);

        try(final BufferedWriter writer = new BufferedWriter(new FileWriter(config.outputFile))) {

            marsMissionSimulation.simulate(10000, (state, i) -> {
                try {
                    state.xyzWrite(writer);
                } catch(final IOException e) {
                    throw new RuntimeException(e);
                }

                if(i % 1000 == 0) {
                    // Informamos que la simulacion avanza
                    System.out.println("Total states processed so far: " + i);
                }
            });
        }
    }

    
    @Data
    @Jacksonized

    @Builder(setterPrefix = "with")
    public static class MarsMissionConfig {
        public double                                               dt;
        public double                                               gravitationalConstant;
        public int                                                  spaceshipMass;
        public int                                                  spaceshipMassScale;
        public int                                                  spaceshipInitialVelocity;
        public double                                               spaceStationDistance;
        public double                                               spaceStationOrbitalVelocity;
        public CelestialBodyData                                    sun;
        public CelestialBodyData                                    earth;
        public CelestialBodyData                                    mars;
        public String                                               outputFile;
    }
    
    //La masa esta medida en 10^30 kg por lo que hay que expandir 
    @Data
    @Jacksonized
    @Builder(setterPrefix = "with")
    public static class CelestialBodyData {
        public double                                               x;
        public double                                               y;
        public double                                               velocityX;
        public double                                               velocityY;
        public double                                               mass;
        public int                                                  massScale;
        public double                                               radius;                                                
    

        public CelestialBody toCelestialBody(){
            return CelestialBody.builder()
            .withX(x)
            .withY(y)
            .withVelocityX(velocityX)
            .withVelocityY(velocityY)
            .withMass(mass)
            .withRadius(radius)
            .build();
        }
    }
}
