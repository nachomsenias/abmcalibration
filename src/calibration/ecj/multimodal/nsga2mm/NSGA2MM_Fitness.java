/*
 * Copyright 2016 by Romaric Pighetti, CNRS, I3S
 * Licensed under the Academic Free License version 3.0
 */
package calibration.ecj.multimodal.nsga2mm;

import java.util.ArrayList;

import ec.EvolutionState;
import ec.Individual;
import ec.multiobjective.MultiObjectiveFitness;
import ec.multiobjective.nsga2.NSGA2MultiObjectiveFitness;
import ec.util.Parameter;
import ec.vector.DoubleVectorIndividual;

public class NSGA2MM_Fitness extends NSGA2MultiObjectiveFitness {

    private static final long                  serialVersionUID = -621564785072665471L;

    public static double                       niche_radius;

    /**
     * DebImplMMNSGA2/ individual (!) circular referenece between fitness and
     * corresponding individual instantce
     */
    public Individual                          individual;

    /**
     * DebImplMMNSGA2/ Adaptative constraint object (fitness object level
     * singleton
     */
    public static NSGA2MM_AdaptativeConstraint AC;

    /**
     * DebImplMMNSGA2/ per dimension niche radius;
     */
    public static double[]                     dimensional_niche_radius;

    /**
     * This setup procedure is specific. setup will not be called by ecj. setup
     * might be called at generation 0 from problem prepareToEvaluate method
     *
     * @param state
     */
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);

        // The last objective is the one introduced for multi-modality and is
        // to be minimized.
        maximize[maximize.length - 1] = true;

        // setup adapative constraint
        AC = new NSGA2MM_AdaptativeConstraint();
        AC.setup(state);

        // DEBNSGA2MMImpl/ per dimension niche parametrization
        // setup unique per dimension niche radius (proximity measure DEB 2012)
        Parameter param = new Parameter("pop.subpop.0");
        Parameter defaultParam = new Parameter("pop.default-subpop");

        // parametrize niche radius
        // This is POP in the paper
        int popSize = state.parameters.getInt(param.push("size"),
                defaultParam.push("size"));

        // This is D in the paper
        //int dimension = state.parameters.getInt(
           //     new Parameter("vector").push("species").push("genome-size"),
             //   defaultParam.push("species").push("genome-size"));
        int dimension =state.parameters.getInt( new Parameter("pop.subpop.0.species.genome-size"),null);

        dimensional_niche_radius = new double[dimension];

        // Noted as T in the paper.
        double numChunkPerDimension = Math.floor(Math.exp(Math.log(popSize)
                / (double) dimension));

        try {
            // Use per dimension min and max gene definition if possible
            for (Integer r = 0; r < dimension; r++) {
                double maxg = state.parameters.getDouble(
                        param.push("species.max-gene." + r),
                        defaultParam.push("species.max-gene." + r));
                double ming = state.parameters.getDouble(
                        param.push("species.min-gene." + r),
                        defaultParam.push("species.min-gene." + r));

                dimensional_niche_radius[r] = (maxg - ming)
                        / numChunkPerDimension;
            }
        } catch (Exception ex) {
            // Else use the species definition for all the dimensions.
            System.out
                    .println("Per dimension parameters like min gene not found, assuming equality...");
            double maxg = state.parameters.getDouble(param
                    .push("species.max-gene"), new Parameter(
                    "vector.species.max-gene"));
            double ming = state.parameters.getDouble(param
                    .push("species.min-gene"), new Parameter(
                    "vector.species.min-gene"));

            dimensional_niche_radius[0] = (maxg - ming) / numChunkPerDimension;
            // Do not forget to copy the results for all dimensions
            for (Integer r = 1; r < dimension; r++) {
                dimensional_niche_radius[r] = dimensional_niche_radius[0];
            }
        }
    }

    public void setObjectives(final EvolutionState state, double[] newObjectives) {
 
        if (newObjectives == null)
            {
            state.output.fatal("Null objective array provided to MultiObjectiveFitness.");
            }
        if (newObjectives.length != objectives.length)
            {
            state.output.fatal("New objective array length does not match current length.");
            }
        for (int i = 0; i < newObjectives.length; i++)
            {
            double _f = newObjectives[i];
            if (_f >= Double.POSITIVE_INFINITY || _f <= Double.NEGATIVE_INFINITY || Double.isNaN(_f))
                {
                state.output.warning("Bad objective #" + i + ": " + _f + ", setting to worst value for that objective.");
                if (maximize[i])
                    newObjectives[i] = minObjective[i];
                else
                    newObjectives[i] = maxObjective[i];
                }
            }
        objectives = newObjectives;
        

        AC.updateBestFitness(this);
    }

    public static void setGen(int generation) {
        AC.setGen();
    }

    /**
     * Divides inds into pareto front ranks (each an ArrayList), and returns
     * them, in order, stored in an ArrayList.
     */
    @SuppressWarnings("rawtypes")
	public static ArrayList<ArrayList<?>> partitionIntoRanks(Individual[] inds) {
        Individual[] dummy = new Individual[0];
        ArrayList<ArrayList<?>> frontsByRank = new ArrayList<ArrayList<?>>();

        while (inds.length > 0) {
            ArrayList<?> front = new ArrayList();
            ArrayList<?> nonFront = new ArrayList();
            MultiObjectiveFitness.partitionIntoParetoFront(inds, front,
                    nonFront);

            // build inds out of remainder
            inds = (Individual[]) nonFront.toArray(dummy);
            frontsByRank.add(front);
        }
        return frontsByRank;
    }

    /**
     * Returns true if I'm better than _fitness. The rule I'm using is this: if
     * I am better in one or more criteria, and we are equal in the others, then
     * betterThan is true, else it is false.
     */
    @Override
    public boolean paretoDominates(MultiObjectiveFitness other) {

        // If individuals are not close enough, they are not comparable.
        // Thus paretoDominates returns false.

        int feasibl = 3;

        if (AC.activated == true) {
            // Vectors feasibility
            // if a & b are not feasible then we choose the most close to
            // the
            // constraint dominates.
            // if one of them is feasible then it dominates the other
            // if both are feasible the the domination is really calculated
            feasibl = AC.feasibility(this, (NSGA2MM_Fitness) other);
        }
        if (feasibl == 0) {
            // XXX : Works only for single objective multimodal problem.
            // Needs tuning for multi-objectives multimodal problems.

            // we need to find the most close to feasibility
            // if the front member is better than the individual, dump the
            // individual and go to the next one
            if (!maximize[0] && this.getObjective(0) < other.getObjective(0)) {
                return true;
            } else if (maximize[0] && this.getObjective(0) > other.getObjective(0)) {
                return true;
            }
            // if the individual was better than the front member, dump the
            // front member. But look over the
            // other front members (don't break) because others might be
            // dominated by the individual as well.
            else {
                return false;
            }

        } else if (feasibl == 1) {
            // only first (this) is feasible
            return true;
        } else if (feasibl == 2) {
            return false;
        } else {
            // both feasible! (feasibl==3)
            // eq.2 modified with proximate
            if (proximate(this.individual, ((NSGA2MM_Fitness) other).individual)) {
                return super.paretoDominates(other);
            } else {
                return false;
            }
        }
    }

    
    private boolean rawDomination(MultiObjectiveFitness other) {
        return super.paretoDominates(other);
    }

    /**
     * Test if individuals are proximate. We must use L1 distance! This
     * proximity measure is made to maintain the diversity in the population /!\
     * Pay attention that the distance we calculate is based on genotype (not in
     * the fitness space)!
     */
    private static Boolean proximate(Individual x1, Individual x2) {
        DoubleVectorIndividual iv1 = (DoubleVectorIndividual) x1;
        DoubleVectorIndividual iv2 = (DoubleVectorIndividual) x2;
        double[] g1 = iv1.genome;
        double[] g2 = iv2.genome;
        int numDecisionVars = g1.length;
        int t;
        for (t = 0; t < numDecisionVars; t++) 
            if (Math.abs(g1[t] - g2[t]) > dimensional_niche_radius[t]) 
                return false;                 // individuals are not proximate
        return true;        // invididuals are proximate
    }

    public boolean sameAs(NSGA2MM_Fitness other) {
        if (this.objectives.length != other.objectives.length)
            return false;
        for (int i = 0; i < this.objectives.length; i++) 
            if (this.objectives[i] != other.objectives[i]) 
                return false;
        return true;
    }

    /**
     * Calculate successive values of the adaptative constraint. in addition to
     * locating global optima, also maintains niches at the local optima. When
     * local optima are undesired, a constraint based on the current-best
     * function value can be added to the problem to make the corresponding
     * variable vectors infeasible. Since the dominance relations between
     * population members are easily influenced by their feasibility (see
     * Section II), we adopt an adaptive constraint that is insignificant at the
     * start of the generations, but becomes increasingly difficult to satisfy
     * as the generations progress.
     */
    private static class NSGA2MM_AdaptativeConstraint {

        // noted as a in the paper
        private double                     fga;

        // noted as b in the paper
        private double                     fgb;

        // noted as Fbest in the paper
        private ArrayList<NSGA2MM_Fitness> fBests = new ArrayList<NSGA2MM_Fitness>();

        // fBest prepared with fgen * epsilon modifications
        // used in constraint computation and updated each generation
        // using the new fBest
        private ArrayList<NSGA2MM_Fitness> fBestsPrepared;

        // noted as epsilon in the paper
        private double                     constrtAccuracy;

        // Whether the constraint is in use or not
        public Boolean                     activated;
        // fgen
        public double                      fgenEpsilon;

        EvolutionState                     state;

        public void updateBestFitness(NSGA2MM_Fitness fitness) {
            NSGA2MM_Fitness clonedFitness = (NSGA2MM_Fitness) fitness.clone();
            clonedFitness.objectives[clonedFitness.objectives.length-1] = 0;
            for (int i = 0; i < fBests.size(); i++) {
                if (fBests.get(i).rawDomination(clonedFitness) || fBests.get(i).sameAs(clonedFitness)) {
                    return;
                }
                if (clonedFitness.rawDomination(fBests.get(i))) {
                    fBests.remove(i);
                    i--;
                }
            }
            fBests.add(clonedFitness);
        }

        /**
         * setup called before each job (n gens)
         */
        public Boolean setup(EvolutionState state) {
            // if we are multimodal
            activated = state.parameters.getString(
                    new Parameter("eval.problem.modality"), null).equals("mm");

            this.state = state;

            if (activated) {

                try {
                    Integer evals = state.parameters.getInt(new Parameter(
                            "evaluations"), null);
                    Integer size = state.parameters.getInt(new Parameter ("pop.subpop.0.size"), null);
                    Integer gens=evals/size;
                   // Integer gens= state.numGenerations;
                    // fga, fgb are part of adaptive constraint computation
                    // (ln 2 − ln 10^14)=−31,543044121
                    fgb = (Math.log(2) - 14 * Math.log(10)) / gens;
                    fga = Math.pow(10, 14) / Math.exp(fgb);

                    fgenEpsilon = Math.pow(10, 14);
                    // fBest set to null
                    // best fitness till the begining
                    fBests = new ArrayList<NSGA2MM_Fitness>();

                    constrtAccuracy = state.parameters.getDouble(new Parameter(
                            "eval.problem.accuracy"), null);

                } catch (Exception ex) {
                    state.output
                            .fatal("Unable to parametrize Adaptative constraint code cause "
                                    + ex.getMessage());
                    return false;
                }

            }
            // no multimodal task
            return true;
        }

        /**
         *
         * @param ngen
         */
        public void setGen() {

            int ngen = state.generation;

            // pre compute fgen * epsilon
            fgenEpsilon = CmptFgenEpsilon(ngen);
            prepareFbest(state);
        }

        private void prepareFbest(EvolutionState state) {
            if (fBests == null || fBests.isEmpty()) {
                return;
            }
            fBestsPrepared = new ArrayList<NSGA2MM_Fitness>();
            for (int i = 0; i < fBests.size(); i++) {
                NSGA2MM_Fitness best = (NSGA2MM_Fitness) fBests.get(i).clone();
                double[] objectives = best.getObjectives();
                for (int j = 0; j < objectives.length-1; j++) {
                    if (best.maximize[j]) {
                        objectives[j] -= fgenEpsilon;
                    } else {
                        objectives[j] += fgenEpsilon;
                    }
                }
                best.setObjectives(state, objectives);
                fBestsPrepared.add(best);
            }
        }

        /**
         * help to compute the adaptative constraint See paper
         *
         * @return
         */
        public double CmptFgenEpsilon(double ngen) {
            return fga * Math.exp(fgb * ngen) * constrtAccuracy;
        }

        /**
         * computes the adaptative constraint
         *
         * @param objective
         * @return
         */
        public Boolean CmptAdaptConstrt(NSGA2MM_Fitness fitness) {
            NSGA2MM_Fitness fitCopy = (NSGA2MM_Fitness) fitness.clone();
            fitCopy.objectives[fitCopy.objectives.length - 1] = 0;

            // Return true if:
            // fBest is null, this is the first gen and the constraint is not
            // applied.
            // the proposed fitness dominates fBest with constraints.
            // none of the proposed fitness and the prepared best dominates the
            // other.
            Boolean retval = fBestsPrepared == null || fBestsPrepared.isEmpty();
            Boolean dominatesSomeone = false;
            Boolean noOneDominates = true;
            for (int i = 0; i < fBestsPrepared.size(); i++) {
                dominatesSomeone = dominatesSomeone
                        || fitCopy.rawDomination(fBestsPrepared.get(i));
                noOneDominates = noOneDominates
                        && !fBestsPrepared.get(i).rawDomination(fitCopy);
            }
            retval = retval || dominatesSomeone || noOneDominates;

            return retval;
        }

        /**
         * Calculates individuals feasibility 0 = both infeasible 1 = first
         * feasible 2 = second feasible 3 = both feasible
         */
        public int feasibility(NSGA2MM_Fitness objectivex,
                NSGA2MM_Fitness objectivey) {
            if (!activated) {
                return 3;
            } else {
                Boolean x, y;

                x = CmptAdaptConstrt(objectivex);
                y = CmptAdaptConstrt(objectivey);

                if (x && y) {
                    return 3;
                } else if (x) {
                    return 1;
                } else if (y) {
                    return 2;
                }

                return 0;
            }
        }

    }

}
