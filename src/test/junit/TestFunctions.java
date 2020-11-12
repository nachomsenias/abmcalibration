package test.junit;

import static org.junit.Assert.fail;

import org.junit.Test;

import util.functions.ArrayFunctions;
import util.random.Randomizer;
import util.random.RandomizerFactory;
import util.random.RandomizerFactory.RandomizerAlgorithm;
import util.random.RandomizerUtils;

public class TestFunctions {
	
	public static final int NUM_ITERATIONS=10;

	@Test
	public void testRandomIndex() {
		
		for (int it=0; it<NUM_ITERATIONS; it++) {
			Randomizer random = RandomizerFactory.createRandomizer(
					RandomizerAlgorithm.XOR_SHIFT_128_PLUS_FAST, 
					RandomizerUtils.PRIME_SEEDS[it]
				);
			
			boolean[][] boolTest = {
					{false,true,true},
					{false,true,false,false,true},
					{true,false,true,true},
					{false,true,true,false,false,true},
					{false,false,true,true,false,true},
					{true,true,false,false,false,true},
				};
			
			for (boolean[] elements: boolTest) {
				int index = ArrayFunctions.selectRandomIndex(elements, random);
				
				boolean success = index<elements.length 
						&& index > -1 
						&& elements[index];
				
				if(!success) {
					fail("Fail Random index: "+index+" for array "+elements);
				}
			}
			
			
		}
		
	}

}
