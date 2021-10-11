package ar.edu.itba.simulacion.tp4.marsMission;

import static ar.edu.itba.simulacion.tp4.MolecularDynamicSolver.*;
import static ar.edu.itba.simulacion.tp4.marsMission.simulation.MarsMissionSimulation.*;
import static ar.edu.itba.simulacion.tp4.marsMission.simulation.MarsMissionSimulation.SYSTEM_DIMENSION;
import static java.util.concurrent.TimeUnit.*;

import java.time.LocalDateTime;

import ar.edu.itba.simulacion.tp4.dynamicSolvers.BeemanSolver;
import ar.edu.itba.simulacion.tp4.dynamicSolvers.GearSolver;
import ar.edu.itba.simulacion.tp4.dynamicSolvers.VerletSolver;
import ar.edu.itba.simulacion.tp4.marsMission.simulation.CelestialBody;
import ar.edu.itba.simulacion.tp4.marsMission.simulation.GravitationalForce;
import ar.edu.itba.simulacion.tp4.marsMission.simulation.MarsMissionSimulation;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

public final class SimulationSettings {
    private SimulationSettings() {
        // static
    }

    // Solver Settings
    public static final int     GEAR_SOLVER_DEGREE = 5;

    // Global Constants
    public static final LocalDateTime   INITIAL_DATE_TIME   = LocalDateTime.of(2021, 10, 24, 0, 0, 0);
    public static final double          MARS_ORBIT          = 2.2799e8;
    public static final long            MARS_ORBIT_SECONDS  = DAYS.toSeconds(687);
    public static final double          EPSILON             = 1e-5;

    // Set return trip analysis
    public static final boolean RETURN_TRIP = true;
    static {
        if(RETURN_TRIP) {
            System.out.println("Analysing return trip!");
        }
    }

    // Collision Information
    public static final int     COLLISION_ITERATION     = RETURN_TRIP ? 89_263 : 104_532;
    public static final int     MAX_COLLISION_TOLERANCE = RETURN_TRIP ? 25_000 : 30_000;

    public static VerletSolver verletSolverSupplier(
        final double    dt, final double    mass, final GravitationalForce force,
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

        private final SolverSupplier solverSupplier;

        SolverStrategy(final SolverSupplier solverSupplier) {
            this.solverSupplier = solverSupplier;
        }

        public SolverSupplier getSolverSupplier() {
            return solverSupplier;
        }
    }

    @Data
    @Jacksonized
    @Builder(setterPrefix = "with")
    public static class MarsMissionConfig {
        public int                  dt;
        public double               gravitationalConstant;
        public SpaceshipInitParams  spaceship;
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
                .withSpaceship              (spaceship.withReturnTrip(RETURN_TRIP))
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
