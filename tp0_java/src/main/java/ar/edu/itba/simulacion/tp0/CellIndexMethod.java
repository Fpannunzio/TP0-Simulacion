package ar.edu.itba.simulacion.tp0;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.IntConsumer;

public class CellIndexMethod {
    
    private final int       M;
    private final double    L;
    private final double    actionRadius;
    private final boolean   periodicOutline;

    public CellIndexMethod(final int M, final double L, final double actionRadius, final boolean periodicOutline) {
        
        int maxMValue = (int) (L / actionRadius);

        if(maxMValue < M) {
            throw new IllegalArgumentException("L to M ratio is too small. Max possible value for M is " + maxMValue);
        }

        this.M = M;
        this.L = L;
        this.actionRadius = actionRadius;
        this.periodicOutline = periodicOutline;
    }

    private int particleCell(final Particle particle) {
        return coordinateToCell(particle.getX(), particle.getY());
    }

    private int coordinateToCell(final double x, final double y) {
        int cellX = (int) (x / (L / M));
        int cellY = (int) (y / (L / M));
        return indexToCell(cellX, cellY);
    }

    private int indexToCell(final int x, final int y) {
        return y * M + x;
    }

    public Map<Integer, Set<Particle>> calculateNeighbours(final List<Particle> particles) {
        final Map<Integer, Set<Particle>> ret = new HashMap<>(particles.size());

        final double maxRadius = particles
            .stream()
            .max(Comparator.comparing(Particle::getRadius))
            .map(Particle::getRadius)
            .orElseThrow(() -> new IllegalArgumentException("No particles were supplied"))
            ;

        int maxMValue = (int) (L / (actionRadius + 2*maxRadius));
        
        if(maxMValue < M) {
            throw new IllegalArgumentException("L to M ratio is too small. Max possible value for M is " + maxMValue);
        }

        // Inicializamos mapa de respuesta
        for(final Particle particle : particles) {
            ret.put(particle.getId(), new HashSet<>());
        }

        // Inicializamos celdas
        final List<Particle>[] cells = buildCells(particles);

        for(final List<Particle> cellValues : cells) {
            for(final Particle particle : cellValues) {
                // Agregamos las particulas de la misma celda
                addNeighbours(particle, cellValues, ret);

                // Agregamos las particulas de las celdas vecinas
                listCellNeighbours(particleCell(particle), neighbourCellId -> addNeighbours(particle, cells[neighbourCellId], ret));
            }
        }

        return ret;
    }

    private List<Particle>[] buildCells(final List<Particle> particles) {
        @SuppressWarnings("unchecked")
        final List<Particle>[] ret = new List[M * M];

        // Inicializamos todas las celdas en el mapa, asignandoles un id unico segun su posicion
        for(int i = 0; i < M * M; i++) {
            ret[i] = new LinkedList<>();
        }

        // Distribuimos las particulas en la celda correspondiente
        for(final Particle particle : particles) {
            ret[particleCell(particle)].add(particle);
        }

        return ret;
    }

    private void addNeighbours(final Particle particle, final List<Particle> potentialNeighbours, final Map<Integer, ? extends Collection<Particle>> cellsNeighbours) {
        final Collection<Particle> currentNeighbours = cellsNeighbours.get(particle.getId());

        for(final Particle neighbour: potentialNeighbours) {
            if(!particle.equals(neighbour) && !currentNeighbours.contains(neighbour) && particle.distanceTo(neighbour, L, periodicOutline) < actionRadius) {
                currentNeighbours.add(neighbour);
                cellsNeighbours.get(neighbour.getId()).add(particle);
            }
        }
    }

    // Como optimizacion, solo listamos la mitad de los vecinos
    // Como todos listan la misma mitad, todos terminan siendo listados
    private void listCellNeighbours(final int cell, final IntConsumer consumer) {
        int cellPositionX = cell % M;
        int cellPositionY = cell / M;

        // System.out.println(cellPositionX * 10000 + cellPositionY);

        // Top
        if(periodicOutline || cellPositionY + 1 < M) {
            consumer.accept(indexToCell(cellPositionX, (cellPositionY + 1) % M));
        }
        // Top-Right
        if(periodicOutline || (cellPositionY + 1 < M && cellPositionX + 1 < M)) {
            consumer.accept(indexToCell((cellPositionX + 1) % M, (cellPositionY + 1) % M));
        }
        // Right
        if(periodicOutline || cellPositionX + 1 < M) {
            consumer.accept(indexToCell((cellPositionX + 1) % M, cellPositionY));
        }
        // Bottom-Right
        if (periodicOutline || (cellPositionY - 1 < 0 && cellPositionX + 1 < M)) {
            consumer.accept(indexToCell((cellPositionX + 1) % M, (cellPositionY - 1) % M));
        }
    }
}