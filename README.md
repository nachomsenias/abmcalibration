# abmcalibration
Code for research in ABM calibration and validation.

This project contains Java implementations of popular evolutionary algorithms such as genetic algorithms and differential evolution. Our code is based in the ECJ Java library and we include novel additions like Coral Reefs Optimization (CRO) and Coral Reefs Optimization with Substrate Layers (CRO-SL) algorithms.

ECJ code for the algorithms is located at common/algorithms and <i>instances</i> folder contains several problem scenarios with increased dimensionality.

The main class running calibration is [ExecuteCalibration](ziocommon/src/main/java/es/ugr/sci2s/soccer/util/ExecuteCalibration.java). An example of how to run this class would be the following:

java -jar runableJarFromExecuteCalibration.jar <some json from instances like [this](instances/0TP/fast/input_aw_wom_fast.json)> <folder for dumping results> "--algorithm=pathToSomeECJFile.ecj" "--repeate=number-of-runs"
  
[RunExpermients.sh](common/instances/RunExperiments.sh) contains a shell script example for running the code.
