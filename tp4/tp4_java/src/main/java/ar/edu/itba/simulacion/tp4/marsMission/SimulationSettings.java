package ar.edu.itba.simulacion.tp4.marsMission;

import static ar.edu.itba.simulacion.tp4.MolecularDynamicSolver.*;
import static ar.edu.itba.simulacion.tp4.marsMission.MarsMissionSimulation.SYSTEM_DIMENSION;

import ar.edu.itba.simulacion.tp4.dynamicSolvers.BeemanSolver;
import ar.edu.itba.simulacion.tp4.dynamicSolvers.GearSolver;
import ar.edu.itba.simulacion.tp4.dynamicSolvers.VerletSolver;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

public final class SimulationSettings {
    private SimulationSettings() {
        // static
    }

    public static final int GEAR_SOLVER_DEGREE = 5;

    public static VerletSolver verletSolverSupplier(
        final double    dt, final double    mass, final GravitationalForce  force,
        final double    x,  final double    y,
        final double    vx, final double    vy) {
        return new VerletSolver(SYSTEM_DIMENSION, dt, mass, force, new MoleculeStateAxis[]{new MoleculeStateAxis(x, vx), new MoleculeStateAxis(y, vy)});
    }

    public static BeemanSolver beemanSolverSupplier(
        final double    dt, final double    mass, final GravitationalForce  force,
        final double    x,  final double    y,
        final double    vx, final double    vy) {
        return new BeemanSolver(SYSTEM_DIMENSION, dt, mass, force, new MoleculeStateAxis[]{new MoleculeStateAxis(x, vx), new MoleculeStateAxis(y, vy)});
    }

    public static GearSolver gearSolverSupplier(
        final double    dt, final double    mass, final GravitationalForce  force,
        final double    x,  final double    y,
        final double    vx, final double    vy) {

        final double[] axis1 = new double[GEAR_SOLVER_DEGREE + 1]; axis1[0] = x; axis1[1] = vx;
        final double[] axis2 = new double[GEAR_SOLVER_DEGREE + 1]; axis2[0] = y; axis2[1] = vy;

        return new GearSolver(SYSTEM_DIMENSION, dt, mass, force, GEAR_SOLVER_DEGREE, GravitationalForce.MAX_R, new double[][] {axis1, axis2});
    }

    public enum SolverStrategy {
        VERLET  (SimulationSettings::verletSolverSupplier),
        BEEMAN  (SimulationSettings::beemanSolverSupplier),
        GEAR    (SimulationSettings::gearSolverSupplier),
        ;

        private final MarsMissionSimulation.SolverSupplier solverSupplier;

        SolverStrategy(final MarsMissionSimulation.SolverSupplier solverSupplier) {
            this.solverSupplier = solverSupplier;
        }

        public MarsMissionSimulation.SolverSupplier getSolverSupplier() {
            return solverSupplier;
        }
    }

    @Data
    @Jacksonized
    @Builder(setterPrefix = "with")
    public static class MarsMissionConfig {
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

        public MarsMissionSimulation toPlanetSimulation() {
            return MarsMissionSimulation.builder()
                .withDt                     (dt)
                .withGravitationalConstant  (gravitationalConstant)
                .withSun                    (sun.toCelestialBody("sun"))
                .withMars                   (mars.toCelestialBody("mars"))
                .withEarth                  (earth.toCelestialBody("earth"))
                .withSpaceship              (null)
                .withSolverSupplier         (solver.getSolverSupplier())
                .build()
                ;
        }
    }

    @Data
    @Jacksonized
    @Builder(setterPrefix = "with")
    public static class CelestialBodyData {
        public double   x;
        public double   y;
        public double   velocityX;
        public double   velocityY;
        public double   mass;
        public int      massScale;
        public double   radius;

        public double scaledMass() {
            return mass * Math.pow(10, massScale);
        }

        public CelestialBody toCelestialBody(final String name) {
            return CelestialBody.builder()
                .withName       (name)
                .withX          (x)
                .withY          (y)
                .withVelocityX  (velocityX)
                .withVelocityY  (velocityY)
                .withMass       (scaledMass())
                .withRadius     (radius)
                .build()
                ;
        }
    }
}
