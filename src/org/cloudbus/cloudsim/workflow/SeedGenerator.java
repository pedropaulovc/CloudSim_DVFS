package org.cloudbus.cloudsim.workflow;

/**
 * This class supplies seeds for random numbers generators. This guarantees that all the
 * policies have exactly the same seeds for a given simulation round, so they experience
 * similar conditions during execution.
 */

public class SeedGenerator {

	static long[] seed = { 6800658, 1895751, 2896999, 3748339, 7469132,
							4173412, 6667379, 8362230, 2674225, 817803,
							2629478, 3269132, 6735181, 2169443, 4157783,
							2643818, 6202665, 1871073, 423686,  4569684,
							6471887, 2893691, 7657629, 4678254, 9677810,
							6801407, 5879245, 2774575, 8146748, 7428661,
							2444081, 2872834, 8599880, 2728013, 9019466,
							21364,   3405579, 7284436, 5053251, 2423951,
							3853226, 255516,  9190858, 6100288, 1200980,
							4498387, 2499233, 1759202, 7432971, 3322528};
			
	public static long getSeed(int round) {
		return seed[round];
	}
}
