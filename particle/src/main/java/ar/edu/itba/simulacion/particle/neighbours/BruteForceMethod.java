package ar.edu.itba.simulacion.particle.neighbours;

import ar.edu.itba.simulacion.particle.Particle2D;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public final class BruteForceMethod {

    private BruteForceMethod() {
        // static class
    }

    public static Map<Integer, List<Particle2D>> calculateNeighbours(
        final List<Particle2D> particles, final double spaceWidth, final double actionRadius, final boolean periodicBorder
    ) {
        final Map<Integer, List<Particle2D>> ret = new HashMap<>();
        for(final Particle2D particle : particles) {
            ret.put(particle.getId(), new ArrayList<>(particles.size()));
        }

        particles.sort(Comparator.comparing(Particle2D::getId));

        final int particleCount = particles.size();

        int i = 0;
        for(final Particle2D particle : particles) {
            if(i + 1 < particleCount) {
                final ListIterator<Particle2D> possibleNeighbours = particles.listIterator(i + 1);
                while (possibleNeighbours.hasNext()) {
                    final Particle2D possibleNeighbour = possibleNeighbours.next();

                    if(particle.distanceTo(possibleNeighbour, spaceWidth, periodicBorder) < actionRadius) {
                        ret.get(particle.getId()).add(possibleNeighbour);
                        ret.get(possibleNeighbour.getId()).add(particle);
                    }
                }
            }
            i++;
        }

        return ret;
    }
}
