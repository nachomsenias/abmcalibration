/*
 * Copyright 2016 by Romaric Pighetti, CNRS, I3S
 * Licensed under the Academic Free License version 3.0
 */
package calibration.ecj.multimodal.mobide;

import ec.Individual;
import ec.util.QuickSort;
import ec.util.SortComparator;

public class Base_HV {
    public static void assignHVolume(Individual[] front) {

        int numObjectives = ((MOBiDE_Fitness) front[0].fitness).getObjectives().length;

        if (numObjectives > 2) {
            throw new RuntimeException(
                    "Cannot compute hypervolume measure with more than 2 objectives");
            // Need to subtract hypercube and the compute the volume...
        } else if (numObjectives == 2) {
            // There are 2 objectives here.

            // Sort against the first objective in increasing order.
            QuickSort.qsort(front, new SortComparator() {

                @Override
                public boolean lt(Object a, Object b) {
                    MOBiDE_Fitness fita = (MOBiDE_Fitness) ((Individual) a).fitness;
                    MOBiDE_Fitness fitb = (MOBiDE_Fitness) ((Individual) b).fitness;
                    return fita.getObjective(0) < fitb.getObjective(0);
                }

                @Override
                public boolean gt(Object a, Object b) {
                    MOBiDE_Fitness fita = (MOBiDE_Fitness) ((Individual) a).fitness;
                    MOBiDE_Fitness fitb = (MOBiDE_Fitness) ((Individual) b).fitness;
                    return fita.getObjective(0) > fitb.getObjective(0);
                }
            });

            ((MOBiDE_Fitness) front[0].fitness).hypervolume = Double.POSITIVE_INFINITY;
            ((MOBiDE_Fitness) front[front.length - 1].fitness).hypervolume = Double.POSITIVE_INFINITY;

            for (int i = 1; i < front.length - 1; i++) {
                MOBiDE_Fitness fita = (MOBiDE_Fitness) ((Individual) front[i - 1]).fitness;
                MOBiDE_Fitness fitb = (MOBiDE_Fitness) ((Individual) front[i]).fitness;
                MOBiDE_Fitness fitc = (MOBiDE_Fitness) ((Individual) front[i + 1]).fitness;
                if (((MOBiDE_Fitness) ((Individual) front[0]).fitness).maximize[0]) {
                    fitb.hypervolume = Math.abs((fitb.getObjective(0) - fita
                            .getObjective(0))
                            * (fitc.getObjective(1) - fitb.getObjective(1)));
                } else {
                    fitb.hypervolume = Math.abs((fitc.getObjective(0) - fitb
                            .getObjective(0))
                            * (fita.getObjective(1) - fitb.getObjective(1)));
                }
            }
        } else {
            throw new RuntimeException(
                    "Cannot compute hypervolume measure with 1 objectives or less !?!");
        }
    }
}
