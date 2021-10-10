#!/bin/bash
echo "Preparando simulacion 🕐️"

DATA=output/ej1.json

mvn -f ../ install -DskipTests -q || exit

echo "Creando particulas 🕓️"

echo "Particluas creadas ✅️"

echo "Ejecutando simulacion 🕘️"

# Ejecutamos simulacion
mvn -f tp4_java exec:java -Dexec.mainClass=ar.edu.itba.simulacion.tp4.oscillator.Ej1 -Dexec.args="$2" -q || exit

echo "Simulacion finalizada ✅️"
python3 tp4_python/ej1.py $DATA || exit