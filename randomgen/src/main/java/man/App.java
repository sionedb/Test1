package man;

/**
 * Example Application to run Random Generator
 *
 */
public class App 
{
    public static void main( String[] args )
    {
 	final int[] exampleNums= { -1, 0, 1, 2, 3 };
  	final float[] exampleProb = { 0.01f, 0.3f, 0.58f, 0.1f, 0.01f };
	final int iterations = 100;
 	RandomGen gen = new RandomGen(exampleNums, exampleProb);

	StringBuilder sb= new StringBuilder();
	sb.append( "RandomGen with example data: { -1, 0, 1, 2, 3 } with probabilities { 0.01f, 0.3f, 0.58f, 0.1f, 0.01f } \n" );
 	sb.append( "Call ").append(iterations).append(" times.....");
	for (int i = 0; i < iterations; i++) {
     	 sb.append(gen.nextNum()).append(",");
    	}
	
	RandomGenSummarizer summarizer = new RandomGenSummarizer(gen);
	sb.append( "\n").append(summarizer.getSummary(true));
	
	System.out.println(sb.toString());
	 
    }
}
