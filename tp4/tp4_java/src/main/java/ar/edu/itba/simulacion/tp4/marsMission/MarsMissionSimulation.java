package ar.edu.itba.simulacion.tp4.marsMission;

import java.util.List;

import ar.edu.itba.simulacion.tp4.MolecularDynamicSolver;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;


public class MarsMissionSimulation {

    public static final int SYSTEM_DIMENSION = 2;

    private final double            dt;
    private final double            gravitationalConstant;
    private final CelestialBody     sun;
    private final CelestialBody     earth;
    private final CelestialBody     mars;
    private final CelestialBody     spaceship;
    private final SolverSupplier    solverSupplier;

    @Builder(setterPrefix = "with")
    public MarsMissionSimulation(
        final double                dt,
        final double                gravitationalConstant,
        final CelestialBody         sun,
        final CelestialBody         earth,
        final CelestialBody         mars,
        final SpaceshipInitParams   spaceship,
        final SolverSupplier        solverSupplier) {

        this.dt                     = dt;
        this.gravitationalConstant  = gravitationalConstant;
        this.sun                    = sun;
        this.earth                  = earth;
        this.mars                   = mars;
        this.spaceship              = buildSpaceShip(spaceship);
        this.solverSupplier         = solverSupplier;

        if(this.earth.getSolver() == null) {
            this.earth.setSolver(buildCelestialBodySolver(this.earth, bodiesAffectingEarth()));
        }
        if(this.mars.getSolver() == null) {
            this.mars.setSolver(buildCelestialBodySolver(this.mars, bodiesAffectingMars()));
        }
        if(this.spaceship != null) {
            this.spaceship.setSolver(buildCelestialBodySolver(this.spaceship, bodiesAffectingSpaceship()));
        }
    }

    private List<CelestialBody> bodiesAffectingEarth() {
        return List.of(this.sun, this.mars);
    }
    private List<CelestialBody> bodiesAffectingMars() {
        return List.of(this.sun, this.earth);
    }
    private List<CelestialBody> bodiesAffectingSpaceship() {
        return List.of(this.sun, this.earth, this.mars);
    }

    private CelestialBody buildSpaceShip(final SpaceshipInitParams spaceshipParams) {
        if(spaceshipParams == null) {
            return null;
        }

        final double earthX     = earth.getX();
        final double earthY     = earth.getY();
        final double earthVx    = earth.getVelocityX();
        final double earthVy    = earth.getVelocityY();

        final double earthDistance = Math.hypot(earthX, earthY);
        final double positionFactor = 1 + (earth.getRadius() + spaceshipParams.spaceStationDistance) / earthDistance; // (1 + d/E)
        final double orbitalVelocity = spaceshipParams.spaceshipInitialVelocity + spaceshipParams.spaceStationOrbitalVelocity;

        return CelestialBody.builder()
            .withName       ("spaceship")
            .withX          (earthX * positionFactor)
            .withY          (earthY * positionFactor)
            .withVelocityX  (Math.signum(earthVx) * orbitalVelocity * (earthY / earthDistance) + earthVx)
            .withVelocityY  (Math.signum(earthVy) * orbitalVelocity * (earthX / earthDistance) + earthVy)
            .withMass       (spaceshipParams.scaledMass())
            .withRadius     (0)
            .build()
            ;
    }

    private MolecularDynamicSolver buildCelestialBodySolver(final CelestialBody celestialBody, final List<CelestialBody> bodiesAffectedBy) {

        return solverSupplier.get(
            dt,
            celestialBody.getMass(),
            new GravitationalForce(gravitationalConstant, celestialBody.getName(), celestialBody.getMass(), bodiesAffectedBy),
            celestialBody.getX(),
            celestialBody.getY(),
            celestialBody.getVelocityX(),
            celestialBody.getVelocityY()
        );
    }

    public String simulate(final SimulationStateNotifier notifier) {
        String ret = null;

        int iteration = 0;
        do {
            if(spaceship != null) {
                spaceship.update();
            }
            earth       .update();
            mars        .update();
            // Al sol no lo updeteamos, consideramos que esta estatico en (0, 0)

            iteration++;
        } while(notifier.notify(iteration, spaceship, earth, mars, sun) && (ret = hasSpaceshipCollided()) == null);

        return ret;
    }

    public double getDt() {
        return dt;
    }

    public double getGravitationalConstant() {
        return gravitationalConstant;
    }

    public CelestialBody getSun() {
        return sun;
    }

    public CelestialBody getEarth() {
        return earth;
    }

    public CelestialBody getMars() {
        return mars;
    }

    public CelestialBody getSpaceship() {
        return spaceship;
    }

    public String hasSpaceshipCollided() {
        if(spaceship == null) {
            return null;
        }
        if(spaceship.hasCollided(earth)) {
            return earth.getName();
        }
        if(spaceship.hasCollided(sun)) {
            return sun.getName();
        }
        if(spaceship.hasCollided(mars)) {
            return mars.getName();
        }
        return null;
    }
    // Hay que pasar la energia de kg*km^2/s^2 kg*m^2/s^2 
    public double getSystemEnergy() {
        double totalEnergy = 0;
        totalEnergy += getCelestialBodyEnergy(earth);
        totalEnergy += getCelestialBodyEnergy(mars);
        totalEnergy += getCelestialBodyEnergy(spaceship);
        return totalEnergy * 1_000_000;
    }

    private double getCelestialBodyEnergy(CelestialBody body){
        return ((GravitationalForce)body.getSolver().getForce()).getPotentialEnergy(body.getX(), body.getY()) + 0.5 * body.getMass() * body.getVelocityModule() * body.getVelocityModule();    
    }

    public MarsMissionSimulation buildNewMission(final SpaceshipInitParams spaceshipParams) {
        return MarsMissionSimulation.builder()
            .withDt                     (dt)
            .withGravitationalConstant  (gravitationalConstant)
            .withSun                    (new CelestialBody(sun))
            .withMars                   (new CelestialBody(mars).withSolver(
                buildCelestialBodySolver(mars, bodiesAffectingMars()).copyState(mars.getSolver())
            ))
            .withEarth                  (new CelestialBody(earth).withSolver(
                buildCelestialBodySolver(earth, bodiesAffectingEarth()).copyState(earth.getSolver())
            ))
            .withSpaceship              (spaceshipParams)
            .withSolverSupplier         (solverSupplier)
            .build()
            ;
    }

    /* ----------------------------------------- Clases Auxiliares ----------------------------------------------- */

    @FunctionalInterface
    public interface SimulationStateNotifier {
        boolean notify(
            final int           iteration,
            final CelestialBody spaceship,
            final CelestialBody earth,
            final CelestialBody mars,
            final CelestialBody sun);
    }

    @FunctionalInterface
    public interface SolverSupplier {
        MolecularDynamicSolver get(
            final double    dt, final double    mass, final GravitationalForce  force,
            final double    x,  final double    y,
            final double    vx, final double    vy);
    }

    @Value
    @Jacksonized
    @Builder(setterPrefix = "with")
    public static class SpaceshipInitParams {
        public int      spaceshipMass;
        public int      spaceshipMassScale;
        public int      spaceshipInitialVelocity;
        public double   spaceStationDistance;
        public double   spaceStationOrbitalVelocity;

        public double scaledMass() {
            return spaceshipMass * Math.pow(10, spaceshipMassScale);
        }
    }
}   
