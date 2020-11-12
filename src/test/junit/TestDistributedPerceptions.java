package test.junit;

import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import model.Model;
import model.ModelBean;
import model.ModelBuilder;
import model.ModelDefinition;
import model.customer.Agent;
import util.exception.sales.SalesScheduleError;
import util.functions.Functions;
import util.random.RandomizerUtils;

@RunWith(Parameterized.class)
public class TestDistributedPerceptions {
	/**
	 * Standard Deviation for Perceptions.
	 */
	double std;
	
	int iteration;
	/**
	 * Initial Perceptions before applying normalization.
	 */
	double[][][] initialPerceptions;
	
	Model m;
	
	public TestDistributedPerceptions(String path, int iteration) 
			throws SalesScheduleError {
		ModelDefinition md = new ModelDefinition();
		md.loadValuesFromFile(new File(path));
		
		std = md.getInitialPerceptionsStdDeviation() + (iteration*0.05);
		
		initialPerceptions=md.getInitialPerceptions();
		
		this.iteration=iteration;
		
		md.setInitialPerceptionsStdDeviation(std);

		ModelBuilder mb = md.createBuilder();
		ModelBean bean = mb.createBean();
		m = mb.build(bean, RandomizerUtils.PRIME_SEEDS[iteration]);
	}
	
	@Parameters
	public static Collection<Object[]> data() {
		ArrayList<Object[]> list = new ArrayList<Object[]>();
		final int ITERATIONS=10;
		
		String directoryPath = "./test/distributedperceptions";
		
		File directory = new File(directoryPath);
		
		for (File f:directory.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return pathname.getName().endsWith(".zio");
				}
			})) {
			for (int i=0; i<ITERATIONS; i++) {
				Object[] filePath = {f.getPath(),i};				
				list.add(filePath);
			}
		}
		return list;
	}

	@Test
	public void checkPerceptionValues() {
		int segments = m.getSegments().getNumSegments();
		int brands = m.getNrBrands();
		int attributes = m.getNrAttributes();
		double[][][] upperNormalizedPerceptions = new double [segments][brands][attributes];
		double[][][] lowerNormalizedPerceptions = new double [segments][brands][attributes];
		double[][][] normalizedPerceptions = new double [segments][brands][attributes];
		
		
		int[][][] upperCount = new int [segments][brands][attributes];
		int[][][] lowerCount = new int [segments][brands][attributes];
		
		//Gather every agent perceptions, accumulating the amount by segments.
		for (Agent c : m.getAgents()) {
			int segmentid=c.segmentId;
			double[][] perceptions = c.getPerceptions(); 
			for (int i=0; i<brands; i++) {
				for (int j=0; j<attributes; j++) {
					if(perceptions[i][j]>initialPerceptions[segmentid][i][j]) {
						upperNormalizedPerceptions[segmentid][i][j]+=perceptions[i][j];
						upperCount[segmentid][i][j]++;
					}
					if(perceptions[i][j]<initialPerceptions[segmentid][i][j]) {
						lowerNormalizedPerceptions[segmentid][i][j]+=perceptions[i][j];
						lowerCount[segmentid][i][j]++;
					}
				}
			}
		}
		
		//Calculate the mean for every segment.
		for (int k=0; k<segments; k++) {
			for (int i=0; i<brands; i++) {
				for (int j=0; j<attributes; j++) {
					upperNormalizedPerceptions[k][i][j]/=upperCount[k][i][j];
					lowerNormalizedPerceptions[k][i][j]/=lowerCount[k][i][j];
					
					normalizedPerceptions[k][i][j]=
							(upperNormalizedPerceptions[k][i][j] 
									+ lowerNormalizedPerceptions[k][i][j])/2;					
				}
			}			
		}
		
		double sensitivity = 0.1;
		
		//Compare the resulting mean with provided value.
		boolean error = false;
		for (int k=0; k<segments; k++) {
			for (int i=0; i<brands; i++) {
				for (int j=0; j<attributes; j++) {
					if(!Functions.equals(
								normalizedPerceptions[k][i][j], 
								initialPerceptions[k][i][j], 
								sensitivity
							)) {
						System.out.println("Perception test failed");
						System.out.println("Expected value: "+initialPerceptions[k][i][j]);
						System.out.println("Recieved value: "+normalizedPerceptions[k][i][j]);
						System.out.println("Std used: "+std);
						System.out.println("Iteration: "+iteration);
						error = true;
					}
				}
			}			
		}
		assertFalse(error);
	}
}
