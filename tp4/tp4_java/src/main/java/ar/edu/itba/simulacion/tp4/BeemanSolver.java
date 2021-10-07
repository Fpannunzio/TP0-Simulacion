package ar.edu.itba.simulacion.tp4;

import java.util.Arrays;

public class BeemanSolver {
    
    private final double[] functionCoeficients;
    private final double[] initialValues;
    private final double m;
    private final double dt;

    private double[] lastValues;
    private double prev_r2;


    public BeemanSolver(final double m, final double dt, final double[] functionCoeficients, final double[] initialValues) {
        
        this.functionCoeficients = functionCoeficients;
        this.initialValues = initialValues;
        this.lastValues = Arrays.copyOf(initialValues, initialValues.length);
        this.m = m;
        this.dt = dt;

        initialAceleration();
    }

    public double[] nextStep() {
        
        double r0 = lastValues[0];
        double r1 = lastValues[1];

        double r2 = (functionEval(r0, r1) / m);

        //r0(t +dt)
        lastValues[0] = r0 + (r1*dt) + ((2/3) * r2 * dt*dt) - ((1/6) * prev_r2 * dt*dt);  
        
        //r1_p(t +dt)
        double predict_r1 = r1 + ((3/2) * r2 * dt) - ((1/2) * prev_r2 * dt);
        
        //r2(t +dt)
        double predict_r2 = (functionEval(lastValues[0], predict_r1) / m);

        //r1(t + dt)
        lastValues[1] = r1 + ((1/3) * predict_r2 * dt) + ((5/6) * r2 * dt) - ((1/6) * prev_r2 * dt);

        prev_r2 = r2;

        return Arrays.copyOf(lastValues, lastValues.length);
    }

    private double functionEval(final double r0, final double r1) {
        return functionCoeficients[0] * r0 + functionCoeficients[1] * r1;
    }

    private void initialAceleration() {

        double r0 = initialValues[0];
        double r1 = initialValues[1];

        double prev_r0 = r0 + (- dt * r1) + ((dt * dt) / (2 * m)) * functionEval(r0, r1);
        double prev_r1 = r1 + (- dt / m) * functionEval(r0, r1);

        prev_r2 = functionEval(prev_r0, prev_r1);
    }
}
