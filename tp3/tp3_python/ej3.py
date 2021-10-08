from dataclasses import dataclass
import json
import sys
import numpy as np
from matplotlib import pyplot as plt
from models import from_dict
from typing import Any, Dict, List, Union
from matplotlib import cm
from models import Collision, Config, Particle, SimulationState, Wall
from plot import Plotter

@dataclass
@from_dict
class RoundSummary:
    states:         List[List[float]]
    times:       List[float]
    temp:           int


def parse_state(data: Dict[str, Any]) -> Union[Collision, SimulationState, Particle]:
    return RoundSummary.from_dict(data)
    

def main(data_path):
    with open(data_path, 'r') as particles_fd:
        rounds: List[RoundSummary] = json.load(particles_fd, object_hook=parse_state)

    positions = list(map(lambda r: np.array(r.states), rounds))
    temps = list(map(lambda state: state.temp, rounds))
    times = list(map(lambda r: np.array(r.times), rounds))

    
    maxIterations = min(map(lambda p: p.shape[0], positions)) - 1


    print(f'Max iterations={maxIterations}')

    fig = plt.figure(figsize=(10, 10))
    ax = fig.add_subplot(1, 1, 1)
    ax.set_xlim([0, 6])
    ax.set_ylim([0, 6])
    ax.set_yticks([])
    ax.set_xticks([])
    
    for i in range(len(temps)):
        ax.plot(*positions[i][:maxIterations].T, label=f"v=[{temps[i]}, {temps[i] + 1}] m/s" , color=cm.get_cmap('tab10')(i))
        print(f"v=[{temps[i]}, {temps[i] + 1}]. Iter={maxIterations}. Duration={np.sum(times[i][:maxIterations])}")
        start = ax.add_artist(plt.Circle((positions[i].T[0, 0],positions[i].T[1, 0]),                               0.1, color='green', alpha=0.3))
        end = ax.add_artist(plt.Circle((positions[i].T[0, maxIterations],positions[i].T[1, maxIterations]),         0.7, color=cm.get_cmap('tab10')(i), alpha=0.3))

    plt.legend(fontsize=14)
    plt.show()


if __name__ == '__main__':
    if len(sys.argv) < 2:
        raise ValueError('Config path must be given by argument')
    try:
        main(sys.argv[1])
    except KeyboardInterrupt:
        pass
