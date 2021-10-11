package ar.edu.itba.simulacion.tp4.marsMission.simulation;

import java.util.List;

import ar.edu.itba.simulacion.tp4.MolecularDynamicSolver;
import lombok.Builder;
import lombok.Value;
import lombok.With;
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

        final CelestialBody startingPlanet = spaceshipParams.returnTrip ? mars : earth;
        final double planetX     = startingPlanet.getX();
        final double planetY     = startingPlanet.getY();
        final double planetVx    = startingPlanet.getVelocityX();
        final double planetVy    = startingPlanet.getVelocityY();
        final double planetDistance = startingPlanet.distanceFrom0();

        final int spaceshipSide = spaceshipParams.returnTrip ? -1 : 1;
        final double positionFactor = 1 + spaceshipSide * (startingPlanet.getRadius() + spaceshipParams.spaceStationDistance) / planetDistance; // (1 + d/E)
        final double orbitalVelocity = spaceshipParams.spaceshipInitialVelocity + spaceshipParams.spaceStationOrbitalVelocity;

        return CelestialBody.builder()
            .withName       ("spaceship")
            .withX          (planetX * positionFactor)
            .withY          (planetY * positionFactor)
            .withVelocityX  (spaceshipSide * Math.signum(planetVx) * orbitalVelocity * (Math.abs(planetY) / planetDistance) + planetVx)
            .withVelocityY  (spaceshipSide * Math.signum(planetVy) * orbitalVelocity * (Math.abs(planetX) / planetDistance) + planetVy)
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
        String collision;

        int iteration = 0;
        do {
            // Actualizamos estado de cuerpos celestes
            if(spaceship != null) {
                spaceship.update();
            }
            earth       .update();
            mars        .update();
            // Al sol no lo updeteamos, consideramos que esta estatico en (0, 0)

            // Calculamos colisiones
            collision = hasSpaceshipCollided();

            iteration++;
        } while(notifier.notify(iteration, spaceship, earth, mars, sun) && collision == null);

        return collision;
    }

    private String hasSpaceshipCollided() {

        if(spaceship == null) {
            return null;
        }
        else if(spaceship.hasCollided(earth)) {
            return earth.getName();
        }
        else if(spaceship.hasCollided(mars)) {
            return mars.getName();
        }
        else if(spaceship.hasCollided(sun)) {
            return sun.getName();
        }
        else {
            return null;
        }
    }

    public double getSystemEnergy() {
        // Hay que pasar la energia de kg*km^2/s^2 kg*m^2/s^2
        return 1_000_000
            * getCelestialBodyEnergy(earth)
            * getCelestialBodyEnergy(mars)
            * getCelestialBodyEnergy(spaceship)
            ;
    }

    // TODO(tobi): Esto se esta calculando bien???
    private double getCelestialBodyEnergy(final CelestialBody body){
        final double potentialEnergy = ((GravitationalForce) body.getSolver().getForce()).getPotentialEnergy(body.getX(), body.getY());
        return potentialEnergy+ body.getKineticEnergy();
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

    @With
    @Value
    @Jacksonized
    @Builder(setterPrefix = "with")
    public static class SpaceshipInitParams {
        public int      spaceshipMass;
        public int      spaceshipMassScale;
        public double   spaceshipInitialVelocity;
        public double   spaceStationDistance;
        public double   spaceStationOrbitalVelocity;
        @Builder.Default public boolean  returnTrip = false;

        public double scaledMass() {
            return spaceshipMass * Math.pow(10, spaceshipMassScale);
        }
    }
}
