/*
 * Copyright 2016 by Romaric Pighetti, CNRS, I3S
 * Licensed under the Academic Free License version 3.0
 */
package calibration.ecj.multimodal.mobide;

import calibration.ecj.multimodal.mobide.MOBiDE_Evaluator.Archive;
import ec.EvolutionState;
import ec.Individual;
import ec.Population;
import ec.Subpopulation;
import ec.de.DEBreeder;
import ec.util.Parameter;

public class MOBiDE_Breeder extends DEBreeder {

    private static final long serialVersionUID = -8077408704941778043L;

    public void setup(final EvolutionState state, final Parameter base) {
        super.setup(state, base);
    }

    public Population breedPopulation(EvolutionState state) {
        Population oldPop = (Population) state.population;
        Population newPop = this.createChildren(state);

        for (int i = 0; i < oldPop.subpops.length; i++) {
            Subpopulation oldSubpop = oldPop.subpops[i];
            Subpopulation newSubpop = newPop.subpops[i];
            Individual[] combinedInds = new Individual[oldSubpop.individuals.length
                    + newSubpop.individuals.length];
            System.arraycopy(newSubpop.individuals, 0, combinedInds, 0,
                    newSubpop.individuals.length);
            System.arraycopy(oldSubpop.individuals, 0, combinedInds,
                    newSubpop.individuals.length, oldSubpop.individuals.length);
            newSubpop.individuals = combinedInds;
        }
        return newPop;
    }

    public Population createChildren(EvolutionState state) {
        if (!(state.evaluator instanceof MOBiDE_Evaluator)) {
            state.output
                    .fatal("MOBiDE_Breeder requires MOBiDE_Evaluator to be the evaluator.");
        }
        Archive archive = ((MOBiDE_Evaluator) state.evaluator).getArchive();

        // prepare the breeder (some global statistics might need to be computed
        // here)
        prepareDEBreeder(state);

        // create the new population
        Population newpop = (Population) state.population.emptyClone();

        // breed the children
        for (int subpop = 0; subpop < state.population.subpops.length; subpop++) {
            if (state.population.subpops[subpop].individuals.length < 4) {

                state.output
                        .fatal("Subpopulation "
                                + subpop
                                + " has fewer than four individuals, and so cannot be used with DEBreeder.");
            }
            Individual[] inds = newpop.subpops[subpop].individuals;
            for (int i = 0; i < inds.length; i++) {
                Individual child = createIndividual(state, subpop, i, 0);

                // Generate a new child until we get one that is not in the
                // archive
                while (archive.contains(child, subpop)) {
                    child = createIndividual(state, subpop, i, 0);
                }

                newpop.subpops[subpop].individuals[i] = child; // unthreaded for
                                                               // now
            }
        }

        // store the current population for competition with the new children
        // It will be used during the evaluation process.
        previousPopulation = state.population;
        return newpop;
    }
}
