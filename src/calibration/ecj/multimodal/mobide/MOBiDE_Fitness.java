/*
 * Copyright 2016 by Romaric Pighetti, CNRS, I3S
 * Licensed under the Academic Free License version 3.0
 */
package calibration.ecj.multimodal.mobide;

import ec.Fitness;
import ec.multiobjective.MultiObjectiveFitness;

public class MOBiDE_Fitness extends MultiObjectiveFitness {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String MOBIDE_RANK_PREAMBLE = "Rank: ";
	public static final String MOBIDE_HYPERVOLUME_PREAMBLE = "Hypervolume: ";

	public String[] getAuxilliaryFitnessNames() { return new String[] { "Rank", "Hypervolume" }; }
	public double[] getAuxilliaryFitnessValues() { return new double[] { rank, hypervolume }; }

	/** Pareto front rank measure (lower ranks are better) */
	public int rank;

	/** Hypervolume along front rank measure (higher hypervolume is better) */
	public double hypervolume;

    /**
     * MoBiDE implem / individual
     *(!) circular reference between fitness and corresponding individual instantce
     */
	public ec.Individual individual;

	public boolean betterThan(Fitness givenOther) {
        MOBiDE_Fitness other = (MOBiDE_Fitness) givenOther;
        return this.rank < other.rank || (this.rank == other.rank && this.hypervolume > other.hypervolume);
    }

}
