package calibration.ecj.de;

import java.util.HashMap;

import ec.EvolutionState;
import ec.Individual;
import ec.Statistics;
import ec.steadystate.SteadyStateBreeder;
import ec.steadystate.SteadyStateDefaults;
import ec.steadystate.SteadyStateEvaluator;
import ec.steadystate.SteadyStateEvolutionState;
import ec.steadystate.SteadyStateExchangerForm;
import ec.steadystate.SteadyStateStatisticsForm;
import ec.util.Checkpoint;
import ec.util.Parameter;

public class CustomSteadyEvolutionState extends SteadyStateEvolutionState {

	/**
	 * Generated.
	 */
	private static final long serialVersionUID = 7923776657558335L;

	public static final String P_REPLACEMENT_PROBABILITY = "replacement-probability";

	/** How many individuals have we added to the initial population? */
	protected int[] individualCount;

	/** Hash table to check for duplicate individuals */
	protected HashMap<Individual, Individual>[] individualHash;

	/** Holds which subpopulation we are currently operating on */
	protected int whichSubpop;

	@Override
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);

		// double check that we have valid evaluators and breeders and
		// exchangers
		if (!(breeder instanceof SteadyStateBreeder))
			state.output.error(
					"You've chosen to use Steady-State Evolution, but your breeder is not of the class SteadyStateBreeder.",
					base);
		if (!(evaluator instanceof SteadyStateEvaluator))
			state.output.error(
					"You've chosen to use Steady-State Evolution, but your evaluator is not of the class SteadyStateEvaluator.",
					base);
		if (!(exchanger instanceof SteadyStateExchangerForm))
			state.output.error(
					"You've chosen to use Steady-State Evolution, but your exchanger does not implement the SteadyStateExchangerForm.",
					base);

		customCheckStatistics(state, statistics, new Parameter(P_STATISTICS));

		if (parameters.exists(SteadyStateDefaults.base().push(P_REPLACEMENT_PROBABILITY),
				null)) {
			replacementProbability = parameters.getDoubleWithMax(
					SteadyStateDefaults.base().push(P_REPLACEMENT_PROBABILITY), null, 0.0,
					1.0);
			if (replacementProbability < 0.0) // uh oh
				state.output.error(
						"Replacement probability must be between 0.0 and 1.0 inclusive.",
						SteadyStateDefaults.base().push(P_REPLACEMENT_PROBABILITY), null);
		} else {
			replacementProbability = 1.0; // always replace
			state.output.message(
					"Replacement probability not defined: using 1.0 (always replace)");
		}
	}

	// recursively prints out warnings for all statistics that are not
	// of steadystate statistics form
	void customCheckStatistics(final EvolutionState state, Statistics stat,
			final Parameter base) {
		if (!(stat instanceof SteadyStateStatisticsForm))
			state.output.warning(
					"You've chosen to use Steady-State Evolution, but your statistics does not implement the SteadyStateStatisticsForm.",
					base);
		for (int x = 0; x < stat.children.length; x++)
			if (stat.children[x] != null)
				customCheckStatistics(state, stat.children[x],
						base.push("child").push("" + x));
	}

	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public void startFresh() {
		output.message("Setting up");
		setup(this, null); // a garbage Parameter

		// POPULATION INITIALIZATION
		output.message("Initializing Generation 0");
		statistics.preInitializationStatistics(this);
		population = initializer.setupPopulation(this, 0);

		// INITIALIZE VARIABLES
		generationSize = 0;
		generationBoundary = false;
		firstTime = true;
		evaluations = 0;
		whichSubpop = -1;

		individualHash = new HashMap[population.subpops.length];
		for (int i = 0; i < population.subpops.length; i++)
			individualHash[i] = new HashMap<Individual, Individual>();

		individualCount = new int[population.subpops.length];
		for (int sub = 0; sub < population.subpops.length; sub++) {
			individualCount[sub] = 0;
			generationSize += population.subpops[sub].individuals.length;
		}

		if (numEvaluations > UNDEFINED && numEvaluations < generationSize)
			output.fatal(
					"Number of evaluations desired is smaller than the initial population of individuals");

		// INITIALIZE CONTACTS -- done after initialization to allow
		// a hook for the user to do things in Initializer before
		// an attempt is made to connect to island models etc.
		exchanger.initializeContacts(this);
		evaluator.initializeContacts(this);
	}

	boolean justCalledPostEvaluationStatistics = false;

	public int evolve() {
		if (generationBoundary && generation > 0) {
			output.message("Generation " + generation + "\tEvaluations " + evaluations);
			statistics.generationBoundaryStatistics(this);
			statistics.postEvaluationStatistics(this);
			justCalledPostEvaluationStatistics = true;
		} else {
			justCalledPostEvaluationStatistics = false;
		}

		if (firstTime) {
			if (statistics instanceof SteadyStateStatisticsForm)
				((SteadyStateStatisticsForm) statistics)
						.enteringInitialPopulationStatistics(this);
			statistics.postInitializationStatistics(this);
			((SteadyStateBreeder) breeder).prepareToBreed(this, 0); // unthreaded
			((SteadyStateEvaluator) evaluator).prepareToEvaluate(this, 0); // unthreaded
			firstTime = false;
		}

		whichSubpop = (whichSubpop + 1) % population.subpops.length; // round
																		// robin
																		// selection

		// is the current subpop full?
		boolean partiallyFullSubpop = (individualCount[whichSubpop] < population.subpops[whichSubpop].individuals.length);

		int newInds = 2;

		// MAIN EVOLVE LOOP
		if (((SteadyStateEvaluator) evaluator).canEvaluate()) // are we ready to
																// evaluate?
		{

			Individual[] ind = new Individual[newInds];
			int numDuplicateRetries = population.subpops[whichSubpop].numDuplicateRetries;

			for (int tries = 0; tries <= numDuplicateRetries; tries++) // see
																		// Subpopulation
			{
				if (partiallyFullSubpop) // is population full?
				{
					for (int i = 0; i < newInds; i++) {
						ind[i] = population.subpops[whichSubpop].species
								.newIndividual(this, 0); // unthreaded
					}
				} else {
					ind = ((JFDEBreeder) breeder).createCustomIndividual(this,
							whichSubpop, 0);
					statistics.individualsBredStatistics(this, ind);
				}

				if (numDuplicateRetries >= 1) {
					for (int i = 0; i < newInds; i++) {
						Object o = individualHash[whichSubpop].get(ind[i]);
						if (o == null) {
							individualHash[whichSubpop].put(ind[i], ind[i]);
						}
					}
					break;
				}
			} // tried to cut down the duplicates

			// evaluate the new individuals
			for (int i = 0; i < newInds; i++) {
				((SteadyStateEvaluator) evaluator).evaluateIndividual(this, ind[i],
						whichSubpop);
			}
		}

		generationBoundary = false;
		for (int i = 0; i < newInds; i++) {
			Individual ind = ((SteadyStateEvaluator) evaluator)
					.getNextEvaluatedIndividual();
			if (ind != null) // do we have an evaluated individual?
			{
				int subpop = ((SteadyStateEvaluator) evaluator)
						.getSubpopulationOfEvaluatedIndividual();

				if (partiallyFullSubpop) // is subpopulation full?
				{
					population.subpops[subpop].individuals[individualCount[subpop]++] = ind;

					// STATISTICS FOR GENERATION ZERO
					if (individualCount[subpop] == population.subpops[subpop].individuals.length)
						if (statistics instanceof SteadyStateStatisticsForm)
							((SteadyStateStatisticsForm) statistics)
									.enteringSteadyStateStatistics(subpop, this);
				} else {
					// mark individual for death
					int deadIndividual = ((JFDEBreeder) breeder).deselectors[subpop]
							.produce(subpop, this, 0);
					Individual deadInd = population.subpops[subpop].individuals[deadIndividual];

					// maybe replace dead individual with new individual
					if (ind.fitness.betterThan(deadInd.fitness) || // it's
																	// better,
																	// we want
																	// it
							random[0].nextDouble() < replacementProbability) // it's
																				// not
																				// better
																				// but
																				// maybe
																				// we
																				// replace
																				// it
																				// directly
																				// anyway
						population.subpops[subpop].individuals[deadIndividual] = ind;

					// update duplicate hash table
					individualHash[subpop].remove(deadInd);

					if (statistics instanceof SteadyStateStatisticsForm)
						((SteadyStateStatisticsForm) statistics)
								.individualsEvaluatedStatistics(this,
										new Individual[] { ind },
										new Individual[] { deadInd },
										new int[] { subpop },
										new int[] { deadIndividual });
				}

				// INCREMENT NUMBER OF COMPLETED EVALUATIONS
				evaluations++;

				// COMPUTE GENERATION BOUNDARY
				// generationBoundary |= (evaluations % generationSize == 0);

				generationBoundary = (evaluations >= generationSize * (generation + 1));
			}

			// SHOULD WE QUIT?
			if (!partiallyFullSubpop
					&& ((SteadyStateEvaluator) evaluator).runComplete(this, ind)
					&& quitOnRunComplete) {
				output.message("Found Ideal Individual");
				return R_SUCCESS;
			}
		}

		if ((numEvaluations > UNDEFINED && evaluations >= numEvaluations) || // using
																				// numEvaluations
				(numEvaluations <= UNDEFINED && generationBoundary
						&& generation == numGenerations - 1)) // not using
																// numEvaluations
		{
			// we are not exchanging again, but we might wish to increment the
			// generation
			// one last time if we hit a generation boundary
			if (generationBoundary)
				generation++;
			return R_FAILURE;
		}

		// EXCHANGING
		if (generationBoundary) {
			// PRE-BREED EXCHANGE
			statistics.prePreBreedingExchangeStatistics(this);
			population = exchanger.preBreedingExchangePopulation(this);
			statistics.postPreBreedingExchangeStatistics(this);
			String exchangerWantsToShutdown = exchanger.runComplete(this);
			if (exchangerWantsToShutdown != null) {
				output.message(exchangerWantsToShutdown);
				return R_SUCCESS;
			}

			// POST BREED EXCHANGE
			statistics.prePostBreedingExchangeStatistics(this);
			population = exchanger.postBreedingExchangePopulation(this);
			statistics.postPostBreedingExchangeStatistics(this);

			// INCREMENT GENERATION AND CHECKPOINT
			generation++;
			if (checkpoint && generation % checkpointModulo == 0) {
				output.message("Checkpointing");
				statistics.preCheckpointStatistics(this);
				Checkpoint.setCheckpoint(this);
				statistics.postCheckpointStatistics(this);
			}
		}
		return R_NOTDONE;
	}

	/**
	 * @param result
	 */
	public void finish(int result) {
		/* finish up -- we completed. */
		((SteadyStateBreeder) breeder).finishPipelines(this);
		if (!justCalledPostEvaluationStatistics) {
			output.message("Generation " + generation + "\tEvaluations " + evaluations);
			statistics.postEvaluationStatistics(this);
		}
		statistics.finalStatistics(this, result);
		finisher.finishPopulation(this, result);
		exchanger.closeContacts(this, result);
		evaluator.closeContacts(this, result);
	}
}
