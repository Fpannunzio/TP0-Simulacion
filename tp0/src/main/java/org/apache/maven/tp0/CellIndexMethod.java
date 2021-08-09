import java.util.List;
import java.util.Map;

public class cellIndexMethod {
    
    int M;
    double L;
    double actionRadius;
    boolean periodicOutline;
    Map<Integer, List<Particle>> cells;
    Map<Integer, Set<Particle>> cellsNeighbors;

    public cellIndexMethod(int M, double L, double actionRadius, boolean periodicOutline) {
        this.M = M;
        this.L = L;
        this.actionRadius = actionRadius;
        this.periodicOutline = periodicOutline;
        this.cellsNeighbors = new HashMap<>();
    }

    public void getParticleNeighbors(List<Particle> particles){
        cells = clasifyParticles(particles, L);
   
        for (Map.Entry<Integer, List<Particle>> cell: cells.entrySet()) {
            for (Particle particle: cell.getValue()) {
               cellsNeighbours.put(particle.getId(), new HashSet<>());
               getNeighbourgsFromCell(particle, actionRadius, cell.getValue(), cellsNeighbors);
               if(periodicOutline) {
                   periodicOutlineNeighborgs(particle, actionRadius, cell, cells, cellsNeighbors);
               }
               else {
                   notPeriodicOutlineNeighborgs(particle, actionRadius, cell, cells, cellsNeighbors);
               }
            }
        }
           
    }

    private Map<Integer, List<Particle>> clasifyParticles(List<Particle> particles) {
        Map<Integer, List<Particle>> cells = new HashMap<>();
        
        for (int i=0; i < this.M * this.M; i++){
            cells.put(i, new ArrayList<Particle>());
        }

        for (int i=0; i < particles.size(); i++){
            cells.put(getParticleCellPosition(particles.get(i), this.L, this.M), particles.get(i));
        }

        return cells;
    }

    private Integer getParticleCellPosition(Particle particle) {
        return getCellPosition(particle.getX(), particle.getY());
    }

    private Integer getCellPosition(double x, double y) {
        int cellX = x / (this.L / this.M);
        int cellY = y / (this.L / this.M);
        return cellY * this.M + cellX;
    }

    private void periodicOutlineNeighborgs(Particle particle, Double actionRadius, Integer cell, Map<Integer, List<Particle>> cells, Map<Integer, Set<Particle>> cellsNeighbors) {
        List<Particle> neighborsCellsParticles = getNeighborsCellsParticles(cell, cells);
        getNeighbourgsFromCell(particle, actionRadius, cells.get(cells.size()), cellsNeighbors);
    }
    
    private void notPeriodicOutlineNeighborgs(Particle particle, Double actionRadius, Integer cell, Map<Integer, List<Particle>> cells, Map<Integer, Set<Particle>> cellsNeighbors) {
        List<Particle> neighborsCellsParticles = getNeighborsCellsParticles(cell, cells);
        getNeighbourgsFromCellNotPeriodic(particle, actionRadius, cells.get(cells.size()), cellsNeighbors);
    }

    private List<Particle> getNeighborsCellsParticlesPeriodic(Integer cellNumber, Map<Integer, List<Particle>> cells) {
        List<Particle> neighborsCellsParticles = new ArrayList<>();
        int cellPositionX = cellNumber % this.M;
        int cellPositionY = cellNumber / this.L;
        int top = getCellPosition(cellPositionY + 1 % this.M, cellPositionX);
        int topRight = getCellPosition(cellPositionY + 1 % this.M, cellPositionX + 1 % this.M);
        int right = getCellPosition(cellPositionY, cellPositionX + 1 % this.M);
        int bottomRight = getCellPosition(cellPositionY - 1 % this.M, cellPositionX + 1 % this.M);
        
        neighborsCellsParticles.addAll(cells.get(top));
        neighborsCellsParticles.addAll(cells.get(topRight));
        neighborsCellsParticles.addAll(cells.get(right));
        neighborsCellsParticles.addAll(cells.get(bottomRight));

        return neighborsCellsParticles;
    }

    private List<Particle> getNeighborsCellsParticlesNot(Integer cellNumber, Map<Integer, List<Particle>> cells) {
        List<Particle> neighborsCellsParticles = new ArrayList<>();
        int cellPositionX = cellNumber % this.M;
        int cellPositionY = cellNumber / this.L;
        if (cellPositionY + 1 < this.M) {
            neighborsCellsParticles.addAll(cells.get(getCellPosition(cellPositionY + 1 % this.M, cellPositionX)));
        }
        if (cellPositionY + 1 < this.M && cellPositionX + 1 < this.M) {
            neighborsCellsParticles.addAll(cells.get(neighborsCellsParticles.addAll(cells.get(topRight)));
        }
        if (cellPositionX + 1 < this.M) {
            neighborsCellsParticles.addAll(cells.get(getCellPosition(cellPositionY, cellPositionX + 1 % this.M)));
        }
        if (cellPositionY - 1 < 0 && cellPositionX + 1 < this.M) {
            neighborsCellsParticles.addAll(cells.get(getCellPosition(cellPositionY - 1 % this.M, cellPositionX + 1 % this.M)));
        }
        
        return neighborsCellsParticles;
    }

    private static void getNeighborgsFromCell(Particle particle, Double actionRadius, List<Particle> cellParticles, Map<Integer, Set<Particle>> cellsNeighbors) {
        for (Particle neighbor: cellParticles) {
            if (!cellsNeighbors.get(particle.getId()).contains(neighbor) & particle.distanceTo(neighbor) < actionRadius) {
                cellsNeighbors.get(particle.getId()).add(neighbor.getId());
            }
        }
        return;
    }
}