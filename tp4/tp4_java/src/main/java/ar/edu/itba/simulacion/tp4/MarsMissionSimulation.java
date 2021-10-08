package ar.edu.itba.simulacion.tp4;

import java.util.List;

import ar.edu.itba.simulacion.tp4.Ej2.MarsMissionConfig;
import ar.edu.itba.simulacion.tp4.MolecularDynamicSolver.MoleculeStateAxis;

public class MarsMissionSimulation {
     
    private final static double     massMultiplier = 1E+30;
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

        earth.setSolver(new BeemanSolver(2, dt, earth.getMass() * massMultiplier, new GravitationalForce(List.of(sun, mars), earth.getMass(), gravitationalConstant), new MoleculeStateAxis[]{new MoleculeStateAxis(earth.getX(),earth.getVelocityX()), new MoleculeStateAxis(earth.getY(), earth.getVelocityY())}));        
        mars.setSolver(new BeemanSolver(2, dt, mars.getMass() * massMultiplier, new GravitationalForce(List.of(sun, earth), mars.getMass(), gravitationalConstant), new MoleculeStateAxis[]{new MoleculeStateAxis(mars.getX(),mars.getVelocityX()), new MoleculeStateAxis(mars.getY(), mars.getVelocityY())}));        
        spaceShip.setSolver(new BeemanSolver(2, dt, spaceShip.getMass() * massMultiplier, new GravitationalForce(List.of(sun, earth, mars), spaceShip.getMass(), gravitationalConstant), new MoleculeStateAxis[]{new MoleculeStateAxis(spaceShip.getX(), spaceShip.getVelocityX()), new MoleculeStateAxis(spaceShip.getY(), spaceShip.getVelocityY())}));        
    }
    
    public void simulate(int iterations) {

        System.out.println("Spaceship position: (" + spaceShip.getX() + ", "+ spaceShip.getY() +") Earth position: (" + earth.getX() + ", "+ earth.getY() +")");
        for (int i = 0; i < iterations; i++) {
            updateCelestialBody(spaceShip);
            updateCelestialBody(earth);
            updateCelestialBody(mars);
        }
        System.out.println("Spaceship position: (" + spaceShip.getX() + ", "+ spaceShip.getY() +") Earth position: (" + earth.getX() + ", "+ earth.getY() +")");

    }

    public void updateCelestialBody(CelestialBody celestialBody) {
        MoleculeStateAxis[] results = celestialBody.getSolver().nextStep();
        celestialBody.setX(results[0].getPosition());
        celestialBody.setVelocityX(results[0].getVelocity());
        celestialBody.setY(results[1].getPosition());
        celestialBody.setVelocityY(results[1].getVelocity());
    }
}
