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
    collisionTimes:         List[List[float]]
    particleCount:          int


def parse_state(data: Dict[str, Any]) -> Union[Collision, SimulationState, Particle]:
    return RoundSummary.from_dict(data)

def main(data_path):
    with open(data_path, 'r') as particles_fd:
        states: List[RoundSummary] = json.load(particles_fd, object_hook=parse_state)

    rounds = list(map(lambda state: np.array(state.collisionTimes), states))
    particleCounts = list(map(lambda state: state.particleCount, states))

    roundCount = rounds[0].shape[0]
    
    iterations = np.array(list(map(lambda r: r.shape[1], rounds)))
    totalTime = np.array(list(map(lambda r: r.sum(axis=1), rounds))).flatten()

    frequency = iterations / totalTime

    binSize = 0.0005

    print(f'Rounds: {roundCount}.')
    for i in range(len(frequency)):
        print(f'Freq for N={particleCounts[i]} is: {frequency[i]}. Iterations: {iterations[i]}.')

    fig = plt.figure(figsize=(10, 10))
    ax = fig.add_subplot(1, 1, 1)
    ax.set_yscale('log')
    ax.set_title(f'BinSize: {binSize}. Rounds: {roundCount}.')
    ax.set_xlabel(r'$t_c$: Tiempo entre colision (s)', size=20)
    ax.set_ylabel(r'Probabilidad del intervalo', size=20)

    for i in range(len(particleCounts)):
        hist, bins = np.histogram(rounds[i].flatten(), bins=np.arange(0, np.max(rounds[i]), binSize))
        ax.scatter(
            bins[:-1], 
            hist / rounds[i].size, 
            color=cm.get_cmap('tab10')(i),
            label=f'N={int(particleCounts[i])}. Iter={iterations[i]}'
        )
    plt.legend()
    plt.show()


if __name__ == '__main__':
    if len(sys.argv) < 2:
        raise ValueError('Config path must be given by argument')
    try:
        main(sys.argv[1])
    except KeyboardInterrupt:
        pass
