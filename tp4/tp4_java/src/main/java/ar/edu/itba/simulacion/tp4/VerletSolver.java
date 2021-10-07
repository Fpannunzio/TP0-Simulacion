package ar.edu.itba.simulacion.tp4;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;

public class VerletSolver {
    
    private final double[] functionCoeficients;
    private final Deque<double[]> values;
    private final double m;
    private final double dt;

    private double prev_r0;
    private double next_r0;

    public VerletSolver(final double m, final double dt, final double[] functionCoeficients, final double[] initialValues) {
        
        this.functionCoeficients = functionCoeficients;
        this.values = new LinkedList<>();
        this.values.push(initialValues);
        this.m = m;
        this.dt = dt;

        //euler para conseguir r0(-dt)
        initialBackStep();
    }

    public double[] nextStep() {
        
        final double[] currentValues = values.peek();
        final double[] nextValues = new double[2];

        final double r0 = currentValues[0];
        final double r1 = currentValues[1];
        
        //r0(t+dt)
        nextValues[0] = 2.0*r0 - prev_r0 + ((dt*dt)/m) * functionEval(r0, r1);
        
        //ES r1(t) Y DEBERIA SER r1(t+dt) TODO:
        nextValues[1] = (nextValues[0] - prev_r0) / (2.0 * dt);

        values.push(nextValues);

        prev_r0 = r0;
        
        return nextValues;
    }

    private double functionEval(final double r0, final double r1) {
        return functionCoeficients[0] * r0 + functionCoeficients[1] * r1;
    }

    private void initialBackStep() {
        
        final double[] currentValues = values.peek();

        final double r0 = currentValues[0];
        final double r1 = currentValues[1];
        
        prev_r0 = eulerPositionStep(r0, r1, m, -dt);
    }

    private double eulerPositionStep(final double r0, final double r1, final double m, final double dt) {
        return r0 + (dt * r1) + ((dt * dt) / (2.0 * m)) * functionEval(r0, r1);
    }

}
