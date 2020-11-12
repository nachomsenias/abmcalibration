/*
 * Copyright 2016 by Romaric Pighetti, CNRS, I3S
 * Licensed under the Academic Free License version 3.0
 */
package calibration.ecj.multimodal.nsga2mm;

import ec.EvolutionState;
import ec.Individual;
import ec.multiobjective.nsga2.NSGA2Evaluator;
import ec.multiobjective.nsga2.NSGA2MultiObjectiveFitness;
import ec.util.SortComparator;
 
public class NSGA2MM_Evaluator extends NSGA2Evaluator {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int lastCallGen = -1;

    public Individual[] buildArchive(EvolutionState state, int subpop) {

        if (state.generation != lastCallGen) {
            this.lastCallGen = state.generation;
            NSGA2MM_Fitness.setGen(state.generation);
        }

        return super.buildArchive(state, subpop);
    }

    /**
     * Computes and assigns the sparsity values of a given front.
     */
    public void assignSparsity(Individual[] front) {
        int numObjectives = ((NSGA2MultiObjectiveFitness) front[0].fitness)
                .getObjectives().length;

        for (int i = 0; i < front.length; i++)
            ((NSGA2MultiObjectiveFitness) front[i].fitness).sparsity = 0;

        for (int i = 0; i < numObjectives; i++) {
            final int o = i;
            // 1. Sort front by each objective.
            // 2. Sum the manhattan distance of an individual's neighbours over
            // each objective.
            // NOTE: No matter which objectives objective you sort by, the
            // first and last individuals will always be the same (they maybe
            // interchanged though). This is because a Pareto front's
            // objective values are strictly increasing/decreasing.
            ec.util.QuickSort.qsort(front, new SortComparator() {
                public boolean lt(Object a, Object b) {
                    Individual i1 = (Individual) a;
                    Individual i2 = (Individual) b;
                    return (((NSGA2MultiObjectiveFitness) i1.fitness)
                            .getObjective(o) < ((NSGA2MultiObjectiveFitness) i2.fitness)
                            .getObjective(o));
                }

                public boolean gt(Object a, Object b) {
                    Individual i1 = (Individual) a;
                    Individual i2 = (Individual) b;
                    return (((NSGA2MultiObjectiveFitness) i1.fitness)
                            .getObjective(o) > ((NSGA2MultiObjectiveFitness) i2.fitness)
                            .getObjective(o));
                }
            });

            // Compute and assign sparsity.
            // the first and last individuals are the sparsest.
            ((NSGA2MultiObjectiveFitness) front[0].fitness).sparsity = Double.POSITIVE_INFINITY;
            ((NSGA2MultiObjectiveFitness) front[front.length - 1].fitness).sparsity = Double.POSITIVE_INFINITY;
            NSGA2MultiObjectiveFitness frontFirst = (NSGA2MultiObjectiveFitness) front[0].fitness;
            NSGA2MultiObjectiveFitness frontLast = (NSGA2MultiObjectiveFitness) front[front.length - 1].fitness;
            double max = frontLast.getObjective(o);
            double min = frontFirst.getObjective(o);

            for (int j = 1; j < front.length - 1; j++) {
                NSGA2MultiObjectiveFitness f_j = (NSGA2MultiObjectiveFitness) (front[j].fitness);
                NSGA2MultiObjectiveFitness f_jplus1 = (NSGA2MultiObjectiveFitness) (front[j + 1].fitness);
                NSGA2MultiObjectiveFitness f_jminus1 = (NSGA2MultiObjectiveFitness) (front[j - 1].fitness);

                // store the NSGA2Sparsity in sparsity
                f_j.sparsity += (f_jplus1.getObjective(o) - f_jminus1
                        .getObjective(o))
                        / ((max - min) * frontFirst.getNumObjectives());
            }
        }
    }
}
