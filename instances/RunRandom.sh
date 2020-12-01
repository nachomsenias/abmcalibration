#!/bin/bash
#$ -N TP25_HC_5IT
#$ -e 25TP_HC_5IT_err.out
#$ -cwd
#$ -q 12H
#$ -l 12H
#$ -M imoya@ugr.es
#$ -m bea
#$ -pe shm 16
module load alhambra/java-1.8.0_144
java -jar RandomEvaluationsCRO.jar "25TP/input_aw_wom_new25touchpoints.json" "25TP/Random" "--start=5" "--repeat=5"
