package ar.edu.itba.simulacion.tp4;

import static ar.edu.itba.simulacion.tp4.MolecularDynamicSolver.AXIS_DIM;

import java.util.List;

import ar.edu.itba.simulacion.tp4.MolecularDynamicSolver.Force;
import ar.edu.itba.simulacion.tp4.MolecularDynamicSolver.MoleculeStateAxis;

public class GravitationalForce implements Force {
    
    private final List<CelestialBody>   celestialBodies;
    private final double                affectedBodyMass;
    private final double                gravitationalConstant;

    public GravitationalForce(final List<CelestialBody> celestialBodies, final double affectedBodyMass, final double gravitationalConstant) {
        this.celestialBodies        = celestialBodies;
        this.affectedBodyMass       = affectedBodyMass;
        this.gravitationalConstant  = gravitationalConstant;
    }

    @Override
    public double apply(int axis, MoleculeStateAxis[] state) {
        final int bodyCount = celestialBodies.size();
        final double[][] forces = new double[bodyCount][AXIS_DIM];

        for(int i = 0; i < bodyCount; i++) {
            final CelestialBody body = celestialBodies.get(i);

            final double distance = body.distanceTo(state[0].position, state[1].position);
            final double normalForce = affectedBodyMass * body.getMass() * gravitationalConstant / (distance * distance);
            forces[i][0] = normalForce * ((body.getX() - state[0].position) / distance);
            forces[i][1] = normalForce * ((body.getY() - state[1].position) / distance);
        }
        
        double force = 0;
        for(final double[] doubles : forces) {
            force += doubles[axis];
        }

        return force;
    }
  
}
