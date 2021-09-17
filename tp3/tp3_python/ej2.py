from dataclasses import dataclass
import json
import sys
import numpy as np
from models import from_dict
from typing import Any, Dict, List, Union
from matplotlib import pyplot as plt
from models import Collision, Config, Particle, SimulationState, Wall
from plot import Plotter
from matplotlib import cm

@dataclass
@from_dict
class Round:
    lastThirdVelocities:            List[float]
    initialVelocities:              List[float]
    particleCount:                  int

@dataclass
@from_dict
class RoundSummary:
    roundsList:                     List[List[Round]]

    
def parse_state(data: Dict[str, Any]) -> Union[Collision, SimulationState, Particle]:
    if 'roundsList' in data:
        return RoundSummary.from_dict(data)
    elif 'lastThirdVelocities' in data:
        return Round.from_dict(data)


def main(data_path):
    with open(data_path, 'r') as particles_fd:
        states: RoundSummary = json.load(particles_fd, object_hook=parse_state)

    lastThirdValues = list(map(lambda state: np.array(list(map(lambda r: r.lastThirdVelocities, state))), states.roundsList))
    initialVelocities = list(map(lambda state: np.array(list(map(lambda r: r.initialVelocities, state))), states.roundsList))
    particleCounts = list(map(lambda state: state[0].particleCount, states.roundsList))
    rounds = np.size(lastThirdValues[0], 0)
    # iterations = np.size(lastThirdValues[0], 1)
    
    binSize = 0.1

    fig = plt.figure(figsize=(16, 10))
    ax = fig.add_subplot(1, 1, 1)
    print(f'BinInicial={(3 * binSize):.2f}. BinUltimoTercio={(binSize):.2f}.')
    ax.set_xlabel(r'$v$: Modulo de la velocidad (m/s)', size=20)
    ax.set_ylabel(r'Densidad de probabilidad', size=20)
    
    for i in range(len(particleCounts)):
        allThird: np.ndarray = lastThirdValues[i].flatten() 
        thirdHist, thirdBin         = np.histogram(allThird.flatten(), bins=np.arange(0, np.max(allThird.flatten()), binSize), density=True)

        ax.plot(thirdBin[:-1], thirdHist, marker='o', color=cm.get_cmap('tab20c')(4*i), alpha=0.5,
            label=f'N={int(particleCounts[i])}. Ultimo tercio.')
        
        print(f'N={int(particleCounts[i])}. Iteraciones={len(lastThirdValues[i][0])}')

    for i in range(len(particleCounts)):
        allInitials: np.ndarray = initialVelocities[i].flatten() 
        initialsHist, initialsBin   = np.histogram(allInitials, bins=np.arange(0, np.max(allInitials), binSize*3), density=True)
        ax.plot(initialsBin[:-1], initialsHist, marker='o', color=cm.get_cmap('tab20b')(8 + 4*i + 2),
            label=f'N={int(particleCounts[i])}. Valores Iniciales.')


    ax.tick_params(labelsize=16)
    ax.legend(fontsize=14)
    plt.show()


if __name__ == '__main__':
    if len(sys.argv) < 2:
        raise ValueError('Config path must be given by argument')
    try:
        main(sys.argv[1])
    except KeyboardInterrupt:
        pass

    
