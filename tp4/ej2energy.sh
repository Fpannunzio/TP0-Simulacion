#!/bin/bash
echo "Preparando simulacion 🕐️"

CONF=config/mars_mission_config_energy.json
DATA=output/ej2_energy.csv

mvn -f ../ install -DskipTests -q || exit

echo "Creando particulas 🕓️"

echo "Particluas creadas ✅️"

echo "Ejecutando simulacion 🕘️"

# Ejecutamos simulacion
mvn -f tp4_java exec:java -Dexec.mainClass=ar.edu.itba.simulacion.tp4.marsMission.SystemEnergyAnalizer -Dexec.args="$CONF" -q || exit

echo "Simulacion finalizada ✅️"
python3 tp4_python/ej2energy.py $DATA || exit