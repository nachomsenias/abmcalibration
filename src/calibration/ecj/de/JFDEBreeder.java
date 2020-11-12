package calibration.ecj.de;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import ec.BreedingPipeline;
import ec.EvolutionState;
import ec.Individual;
import ec.Initializer;
import ec.Population;
import ec.SelectionMethod;
import ec.steadystate.SteadyStateBSourceForm;
import ec.steadystate.SteadyStateBreeder;
import ec.steadystate.SteadyStateDefaults;
import ec.steadystate.SteadyStateEvolutionState;
import ec.util.Parameter;
import ec.vector.DoubleVectorIndividual;

/**
 * DEBreeder extension using JFRobles custom implementation.
 * 
 * @author imoya
 */

public class JFDEBreeder extends SteadyStateBreeder {
	/**
	 * Generated.
	 */
	private static final long serialVersionUID = 6704226675829490862L;

	public static final int JF_TOURNAMENT_SIZE = 6;
	public static final double CR_UNSPECIFIED = -1;

	/**
	 * If st.firstTimeAround, this acts exactly like SimpleBreeder. Else, it
	 * only breeds one new individual per subpopulation, to place in position 0
	 * of the subpopulation.
	 */
	BreedingPipeline[] bp;

	public static final String P_DESELECTOR = "deselector";
	// public static final String P_RETRIES = "duplicate-retries";

	/** Loaded during the first iteration of breedPopulation */
	public SelectionMethod deselectors[];

	/** Scaling factor for mutation */
	public double F = 0.0;
	/** Probability of crossover per gene */
	public double Cr = CR_UNSPECIFIED;

	public int retries = 0;

	public static final String P_F = "f";
	public static final String P_Cr = "cr";
	public static final String P_OUT_OF_BOUNDS_RETRIES = "out-of-bounds-retries";

	public JFDEBreeder() {
		bp = null;
		deselectors = null;
	}

	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);

		if (!clonePipelineAndPopulation)
			state.output.fatal(
					"clonePipelineAndPopulation must be true for SteadyStateBreeder -- we'll use only one Pipeline anyway.");

		Parameter p = new Parameter(Initializer.P_POP).push(Population.P_SIZE);
		int size = state.parameters.getInt(p, null, 1);

		// if size is wrong, we'll let Population complain about it -- for us,
		// we'll just make 0-sized arrays and drop out.
		if (size > 0)
			deselectors = new SelectionMethod[size];

		// load the deselectors
		for (int x = 0; x < deselectors.length; x++) {
			deselectors[x] = (SelectionMethod) (state.parameters.getInstanceForParameter(
					SteadyStateDefaults.base().push(P_DESELECTOR).push(String.valueOf(x)), null,
					SelectionMethod.class));
			if (!(deselectors[x] instanceof SteadyStateBSourceForm))
				state.output.error("Deselector for subpopulation " + x
						+ " is not of SteadyStateBSourceForm.");
			deselectors[x].setup(state,
					SteadyStateDefaults.base().push(P_DESELECTOR).push("" + x));
		}
		state.output.exitIfErrors();

		if (sequentialBreeding) // uh oh
			state.output.fatal(
					"SteadyStateBreeder does not support sequential evaluation.",
					base.push(P_SEQUENTIAL_BREEDING));

		if (!state.parameters.exists(base.push(P_Cr), null)) // it wasn't
																// specified --
																// hope we know
																// what we're
																// doing
			Cr = CR_UNSPECIFIED;
		else {
			Cr = state.parameters.getDouble(base.push(P_Cr), null, 0.0);
			if (Cr < 0.0 || Cr > 1.0)
				state.output.fatal(
						"Parameter not found, or its value is outside of [0.0,1.0].",
						base.push(P_Cr), null);
		}

		F = state.parameters.getDouble(base.push(P_F), null, 0.0);
		if (F < 0.0 || F > 1.0)
			state.output.fatal(
					"Parameter not found, or its value is outside of [0.0,1.0].",
					base.push(P_F), null);

		retries = state.parameters.getInt(base.push(P_OUT_OF_BOUNDS_RETRIES), null, 0);
		if (retries < 0)
			state.output.fatal(" Retries must be a value >= 0.0.",
					base.push(P_OUT_OF_BOUNDS_RETRIES), null);

		// How often do we retry if we find a duplicate?
		/*
		 * numDuplicateRetries = state.parameters.getInt(
		 * SteadyStateDefaults.base().push(P_RETRIES),null,0); if
		 * (numDuplicateRetries < 0) state.output.fatal(
		 * "The number of retries for duplicates must be an integer >= 0.\n",
		 * base.push(P_RETRIES),null);
		 */
	}

	/**
	 * Called to check to see if the breeding sources are correct -- if you use
	 * this method, you must call state.output.exitIfErrors() immediately
	 * afterwards.
	 */
	public void sourcesAreProperForm(final SteadyStateEvolutionState state,
			final BreedingPipeline[] breedingPipelines) {
		for (int x = 0; x < breedingPipelines.length; x++) {
			// all breeding pipelines are SteadyStateBSourceForm
			// if (!(breedingPipelines[x] instanceof SteadyStateBSourceForm))
			// state.output.error("Breeding Pipeline of subpopulation " + x + "
			// is not of SteadyStateBSourceForm");
			((SteadyStateBSourceForm) (breedingPipelines[x])).sourcesAreProperForm(state);
		}
	}

	/**
	 * Called whenever individuals have been replaced by new individuals in the
	 * population.
	 */
	public void individualReplaced(final SteadyStateEvolutionState state,
			final int subpopulation, final int thread, final int individual) {
		for (int x = 0; x < bp.length; x++)
			((SteadyStateBSourceForm) bp[x]).individualReplaced(state, subpopulation,
					thread, individual);
		// let the deselector know
		((SteadyStateBSourceForm) deselectors[subpopulation]).individualReplaced(state,
				subpopulation, thread, individual);
	}

	public void finishPipelines(EvolutionState state) {
		for (int x = 0; x < deselectors.length; x++) {
			bp[x].finishProducing(state, x, 0);
			deselectors[x].finishProducing(state, x, 0);
		}
	}

	public void prepareToBreed(EvolutionState state, int thread) {
		final SteadyStateEvolutionState st = (SteadyStateEvolutionState) state;
		// set up the breeding pipelines
		bp = new BreedingPipeline[st.population.subpops.length];
		for (int pop = 0; pop < bp.length; pop++) {
			bp[pop] = (BreedingPipeline) st.population.subpops[pop].species.pipe_prototype
					.clone();
			if (!bp[pop].produces(st, st.population, pop, 0))
				st.output.error("The Breeding Pipeline of subpopulation " + pop
						+ " does not produce individuals of the expected species "
						+ st.population.subpops[pop].species.getClass().getName()
						+ " and with the expected Fitness class "
						+ st.population.subpops[pop].species.f_prototype.getClass()
								.getName());
		}
		// are they of the proper form?
		sourcesAreProperForm(st, bp);
		// because I promised when calling sourcesAreProperForm
		st.output.exitIfErrors();

		// warm them up
		for (int pop = 0; pop < bp.length; pop++) {
			bp[pop].prepareToProduce(state, pop, 0);
			deselectors[pop].prepareToProduce(state, pop, 0);
		}
	}

//	public Individual breedIndividual(final EvolutionState state, int subpop,
//			int thread) {
//		// final SteadyStateEvolutionState st = (SteadyStateEvolutionState)
//		// state;
//		Individual[] newind = new Individual[1];
//
//		// breed a single individual
//		bp[subpop].produce(1, 1, 0, subpop, newind, state, thread);
//		return newind[0];
//	}

	// @Override
	// public Population breedPopulation(EvolutionState state) {
	// // double check that we're using DEEvaluator
	// if (!(state.evaluator instanceof DEEvaluator))
	// state.output.warnOnce(
	// "DEEvaluator not used, but DEBreeder used. This is almost certainly
	// wrong.");
	//
	// // prepare the breeder (some global statistics might need to be computed
	// // here)
	// prepareDEBreeder(state);
	//
	// // create the new population
	// Population newpop = (Population) state.population.emptyClone();
	//
	// // breed the children
	// for (int subpop = 0; subpop < state.population.subpops.length; subpop++)
	// {
	// if (state.population.subpops[subpop].individuals.length <
	// JF_TOURNAMENT_SIZE)
	// state.output.fatal("Subpopulation " + subpop
	// + " has fewer than four individuals, and so cannot be used with
	// DEBreeder.");
	//
	// Individual[] inds = newpop.subpops[subpop].individuals;
	// for (int i = 0; i < inds.length; i+=2) {
	// DoubleVectorIndividual[] children = createCustomIndividual(state, subpop,
	// i,0);
	// newpop.subpops[subpop].individuals[i] = children[0];
	// newpop.subpops[subpop].individuals[i+1] = children[1];
	// }
	// }
	//
	// // store the current population for competition with the new children
	// previousPopulation = state.population;
	// return newpop;
	// }

	public DoubleVectorIndividual[] createCustomIndividual(EvolutionState state,
			int subpop, int thread) {
		Individual[] inds = state.population.subpops[subpop].individuals;

		DoubleVectorIndividual[] children = new DoubleVectorIndividual[2];

		DoubleVectorIndividual v = (DoubleVectorIndividual) (state.population.subpops[subpop].species
				.newIndividual(state, thread));
		DoubleVectorIndividual vv = (DoubleVectorIndividual) (state.population.subpops[subpop].species
				.newIndividual(state, thread));
//		int retry = -1;
//		do {
//			retry++;

			// select three indexes different from each other and from that of
			// the current parent
			// int r0, r1, r2, r3, r4, r5;
			// do {
			// r0 = state.random[thread].nextInt(inds.length);
			// } while (r0 == index);
			// do {
			// r1 = state.random[thread].nextInt(inds.length);
			// } while (r1 == r0 || r1 == index);
			// do {
			// r2 = state.random[thread].nextInt(inds.length);
			// } while (r2 == r1 || r2 == r0 || r2 == index);

		int[] r = runTournament(state, inds, 0, JF_TOURNAMENT_SIZE);

		DoubleVectorIndividual r0 = (DoubleVectorIndividual) (inds[r[0]]);
		DoubleVectorIndividual r1 = (DoubleVectorIndividual) (inds[r[1]]);
		DoubleVectorIndividual r2 = (DoubleVectorIndividual) (inds[r[2]]);
		DoubleVectorIndividual r3 = (DoubleVectorIndividual) (inds[r[3]]);
		DoubleVectorIndividual r4 = (DoubleVectorIndividual) (inds[r[4]]);
		DoubleVectorIndividual r5 = (DoubleVectorIndividual) (inds[r[5]]);

		for (int i = 0; i < v.genome.length; i++) {
			v.genome[i] = r0.genome[i] + F * (r1.genome[i] - r2.genome[i]);
			vv.genome[i] = r3.genome[i] + F * (r4.genome[i] - r5.genome[i]);
		}
		
//		} while ((!valid(v)  !valid(vv)) && retry < retries);
//		if (retry >= retries && (!valid(v) || !valid(vv))) // we reached our
//															// maximum
//		{
//			// completely reset and be done with it
//			v.reset(state, thread);
//			vv.reset(state, thread);
//		}

		children[0] = fixIndividual(v);
		children[1] = fixIndividual(vv);

		return children;
	}
	
	private DoubleVectorIndividual fixIndividual(DoubleVectorIndividual ind) {
		ind.clamp();
		return ind;
	}

	private int[] runTournament(EvolutionState state, Individual[] inds, int thread,
			int size) {
		List<Integer> solutions = new ArrayList<Integer>(size);

		int counter = 0;
		while (counter < size) {
			int first = state.random[thread].nextInt(inds.length);
			int second = state.random[thread].nextInt(inds.length);

			if (!solutions.contains(first)
					&& inds[first].fitness.betterThan((inds[second].fitness))) {
				solutions.add(first);
			} else if (!solutions.contains(second)) {
				solutions.add(second);
			} else {
				continue;
			}

			counter++;
		}
		Integer[] empty = {};
		return ArrayUtils.toPrimitive(solutions.toArray(empty));
	}
}
