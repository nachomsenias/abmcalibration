package calibration.fitness.history;

import org.apache.commons.math3.stat.StatUtils;

import util.functions.ArrayFunctions;
import util.functions.MatrixFunctions;

public class ScoreBean {

	public class ScoreWrapper {
		public ScoreBean historySalesScore;
		public ScoreBean totalSalesScore;
		
		public ScoreBean awarenessScore;
		public ScoreBean totalAwarenessScore;
		
		public ScoreBean[] perceptionsScore;
		
		public ScoreBean womVolumeScore;
		
		public double finalScore = 100.0;
		
		public String printGlobalScores() {
			StringBuffer buffer = new StringBuffer("Aggregated score: "+finalScore);
			if(historySalesScore!=null) {
				buffer.append("\n	History sales score: " +
						historySalesScore.score);
			}
			if(totalSalesScore!=null) {
				buffer.append("\n	Total sales score: " +
						totalSalesScore.score);
			}
			if(awarenessScore!=null) {
				buffer.append("\n	Awareness score: " +
						awarenessScore.score);
			}
			if(perceptionsScore!=null) {
				buffer.append("\n	Perceptions score: [ "); 
				for (ScoreBean p:perceptionsScore) {
					buffer.append(p.score+" ");
				}
				buffer.append("]");
			}
			if(womVolumeScore!=null) {
				buffer.append("\n	WoM Volume score: " +
						womVolumeScore.score);
			}
			return buffer.toString();
		}
	}
	
	private double score;
	private double[] scoreByBrand;
	private double[][] scoreByBrandBySegment;
	
	public ScoreBean() {}
	
	public ScoreBean(double[] scoreByBrand) {
		this.scoreByBrand = scoreByBrand;
		score = StatUtils.mean(scoreByBrand);		
	}
	
	public ScoreBean(double[][] scoreByBrandBySegment) {
		this.scoreByBrandBySegment = scoreByBrandBySegment;
		
		int brands = scoreByBrandBySegment.length;
		this.scoreByBrand = new double [brands];
		for (int b=0; b<brands; b++) {
			scoreByBrand[b] = StatUtils.mean(scoreByBrandBySegment[b]);
		}
		score = StatUtils.mean(scoreByBrand);
	}
	
	/*
	 * GETTERS & SETTERS.
	 */

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public double[] getScoreByBrand() {
		return scoreByBrand;
	}

	public void setScoreByBrand(double[] scoreByBrand) {
		this.scoreByBrand = scoreByBrand;
	}

	public double[][] getScoreByBrandBySegment() {
		return scoreByBrandBySegment;
	}

	public void setScoreByBrandBySegment(double[][] scoreByBrandBySegment) {
		this.scoreByBrandBySegment = scoreByBrandBySegment;
	}
	
	/*
	 * STATIC MANIPULATION METHODS.
	 */
	
	public static ScoreBean linearCombination(ScoreBean first, 
			ScoreBean second, double alpha) {
		
		ScoreBean combined;
		
		if(first.scoreByBrandBySegment!=null) {
			//Segment detail
			combined = new ScoreBean(
					MatrixFunctions.linearCombination(
							first.scoreByBrandBySegment, 
							second.scoreByBrandBySegment, alpha));
		} else {
			//Brand detail
			combined = new ScoreBean(
					ArrayFunctions.linearCombination(
							first.scoreByBrand, second.scoreByBrand, alpha));
		}
		
		return combined;
	}
	
	public static ScoreBean mergeBeans(ScoreBean[] beans) {
		ScoreBean base = beans[0];
		
		ScoreBean result;
		int iterations = beans.length;
		int brands = base.scoreByBrand.length;
		
		if(base.scoreByBrandBySegment!=null) {
			int segments = base.scoreByBrandBySegment[0].length;
			double[][] scoreByBrandBySegment = new double[brands][segments];
			
			for (int b =0; b<brands; b++) {
				for (int seg =0; seg<segments; seg++) {
					double[] auxMC = new double[iterations];
					for (int mc = 0; mc<iterations; mc++) {
						auxMC[mc] = beans[mc].scoreByBrandBySegment[b][seg];
					}
					scoreByBrandBySegment[b][seg] = StatUtils.mean(auxMC);
				}
			}
			
			result = new ScoreBean(scoreByBrandBySegment);
		} else {
			double[] scoreByBrand = new double[brands];
			for (int b =0; b<brands; b++) {
				double[] auxMC = new double[iterations];
				for (int mc = 0; mc<iterations; mc++) {
					auxMC[mc] = beans[mc].scoreByBrand[b];
				}
				scoreByBrand[b] = StatUtils.mean(auxMC);
			}
			
			result = new ScoreBean(scoreByBrand);
		}
		
		return result;
	}
	
	public static double getAverageScore(ScoreBean[] beans) {
		double score = 0;
		
		for (ScoreBean bean : beans) {
			score+=bean.score;
		}
		score/=beans.length;
		return score;
	}
}
