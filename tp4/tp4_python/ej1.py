from dataclasses import dataclass
import json
import sys
import numpy as np
from matplotlib import pyplot as plt
from typing import Any, Dict, List, Union
from matplotlib import cm
from formater import MathTextSciFormatter



def main(data_path):
    with open(data_path, 'r') as particles_fd:
        rounds: List[List[float]] = json.load(particles_fd)

    results = np.array(list(map(lambda r: np.array(r)[:,0], rounds)))
    t = np.linspace(0, 5, len(results[0]))
    analytic = np.exp(( -100/(2*70)* t)) * np.cos(((1e4/70) - 100**2/(4 * 70**2))**(0.5) * t)
    
    fig = plt.figure(figsize=(10, 10))
    ax = fig.add_subplot(1, 1, 1)

    ax.xaxis.set_major_formatter(MathTextSciFormatter("%1.3e"))
    ax.yaxis.set_major_formatter(MathTextSciFormatter("%1.4e"))
    ax.tick_params(labelsize=16)

    ax.set_xlabel(r'$t$ (s)', size=20)
    ax.set_ylabel(r'r (m)', size=20)

    labels = ['Verlet', 'Beemam', 'Gear O(5)']

    for i in range(len(results)):
        ax.plot(t, results[i], label=labels[i])
        print(f'MSD de {labels[i]}: {(np.square(analytic - results[i])).mean()} ')

    ax.plot(t, analytic, label="Analítica")
    plt.legend(fontsize=14)
    plt.show()


if __name__ == '__main__':
    if len(sys.argv) < 2:
        raise ValueError('Config path must be given by argument')
    try:
        main(sys.argv[1])
    except KeyboardInterrupt:
        pass
