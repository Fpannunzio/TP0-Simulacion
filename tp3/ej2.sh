#!/bin/bash
echo "Preparando simulacion 🕐️"

mvn -f ../ clean install -DskipTests -q || exit

CONF="config/ej2_config.json"
DATA="output/ej2_out.json"

echo "Ejecutando simulacion 🕘️"

# Ejecutamos simulacion
mvn -f tp3_java exec:java -Dexec.mainClass=ar.edu.itba.simulacion.tp3.Ej2 -Dexec.args="$CONF" -q || exit

echo "Simulacion finalizada ✅️"

echo "Renderizando 🚀️"

# Renderizamos grafico
python3 tp3_python/ej2.py $DATA || exit

