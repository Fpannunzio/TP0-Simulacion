import json
import math
import sys
from dataclasses import dataclass
from typing import Any, Dict, List, Union

import numpy as np
from matplotlib import cm, markers
from matplotlib import pyplot as plt
from matplotlib.ticker import AutoMinorLocator, AutoLocator

from models import from_dict
from caudal import caudal
from ej3 import mean_and_std


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
    d_vals = np.array(list(map(lambda r: r.distanceParticle.doorDistance, round_summary.rounds)))

    ################################################
    # Plot ajustes
    ################################################
    

    b, errors = lineal_fitting(round_q[:,0], d_vals)

    fig = plt.figure(figsize=(16, 10))
    ax = fig.add_subplot(1, 1, 1)

    ax.tick_params(labelsize=16)
    ax.set_xlabel(r'$B (s^{-1}m^{-3/2})$', size=20)
    ax.set_ylabel(r'Error Cuadratico Medio ($s^{-2}$)', size=20)
    ax.set_yscale('log')

    ax.plot(b, errors)
    print(f'Error: {errors[np.argmin(errors)]}. B: {b[np.argmin(errors)]}')
    ax.grid(which="both")

    ax.set_axisbelow(True)
    plt.show()
    
    
    # fig = plt.figure('Todos', figsize=(16, 10))
    # ax = fig.add_subplot(1, 1, 1)

    # ax.tick_params(labelsize=16)

    # ax.set_xlabel(r'$d (m)$', size=20)
    # ax.set_ylabel(r'$B (s^{-1}m^{-3/2})$', size=20)

    # ax.plot(d_vals, best_b, 'x:', ms=10)
    # ax.xaxis.set_ticks(d_vals)
    # ax.xaxis.set_minor_locator(AutoMinorLocator(n = 2))
    # ax.grid(which="both")

    # ax.set_axisbelow(True)
    



def lineal_fitting(mean_qs, d, start=0, end=0.2, count=10_000):

    b = np.linspace(start, end, count)
    errors = np.sum((b.reshape((b.size, 1)) * d**1.5 - mean_qs) ** 2, axis=1) / d.size

    return (b, errors)


if __name__ == '__main__':
    if len(sys.argv) < 2:
        raise ValueError('Config path must be given by argument')
    try:
        main(sys.argv[1])
    except KeyboardInterrupt:
        pass
