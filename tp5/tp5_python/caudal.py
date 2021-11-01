from dataclasses import dataclass
import json
import sys
import numpy as np
from models import from_dict
from matplotlib import pyplot as plt
from matplotlib import cm
from typing import Any, Dict, List, Union
           

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

    freed_particles = list(map(lambda e: np.array(e), rounds.escapesByRun))

    W = 100

    q = caudal(freed_particles, rounds.dt, W)

    fig = plt.figure(figsize=(16, 10))
    ax = fig.add_subplot(1, 1, 1)

    ax.tick_params(labelsize=16)

    ax.set_title(f'W ={W}')
    ax.set_xlabel(r'$t$ (s)', size=20)
    ax.set_ylabel(r'caudal(t) ($s^{-1}$)', size=20)
 
    ax.scatter(np.linspace(0, len(q)*rounds.dt, len(q)), q)
    
    ax.grid(which="both")
    ax.set_axisbelow(True)
    plt.show()

def caudal(particles, dt, window_size=200) -> np.ndarray:

    max_iterations = min(map(lambda p: p.shape[0], particles)) - 1
    truncated_particles = np.array(list(map(lambda e: e[:max_iterations], particles)))

    window = np.lib.stride_tricks.sliding_window_view(np.mean(truncated_particles, axis=0), window_size)
    return np.sum(window, axis=1) / window_size / dt


if __name__ == '__main__':
    if len(sys.argv) < 2:
        raise ValueError('Config path must be given by argument')
    try:
        main(sys.argv[1])
    except KeyboardInterrupt:
        pass
