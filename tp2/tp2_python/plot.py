from typing import List

from matplotlib import pyplot as plt
import matplotlib.cm as cm
import matplotlib.animation as animation

import numpy as np
import math
from numpy.core.fromnumeric import size

from models import Particle


class Plotter:

    def __init__(self, space_width: float, action_radius: float, periodic_border: bool, states: List[List[Particle]]) -> None:
        self.space_width = space_width
        self.action_radius = action_radius
        self.periodic_border = periodic_border
        self.states: List[List[Particle]] = states

        # Plot init

        self.fig = plt.figure(figsize=(10, 10))
        self.ax1 = self.fig.add_subplot(1, 1, 1)
        self.ax1.set_aspect(1)
        self.ax1.set_xlim([0, self.space_width])
        self.ax1.set_ylim([0, self.space_width])

    def plot_gen(self, gen):
        # Graficamos el estado inicial como test
        first_state: List[Particle] = self.states[gen]
        first_state_x: List[float] = list(map(lambda p: p.x, first_state))
        first_state_y: List[float] = list(map(lambda p: p.y, first_state))
        first_state_velocity_mod: List[float] = list(map(lambda p: p.velocityMod, first_state))
        first_state_velocity_dir: List[float] = list(map(lambda p: p.velocityDir, first_state))
        
        # NO estoy seguro si cos y sin van asi o al reves
        first_state_u: List[float] = np.cos(first_state_velocity_dir) * first_state_velocity_mod
        first_state_v: List[float] = np.sin(first_state_velocity_dir) * first_state_velocity_mod

        colors = cm.hsv((np.array(first_state_velocity_dir) + np.pi) / (np.pi*2))

        # plt.scatter(first_state_x, first_state_y, s=self.space_width / 100, color='blue', alpha=0.5)

        self.ax1.clear()
        self.ax1.quiver(first_state_x, first_state_y, first_state_u, first_state_v, color=colors)
        self.ax1.scatter(first_state_x, first_state_y, color=colors)
        self.ax1.set_aspect(1)
        self.ax1.set_xlim([0, self.space_width])
        self.ax1.set_ylim([0, self.space_width])
        self.ax1.set_yticks([])
        self.ax1.set_xticks([])

    def plot(self):
        
        ani = animation.FuncAnimation(self.fig, self.plot_gen, interval=50, frames=len(self.states), repeat=False)
        plt.show()
        # Writer = animation.writers['ffmpeg']
        # writer = Writer(fps=15, metadata=dict(artist='Me'), bitrate=1800)
        # ani.save('pepe.mp4', writer=writer)

        return ani
