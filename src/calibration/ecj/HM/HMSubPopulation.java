/**
 * 
 */
package calibration.ecj.HM;



import ec.*;
import ec.util.*;
import ec.vector.FloatVectorSpecies;

import java.util.*;
/**
 * @author ebermejo
 * @email enric2186@gmail.com
 */
public class HMSubPopulation extends Subpopulation {
	/**
	 * Default serial version
	 */
	private static final long serialVersionUID = 1L;
	//=========================================================================
	//		FIELDS
	//=========================================================================
	/*List of reef occupation- assigns the individual number of the population*/

	/*Array*/
	public ArrayList<Double> HM_a;
	public ArrayList<Double> HM_b;


	public int next_index;
	

	//=========================================================================
	//		METHODS
	//=========================================================================
	public void fill_iterators(EvolutionState state, int a, int b, int length){
 		this.HM_a=new ArrayList<Double>(length);
		this.HM_b=new ArrayList<Double>(length);
		FloatVectorSpecies s =(FloatVectorSpecies)this.species;
 		double step_a=(s.maxGene(a)-s.minGene(a))/length;
		double step_b=(s.maxGene(a)-s.minGene(b))/length;
		for (int i=0;i<length;i++) {
			this.HM_a.add(i,step_a*i);
			this.HM_b.add(i,step_b*i);
		}

	}   


	@Override
	public void setup(EvolutionState state, Parameter base) {
		// TODO Auto-generated method stub
		super.setup(state, base);
 
	}

	@Override
	/*
	 * Initialize the population.  
	 */
	public void populate(EvolutionState state, int thread) {        
        /*****	Constant defined as the best individual obtained by our experimentation		*****/
        double [] TP0=new double[]{0.0005159267726572,0.0008626252079206091,0.17774289395819012,0.010197392962479734,0.0018188689312895853,0.18744899620881333,0.003658375961260114,0.2719552048887558,0.9916833830872808,0.3347320996710943,0.8790388043148295,0.4046639785235853,0.17142916456925245,0.2258736689408668,0.018024139355811725,0.7902057871162088,0.8313390018657469,0.04814152967608935,0.029204477214211336,0.12082517994430282,0.006752404343256274,0.14348716845155138,0.0197113641284177,0.01306953751758715};
        // ,160, 0, 11, 5, 329, 146, 0, 31, 0, 13, 1, 0, 127, 1, 0, 0, 1, 0 ,12, 0, 3, 0, 0, 2, 2, 0, 0, 24, 20, 10, 0, 152, 0, 0, 0, 0, 6, 2, 233, 0, 200, 724, 0, 17, 0, 0, 0, 44};
        double [] TP15=new double[] {0.00021933433911135644, 0.003763256775817493, 0.04301328048592154 ,0.08723338233258777, 0.0011960972064235297, 0.8108683256351559, 0.08139221608965387, 0.01210299226791368, 0.019619421034034523, 0.010074008096821243, 0.11375110630615551, 0.010276173131981054, 0.013815225433081212 ,0.012477620636487703, 0.3087796531990063, 0.027851047034531718, 0.008373919476296374, 0.005508085081992247, 0.009248412537353954, 0.01835029107082439, 0.016430216753332803, 0.17377472983632908, 0.009132294153572633, 0.09639665218370161, 0.0014789567762308162, 0.005276338705998221, 0.0068355475879836106, 0.0056469421580097046, 0.07298270686248161, 0.036876474915028545, 0.8435102447357843, 0.010632010320999882, 0.005136892874422575, 0.8001960809432972, 0.008346680215603072, 0.023933282163623712, 0.014001509571972916, 0.2566502548064083, 0.25904472410666, 0.0206334560059938, 0.0049479863248722165, 0.004444671624333755, 0.00000882567872752751, 0.009336147478800794, 0.08960548713703918, 0.6195163078290815, 0.010386563070460039, 0.2828052277298329, 0.11184698479467062, 0.03487788825242198, 0.038125743934976926, 0.000531075916453667, 0.029161134180949986, 0.052006093004563575, 0.0005083242012649068, 0.0029487447044310435, 0.10515097217862301 ,0.11418555837594281, 0.09354145410495658, 0.03154416368421952, -0.000971298313655592, 0.10772603686642135, 0.03812592182965533, 0.00041371668728783695,0.019128787203359605, 0.015287501376543344, 0.09383932258895157 ,0.00003756648223194887, 0.007000246975001039};
        state.output.message("Populating");
        
        // populating with fixed individuals	
        for(int x=0;x<individuals.length;x++){ 
			this.individuals[x] = species.newIndividual(state, thread);
			FloatVectorSpecies s =(FloatVectorSpecies)this.species;
			int n_genes=s.genomeSize;
 			switch(n_genes) {
				case 24: // CASE TP0 
					for (int l=0;l<n_genes;l++) 
						((DoubleHM)this.individuals[x]).genome[l]=TP0[l];
					break;
				case 69:
					for (int l=0;l<n_genes;l++)
						((DoubleHM)this.individuals[x]).genome[l]=TP15[l];
					break;
				default:
			        state.output.fatal("Heat Map not configured for this problem");
					break;
			}
			this.individuals[x].evaluated=true;
 
        }
        state.output.message("A static Population has been generated");
	}	
}
