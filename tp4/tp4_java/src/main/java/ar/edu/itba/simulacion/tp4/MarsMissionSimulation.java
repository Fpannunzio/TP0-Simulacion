package ar.edu.itba.simulacion.tp4;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.function.ObjIntConsumer;

import ar.edu.itba.simulacion.particle.marshalling.XYZWritable;
import ar.edu.itba.simulacion.tp4.Ej2.MarsMissionConfig;
import ar.edu.itba.simulacion.tp4.MolecularDynamicSolver.MoleculeStateAxis;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

public class MarsMissionSimulation {
     
    private final static int        gearDimention = 2;
    private final static int        gearDegree = 5;

    private final double            gravitationalConstant;
    private final CelestialBody     sun;
    private final CelestialBody     earth;
    private final CelestialBody     mars;
    private final CelestialBody     spaceShip;

    public MarsMissionSimulation(MarsMissionConfig marsMissionConfig) {

        gravitationalConstant = marsMissionConfig.gravitationalConstant;
        sun = marsMissionConfig.sun.toCelestialBody();
        earth = marsMissionConfig.earth.toCelestialBody();
        mars = marsMissionConfig.mars.toCelestialBody();
        spaceShip = buildSpaceShip(marsMissionConfig);
        addCelestialBodiesSolver(marsMissionConfig.dt);
    }

    private CelestialBody buildSpaceShip(MarsMissionConfig marsMissionConfig) {

        // (1 + d/E)
        double positionFactor = 1 + (earth.getRadius() + marsMissionConfig.spaceStationDistance)/(Math.hypot(earth.getX(), earth.getY()));
        double spaceShipOrbitalVelocity = marsMissionConfig.spaceshipInitialVelocity + marsMissionConfig.spaceStationOrbitalVelocity;

        return CelestialBody.builder()
        .withX(earth.getX() * positionFactor)
        .withY(earth.getY() * positionFactor)
        .withVelocityX(Math.signum(earth.getVelocityX())*spaceShipOrbitalVelocity * (earth.getX()/Math.hypot(earth.getX(), earth.getY())) + earth.getVelocityX())
        .withVelocityY(Math.signum(earth.getVelocityY())*spaceShipOrbitalVelocity * (earth.getY()/Math.hypot(earth.getX(), earth.getY())) + earth.getVelocityY())
        .withMass(marsMissionConfig.spaceshipMass)
        .withRadius(0)
        .build();
    }

    private void addCelestialBodiesSolver(double dt) {

        earth.setSolver(gearSolverBuilder(dt, earth, List.of(sun, mars)));        
        mars.setSolver(gearSolverBuilder(dt, mars, List.of(sun, earth)));        
        spaceShip.setSolver(gearSolverBuilder(dt, spaceShip, List.of(sun, earth, mars)));        
    }

    private GearSolver gearSolverBuilder(double dt, CelestialBody celestialBody, List<CelestialBody> affectingCelestialBoddies) {
        
        return GearSolver.builder()
       .withDimensions(gearDimention)
       .withDegree(gearDegree)
       .withDt(dt)
       .withMass(celestialBody.getScaledMass())
       .withForce(new GravitationalForce(affectingCelestialBoddies, celestialBody.getScaledMass(), gravitationalConstant))
       .withForceAxisMaxR(1)
       .withInitialState(getGearInitialState(celestialBody))
       .build();
    }
    
    private double[][] getGearInitialState(CelestialBody celestialBody) {
        final double[][] initialState = new double[gearDimention][gearDegree + 1];
        
        initialState[0][0] = celestialBody.getX();
        initialState[1][0] = celestialBody.getY();
        initialState[0][1] = celestialBody.getVelocityX();
        initialState[1][1] = celestialBody.getVelocityY();

        return initialState;
        
    }

    public void simulate(int iterations, final ObjIntConsumer<MissionControlState> callback) {
        final MissionControlState missionControlState = new MissionControlState(List.of(spaceShip, earth, mars, sun));
        for (int i = 0; i < iterations; i++) {
            updateCelestialBody(spaceShip);
            updateCelestialBody(earth);
            updateCelestialBody(mars);
            callback.accept(missionControlState, i);
        }

    }

    public void updateCelestialBody(CelestialBody celestialBody) {
        MoleculeStateAxis[] results = celestialBody.getSolver().nextStep();
        celestialBody.setX(results[0].getPosition());
        celestialBody.setVelocityX(results[0].getVelocity());
        celestialBody.setY(results[1].getPosition());
        celestialBody.setVelocityY(results[1].getVelocity());
    }
  
    @Data
    @Jacksonized
    @Builder(setterPrefix = "with")
    public static class MissionControlState implements XYZWritable {
        private List<CelestialBody> celestialBodies;

        @Override
        public void xyzWrite(Writer writer) throws IOException {
            writer.write(String.valueOf(celestialBodies.size()));
            XYZWritable.newLine(writer);
            XYZWritable.newLine(writer);
            
            for(final CelestialBody celestialBody : celestialBodies) {
                celestialBody.xyzWrite(writer);
            }
        }
    }  
}   

