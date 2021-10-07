package ar.edu.itba.simulacion.tp4;

import java.util.Arrays;

public class VerletSolver {
    
    private final double[] functionCoeficients;
    private final double[] initialValues;
    private final double m;
    private final double dt;

    private double[] lastValues;
    private double prev_r0;
    private double next_r0;

    public VerletSolver(final double m, final double dt, final double[] functionCoeficients, final double[] initialValues) {
        
        this.functionCoeficients = functionCoeficients;
        this.initialValues = initialValues;
        this.lastValues = Arrays.copyOf(initialValues, initialValues.length);
        this.m = m;
        this.dt = dt;

        //euler para conseguir r0(-dt)
        initialBackStep();
    }

    public double[] nextStep() {
        
        prev_r0 = lastValues[0];
        
        //r0
        double r0 = lastValues[0];
        
        //r1
        double r1 = (next_r0 - prev_r0) / 2 / dt;

        //r0(t+dt)
        next_r0 = 2*r0 - prev_r0 + dt*dt/m * functionEval(r0, r1);

        lastValues[1] = r1;
        
        double[] ans = Arrays.copyOf(lastValues, lastValues.length);
        
        lastValues[0] = next_r0;
        
        return ans;
    }

    private double functionEval(final double r0, final double r1) {
        return functionCoeficients[0] * r0 + functionCoeficients[1] * r1;
    }

    private void initialBackStep() {

        double r0 = initialValues[0];
        double r1 = initialValues[1];
        
        prev_r0 = r0 + (-dt * r1) + ((dt * dt) / (2 * m)) * functionEval(r1, r0);
    }
}
