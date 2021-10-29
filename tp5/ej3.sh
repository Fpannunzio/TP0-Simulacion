#!/bin/bash
echo "Preparando simulacion 🕐️"

CONFIG=config/ej3_config.json
DATA="output/ej3.json"

mvn -f ../ install -DskipTests -q || exit

echo "Creando particulas 🕓️"

echo "Particluas creadas ✅️"

echo "Ejecutando simulacion 🕘️"

# Ejecutamos simulacion
mvn -f tp5_java exec:java -Dexec.mainClass=ar.edu.itba.simulacion.tp5.Ej3 -Dexec.args="$CONFIG" -q || exit

# Renderizamos grafico
python3 tp5_python/ej3.py $DATA || exit