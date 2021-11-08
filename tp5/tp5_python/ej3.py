import json
import sys
from dataclasses import dataclass
from typing import Any, Dict, List, Union

import numpy as np
from matplotlib import cm
from matplotlib import pyplot as plt
from matplotlib.ticker import AutoMinorLocator, AutoLocator
from numpy.core.function_base import linspace


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
    stable_q_start  = int(10    / round_summary.rounds[0].dt)
    stable_q_end    = int(45    / round_summary.rounds[0].dt)

    rounds = list(map(lambda round: list(map(lambda sim: np.array(sim), round.escapesByRun)), round_summary.rounds))
    
    q = list(map(lambda r: caudal(r, round_summary.rounds[0].dt, window_size), rounds))
    
    round_q = np.array(list(map(lambda r: mean_and_std(caudal(r, round_summary.rounds[0].dt, window_size)[stable_q_start:stable_q_end]), rounds)))
    d = np.array(list(map(lambda r: r.distanceParticle.doorDistance, round_summary.rounds)))

    b, errors = lineal_fitting(round_q[:,0], d)



    # Caudal
    plot_q(round_summary.rounds[0].dt ,q, d, stable_q_start, stable_q_end)

    # Ej 3
    plot_q_and_fitted_line(d, round_q, b[np.argmin(errors)])

    # Ej 4
    plot_error_fitting(b, errors)

    
    plt.show()

def plot_q(dt, q, d_list, start, end):
    
    fig = plt.figure(figsize=(16, 10))
    ax = fig.add_subplot(1, 1, 1)

    ax.tick_params(labelsize=16)
    ax.set_xlabel(r'$t$ (s)', size=24)
    ax.set_ylabel(r'$Q(t)$: Caudal ($s^{-1}$)', size=24)

    for qd, d in zip(q, d_list):
        t = np.linspace(0, len(qd)*dt, len(qd))
        ax.scatter(t, qd, label=f'd={d}m')

    ax.grid(which="both")
    ax.legend(fontsize=20)
    ax.set_axisbelow(True)

def plot_error_fitting(b, errors):
    
    fig = plt.figure(figsize=(16, 10))
    ax = fig.add_subplot(1, 1, 1)

    ax.tick_params(labelsize=16)
    ax.set_xlabel(r'$B (s^{-1}m^{-3/2})$', size=24)
    ax.set_ylabel(r'Error Cuadratico Medio ($s^{-2}$)', size=24)
    # ax.set_yscale('log')

    ax.plot(b, errors)
    ax.plot(b[np.argmin(errors)], errors[np.argmin(errors)], 'xr', markersize=20)
    print(f'Error: {errors[np.argmin(errors)]}. B: {b[np.argmin(errors)]}')
    ax.grid(which="both")

    ax.set_axisbelow(True)

def plot_q_and_fitted_line(d, round_q, b):
    fig = plt.figure(figsize=(16, 10))
    ax = fig.add_subplot(1, 1, 1)
    ax.tick_params(labelsize=16)
    ax.set_xlabel(r'$d$: Tamaño de la puerta (m)', size=24)
    ax.set_ylabel(r'$<Q_d>$: Caudal medio ($s^{-1}$)', size=24)
 
    # ax.errorbar(d, round_q[:,0], yerr=round_q[:,1], capsize=2, label=r'<Q_d>')
    ax.errorbar(d, round_q[:,0], 
        fmt='x:', 
        yerr=round_q[:,1], 
        capsize=2, 
        label=r'<Q_d>',
        markersize=20
        )
    
    continuous_d = np.linspace(d.min(), d.max(), 1_000)
    ax.plot(continuous_d, b*continuous_d**1.5, label=r'$Q(d) = Bd^{3/2}$')

    ax.grid(which="both")
    ax.xaxis.set_ticks(d)
    ax.xaxis.set_minor_locator(AutoMinorLocator(n = 2))
    ax.legend(fontsize=20)
    ax.set_axisbelow(True)

def mean_and_std(a) -> np.ndarray:

    return np.array((np.mean(a), np.std(a)))

def lineal_fitting(mean_qs, d, start=1, end=2, count=100_000):

    b = np.linspace(start, end, count)
    errors = np.sum((b.reshape((b.size, 1)) * d**1.5 - mean_qs) ** 2, axis=1) / d.size

    return (b, errors)

def mean_n(times):

    iterations = len(times)
    meanStd = np.empty((iterations, 2))
    
    for i in range(iterations):
        meanStd[i][0] = np.mean(times[i])
        meanStd[i][1] = np.std(times[i])

    return meanStd

if __name__ == '__main__':
    if len(sys.argv) < 2:
        raise ValueError('Config path must be given by argument')
    try:
        main(sys.argv[1])
    except KeyboardInterrupt:
        pass
