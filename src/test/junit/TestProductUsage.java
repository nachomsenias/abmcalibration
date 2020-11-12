package test.junit;

import static org.junit.Assert.*;

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
import model.sales.SalesScheduler;
import util.exception.sales.SalesScheduleError;
import util.random.RandomizerUtils;
import util.statistics.Statistics;
import util.statistics.Statistics.TimePeriod;

/**
 * This class contains unit tests included for the ProductUsage module.
 * 
 * Currently, the only test implements is concerned with frequency
 * checking at the usage planning. Even if the planning is defined at
 * the agent level, it feels appropriate to check it apart.
 * 
 * @author imoya
 *
 */
@RunWith(Parameterized.class)
public class TestProductUsage {
	
	private int[] frequencies;
	int iteration;
	
	Model m;
	
	public TestProductUsage(
			String path, 
			Integer iteration,
			Boolean usePeriod,
			String period,
			Boolean weekly
		) throws SalesScheduleError {
		
		ModelDefinition md = new ModelDefinition();
		md.loadValuesFromFile(new File(path));
		
		frequencies=md.getUsageFrequencies();
		if(usePeriod) {
			int periodLength = 
				Statistics.calculateWeeksPerPeriod(TimePeriod.valueOf(period));
			
			for(int i=0; i<frequencies.length; i++) {
				frequencies[i]=periodLength;
			}
			
			md.setUsageFrequencies(frequencies);
		}
		
		if(!weekly) {
			md.setStepsForWeek(Model.DAYS_OF_WEEK);
		}
		
		this.iteration=iteration;
		
		ModelBuilder mb = md.createBuilder();
		ModelBean bean = mb.createBean();
		m = mb.build(bean, RandomizerUtils.PRIME_SEEDS[iteration]);
		
		SalesScheduler ss = m.getSalesScheduler();
		ss.enableTest();
		m.runSilent();
	}
	
	@Parameters
	public static Collection<Object[]> data() {
		final int ITERATIONS=10;
		ArrayList<Object[]> list = new ArrayList<Object[]>();
		
		String directoryPath = "./test/productusage";
		
		File directory = new File(directoryPath);
		
		for (File f:directory.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return pathname.getName().endsWith(".zio");
				}
			})) {		
			for (int i=0; i<ITERATIONS; i++) {
				String path = f.getPath();
				Object[] filePath = {path,new Integer(i),new Boolean(false),null,new Boolean(false)};
				list.add(filePath);
				Object[] moarfilePath = {path,new Integer(i),new Boolean(false),null,new Boolean(true)};
				list.add(moarfilePath);
				for (TimePeriod period : TimePeriod.values()) {
					Object[] moreFilePath = {path,new Integer(i),new Boolean(true),null,new Boolean(false)};
					moreFilePath[3] = period.toString();
					list.add(moreFilePath);
					Object[] evenMoreFilePath = {path,new Integer(i),new Boolean(true),null,new Boolean(true)};
					evenMoreFilePath[3] = period.toString();
					list.add(evenMoreFilePath);
				}
			}
		}
		return list;
	}

	/**
	 * This test checks that the time-steps between each product usage
	 * does not exceeds the limits.
	 * 
	 * The limit implied for the strategy use while assigning uses, is
	 * double the frequency defined.
	 */
	@Test
	public void frequencyTest() {
		
		boolean error = false;
		for (Agent c : m.getAgents()) {
			byte [] usage=c.getUsePlanning();
			if(usage==null) {
				continue;
			}
			int freq = frequencies[c.segmentId];
			
			int cont = 0;
			boolean firstUse=false;
			while(cont<usage.length && !firstUse) {
				if(usage[cont]>0) {
					firstUse=true;
				} else {
					cont++;
				}
			}
			if(firstUse) {
				int anotherCont=0;
				for (int i=cont+1; i<usage.length; i++) {
					if(usage[i]>0) {
						error |= anotherCont>2*freq;
						if(error) {
							fail();	
						} else {
							anotherCont=0;
						}						
					} else {
						anotherCont++;
					}					
				}
			}
		}
		assertFalse(error);
	}
}
