package test.junit;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
		TestDecisionMaking.class, 
		TestProductUsage.class,
		TestSalesScheduler.class, 
		TestTPScheduler.class , 
		TestDistributedPerceptions.class ,
		TestFunctions.class
	})
public class AllTests {

}
