/*
 * Copyright 2016 by Romaric Pighetti, CNRS, I3S
 * Licensed under the Academic Free License version 3.0
 */
package calibration.ecj.multimodal.mobide;

import java.util.ArrayList;
import java.util.HashMap;

import ec.EvolutionState;
import ec.Individual;
import ec.Initializer;
import ec.Population;
import ec.Subpopulation;
import ec.multiobjective.MultiObjectiveFitness;
import ec.simple.SimpleEvaluator;
import ec.util.Parameter;
import ec.util.SortComparator;
import ec.vector.DoubleVectorIndividual; 

public class MOBiDE_Evaluator extends SimpleEvaluator {

    private static final long                  serialVersionUID = -649758141313226887L;
    // protected Population archive = null;
    private Archive                            archive          = null;
    private double                             alpha            = 0.1;
    protected HashMap<Integer, MOBiDE_Fitness> gbest            = new HashMap<Integer, MOBiDE_Fitness>();

    public int                                 originalPopSize[];

    public void setup(final EvolutionState state, final Parameter base) {
        super.setup(state, base);
        Parameter p = new Parameter(Initializer.P_POP);
        int subpopsLength = state.parameters.getInt(p.push(Population.P_SIZE),
                null, 1);
        Parameter p_subpop;
        Parameter p_default_subpop = p.push("default-subpop").push("size");
        originalPopSize = new int[subpopsLength];
        for (int i = 0; i < subpopsLength; i++) {
            p_subpop = p.push(Population.P_SUBPOP).push("" + i)
                    .push(Subpopulation.P_SUBPOPSIZE);
            originalPopSize[i] = state.parameters.getInt(p_subpop,
                    p_default_subpop, 1);
        }
    }

    public Archive getArchive() {
        return this.archive;
    }

    public void evaluatePopulation(EvolutionState state) {

        if (archive == null) {
            archive = this.new Archive();
            archive.init(state);
        }

        if (state.breeder instanceof MOBiDE_Breeder) {

            super.evaluatePopulation(state);

            if (state.generation == 0) {
                return;
            }

            // For each subpops, select the individuals from the combined
            // pop that must be part of the new population.
            for (int i = 0; i < state.population.subpops.length; i++) {

                // Set the individuals in the population to the ones we
                // kept. Also updates gbest.
                state.population.subpops[i].individuals = selectIndividuals(
                        state, i);

                // Update and clearing of the archive for the current subpop
                // there.
                archive.updateArchive(state.population, i, state);
                archive.clearArchive(i, state);
            }

        } else {
            state.output
                    .fatal("MOBiDE_Evaluator requires MOBiDE_Breeder to be the breeder.");
        }
    }

    private Individual[] selectIndividuals(EvolutionState state, int subpop) {
        // Do the non-dominated and hypervolume sorting here to
        // create the new population. AssignFrontRanks also updates
        // gbest.
        Individual[] dummy = new Individual[0];

        ArrayList<ArrayList<Individual>> ranks = assignFrontRanks(
                state.population.subpops[subpop], state, subpop);
        int j = 0;
        ArrayList<Individual> newIndividuals = new ArrayList<Individual>();
        // Add as much complete rank as possible
        while (newIndividuals.size() + ranks.get(j).size() < originalPopSize[subpop]) {
            // Not needed, the hypervolume is not used outside of this method.
            // It is needed only for the last front.
            // assignHypervolume(ranks.get(j));
            newIndividuals.addAll(ranks.get(j));
            j++;
        }

        // If the population is not filled but the next rank to add is too big
        // to fit
        if (newIndividuals.size() < originalPopSize[subpop]) {
            Individual[] currentRank = ranks.get(j).toArray(new Individual[0]);

            // Discard elements based on the hypervolume contribution until we
            // get the right number of elements
            while (currentRank.length + newIndividuals.size() > originalPopSize[subpop]) {
                // XXX: if only one individual has to be selected, we have to choose between
                // two infinite hypervolumes. Current implementation relies on the sorting done
                // by the quicksort algorithm. We can make a special case comparing the 2 infinite
                // or keeping the best individual on the multimodal function.
                Base_HV.assignHVolume(currentRank);

                // first sort the rank by hypervolume contribution value
                ec.util.QuickSort.qsort(currentRank, new SortComparator() {
                    public boolean lt(Object a, Object b) {
                        Individual i1 = (Individual) a;
                        Individual i2 = (Individual) b;
                        return (((MOBiDE_Fitness) i1.fitness).hypervolume < ((MOBiDE_Fitness) i2.fitness).hypervolume);
                    }

                    public boolean gt(Object a, Object b) {
                        Individual i1 = (Individual) a;
                        Individual i2 = (Individual) b;
                        return (((MOBiDE_Fitness) i1.fitness).hypervolume > ((MOBiDE_Fitness) i2.fitness).hypervolume);
                    }
                });

                // Discard the individual with the lowest hypervolume
                // contribution
                Individual[] newRank = new Individual[currentRank.length - 1];
                System.arraycopy(currentRank, 1, newRank, 0, newRank.length);
                currentRank = newRank;
            }

            // Add the kept elements to the population
            for (int k = 0; k < currentRank.length; k++) {
                newIndividuals.add(currentRank[k]);
            }
        } else if (newIndividuals.size() != originalPopSize[subpop]) {
            state.output
                    .fatal("MOBiDE_Evaluator: wrong size in newIndividual...");
        }
        return newIndividuals.toArray(dummy);
    }

    private double distance(DoubleVectorIndividual ind,
            DoubleVectorIndividual ind1) {
        double result = 0;
        for (int i = 0; i < ind.genomeLength(); i++) {
            result += Math.pow(ind.genome[i] - ind1.genome[i], 2);
        }
        return Math.sqrt(result);
    }

    /**
     * Divides inds into ranks and assigns each individual's rank to be the rank
     * it was placed into. Each front is an ArrayList.
     */
    private ArrayList<ArrayList<Individual>> assignFrontRanks(
            Subpopulation subpop, EvolutionState state, int subpopIndex) {
        Individual[] inds = subpop.individuals;

        @SuppressWarnings("unchecked")
        ArrayList<ArrayList<Individual>> frontsByRank = MultiObjectiveFitness
                .partitionIntoRanks(inds);

        int numRanks = frontsByRank.size();
        for (int rank = 0; rank < numRanks; rank++) {
            ArrayList<Individual> front = (frontsByRank.get(rank));
            int numInds = front.size();
            for (int ind = 0; ind < numInds; ind++) {
                // Set the rank of each individual
                ((MOBiDE_Fitness) ((front.get(ind)).fitness)).rank = rank;

                // Update gbest, it must be done by the end of the evaluation
                // process. Doing this here prevent us from doing another loop
                // on the population just for this.
                MOBiDE_Fitness fitness = (MOBiDE_Fitness) ((front.get(ind)).fitness);
                updateGbest(state, fitness, subpopIndex);
            }
        }
        return frontsByRank;
    }

    // XXX: Works only for one multimodal objective (2 objectives, multimodal
    // plus diversity).
    private void updateGbest(EvolutionState state, MOBiDE_Fitness fitness,
            int subpopIndex) {
        fitness = (MOBiDE_Fitness) fitness.clone();
        double[] objectives = fitness.getObjectives();
        objectives[objectives.length - 1] = 0;
        if (gbest.get(subpopIndex) == null
                || fitness.paretoDominates(gbest.get(subpopIndex))) {
            gbest.put(subpopIndex, fitness);
        }
    }

    public class Archive {
        private double     delta   = 0.0;
        private Population archive = null;

        private Archive() {
        }

        private void init(EvolutionState state) {
            delta = state.parameters.getDouble(new Parameter(
                    "eval.archive.delta"), null);
            archive = (Population) state.population.emptyClone();
            for (int i = 0; i < archive.subpops.length; i++) {
                archive.subpops[i].individuals = new Individual[0];
            }
        }

        // XXX: Works only for one multimodal objective (2 objectives,
        // multimodal plus diversity).
        private void clearArchive(int popIndex, EvolutionState state) {
            Individual[] inds = archive.subpops[popIndex].individuals;

            // Compute the mean of fitnesses in the archive.
            MOBiDE_Fitness avgFitness = (MOBiDE_Fitness) inds[0].fitness
                    .clone();
            double[] objectives = avgFitness.getObjectives();
            objectives[objectives.length - 1] = 0;
            for (int i = 1; i < inds.length; i++) {
            	MOBiDE_Fitness fit = (MOBiDE_Fitness) inds[i].fitness;
                for (int j = 0; j < objectives.length - 1; j++) {
                    objectives[j] += fit.getObjective(j);
                }
            }

            for (int j = 0; j < objectives.length; j++) {
                objectives[j] /= inds.length;
            }
            // No need to call setObjectives, the reference to the array hasn't
            // changed.
            // avgFitness.setObjectives(state, objectives);

            // Remove all elements that have a fitness worse that the average.
            for (int i = 0; i < inds.length; i++) {
                MultiObjectiveFitness currentFitness = (MultiObjectiveFitness) inds[i].fitness
                        .clone();
                double[] currentObjectives = currentFitness.getObjectives();
                currentObjectives[currentObjectives.length - 1] = 0;
                if (avgFitness.paretoDominates(currentFitness)
                        && Math.abs(currentFitness.getObjective(0)
                                - gbest.get(popIndex).getObjective(0)) > 0.001) {
                    Individual[] newInds = new Individual[inds.length - 1];
                    inds[i] = inds[inds.length - 1];
                    System.arraycopy(inds, 0, newInds, 0, newInds.length);
                    inds = newInds;
                    // We don't want to skip an element, as we just removed one
                    // we need to do this.
                    i--;
                }
            }
            // Put the individuals array back into the archive population.
            // The reference changed since we created new smaller arrays.
            archive.subpops[popIndex].individuals = inds;
        }

        // XXX: Works only for one multimodal objective (2 objectives,
        // multimodal plus diversity).
        private void updateArchive(Population pop, int popIndex,
                EvolutionState state) {
            // Prepare the best fitness using the alpha factor
        	MOBiDE_Fitness preparedBest = (MOBiDE_Fitness) gbest.get(popIndex)
                    .clone();
            double[] bestObjectives = preparedBest.getObjectives();
            for (int i = 0; i < bestObjectives.length - 1; i++) {
                if (bestObjectives[i] > 0.001) {
                    // (1 + alpha) for minimization, (1 - alpha) for
                    // maximization
                    bestObjectives[i] = bestObjectives[i]
                            * (1 + (preparedBest.maximize[i] ? -1 : 1) * alpha);
                } else if (bestObjectives[i] < -0.001) {
                    // (1 - alpha) for minimization, (1 + alpha) for
                    // maximization
                    bestObjectives[i] = bestObjectives[i]
                            * (1 - (preparedBest.maximize[i] ? -1 : 1) * alpha);
                } else {
                    if (preparedBest.maximize[i]) {
                        bestObjectives[i] = -0.001;
                    } else {
                        bestObjectives[i] = 0.001;
                    }
                }
            }
            // gbest has already its last objective set to 0.
            // No need to call setObjectives, no new array is created
            // we altered the existing one.

            // Process all combined inds to see if it can be added.
            Individual[] individuals = pop.subpops[popIndex].individuals;

            // Sort so that we study the best fitness first. If several
            // individuals are within a delta radius, the best one is added
            // and other are discarded. Not in the paper and not bringing
            // much...
            // QuickSort.qsort(individuals, new SortComparator() {
            //
            // @Override
            // public boolean lt(Object a, Object b) {
            // MObiDE_Fitness fita = (MObiDE_Fitness) ((Individual) a).fitness;
            // MObiDE_Fitness fitb = (MObiDE_Fitness) ((Individual) b).fitness;
            // return fita.getObjective(0) > fitb.getObjective(0);
            // }
            //
            // @Override
            // public boolean gt(Object a, Object b) {
            // MObiDE_Fitness fita = (MObiDE_Fitness) ((Individual) a).fitness;
            // MObiDE_Fitness fitb = (MObiDE_Fitness) ((Individual) b).fitness;
            // return fita.getObjective(0) < fitb.getObjective(0);
            // }
            // });

            ArrayList<Individual> newIndividuals = new ArrayList<Individual>();

            for (int i = 0; i < individuals.length; i++) {
            	MOBiDE_Fitness fit = (MOBiDE_Fitness) individuals[i].fitness
                        .clone();
                double[] objectives = fit.getObjectives();
                objectives[objectives.length - 1] = 0;
                // Don't need to call setObjectives, ref not changed.

                // Check if the fitness is good enough
                if (fit.paretoDominates(preparedBest)) {
                    // If it is, test if it's not too close from another ind in
                    // the archive.
                    boolean canBeAdded = true;
                    for (int j = 0; j < newIndividuals.size(); j++) {
                        if (distance(
                                (DoubleVectorIndividual) newIndividuals.get(j),
                                (DoubleVectorIndividual) individuals[i]) < delta) {
                            canBeAdded = false;
                            break;
                        }
                    }
                    if (!canBeAdded) {
                        continue;
                    }

                    // This test should always pass due to the way individuals
                    // are generated.
                    if (!this.contains(individuals[i], popIndex)) {
                        newIndividuals.add((Individual) individuals[i].clone());
                    }
                }
            }

            // Add individuals to the archive population
            Individual[] archiveInds = archive.subpops[popIndex].individuals;
            Individual[] newSubpop = new Individual[archiveInds.length
                    + newIndividuals.size()];
            System.arraycopy(archiveInds, 0, newSubpop, 0, archiveInds.length);
            for (int i = 0; i < newIndividuals.size(); i++) {
                newSubpop[i + archiveInds.length] = newIndividuals.get(i);
            }
            archive.subpops[popIndex].individuals = newSubpop;
        }

        // returns true if L2 dist < delta for an individual in the archive
        public boolean contains(Individual child, int subpop) {
            Individual[] archiveInds = archive.subpops[subpop].individuals;
            if (archiveInds.length == 0) {
                return false;
            } else {
                double[] genome1 = ((DoubleVectorIndividual) child).genome;
                for (int i = 0; i < archiveInds.length; i++) {
                    double[] genome2 = ((DoubleVectorIndividual) archiveInds[i]).genome;
                    double dist = 0;
                    for (int j = 0; j < genome1.length; j++) {
                        dist += Math.pow(genome2[j] - genome1[j], 2);
                    }
                    if (Math.sqrt(dist) < delta) {
                        return true;
                    }
                }
            }
            return false;
        }

        public Individual[] getIndividuals(int subpop) {
            return archive.subpops[subpop].individuals.clone();
        }
    }
}
