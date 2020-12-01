package es.ugr.sci2s.soccer.test.junit;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import es.ugr.sci2s.soccer.beans.ResultContainer;
import es.ugr.sci2s.soccer.beans.SimulationConfig;
import es.ugr.sci2s.soccer.beans.SimulationResult;
import es.ugr.sci2s.soccer.workers.SimulationWorker;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.junit.Test;

import com.google.gson.Gson;

import model.ModelDefinition;
import model.ModelRunner;
import util.functions.ArrayFunctions;
import util.functions.Functions;
import util.functions.MatrixFunctions;
import util.io.StatisticsRecordingBean;
import util.statistics.MonteCarloStatistics;
import util.statistics.Statistics.TimePeriod;

public class OutputTest {
	
	String testFolder = "test/output/";
	
	int brandId=0;
	
	SimulationResult[] results;
	
	ModelDefinition md;
	SimulationConfig sc;
	String simConfig;
	
	public OutputTest() throws IOException {
		//OutputFile File
		String file = testFolder+"exampleOutputFull.json";
		
		//Read JSON files
		BufferedReader br = new BufferedReader(new FileReader(file));
		
		StringBuilder buffer = new StringBuilder();
		String line;
		while((line = br.readLine())!=null) {
			buffer.append(line);
		}
		br.close();
		
		String stringConfig = buffer.toString();
		
		//Get Values
		Gson gson = new Gson();
		
		results = gson.fromJson(stringConfig, SimulationResult[].class);
		
		//Zio file
		file = testFolder+"outputFileSales.json";
		
		br = new BufferedReader(new FileReader(file));
		
		buffer = new StringBuilder();
		while((line = br.readLine())!=null) {
			buffer.append(line);
		}
		br.close();
		
		simConfig = buffer.toString();
		
		sc = gson.fromJson(simConfig, SimulationConfig.class);
		
		md = sc.getModelDefinition();
	}
	@Test
	public void totalTest() {
		int numResults = results.length;
		for (int i=0; i<numResults; i++) {
			SimulationResult result = results[i];
			
			double[][] salesByBrandBySeg = result.salesByBrandBySegmentAvg;
			
			double[][][] salesByBrandBySegByStep = result.salesByBrandBySegmentByStepAvg; 
			int weeks = salesByBrandBySegByStep[0][0].length;
			
			for (int b=0; b<salesByBrandBySegByStep.length; b++) {
				for (int s=0; s<salesByBrandBySegByStep[b].length; s++) {
					double salesStepSum = 0;
					
					for (int w =0; w<weeks; w++) {
						salesStepSum+=salesByBrandBySegByStep[b][s][w];
					}
					
					double max_difference = salesByBrandBySeg[b][s] * 0.002;
					
					boolean equals = Functions.equals(salesStepSum, salesByBrandBySeg[b][s], 
							max_difference);
					assertTrue(equals);
				}
			}
		}
	}
	
	@Test
	public void overTimeTest() throws IOException {
		System.out.println(" ** Overtime Test ** START **");
		try {
			MonteCarloStatistics stats = ModelRunner.simulateModel(md, sc.getnMC(), 
					false, StatisticsRecordingBean.onlySalesBean());
			
			double [][][][] salesByStepMC= 
					stats.computeScaledSalesByBrandBySegmentByStep(TimePeriod.WEEKLY);
			int [][][] totalSalesMC= stats.computeScaledSalesByBrandBySegment();
			
			boolean [] failed = new boolean[salesByStepMC.length];
			
			for (int mc = 0; mc<sc.getnMC(); mc++) {
				int totalMC = MatrixFunctions.addMatrix(totalSalesMC[mc]);
				int stepMC = MatrixFunctions.add3dMatrix(salesByStepMC[mc]);
		
				if(totalMC!=stepMC){
					failed[mc] = true;
				} else {
					System.out.println(" ** MC:"+mc+" ** Total value: "+totalMC);
				}
				
				for (int b = 0; b < stats.getNrBrands(); b++) {
					int totalBrand = ArrayFunctions.addArray(totalSalesMC[mc][b]);
					int totalBrandStep = MatrixFunctions.addMatrix(salesByStepMC[mc][b]);
					
					if(totalBrand!=totalBrandStep){
						failed[mc] = true;
					} else {
						System.out.println(" ** Brand:"+b+" ** Brand value: "+totalBrand);
					}
				}
			}
			if(BooleanUtils.or(failed)) {
				fail();
			}
		}catch(Exception e) {
			System.out.println(e.getMessage());
			fail();
		}
		System.out.println(" ** Overtime Test ** END **");
	}

	public double[] contributionTest() {
		int numResults = results.length;
		
		//Baseline Sales
		double baseLineSales = 0;
		
		double[] salesByBrandBySeg = results[0].salesByBrandBySegmentAvg[brandId];
		
		for (int s=0; s<salesByBrandBySeg.length; s++) {
			baseLineSales+=salesByBrandBySeg[s];			
		}
		
		//Sales increment by Touchpoint Id
		double [] increment = new double [numResults-1];
		
		for (int i=1; i<numResults; i++) {
			
			//Increment for this touchpoint
			double tpIncrement = 0;
			
			salesByBrandBySeg = results[i].salesByBrandBySegmentAvg[brandId];
			
			//Add sales by segment
			for (int s=0; s<salesByBrandBySeg.length; s++) {
				tpIncrement+=salesByBrandBySeg[s];			
			}
			
			tpIncrement -= baseLineSales;
			
			//If the increment is below 0, we truncate it to 0.
			if(tpIncrement<0) {
				tpIncrement=0.0;
			}
			
			increment[i-1] = tpIncrement;
		}
		
		//The addition of every touchpoint increment.
		double totalIncrements = StatUtils.sum(increment);
		
		double[] percentageIncrements = new double [numResults-1];
		
		// Percentage of increment(i) = increment(i) / totalIncrements
		for (int i=1; i<numResults; i++) {
			int j= i-1;
			percentageIncrements[j] = increment[j]/totalIncrements;
		}
		
		System.out.println(Arrays.toString(percentageIncrements));
		
		return percentageIncrements;
	}

	public double[] equityTest() {		
		int numResults = results.length;
		
		
		double[] equityByTp = new double [numResults];
		
		for (int tp =0; tp<numResults; tp++) {
			equityByTp[tp] = calculateEquity(results[tp], brandId);
		}
		
		System.out.println(Arrays.toString(equityByTp));
		
		return equityByTp;
	}
	
	private double calculateEquity(SimulationResult result, int brandId) {
		double equityValue = 0;
		
		double[][] drivers = md.getDrivers();
		
		double[][][][] perceptions = result.perceptionsByDriverByBrandBySegmentByStepAvg;
		
		double [][] perceptionsBySegmentByDriver = 
				new double [md.getNumberOfSegments()][md.getNumberOfAttributes()];
		
		double[] equityBySegment = new double [md.getNumberOfSegments()];
		
		for (int seg = 0; seg<md.getNumberOfSegments(); seg++) {
			for (int d = 0; d<md.getNumberOfAttributes(); d++) {
				int steps = perceptions[d][brandId][seg].length;
				perceptionsBySegmentByDriver[seg][d] = perceptions[d][brandId][seg][steps-1];
			}
			equityBySegment[seg] = new Mean().evaluate(
					perceptionsBySegmentByDriver[seg],
					drivers[seg]);
		}
		
		equityValue = StatUtils.mean(equityBySegment);
		
		return equityValue;
	}
	
	@Test
	public void singleEquityTest() {
		
		ResultContainer result = new ResultContainer();
		
		Map<Integer, ResultContainer> table = new HashMap<Integer, ResultContainer>();
		
		SimulationWorker worker = new SimulationWorker(simConfig, result, table, 0);
		
		worker.executeSimple();
		
		SimulationResult simResult = result.getSimpleResult();
		
		double equity = calculateEquity(simResult, brandId);
		
		System.out.println(equity);
	}
	
	@Test
	public void contributionAndEquityTest() {
		double [] contributions = contributionTest();
		double [] equity = equityTest();
		
		double[] stacked = new double [contributions.length];
		
		double baseEquity = equity[0];
		
		for (int tp = 0; tp<stacked.length; tp ++) {
			double incEquity = (equity[tp+1] / baseEquity)-1.0;
			
			double stack = incEquity+contributions[tp];
			System.out.println("-----TP = "+(tp+1)+"-----");
			System.out.println(incEquity / stack);
			System.out.println(contributions[tp] / stack);
			System.out.println("--------------");
		}
		
		System.out.println("-----END------");
	}
	
	@Test
	public void totalSalesMCTest() {
		//OutputFile File
		String file = testFolder+"SingleOutputSales_id29.json";
		
		try {
			//Read JSON files
			BufferedReader br = new BufferedReader(new FileReader(file));
			
			StringBuilder buffer = new StringBuilder();
			String line;
			while((line = br.readLine())!=null) {
				buffer.append(line);
			}
			br.close();
			
			String stringConfig = buffer.toString();
			
			//Get Values
			Gson gson = new Gson();
			
			SimulationResult result = gson.fromJson(stringConfig, SimulationResult.class);
			
			double[][][] salesmc=result.salesByBrandBySegmentMC;
			
			int brands = salesmc.length;
			
			double[][] salesByBrandByMc = new double[brands][];
			
			double[] min = new double [brands];
			double[] lq = new double [brands];
			double[] median = new double [brands];
			double[] uq = new double [brands];
			double[] max = new double [brands];
			
			Percentile percentile = new Percentile();
			
			for (int b = 0; b<brands; b++) {
				salesByBrandByMc[b] = new double [salesmc[b][0].length];
				for (int s = 0; s<salesmc[b].length; s++) {
					for (int mc = 0; mc<salesmc[b][s].length;mc++) {
						salesByBrandByMc[b][mc]+=salesmc[b][s][mc];
					}
				}
				min[b] = NumberUtils.min(salesByBrandByMc[b]);
				max[b] = NumberUtils.max(salesByBrandByMc[b]);
				
				lq[b] = percentile.evaluate(salesByBrandByMc[b], 25);
				median[b] = percentile.evaluate(salesByBrandByMc[b], 50);
				uq[b] = percentile.evaluate(salesByBrandByMc[b], 75);
			}
			
			System.out.println(Arrays.toString(min));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void salesProfitTest() {
		//OutputFile File
		String file = testFolder+"SingleOutputSales_id29.json";
		
		double baseProfit = 0.07;
		
		try {
			//Read JSON files
			BufferedReader br = new BufferedReader(new FileReader(file));
			
			StringBuilder buffer = new StringBuilder();
			String line;
			while((line = br.readLine())!=null) {
				buffer.append(line);
			}
			br.close();
			
			String stringConfig = buffer.toString();
			
			//Get Values
			Gson gson = new Gson();
			
			SimulationResult result = gson.fromJson(stringConfig, SimulationResult.class);
			
			double[][][] salesmc=result.salesByBrandBySegmentMC;
			
			int brands = salesmc.length;
			
			double[][] salesByBrandByMc = new double[brands][];
			
			double[] min = new double [brands];
			double[] lq = new double [brands];
			double[] median = new double [brands];
			double[] uq = new double [brands];
			double[] max = new double [brands];
			double[] profit = new double [brands];
			
			Percentile percentile = new Percentile();
			
			for (int b = 0; b<brands; b++) {
				salesByBrandByMc[b] = new double [salesmc[b][0].length];
				for (int s = 0; s<salesmc[b].length; s++) {
					for (int mc = 0; mc<salesmc[b][s].length;mc++) {
						salesByBrandByMc[b][mc]+=salesmc[b][s][mc];
					}
				}
				min[b] = NumberUtils.min(salesByBrandByMc[b]);
				max[b] = NumberUtils.max(salesByBrandByMc[b]);
				
				lq[b] = percentile.evaluate(salesByBrandByMc[b], 25);
				median[b] = percentile.evaluate(salesByBrandByMc[b], 50);
				uq[b] = percentile.evaluate(salesByBrandByMc[b], 75);
				
				profit[b] = StatUtils.mean(salesByBrandByMc[b]) * baseProfit;
			}
			
			System.out.println(Arrays.toString(min));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void salesEvolutionMCTest() {
		//OutputFile File
		String file = testFolder+"SingleOutputSales_id29.json";
		
		try {
			//Read JSON files
			BufferedReader br = new BufferedReader(new FileReader(file));
			
			StringBuilder buffer = new StringBuilder();
			String line;
			while((line = br.readLine())!=null) {
				buffer.append(line);
			}
			br.close();
			
			String stringConfig = buffer.toString();
			
			//Get Values
			Gson gson = new Gson();
			
			SimulationResult result = gson.fromJson(stringConfig, SimulationResult.class);
			
			double[][][] minseg=result.salesByBrandBySegmentByStepMin;
			double[][][] avgseg=result.salesByBrandBySegmentByStepAvg;
			double[][][] maxseg=result.salesByBrandBySegmentByStepMax;
			
			int brands = avgseg.length;
			
			double[][] min = new double [brands][];
			double[][] avg = new double [brands][];
			double[][] max = new double [brands][];
			
			for (int b = 0; b<brands; b++) {
				min[b] = new double[minseg[b][0].length];
				avg[b] = new double[avgseg[b][0].length];
				max[b] = new double[maxseg[b][0].length];
				
				for (int s = 0; s<avgseg[b].length; s++) {
					for (int step = 0; step<avgseg[b][s].length; step++) {
						min[b][step]+=minseg[b][s][step];
						avg[b][step]+=avgseg[b][s][step];
						max[b][step]+=maxseg[b][s][step];
					}
				}
			}
			
			System.out.println(Arrays.toString(avg));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void awarenessEvolutionMCTest() {
		//OutputFile File
		String file = testFolder+"exampleOutputAwareness.json";
		
		try {
			//Read JSON files
			BufferedReader br = new BufferedReader(new FileReader(file));
			
			StringBuilder buffer = new StringBuilder();
			String line;
			while((line = br.readLine())!=null) {
				buffer.append(line);
			}
			br.close();
			
			String stringConfig = buffer.toString();
			
			//Get Values
			Gson gson = new Gson();
			
			SimulationResult result = gson.fromJson(stringConfig, SimulationResult.class);
			
			double[][][] minseg=result.awarenessByBrandBySegByStepMin;
			double[][][] avgseg=result.awarenessByBrandBySegByStepAvg;
			double[][][] maxseg=result.awarenessByBrandBySegByStepMax;
			
			int brands = avgseg.length;
			
			double[][] min = new double [brands][];
			double[][] avg = new double [brands][];
			double[][] max = new double [brands][];
			
			//Average using sizes
			double[] segmentSizes = {0.092,0.252,0.241,0.18,0.235};
			
			for (int b = 0; b<brands; b++) {
				min[b] = new double[minseg[b][0].length];
				avg[b] = new double[avgseg[b][0].length];
				max[b] = new double[maxseg[b][0].length];
				
				for (int s = 0; s<avgseg[b].length; s++) {
					for (int step = 0; step<avgseg[b][s].length; step++) {
						min[b][step]+=minseg[b][s][step]*segmentSizes[s];
						avg[b][step]+=avgseg[b][s][step]*segmentSizes[s];
						max[b][step]+=maxseg[b][s][step]*segmentSizes[s];
					}
				}
			}
			
			System.out.println(Arrays.toString(avg));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void perceptionsEvolutionMCTest() {
		//OutputFile File
		String file = testFolder+"exampleOutputPerceptions_single.json";
		
		try {
			//Read JSON files
			BufferedReader br = new BufferedReader(new FileReader(file));
			
			StringBuilder buffer = new StringBuilder();
			String line;
			while((line = br.readLine())!=null) {
				buffer.append(line);
			}
			br.close();
			
			String stringConfig = buffer.toString();
			
			//Get Values
			Gson gson = new Gson();
			
			SimulationResult result = gson.fromJson(stringConfig, SimulationResult.class);
			
			double[][][][] minsegatt=result.perceptionsByDriverByBrandBySegmentByStepMin;
			double[][][][] avgsegatt=result.perceptionsByDriverByBrandBySegmentByStepAvg;
			double[][][][] maxsegatt=result.perceptionsByDriverByBrandBySegmentByStepMax;
			
			int atts = avgsegatt.length;
			int brands = avgsegatt[0].length;
			
			double[][][] minseg = new double [brands][][];
			double[][][] avgseg = new double [brands][][];
			double[][][] maxseg = new double [brands][][];

			double[][] min = new double [brands][];
			double[][] avg = new double [brands][];
			double[][] max = new double [brands][];
			
			//Average using sizes
			double[] segmentSizes = {0.092,0.252,0.241,0.18,0.235};
			
			for (int b = 0; b<brands; b++) {
				int segments = avgsegatt[0][b].length;
				
				minseg[b] = new double [segments][];
				avgseg[b] = new double [segments][];
				maxseg[b] = new double [segments][];
				
				int steps = avgsegatt[0][b][0].length;
				
				min[b] = new double [steps];
				avg[b] = new double [steps];
				max[b] = new double [steps];
				
				for (int seg = 0; seg<segments; seg++) {
					minseg[b][seg] = new double [steps];
					avgseg[b][seg] = new double [steps];
					maxseg[b][seg] = new double [steps];
					
					for (int step = 0; step<steps; step++) {
						
						for (int a = 0; a<atts; a++) {
							minseg[b][seg][step] += minsegatt[a][b][seg][step];
							avgseg[b][seg][step] += avgsegatt[a][b][seg][step];
							maxseg[b][seg][step] += maxsegatt[a][b][seg][step];
						}
						minseg[b][seg][step] /= atts;
						avgseg[b][seg][step] /= atts;
						maxseg[b][seg][step] /= atts;
						
						min[b][step] += minseg[b][seg][step]*segmentSizes[seg];
						avg[b][step] += avgseg[b][seg][step]*segmentSizes[seg];
						max[b][step] += maxseg[b][seg][step]*segmentSizes[seg];
					}
				}
			}
			
			System.out.println(Arrays.toString(avg));
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void perceptionsEvolutionMCTestSelectedSegments() {
		//OutputFile File
		String file = testFolder+"exampleOutputPerceptions_single.json";
		
		try {
			//Read JSON files
			BufferedReader br = new BufferedReader(new FileReader(file));
			
			StringBuilder buffer = new StringBuilder();
			String line;
			while((line = br.readLine())!=null) {
				buffer.append(line);
			}
			br.close();
			
			String stringConfig = buffer.toString();
			
			//Get Values
			Gson gson = new Gson();
			
			SimulationResult result = gson.fromJson(stringConfig, SimulationResult.class);
			
			double[][][][] minsegatt=result.perceptionsByDriverByBrandBySegmentByStepMin;
			double[][][][] avgsegatt=result.perceptionsByDriverByBrandBySegmentByStepAvg;
			double[][][][] maxsegatt=result.perceptionsByDriverByBrandBySegmentByStepMax;
			
			int atts = avgsegatt.length;
			int brands = avgsegatt[0].length;
			
			double[][][] minseg = new double [brands][][];
			double[][][] avgseg = new double [brands][][];
			double[][][] maxseg = new double [brands][][];

			double[][] min = new double [brands][];
			double[][] avg = new double [brands][];
			double[][] max = new double [brands][];
			
			//Average using sizes
			double[] segmentSizes = {0.252,0.241,0.235};
			double totalSegments = StatUtils.sum(segmentSizes);
			int[] segmentIndexes = {1,2,4};
			
			for (int b = 0; b<brands; b++) {
				int segments = avgsegatt[0][b].length;
				
				minseg[b] = new double [segments][];
				avgseg[b] = new double [segments][];
				maxseg[b] = new double [segments][];
				
				int steps = avgsegatt[0][b][0].length;
				
				min[b] = new double [steps];
				avg[b] = new double [steps];
				max[b] = new double [steps];
				
				for (int seg : segmentIndexes) {
					minseg[b][seg] = new double [steps];
					avgseg[b][seg] = new double [steps];
					maxseg[b][seg] = new double [steps];
					
					for (int step = 0; step<steps; step++) {
						
						for (int a = 0; a<atts; a++) {
							minseg[b][seg][step] += minsegatt[a][b][seg][step];
							avgseg[b][seg][step] += avgsegatt[a][b][seg][step];
							maxseg[b][seg][step] += maxsegatt[a][b][seg][step];
						}
						minseg[b][seg][step] /= atts;
						avgseg[b][seg][step] /= atts;
						maxseg[b][seg][step] /= atts;
						
						int index = ArrayUtils.indexOf(segmentIndexes, seg);
						double size = segmentSizes[index]/totalSegments;
						
						
						min[b][step] += minseg[b][seg][step]*size;
						avg[b][step] += avgseg[b][seg][step]*size;
						max[b][step] += maxseg[b][seg][step]*size;
					}
				}
			}
			
			System.out.println(Arrays.toString(avg));
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void perceptionContribution() {
		//OutputFile File
		String file = testFolder+"outputPerceptionContributions.json";
		
		try {
			//Read JSON files
			BufferedReader br = new BufferedReader(new FileReader(file));
			
			StringBuilder buffer = new StringBuilder();
			String line;
			while((line = br.readLine())!=null) {
				buffer.append(line);
			}
			br.close();
			
			String stringConfig = buffer.toString();
			
			//Get Values
			Gson gson = new Gson();
			
			SimulationResult result = gson.fromJson(stringConfig, SimulationResult.class);
			
			//Zio file
			file = testFolder+"outputFileSales.json";
			
			br = new BufferedReader(new FileReader(file));
			
			buffer = new StringBuilder();
			while((line = br.readLine())!=null) {
				buffer.append(line);
			}
			br.close();
			
			simConfig = buffer.toString();
					
			SimulationConfig sc = gson.fromJson(simConfig, SimulationConfig.class);
			
			ModelDefinition md = sc.getModelDefinition();
			
			double[][][][] contributionToPerceptions = 
					result.contributionByTouchpointByDriverByBrandBySegmentAvg;
			
			double [][][] contributionByTPSegmentDriver = 
					new double [contributionToPerceptions.length]
							[md.getNumberOfSegments()][md.getNumberOfAttributes()];
			
			double [][] segmentContributionByTPSegment= 
					new double [contributionToPerceptions.length]
							[md.getNumberOfSegments()];
			
			double [] contributionByTP= 
					new double [contributionToPerceptions.length];
			
			
			for (int tp = 0; tp<contributionToPerceptions.length; tp ++) {
				for (int seg = 0; seg<contributionByTPSegmentDriver[tp].length; seg++) {
					for (int a = 0; a<contributionByTPSegmentDriver[tp][seg].length; a++) {
						contributionByTPSegmentDriver[tp][seg][a] = 
								contributionToPerceptions[tp][a][0][seg];
					}
					segmentContributionByTPSegment[tp][seg] = 
							StatUtils.mean(contributionByTPSegmentDriver[tp][seg]);
				}
				contributionByTP[tp] = StatUtils.sum(segmentContributionByTPSegment[tp]);
			}
			
			System.out.println(Arrays.toString(contributionByTP));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void womVolumenByBrandTest() {
		//OutputFile File
		String file = testFolder+"womVolumen_id29.json";
		
		try {
			//Read JSON files
			BufferedReader br = new BufferedReader(new FileReader(file));
			
			StringBuilder buffer = new StringBuilder();
			String line;
			while((line = br.readLine())!=null) {
				buffer.append(line);
			}
			br.close();
			
			String stringConfig = buffer.toString();
			
			//Get Values
			Gson gson = new Gson();
			
			SimulationResult result = gson.fromJson(stringConfig, SimulationResult.class);
			
			double[][][] volumeByBrandBySegByMC=result.womVolumeByBrandBySegMC;
			
			double[][][] minVolumeByBrandBySegByStep=result.womVolumeByBrandBySegByStepMin;
			double[][][] avgVolumeByBrandBySegByStep=result.womVolumeByBrandBySegByStepAvg;
			double[][][] maxVolumeByBrandBySegByStep=result.womVolumeByBrandBySegByStepMax;
			
			int brands = minVolumeByBrandBySegByStep.length;

			double[] min = new double [brands];
			double[] avg = new double [brands];
			double[] max = new double [brands];
			
			double[][] minSeg = new double [brands][];
			double[][] avgSeg = new double [brands][];
			double[][] maxSeg = new double [brands][];			
			
			double[][] minByStep = new double [brands][];
			double[][] avgByStep = new double [brands][];
			double[][] maxByStep = new double [brands][];
			
			for (int b = 0; b<brands; b++) {
				int segments = volumeByBrandBySegByMC[b].length;
				
				minSeg[b] = new double [segments];
				avgSeg[b] = new double [segments];
				maxSeg[b] = new double [segments];
				
				int steps = minVolumeByBrandBySegByStep[b][0].length;

				minByStep[b] = new double [steps];
				avgByStep[b] = new double [steps];
				maxByStep[b] = new double [steps];
				
				
				for (int seg = 0; seg<segments; seg++) {
					for (int step = 0; step<steps; step++) {
						minByStep[b][step] += minVolumeByBrandBySegByStep[b][seg][step];
						avgByStep[b][step] += avgVolumeByBrandBySegByStep[b][seg][step];
						maxByStep[b][step] += maxVolumeByBrandBySegByStep[b][seg][step];
					}
					
					minSeg[b][seg] = NumberUtils.min(volumeByBrandBySegByMC[b][seg]);
					avgSeg[b][seg] = StatUtils.mean(volumeByBrandBySegByMC[b][seg]);
					maxSeg[b][seg] = NumberUtils.max(volumeByBrandBySegByMC[b][seg]);
				}
				
				min[b] = StatUtils.sum(minSeg[b]);
				avg[b] = StatUtils.sum(avgSeg[b]);
				max[b] = StatUtils.sum(maxSeg[b]);
				
				
				double max_difference = avg[b]*0.001;
				if(!Functions.equals(avg[b], StatUtils.sum(avgByStep[b]), max_difference)) {
					fail();
				}
			}
			
			System.out.println(Arrays.toString(avg));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void womVolumenByDriverTest() {
		//OutputFile File
		String file = testFolder+"womVolumen_id29.json";
		
		try {
			//Read JSON files
			BufferedReader br = new BufferedReader(new FileReader(file));
			
			StringBuilder buffer = new StringBuilder();
			String line;
			while((line = br.readLine())!=null) {
				buffer.append(line);
			}
			br.close();
			
			String stringConfig = buffer.toString();
			
			//Get Values
			Gson gson = new Gson();
			
			SimulationResult result = gson.fromJson(stringConfig, SimulationResult.class);
			
			double[][][] volumeByDriverBySegByMC=result.womVolumeByDriverBySegMC;
			
			double[][][] minVolumeByDriverBySegByStep=result.womVolumeByDriverBySegByStepMin;
			double[][][] avgVolumeByDriverBySegByStep=result.womVolumeByDriverBySegByStepAvg;
			double[][][] maxVolumeByDriverBySegByStep=result.womVolumeByDriverBySegByStepMax;
			
			int drivers = minVolumeByDriverBySegByStep.length;

			double[] min = new double [drivers];
			double[] avg = new double [drivers];
			double[] max = new double [drivers];
			
			double[][] minSeg = new double [drivers][];
			double[][] avgSeg = new double [drivers][];
			double[][] maxSeg = new double [drivers][];			
			
			double[][] minByStep = new double [drivers][];
			double[][] avgByStep = new double [drivers][];
			double[][] maxByStep = new double [drivers][];
			
			for (int d = 0; d<drivers; d++) {
				int segments = volumeByDriverBySegByMC[d].length;
				
				minSeg[d] = new double [segments];
				avgSeg[d] = new double [segments];
				maxSeg[d] = new double [segments];
				
				int steps = minVolumeByDriverBySegByStep[d][0].length;

				minByStep[d] = new double [steps];
				avgByStep[d] = new double [steps];
				maxByStep[d] = new double [steps];
				
				
				for (int seg = 0; seg<segments; seg++) {
					for (int step = 0; step<steps; step++) {
						minByStep[d][step] += minVolumeByDriverBySegByStep[d][seg][step];
						avgByStep[d][step] += avgVolumeByDriverBySegByStep[d][seg][step];
						maxByStep[d][step] += maxVolumeByDriverBySegByStep[d][seg][step];
					}
					
					minSeg[d][seg] = NumberUtils.min(volumeByDriverBySegByMC[d][seg]);
					avgSeg[d][seg] = StatUtils.mean(volumeByDriverBySegByMC[d][seg]);
					maxSeg[d][seg] = NumberUtils.max(volumeByDriverBySegByMC[d][seg]);
				}
				
				min[d] = StatUtils.sum(minSeg[d]);
				avg[d] = StatUtils.sum(avgSeg[d]);
				max[d] = StatUtils.sum(maxSeg[d]);
				
				
				double max_difference = avg[d]*0.001;
				if(!Functions.equals(avg[d], StatUtils.sum(avgByStep[d]), max_difference)) {
					fail();
				}
			}
			
			System.out.println(Arrays.toString(avg));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void womReachTest() {
		//OutputFile File
		String file = testFolder+"womReach_id29.json";
		
		try {
			//Read JSON files
			BufferedReader br = new BufferedReader(new FileReader(file));
			
			StringBuilder buffer = new StringBuilder();
			String line;
			while((line = br.readLine())!=null) {
				buffer.append(line);
			}
			br.close();
			
			String stringConfig = buffer.toString();
			
			//Get Values
			Gson gson = new Gson();
			
			SimulationResult result = gson.fromJson(stringConfig, SimulationResult.class);
			
						
			double[][][] minReachByBrandBySegByStep=result.womReachByBrandBySegByStepMin;
			double[][][] avgReachByBrandBySegByStep=result.womReachByBrandBySegByStepAvg;
			double[][][] maxReachByBrandBySegByStep=result.womReachByBrandBySegByStepMax;
			
			int brands = minReachByBrandBySegByStep.length;		
			
			double[][] minByStep = new double [brands][];
			double[][] avgByStep = new double [brands][];
			double[][] maxByStep = new double [brands][];
			
			for (int d = 0; d<brands; d++) {
				int segments = minReachByBrandBySegByStep[d].length;
				
				int steps = minReachByBrandBySegByStep[d][0].length;

				minByStep[d] = new double [steps];
				avgByStep[d] = new double [steps];
				maxByStep[d] = new double [steps];
				
				
				for (int seg = 0; seg<segments; seg++) {
					for (int step = 0; step<steps; step++) {
						minByStep[d][step] += minReachByBrandBySegByStep[d][seg][step];
						avgByStep[d][step] += avgReachByBrandBySegByStep[d][seg][step];
						maxByStep[d][step] += maxReachByBrandBySegByStep[d][seg][step];
					}
				}
			}
			
			System.out.println(Arrays.toString(avgByStep));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void womContributionsTest() {
		//OutputFile File
		String file = testFolder+"womContributions_id29.json";
		
		try {
			//Read JSON files
			BufferedReader br = new BufferedReader(new FileReader(file));
			
			StringBuilder buffer = new StringBuilder();
			String line;
			while((line = br.readLine())!=null) {
				buffer.append(line);
			}
			br.close();
			
			String stringConfig = buffer.toString();
			
			//Get Values
			Gson gson = new Gson();
			
			SimulationResult result = gson.fromJson(stringConfig, SimulationResult.class);
			
			double[][][][] womContributionsByTpBrandSegMC = result.womContributionByTpByBrandBySegMC;

			int tp = womContributionsByTpBrandSegMC.length;
			
			int brands = womContributionsByTpBrandSegMC[0].length;
			
			double[][][] womContributionsByTpBrandSegMin = new double [tp][brands][];
			double[][][] womContributionsByTpBrandSegAvg = new double [tp][brands][];
			double[][][] womContributionsByTpBrandSegMax = new double [tp][brands][];
			
			double[][] womContributionsBrandTpMin = new double [brands][tp];
			double[][] womContributionsBrandTpAvg = new double [brands][tp];
			double[][] womContributionsBrandTpMax = new double [brands][tp];
			
			double[] womContributionsBrandMin = new double [brands];
			double[] womContributionsBrandAvg = new double [brands];
			double[] womContributionsBrandMax = new double [brands];
			
			for (int b = 0; b<brands; b++) {
				for (int t = 0; t<tp; t++) {
					int segments = womContributionsByTpBrandSegMC[0][b].length;
					
					womContributionsByTpBrandSegMin[t][b] = new double [segments];
					womContributionsByTpBrandSegAvg[t][b] = new double [segments];
					womContributionsByTpBrandSegMax[t][b] = new double [segments];
					
					
					for (int seg = 0; seg<segments; seg++) {
						womContributionsByTpBrandSegMin[t][b][seg] = 
								StatUtils.min(womContributionsByTpBrandSegMC[t][b][seg]);
						womContributionsByTpBrandSegAvg[t][b][seg] = 
								StatUtils.mean(womContributionsByTpBrandSegMC[t][b][seg]);
						womContributionsByTpBrandSegMax[t][b][seg] = 
								StatUtils.max(womContributionsByTpBrandSegMC[t][b][seg]);
					}
					
					womContributionsBrandTpMin[b][t] = 
							StatUtils.sum(womContributionsByTpBrandSegMin[t][b]);
					womContributionsBrandTpAvg[b][t] = 
							StatUtils.sum(womContributionsByTpBrandSegAvg[t][b]);
					womContributionsBrandTpMax[b][t] = 
							StatUtils.sum(womContributionsByTpBrandSegMax[t][b]);
				}
				
				womContributionsBrandMin[b] = StatUtils.sum(womContributionsBrandTpMin[b]);
				womContributionsBrandAvg[b] = StatUtils.sum(womContributionsBrandTpAvg[b]);
				womContributionsBrandMax[b] = StatUtils.sum(womContributionsBrandTpMax[b]);
			}
			
			System.out.println(Arrays.toString(womContributionsBrandAvg));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
