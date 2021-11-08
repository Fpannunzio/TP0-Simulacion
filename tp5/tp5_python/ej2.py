from dataclasses import dataclass
import json
import sys
import numpy as np
from numpy.core.function_base import linspace
from numpy.lib.function_base import gradient
from models import from_dict
from matplotlib import pyplot as plt
from matplotlib import cm
from typing import Any, Dict, List, Union


@dataclass
@from_dict
class RoundSummary:         
    timesByParticle:               List[List[float]]

def parse_state(data: Dict[str, Any]) -> RoundSummary:
    
    return RoundSummary.from_dict(data)

def main(data_path):
    with open(data_path, 'r') as particles_fd:
        rounds: RoundSummary = json.load(particles_fd, object_hook=parse_state)

    times = list(map(lambda e: list(map(lambda p: p, e)), rounds.timesByParticle))
    particleCount = len(times)
    meanStd = calcMeanStd(times)

    window_size = 7
    dt = 0.035
    
    end_t = meanStd[-1,0]
    t = np.linspace(0, end_t, np.int64(end_t/dt))

    fig2 = plt.figure(2, figsize=(16, 10))
    k_udal = caudal(meanStd[:,0], dt, window_size)

    # fig2.add_subplot(1, 1, 1).scatter(t[:len(k_udal)], k_udal)
    fig2.add_subplot(1, 1, 1).scatter(meanStd[:len(k_udal), 0], k_udal)

    fig = plt.figure(figsize=(16, 10))
    ax = fig.add_subplot(1, 1, 1)

    ax.tick_params(labelsize=16)

    ax.set_xlabel(r'$t$ (s)', size=24)
    ax.set_ylabel(r'n(t): descarga', size=24)

    ax.errorbar(
        meanStd[:,0]
        , range(particleCount)
        , xerr=meanStd[:,1]
        , color=cm.get_cmap('tab10')(0)
        , capsize=2
    )
    
    ax.grid(which="both")
    ax.set_axisbelow(True)
    plt.show()

def calcMeanStd(times):

    iterations = len(times)
    meanStd = np.empty((iterations, 2))
    
    for i in range(iterations):
        meanStd[i][0] = np.mean(times[i])
        meanStd[i][1] = np.std(times[i])

    return meanStd

def caudal(n, dt, w_size=10):

    # N fijo
    q = w_size / (n[w_size:] - n[:-w_size])

    # end_t = n[-1]

    # t = np.linspace(0, end_t, np.int64(end_t/dt))

    # window = np.empty((len(t) - w_size, 2))

    # window[:, 0] = t[:-w_size]
    # window[:, 1] = t[w_size:]

    # # window = [t, t][:,1] + w_size * dt
    
    # q = np.empty(len(window))

    # for w, i in zip(window, range(len(window))):
    #     q[i] = (np.size(np.where((n >= w[0]) & (n < w[-1]))) / w_size / dt)

    return q

if __name__ == '__main__':
    if len(sys.argv) < 2:
        raise ValueError('Config path must be given by argument')
    try:
        main(sys.argv[1])
    except KeyboardInterrupt:
        pass
