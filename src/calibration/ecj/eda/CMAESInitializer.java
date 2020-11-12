/*
  Copyright 2015 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package calibration.ecj.eda;

import ec.EvolutionState;
import ec.Individual;
import ec.Population;
/* 
 * CMAESInitializer.java
 * 
 * Created: Wed Jul  8 12:35:31 EDT 2015
 * By: Sam McKay and Sean Luke
 */
import ec.simple.SimpleInitializer;

/**
 * CMAESInitializer is a SimpleInitializer which ensures that the subpopulations
 * are all set to the provided or computed lambda values.
 *
 * @author Sam McKay and Sean Luke
 * @version 1.0
 */

public class CMAESInitializer extends SimpleInitializer {
	private static final long serialVersionUID = 1;

	public Population setupPopulation(final EvolutionState state, int thread) {
		Population p = super.setupPopulation(state, thread);

		// reset to lambda in size!
		for (int i = 0; i < p.subpops.length; i++) {
			// Individual[] oldInds = (Individual[])
			// p.subpops[i].individuals.toArray(new Individual[0]);
			Individual[] oldInds = (Individual[]) p.subpops[i].individuals;
			if (p.subpops[i].species instanceof CMAESSpecies) {
				int lambda = (int) (((CMAESSpecies) p.subpops[i].species).lambda);
				if (lambda < oldInds.length) // need to reduce
				{
					Individual[] newInds = new Individual[lambda];
					System.arraycopy(oldInds, 0, newInds, 0, lambda);
					oldInds = newInds;
				} else if (lambda > oldInds.length) // need to increase
				{
					Individual[] newInds = new Individual[lambda];
					System.arraycopy(oldInds, 0, newInds, 0, oldInds.length);
//					for (int j = oldInds.length; j < lambda; j++)
					for (int j = 0; j < lambda; j++)
						newInds[j] = p.subpops[i].species.newIndividual(state,
								thread);
					oldInds = newInds;
				}
			} else
				state.output.fatal("Species of subpopulation " + i
						+ " is not a CMAESSpecies.  It's a " + p.subpops[i].species);
//			p.subpops[i].individuals = new Individual[p.subpops[i].individuals.length];
			p.subpops[i].individuals = new Individual[oldInds.length];
			for (int j = 0; j < oldInds.length; j++)
				p.subpops[i].individuals[j]=oldInds[j]; // yuck, but 1.5
																// doesn't have
																// Arrays.asList
		}

		return p;
	}
}
