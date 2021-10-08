package ar.edu.itba.simulacion.tp4;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

public class Ej2 {
    
    public static void main(String[] args) throws IOException {
         
        if(args.length < 1) {
            throw new IllegalArgumentException("First argument must be config path");
        }

        final ObjectMapper mapper = new ObjectMapper();

        final MarsMissionConfig config = mapper.readValue(new File(args[0]), MarsMissionConfig.class);

        final MarsMissionSimulation marsMissionSimulation = new MarsMissionSimulation(config);

        marsMissionSimulation.simulate(10000);
    
    }
    
    @Data
    @Jacksonized
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder(setterPrefix = "with")
    public static class MarsMissionConfig {
        public double                                               dt;
        public double                                               gravitationalConstant;
        public int                                                  spaceshipMass;
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
    @AllArgsConstructor
    @NoArgsConstructor
    @Jacksonized
    @Builder(setterPrefix = "with")
    public static class CelestialBodyData {
        public double                                               x;
        public double                                               y;
        public double                                               velocityX;
        public double                                               velocityY;
        public double                                               mass;
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
