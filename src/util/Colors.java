package util;
import java.awt.Color;

/**
 * Defines a set of colors to be used in the GUI.
 * 
 * @author ktrawinski
 *
 */
public class Colors {

	// ########################################################################
	// Variables
	// ########################################################################
	
	// A number of colors to be used
	// Provided by hand. So far 9 different colors for up to 9 brands/companies
	static int NRCOLORS = 9;
	static int RED = 0;
	static int GREEN = 1;
	static int BLUE = 2;
	

	// An array containing a palette of colors 
	private int[][]palette;
	
	// ########################################################################
	// Constructors
	// ########################################################################
	
	/**
	 * Constructs a palette of colors.
	 * @param PaletteType
	 */
	public Colors(int PaletteType) {
		
		//  So far there are implemented 3 options:
		//  1: Standard - 
		//  www.mulinblog.com/a-color-palette-optimized-for-data-visualization
		//  2: Green - colorbrewer2.com
		//  3: Purple - colorbrewer2.com
		//  4: Qualitative 1st - colorbrewer2.com
		//  5: Qualitative 3rd - colorbrewer2.com
		//  5: Qualitative 4th - colorbrewer2.com
		//  default: Standard
		switch(PaletteType) {
			case 1:
				palette = new int[][]{ 
					{128,128,128},	// grey
					{0,0,255},		// blue
					{255,165,0},	// orange
					{0,128,0},		// green
					{255,192,203},	// pink
					{165,42,42},	// brown
					{128,0,128},	// purple
					{255,255,0},	// yellow
					{255,0,0},	// red
				};
				break;
				
			case 2:
				palette = new int[][]{ 
					{0,69,41},
					{0,104,55},
					{35,132,67},
					{65,171,93},
					{120,198,121},
					{173,221,142},
					{217,240,163},
					{247,252,185},
					{255,255,229},
				};
				break;
				
			case 3:		
				palette = new int[][]{ 
					{77,0,75},
					{129,15,124},
					{136,65,157},
					{140,107,177},
					{140,150,198},
					{158,188,218},
					{191,211,230},
					{224,236,244},
					{247,252,253},
				};
				break;		
				
			case 4:		
				palette = new int[][]{ 
					{166,206,227},
					{31,120,180},
					{178,223,138},
					{51,160,44},
					{251,154,153},
					{227,26,28},
					{253,191,111},
					{255,127,0},
					{202,178,214},	
				};
				break;		
				
			case 5:		
				palette = new int[][]{ 
					{228,26,28},	// red
					{51,51,51},		// blue
					{77,175,74},	// green
					{152,78,163},	// yellow
					{255,127,0},	// pink
					{0,102,204},	// light blue
					{166,86,40},
					{247,129,191},
					{153,153,153},		
				};
				break;	
				
			case 6:		
				palette = new int[][]{ 
					{141,211,199},
					{255,255,179},
					{190,186,218},
					{251,128,114},
					{128,177,211},
					{253,180,98},
					{179,222,105},
					{252,205,229},
					{217,217,217},		
				};
				break;					
		
			default:
				palette = new int[][]{ 
					{128,128,128},	// grey
					{0,0,255},		// blue
					{255,165,0},	// orange
					{0,128,0},		// green
					{255,192,203},	// pink
					{165,42,42},	// brown
					{128,0,128},	// purple
					{255,255,0},	// yellow
					{255,0,0},	// red
				};	
		}
	}

	// ########################################################################
	// Methods/Functions
	// ########################################################################

	/**
	 * Gets the number of colors.
	 * @return - the number of colors.
	 */
	public int getNrColors() {
		return NRCOLORS;
	}

	/**
	 * Gets the palette of colors.
	 * @return - the palette of colors
	 */
	public int[][] getPalette() {
		return palette;
	}

	/**
	 * Sets the palette of colors.
	 * @param palette - the palette of colors
	 */
	public void setPalette(int[][] palette) {
		this.palette = palette;
	}

	/**
	 * Gets the red color of the palette of colors.
	 * @param Ind - the index in the palette.
	 * @return - the red color of the palette.
	 */
	public int getPaletteColorRed(int Ind) {
		return palette[Ind][RED];
	}
	
	/**
	 * Gets the green color of the palette of colors.
	 * @param Ind - the index in the palette.
	 * @return - the green color of the palette.
	 */
	public int getPaletteColorGreen(int Ind) {
		return palette[Ind][GREEN];
	}
	
	/**
	 * Gets the blue color of the palette of colors.
	 * @param Ind - the index in the palette.
	 * @return - the blue color of the palette.
	 */
	public int getPaletteColorBlue(int Ind) {
		return palette[Ind][BLUE];
	}
	
	/** 
	 * Gets the instance of the color class.
	 * @param Ind - the index in the palette.
	 * @return - the color object with set RGB.
	 */
	public Color getPaletteColor(int Ind) {
		
		int red = getPaletteColorRed(Ind);
		int green = getPaletteColorGreen(Ind);
		int blue = getPaletteColorBlue(Ind);
		
		Color col = new Color(red, green, blue);
		
		return col;
	}
}
