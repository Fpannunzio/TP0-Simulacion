import json
import sys
from typing import List
from matplotlib import pyplot as plt
from matplotlib.ticker import AutoMinorLocator

import numpy as np
from formater import MathTextSciFormatter

from models import VelocityPerIterationInfo


def main(data_path):
    with open(data_path, 'r') as result_fd:
        result: VelocityPerIterationInfo = json.load(result_fd, object_hook=VelocityPerIterationInfo.from_dict)

    minutesStep = result.secondsStep / 60
    velocitiesCount = len(result.velocities)
    minutes = np.linspace(0, minutesStep * velocitiesCount, velocitiesCount)

    fig = plt.figure(figsize=(16, 10))
    ax = fig.add_subplot(1, 1, 1)
    ax.xaxis.set_major_formatter(MathTextSciFormatter("%1.0e"))
    # ax.yaxis.set_minor_formatter(MathTextSciFormatter("%1.5e"))
    ax.grid(which="both")
    ax.set_axisbelow(True)
    # ax.yaxis.set_minor_locator(AutoMinorLocator(n = 2))

    ax.set_xlabel(r'Tiempo de viaje (min)', size=20, labelpad=20)
    ax.set_ylabel(r'$v$: Modulo de la velocidad de la nave (km/s)', size=20, labelpad=20)
    ax.tick_params(labelsize=16)
    ax.tick_params(labelsize=12, which='minor')

    ax.plot(minutes, result.velocities)

    plt.show()


if __name__ == '__main__':
    if len(sys.argv) < 2:
        raise ValueError('Config path must be given by argument')
    try:
        main(sys.argv[1])
    except KeyboardInterrupt:
        pass
