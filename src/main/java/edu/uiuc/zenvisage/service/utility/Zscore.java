/**
 * 
 */
package edu.uiuc.zenvisage.service.utility;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.util.FastMath;

/**
 *
 */
public class Zscore implements Normalization {

	/* (non-Javadoc)
	 * @see normalization.Normalization#normalize(double[])
	 * Perform ZScore normalization.
	 */
	@Override
	public void normalize(double[] normalizedValues) {
		// TODO Auto-generated method stub
		double mean = StatUtils.mean(normalizedValues);
		double std = FastMath.sqrt(StatUtils.variance(normalizedValues));
		for(int i = 0; i < normalizedValues.length; i++) {
			if (std == 0)
				normalizedValues[i] = 0;
			else 
				normalizedValues[i] = (normalizedValues[i] - mean) / std;
		}
	}

}
