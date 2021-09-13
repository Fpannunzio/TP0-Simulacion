#!/bin/bash
echo "Preparando simulacion 🕐️"

mvn -f ../ clean install -DskipTests -q || exit

CONF="config/ej3_example.json"
DATA="output/ej3_out.json"

echo "Ejecutando simulacion 🕘️"

# Ejecutamos simulacion
mvn -f tp3_java exec:java -Dexec.mainClass=ar.edu.itba.simulacion.tp3.Ej3 -Dexec.args="$CONF" -q || exit

echo "Simulacion finalizada ✅️"

echo "Renderizando 🚀️"

# Renderizamos grafico
python3 tp3_python/ej3.py $DATA || exit