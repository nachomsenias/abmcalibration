#!/bin/bash

# Options:: 
# --repeat=it		Runs "it" executions for each problem.
# --start=i			Start the algorithm executions from "i".
# --single=i		Runs a single execution using the "i-th" iteration seed.
# --algorithm 		ECJ config file (see algorithms folder for ecj files).

for i in 0 5 7 10 12 15 17 20 22 25 30 35
do
	java -jar runCalibration.jar "${i}TP/input_aw_wom_new${i}touchpoints.json" "${i}TP/$1" "--algorithm=../algorithms/$1.ecj" "--repeate=5" > "${i}TP/$1.log"
	echo " --- Finished folder ${i}TP --- "
done
