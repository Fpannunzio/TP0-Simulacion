package ar.edu.itba.simulacion.tp4.marsMission;

import java.util.List;

import ar.edu.itba.simulacion.tp4.MolecularDynamicSolver;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

public class MarsMissionSimulation {

    public static final int SYSTEM_DIMENSION = 2;

    private final double            gravitationalConstant;
    private final CelestialBody     sun;
    private final CelestialBody     earth;
    private final CelestialBody     mars;
    private final CelestialBody     spaceship;

    @Builder(setterPrefix = "with")
    public MarsMissionSimulation(
        final double                dt,
        final double                gravitationalConstant,
        final CelestialBody         sun,
        final CelestialBody         earth,
        final CelestialBody         mars,
        final SpaceshipInitParams   spaceship,
        final SolverSupplier        solverSupplier) {

        this.gravitationalConstant  = gravitationalConstant;
        this.sun                    = sun;
        this.earth                  = earth;
        this.mars                   = mars;
        this.spaceship              = buildSpaceShip(spaceship);

        this.earth      .setSolver(buildCelestialBodySolver(dt, this.earth,     List.of(sun, mars),         solverSupplier));
        this.mars       .setSolver(buildCelestialBodySolver(dt, this.mars,      List.of(sun, earth),        solverSupplier));
        this.spaceship  .setSolver(buildCelestialBodySolver(dt, this.spaceship, List.of(sun, earth, mars),  solverSupplier));
    }

    private CelestialBody buildSpaceShip(final SpaceshipInitParams spaceshipParams) {
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
            .withVelocityX  (-Math.signum(earthVx) * orbitalVelocity * (earthX / earthDistance) + earthVx)
            .withVelocityY  (-Math.signum(earthVy) * orbitalVelocity * (earthY / earthDistance) + earthVy)
            .withMass       (spaceshipParams.scaledMass())
            .withRadius     (0)
            .build()
            ;
    }

    private MolecularDynamicSolver buildCelestialBodySolver(
        final double                dt,
        final CelestialBody         celestialBody,
        final List<CelestialBody>   bodiesAffectedBy,
        final SolverSupplier        solverSupplier) {

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

    public void simulate(final int iterations, final SimulationStateConsumer consumer) {
        consumer.accept(0, spaceship, earth, mars, sun);

        for(int i = 1; i <= iterations; i++) {
            spaceship   .update();
            earth       .update();
            mars        .update();
            // Al sol no lo updeteamos, consideramos que esta estatico en (0, 0)

            consumer.accept(i, spaceship, earth, mars, sun);
        }
    }

    /* ----------------------------------------- Clases Auxiliares ----------------------------------------------- */

    @FunctionalInterface
    public interface SimulationStateConsumer {
        void accept(
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
