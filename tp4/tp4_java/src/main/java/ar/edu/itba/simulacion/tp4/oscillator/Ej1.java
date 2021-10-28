package ar.edu.itba.simulacion.tp4.oscillator;

import static ar.edu.itba.simulacion.tp4.oscillator.SimulationSettings.*;
import static ar.edu.itba.simulacion.tp4.oscillator.SimulationSettings.getVerletSolver;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import ar.edu.itba.simulacion.tp4.dynamicSolvers.BeemanSolver;
import ar.edu.itba.simulacion.tp4.dynamicSolvers.GearSolver;
import ar.edu.itba.simulacion.tp4.dynamicSolvers.VerletSolver;

public final class Ej1 {
    private Ej1() {
        // static
    }

    public static final int     count   = (int) (5e4);
    public static final double  tf      = 5;
    public static final double  dt      = 1e-4;

    public static void main(String[] args) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();

        final double[][][] results = new double[3][count][];

        final VerletSolver  vSolver    = getVerletSolver(dt);
        final BeemanSolver  bSolver    = getBeemanSolver(dt);
        final GearSolver    gSolver    = getGearSolver(dt);

        for (int i = 0; i < count; i++) {
            results[0][i] = vSolver.oneAxisNextStep().r;
            results[1][i] = bSolver.oneAxisNextStep().r;
            results[2][i] = gSolver.oneAxisNextStep().r;
        }

        mapper.writeValue(new File("output/ej1.json"), results);
    }
}
