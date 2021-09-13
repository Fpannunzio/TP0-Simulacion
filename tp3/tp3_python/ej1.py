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

    values = np.array(list(map(lambda state: np.array(state.collisionTimes), states)))
    particleCounts = list(map(lambda state: state.particleCount, states))

    rounds = values.shape[1]
    iterations = values.shape[2]

    frequency = iterations / values.sum(axis=2)

    mean = np.mean(frequency, axis = 1)
    std = np.std(frequency, axis = 1)

    binSize = 0.0005

    print(f'Rounds: {rounds}. Iterations: {iterations}')
    for i in range(len(mean)):
        print(f'Mean for N={particleCounts[i]} is: {mean[i]} and std is: {std[i]}')
    # plotter: Plotter = Plotter(config.spaceWidth, config.iterations, states).plot()

    fig = plt.figure(figsize=(10, 10))
    ax = fig.add_subplot(1, 1, 1)
    ax.set_yscale('log')
    ax.set_title(f'BinSize: {binSize}. Rounds: {rounds}. Iterations: {iterations}.')
    ax.set_xlabel(r'$t_c$: Tiempo entre colision (s)', size=20)
    ax.set_ylabel(r'Probabilidad por intervalo', size=20)

    for i in range(len(particleCounts)):
        hist, bins = np.histogram(values[i].flatten(), bins=np.arange(0, np.max(values[i]), binSize))
        ax.scatter(
            bins[:-1], 
            hist / values[i].size, 
            color=cm.get_cmap('tab10')(i),
            label=f'N= {int(particleCounts[i])}'
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
