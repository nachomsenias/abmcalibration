/**
 * 
 */
package calibration.ecj.multimodal.shade;



import ec.*;
import ec.util.*; 
/**
 * @author ebermejo
 * @email enric2186@gmail.com
 */
public class SHADESubPopulation extends Subpopulation {
	/**
	 * Default serial version
	 */
	private static final long serialVersionUID = 1L;
	//=========================================================================
	//		FIELDS
	//=========================================================================
    public static final String P_ARC_RATE= "arc-rate";

    int num_arc_inds;

    double[] pop_sf, pop_cr,success_sf, success_cr, dif_fitness,memory_sf, memory_cr;
    Individual[] Archive;
    int arc_size;
    double arc_rate;
    int initial_pop_size;
    int min_pop_size;
	@Override
	public void setup(EvolutionState state, Parameter base) {
 		// TODO Auto-generated method stub
		super.setup(state, base);
		

    	arc_rate = state.parameters.getDouble(base.push(P_ARC_RATE),null,0.0);
        if ( arc_rate < 0.0 || arc_rate > 5.0 )
            state.output.fatal( "Parameter not found, or its value is outside of [0.0,5.0].", base.push(P_ARC_RATE), null );
        Parameter parameter=  new Parameter("pop.subpop.0.size");
        int pop_size=state.parameters.getInt(parameter, null,0);
        initial_pop_size=pop_size;
        min_pop_size=4;
        //TODO: CHECK both		
        arc_size = (int) Math.round(arc_rate * pop_size);
        
		pop_sf = new double[pop_size];
		pop_cr = new double[pop_size];
    	success_sf= new double[pop_size];
    	success_cr= new double[pop_size];
    	dif_fitness= new double[pop_size];

        num_arc_inds=0;
        this.Archive=new Individual[arc_size];
        Parameter parameter2=  new Parameter("pop.subpop.0.species.genome-size");
        int memory_size=state.parameters.getInt(parameter2, null,0);
    	memory_sf= new double[memory_size];
    	memory_cr= new double[memory_size];
    	for (int i=0;i<memory_size;i++) {
    		memory_sf[i]=0.5;
    		memory_cr[i]=0.5;
    	}
		}
	public void reducePopulationWithSort(int num_redu_inds) {
		int worst_ind;
		 
		    for (int i = 0; i < num_redu_inds; i++) {
		        worst_ind = 0;
		        for (int j = 1; j < individuals.length; j++) {
		        	if (individuals[worst_ind].fitness.betterThan(individuals[j].fitness)) worst_ind = j;
		        }
		        this.resize( worst_ind);
 		    }
		    arc_size = (int)Math.round(individuals.length * arc_rate);
   	        if (num_arc_inds > arc_size) num_arc_inds = arc_size;
	}	 		 
	 @Override
	 public void resize( int del_index) {
		    int original_size = individuals.length;
 		    Individual [] resized_temp = new Individual[original_size - 1];
		    int index_count = 0;
		 
		    for (int i = 0; i < original_size; i++) {
		        if (i != del_index) {
		        resized_temp[index_count] = (Individual) individuals[i].clone();
		        resized_temp[index_count].fitness= (Fitness) individuals[i].fitness.clone();
		        index_count++;
		        }
		    }
		    individuals=resized_temp;
		} 

}
