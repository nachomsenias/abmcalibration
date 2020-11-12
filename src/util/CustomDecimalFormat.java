package util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class CustomDecimalFormat {

	public static DecimalFormat getFormat() {
		return (DecimalFormat) NumberFormat.getNumberInstance(new Locale("en", "UK"));
	}
}
