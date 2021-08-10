package org.apache.maven.tp0;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CellIndexMethod {
    
    int M;
    double L;
    double actionRadius;
    boolean periodicOutline;
    
    long startMillis;
    long stopMillis;

    Map<Integer, List<Particle>> cells;
    Map<Integer, Set<Particle>> cellsNeighbours;

    public CellIndexMethod(int M, double L, double actionRadius, boolean periodicOutline) {
        this.M = M;
        this.L = L;
        this.actionRadius = actionRadius;
        this.periodicOutline = periodicOutline;
        this.cellsNeighbours = new HashMap<>();
    }

    public Map<Integer, Set<Particle>> getParticleneighbours(List<Particle> particles){

        startMillis = System.currentTimeMillis();

        cells = clasifyParticles(particles);
   
        for (Map.Entry<Integer, List<Particle>> cell: cells.entrySet()) {
            for (Particle particle : cell.getValue()) {
               cellsNeighbours.put(particle.getId(), new HashSet<Particle>());
               getNeighbourgsFromCell(particle, cell.getValue());
               getNeighbourgs(particle, cell.getKey());
            }
        }
        
        stopMillis = System.currentTimeMillis();
        return cellsNeighbours;
    }

    private Map<Integer, List<Particle>> clasifyParticles(List<Particle> particles) {
        Map<Integer, List<Particle>> cells = new HashMap<>();
        
        for (int i=0; i < M * M; i++){
            cells.put(i, new ArrayList<Particle>());
        }

        for (int i=0; i < particles.size(); i++){
            cells.get(getParticleCellPosition(particles.get(i))).add(particles.get(i));
        }

        return cells;
    }

    private Integer getParticleCellPosition(Particle particle) {
        return getCellPosition(particle.getX(), particle.getY());
    }

    private Integer getCellPosition(double x, double y) {
        int cellX = (int) (x / (L / M));
        int cellY = (int) (y / (L / M));
        return cellY * M + cellX;
    }

    private void getNeighbourgs(Particle particle, Integer cell) {
        List<Particle> neighboursCellsParticles;
        if(periodicOutline) {
            neighboursCellsParticles = getNeighboursCellsParticlesPeriodic(cell);
        }
        else {
            neighboursCellsParticles = getNeighboursCellsParticlesNotPeriodic(cell);
        }
        
        getNeighbourgsFromCell(particle, neighboursCellsParticles);
    }

    private List<Particle> getNeighboursCellsParticlesPeriodic(Integer cellNumber) {
        List<Particle> neighboursCellsParticles = new ArrayList<>();
        int cellPositionX = cellNumber % M;
        int cellPositionY = (int) (cellNumber / L);
        int top = getCellPosition(cellPositionY + 1 % M, cellPositionX);
        int topRight = getCellPosition(cellPositionY + 1 % M, cellPositionX + 1 % M);
        int right = getCellPosition(cellPositionY, cellPositionX + 1 % M);
        int bottomRight = getCellPosition(cellPositionY - 1 % M, cellPositionX + 1 % M);
        
        neighboursCellsParticles.addAll(cells.get(top));
        neighboursCellsParticles.addAll(cells.get(topRight));
        neighboursCellsParticles.addAll(cells.get(right));
        neighboursCellsParticles.addAll(cells.get(bottomRight));

        return neighboursCellsParticles;
    }

    private List<Particle> getNeighboursCellsParticlesNotPeriodic(Integer cellNumber) {
        List<Particle> neighboursCellsParticles = new ArrayList<>();
        int cellPositionX = cellNumber % M;
        int cellPositionY = (int) (cellNumber / L);

        if (cellPositionY + 1 < M) {
            neighboursCellsParticles.addAll(cells.get(getCellPosition(cellPositionY + 1 % M, cellPositionX)));
        }
        if (cellPositionY + 1 < M && cellPositionX + 1 < M) {
            neighboursCellsParticles.addAll(cells.get(getCellPosition(cellPositionY + 1 % M, cellPositionX + 1 % M)));
        }
        if (cellPositionX + 1 < M) {
            neighboursCellsParticles.addAll(cells.get(getCellPosition(cellPositionY, cellPositionX + 1 % M)));
        }
        if (cellPositionY - 1 < 0 && cellPositionX + 1 < M) {
            neighboursCellsParticles.addAll(cells.get(getCellPosition(cellPositionY - 1 % M, cellPositionX + 1 % M)));
        }
        
        return neighboursCellsParticles;
    }

    private void getNeighbourgsFromCell(Particle particle, List<Particle> cellParticles) {
        for (Particle neighbour: cellParticles) {
            if (!cellsNeighbours.get(particle.getId()).contains(neighbour) && particle.distanceTo(neighbour) < actionRadius) {
                cellsNeighbours.get(particle.getId()).add(neighbour);
                cellsNeighbours.get(neighbour.getId()).add(particle);
            }
        }
        return;
    }

    public long getTimeElapsed(){
        return stopMillis - startMillis;
    }
}