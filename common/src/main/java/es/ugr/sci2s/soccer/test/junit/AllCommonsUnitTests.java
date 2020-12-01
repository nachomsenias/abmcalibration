package es.ugr.sci2s.soccer.test.junit;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
	ExceptionUnitTest.class,
	OutputTest.class,
	StatisticTest.class,
	TranslationUnitTest.class
	})

public class AllCommonsUnitTests {

}
