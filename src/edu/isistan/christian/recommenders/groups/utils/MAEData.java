package edu.isistan.christian.recommenders.groups.utils;

public class MAEData {

	double maeScore;
	double coverage;
	
	public MAEData(double maeScore, double coverage) {
		super();
		this.maeScore = maeScore;
		this.coverage = coverage;
	}

	public double getMaeScore() {
		return maeScore;
	}

	public double getCoverage() {
		return coverage;
	}

	@Override
	public String toString() {
		return "MAEData [mae=" + maeScore + ", coverage=" + coverage + "]";
	}
	
}
