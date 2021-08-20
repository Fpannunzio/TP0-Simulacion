package ar.edu.itba.simulacion.tp2;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OffLattice {

    private List<IterationState> iterations;
    private final CellIndexMethod cim;

    private final int       M;
    private final double    L;
    private final double    actionRadius;


    public OffLattice(List<IterationState> iterations, int M, double L, double actionRadius) {
        this.iterations = new LinkedList<>();
        this.M = M;
        this.L = L;
        this.actionRadius = actionRadius;
        this.cim = new CellIndexMethod(M, L, actionRadius, true);
    }

    public List<IterationState> runIterations(final List<Particle2D> particles, final int time) {
        Map<Integer, Set<Particle2D>> neighbours;
        
        iterations.add(new IterationState(particles));

        for (int i=0; i < time; i++) {

            IterationState newIteration = new IterationState();
            neighbours = cim.calculateNeighbours(iterations.get(i).getParticles());

            for(Particle2D particle : iterations.get(i).getParticles()) {
                newIteration.appendParticle(particleNextState(particle, neighbours.get(particle.getId())));
            }
            iterations.add(newIteration);   
        }

        return iterations;
    }

    private Particle2D particleNextState(Particle2D particle, Set<Particle2D> neighbourParticles) {
        
        double avgSin = neighbourParticles.stream().map(p -> Math.sin(p.getVelocityDir())).mapToDouble(Double::doubleValue).average().orElse(particle.getVelocityDir());
        double avgCos = neighbourParticles.stream().map(p -> Math.cos(p.getVelocityDir())).mapToDouble(Double::doubleValue).average().orElse(particle.getVelocityDir());

        return Particle2D.builder()
            .withId(particle.getId())
            .withX(particle.getX() + particle.getXShift(1))
            .withY(particle.getY() + particle.getYShift(1))
            .withVelocityMod(particle.getVelocityMod())
            .withVelocityDir(Math.atan2(avgCos, avgSin))
            .withRadius(particle.getRadius())
            .build();
    }
    
    private static class IterationState {

        List<Particle2D> particles;
    
        private IterationState() {
            this.particles = new LinkedList<>();
        }

        private IterationState(List<Particle2D> particles) {
            this.particles = particles;
        }
        
        private List<Particle2D> getParticles() {
            return particles;
        }
        
        private void appendParticle(Particle2D particle) {
            particles.add(particle);
        }
    
    }
}