from typing import List

from matplotlib import pyplot as plt
import matplotlib.cm as cm
import matplotlib.animation as animation

import numpy as np

from models import Particle, SimulationState


class Plotter:

    def __init__(self, space_width: float, iterations: int, states: List[SimulationState]) -> None:
        self.space_width = space_width
        self.iterations = iterations
        self.states: List[SimulationState] = states

        # Plot init

        self.fig = plt.figure(figsize=(10, 10))
        self.ax1 = self.fig.add_subplot(1, 1, 1)
        self.ax1.set_aspect(1)
        self.ax1.set_xlim([0, self.space_width])
        self.ax1.set_ylim([0, self.space_width])

        self.ani: animation.FuncAnimation

    def plot_gen(self, gen):
        state: SimulationState = self.states[gen]
        particles: List[Particle] = state.particles

        state_x: List[float] = list(map(lambda p: p.x, particles))
        state_y: List[float] = list(map(lambda p: p.y, particles))
        state_velocity_x: List[float] = list(map(lambda p: p.velocityX, particles))
        state_velocity_y: List[float] = list(map(lambda p: p.velocityY, particles))
        state_velocity_dir: List[float] = list(map(lambda p: p.velocityDir, particles))

        colors = cm.hsv((np.array(state_velocity_dir) + np.pi) / (np.pi*2))

        self.ax1.clear()

        for particle in particles:
            self.ax1.add_artist(plt.Circle((particle.x, particle.y), particle.radius, alpha=0.5, picker=True))

        self.ax1.quiver(state_x, state_y, state_velocity_x, state_velocity_y, color=colors)
        self.ax1.scatter(state_x, state_y, color=colors)
        self.ax1.set_aspect(1)
        self.ax1.set_xlim([0, self.space_width])
        self.ax1.set_ylim([0, self.space_width])
        self.ax1.set_yticks([])
        self.ax1.set_xticks([])

    def plot(self):
        
        self.ani = animation.FuncAnimation(self.fig, self.plot_gen, interval=1000, frames=len(self.states), repeat=False)
        plt.show()
        # Writer = animation.writers['ffmpeg']
        # writer = Writer(fps=24, bitrate=1800)
        # ani.save('va-density-260.mp4', writer=writer)
