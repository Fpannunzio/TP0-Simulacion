package ar.edu.itba.simulacion.tp4;

import static ar.edu.itba.simulacion.tp4.MarsMissionSimulation.*;
import static ar.edu.itba.simulacion.tp4.MolecularDynamicSolver.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import ar.edu.itba.simulacion.particle.marshalling.XYZWritable;
import ar.edu.itba.simulacion.tp4.dynamicSolvers.BeemanSolver;
import ar.edu.itba.simulacion.tp4.dynamicSolvers.GearSolver;
import ar.edu.itba.simulacion.tp4.dynamicSolvers.VerletSolver;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

public class Ej2 {

    public static final int GEAR_SOLVER_DEGREE = 5;

    public static void main(String[] args) throws IOException {
         
        if(args.length < 1) {
            throw new IllegalArgumentException("First argument must be config path");
        }

        final ObjectMapper mapper = new ObjectMapper();

        final MarsMissionConfig config = mapper.readValue(new File(args[0]), MarsMissionConfig.class);

        final MarsMissionSimulation simulation = config.toSimulation();

        try(final BufferedWriter writer = new BufferedWriter(new FileWriter(config.outputFile))) {

            simulation.simulate(10000, (i, spaceship, earth, mars, sun) -> {
                // Imprimimos estado
                XYZWritable.xyzWrite(writer, List.of(spaceship, earth, mars, sun));

                if(i % 1000 == 0) {
                    // Informamos que la simulacion avanza
                    System.out.println("Total states processed so far: " + i);
                }
            });
        }
    }

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
        VERLET  (Ej2::verletSolverSupplier),
        BEEMAN  (Ej2::beemanSolverSupplier),
        GEAR    (Ej2::gearSolverSupplier),
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
        public double               dt;
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
                .withSun                    (sun.toCelestialBody())
                .withMars                   (mars.toCelestialBody())
                .withEarth                  (earth.toCelestialBody())
                .withSpaceship              (spaceship)
                .withSolverSupplier         (solver.getSolverSupplier())
                .build()
                ;
        }
    }
    
    //La masa esta medida en 10^30 kg por lo que hay que expandir 
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

        public CelestialBody toCelestialBody(){
            return CelestialBody.builder()
                .withX          (x)
                .withY          (y)
                .withVelocityX  (velocityX)
                .withVelocityY  (velocityY)
                .withMass       (mass)
                .withMassScale  (massScale)
                .withRadius     (radius)
                .build()
                ;
        }
    }
}
