package ar.edu.itba.simulacion.tp4;

import ar.edu.itba.simulacion.tp4.Ej2.MarsMissionConfig;

public class MarsMissionSimulation {
    
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

        double positionFactor = (earth.getRadius() + marsMissionConfig.spaceStationDistance)/(Math.hypot(earth.getX(), earth.getY()));
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
        sun.setSolver(new GearSolver(sun.getMass(), dt,));
        
    }

    private double getShipXPosition(double spaceStationDistance) {
        return 1 + 
         

    }
    

}
