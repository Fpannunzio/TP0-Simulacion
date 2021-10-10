from dataclasses import dataclass
import json
import sys
import numpy as np
from matplotlib import pyplot as plt
from typing import Any, Dict, List, Union
from matplotlib import cm



def main(data_path):
    with open(data_path, 'r') as particles_fd:
        rounds: List[List[float]] = json.load(particles_fd)

    results = np.array(list(map(lambda r: np.array(r)[:,0], rounds)))
    t = np.linspace(0, 5, 10_000)
    analytic = np.exp(( -100/(2*70)* t)) * np.cos(((1e4/70) - 100**2/(4 * 70**2))**(0.5) * t)
    
    fig = plt.figure(figsize=(10, 10))
    ax = fig.add_subplot(1, 1, 1)

    labels = ['Verlet', 'Beemam', 'Gear']

    ax.plot(t, analytic, label="Analytic")
    for i in range(len(results)):
        ax.plot(t, results[i], label=labels[i])
        print(f'MSD de {labels[i]}: {np.sum(np.power(analytic - results[i], 2))}')

    plt.legend(fontsize=14)
    plt.show()


if __name__ == '__main__':
    if len(sys.argv) < 2:
        raise ValueError('Config path must be given by argument')
    try:
        main(sys.argv[1])
    except KeyboardInterrupt:
        pass
