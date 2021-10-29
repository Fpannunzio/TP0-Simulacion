import json
import sys
from dataclasses import dataclass
from typing import Any, Dict, List, Union

import numpy as np
from matplotlib import cm
from matplotlib import pyplot as plt
from matplotlib.ticker import AutoMinorLocator, AutoLocator


from models import from_dict
from caudal import caudal

@dataclass
@from_dict
class Conf:
    doorDistance:               float 
    particleCount:              int


@dataclass
@from_dict
class Round:
    dt:                         float          
    distanceParticle:           Conf
    escapesByRun:               List[List[int]]       

@dataclass
@from_dict
class RoundSummary:          
    rounds:                     List[Round]

def parse_state(data: Dict[str, Any]) -> Union[RoundSummary, Round, Conf]:
    if 'rounds' in data:
        return RoundSummary.from_dict(data)
    if 'doorDistance' in data:
        return Conf.from_dict(data)
    return Round.from_dict(data)

def main(data_path):
    with open(data_path, 'r') as particles_fd:
        round_summary: RoundSummary = json.load(particles_fd, object_hook=parse_state)

    window_size     = 200
    stable_q_start  = 250
    stable_q_end    = 1250


    rounds = list(map(lambda round: list(map(lambda sim: np.array(sim), round.escapesByRun)), round_summary.rounds))

    round_q = np.array(list(map(lambda r: mean_and_std(caudal(r, window_size)[stable_q_start:stable_q_end]), rounds)))

    fig = plt.figure(figsize=(16, 10))
    ax = fig.add_subplot(1, 1, 1)

    ax.tick_params(labelsize=16)

    ax.set_xlabel(r'$d$: tamaño de la puerta (m)', size=20)
    ax.set_ylabel(r'caudal medio (1/s)', size=20)
 
    d = list(map(lambda r: r.distanceParticle.doorDistance, round_summary.rounds))

    ax.errorbar(d, round_q[:,0], yerr=round_q[:,1], capsize=2)
    ax.grid(which="both")
    ax.xaxis.set_ticks(d)
    ax.xaxis.set_minor_locator(AutoMinorLocator(n = 2))
    ax.set_axisbelow(True)
    plt.show()

def mean_and_std(a) -> np.ndarray:

    return np.array((np.mean(a), np.std(a)))

if __name__ == '__main__':
    if len(sys.argv) < 2:
        raise ValueError('Config path must be given by argument')
    try:
        main(sys.argv[1])
    except KeyboardInterrupt:
        pass
