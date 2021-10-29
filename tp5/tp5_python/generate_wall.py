import numpy as np
from numpy.core.fromnumeric import repeat
import sys

# START CONFIG

count = 256
startx = 0
starty = 0
L = 20
d = 1.2
wallR = 0.05
filePath = sys.argv[1] if len(sys.argv) > 1 else "door.exyz"

farAwayTargetY = -10

# END CONFIG

endx = startx + L
endy = starty + L

points = np.linspace(0, L, count)

leftWall    = np.array((np.repeat(startx, count), points + starty)).T
rightWall   = np.array((np.repeat(endx, count), points + starty)).T
topWall     = np.array((points + startx, np.repeat(endy, count))).T

leftBottomWall  = np.linspace(0, startx + L/2 - d/2, count//2)
rightBottomWall = np.linspace(startx + L/2 + d/2, L, count//2)

bottomWall  = np.array((np.hstack((leftBottomWall, rightBottomWall)), np.repeat(starty, count))).T

farAwayTarget = np.array((np.linspace(startx + L/2 - d/2, startx + L/2 + d/2, count//2), np.repeat(farAwayTargetY, count//2))).T

allWalls = np.vstack((leftWall, rightWall, topWall, bottomWall))

f = open(filePath, "w")

f.write(f'{allWalls.shape[0] + farAwayTarget.shape[0]}\n')
f.write('\n')

for p in allWalls:
    # Type, X, Y, Vx, Vy, m, r
    f.write(f'WALL {p[0]} {p[1]} 0 0 0 {wallR}\n')

for p in farAwayTarget:
    # Type, X, Y, Vx, Vy, m, r
    f.write(f'TARGET {p[0]} {p[1]} 0 0 0 {wallR}\n')

f.close()