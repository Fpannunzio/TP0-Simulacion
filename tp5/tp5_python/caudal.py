from dataclasses import dataclass
import json
import sys
import numpy as np
from models import from_dict
from matplotlib import pyplot as plt
from matplotlib import cm
from typing import Any, Dict, List, Union

from formater import MathTextSciFormatter
           

@dataclass
@from_dict
class RoundSummary:
    dt:                         float           
    escapesByRun:               List[List[int]]

def parse_state(data: Dict[str, Any]) -> RoundSummary:
        return RoundSummary.from_dict(data)

def main(data_path):
    with open(data_path, 'r') as particles_fd:
        rounds: RoundSummary = json.load(particles_fd, object_hook=parse_state)

    times = list(map(lambda e: list(map(lambda p: p.time, e)), rounds.escapesByRun))
    freedParticles = list(map(lambda e: np.fromiter(map(lambda p: p.escapeCount, e), dtype=int), rounds.escapesByRun))


    maxIterations = min(map(lambda p: p.shape[0], freedParticles)) - 1
    freedParticles = np.array(list(map(lambda e: e[:maxIterations], freedParticles)))

    windowSize = 200
    window = np.lib.stride_tricks.sliding_window_view(np.mean(freedParticles, axis=0), windowSize)
    q = np.sum(window, axis=1) / windowSize

    fig = plt.figure(figsize=(16, 10))
    ax = fig.add_subplot(1, 1, 1)


    ax.tick_params(labelsize=16)

    ax.set_xlabel(r'$t$ (s)', size=20)
    ax.set_ylabel(r'n (t) (1/s)', size=20)
 
    ax.scatter(range(len(q)), q, marker='o', color=cm.get_cmap('tab20c')(0))## TODO ponemos la seed?
        # ax.plot(times[i], freedParticles[i], label=f'n(t) iteracion {i}', color=cm.get_cmap('tab20c')(i))
    

    plt.legend(fontsize=14)
    plt.show()


if __name__ == '__main__':
    if len(sys.argv) < 2:
        raise ValueError('Config path must be given by argument')
    try:
        main(sys.argv[1])
    except KeyboardInterrupt:
        pass
