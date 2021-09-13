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
    temp:           int


def parse_state(data: Dict[str, Any]) -> Union[Collision, SimulationState, Particle]:
    return RoundSummary.from_dict(data)
    

def main(data_path):
    with open(data_path, 'r') as particles_fd:
        rounds: List[RoundSummary] = json.load(particles_fd, object_hook=parse_state)

    positions = list(map(lambda r: np.array(r.states), rounds))
    temps = list(map(lambda state: state.temp, rounds))

    iterations = 10_000

    fig = plt.figure(figsize=(10, 10))
    ax = fig.add_subplot(1, 1, 1)
    ax.set_xlim([0, 6])
    ax.set_ylim([0, 6])
    ax.set_title(f'Iterations: {iterations}')
    ax.set_yticks([])
    ax.set_xticks([])
    
    for i in range(len(temps)):
        ax.plot(*positions[i][:iterations].T, label=f"v=[{temps[i]}, {temps[i] + 1}]", color=cm.get_cmap('jet')(temps[i] / np.max(temps)))
        start = ax.add_artist(plt.Circle((positions[i].T[0, 0],positions[i].T[1, 0]),       0.1, color='green', alpha=0.3))
        end = ax.add_artist(plt.Circle((positions[i].T[0, iterations],positions[i].T[1, iterations]),       0.1, color='red', alpha=0.3))

    plt.legend()
    plt.show()


if __name__ == '__main__':
    if len(sys.argv) < 2:
        raise ValueError('Config path must be given by argument')
    try:
        main(sys.argv[1])
    except KeyboardInterrupt:
        pass
