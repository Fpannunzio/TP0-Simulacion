package ar.edu.itba.simulacion.tp4;

import java.util.List;

import ar.edu.itba.simulacion.tp4.MolecularDynamicSolver.Force;
import ar.edu.itba.simulacion.tp4.MolecularDynamicSolver.MoleculeStateAxis;

public class GravitationalForce implements Force {
    
    private final List<CelestialBody>           celestialBodies;
    private final double                        affectedBodyMass;
    private final double                        gravitationalConstant;

    public GravitationalForce(List<CelestialBody> celestialBodies, double affectedBodyMass, double gravitationalConstant) {
        this.celestialBodies = celestialBodies;
        this.affectedBodyMass = affectedBodyMass;
        this.gravitationalConstant = gravitationalConstant;
    }

    @Override
    public double apply(int axis, MoleculeStateAxis[] state) {
        final double [][] forces = new double[celestialBodies.size()][2];
        double distance;
        double normalForce;
        for (int i = 0; i < celestialBodies.size(); i++) {
            distance = celestialBodies.get(i).calculateDistance(state[0].position, state[1].position);
            normalForce = affectedBodyMass * celestialBodies.get(i).getMass() * gravitationalConstant / distance;
            forces[i][0] = normalForce * ((celestialBodies.get(i).getX() - state[0].position) / distance);
            forces[i][1] = normalForce * ((celestialBodies.get(i).getY() - state[1].position) / distance);
        }
        
        double force = 0;
        for (int i = 0; i < forces.length; i++) {
            force += forces[i][axis];
        }

        return force;
    }
  
}
