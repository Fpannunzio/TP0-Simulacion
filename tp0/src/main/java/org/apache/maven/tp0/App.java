package org.apache.maven.tp0;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Hello world!
 *
 */
public class App 
{
    //receive N, L, M, rc, PeriodicOutline
    public static void main( String[] args )
    {
        List<Particle> particles = initParticles(args);
        CellIndexMethod CIM = new CellIndexMethod(Integer.valueOf(args[3]), Double.valueOf(args[2]), Double.valueOf(args[4]), true);
        Map<Integer, Set<Particle>> cellsNeighbours = CIM.getParticleneighbours(particles);

    }

    private static List<Particle> initParticles(String[] args) {
        List<Particle> particles = new ArrayList<>();
        for (int i = 0; i < Integer.valueOf(args[0]); i++) {
            particles.add(new Particle(i, Math.floor(Math.random()*(Double.valueOf(args[2])*Integer.valueOf(args[3])+ 1)), Math.floor(Math.random()*(Double.valueOf(args[2])*Integer.valueOf(args[3])+ 1)) , Integer.valueOf(args[1])));
        }
        return particles;
    }

    private static void bruteForce(){

    }
}