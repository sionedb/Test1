package man;

import static man.RandomGen.ACCEPTABLE_ERROR;
import static man.RandomGenTest.EXAMPLE_NUM;
import static man.RandomGenTest.EXAMPLE_PROB;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

/*
* Tests on the helper class {@link RandomGenSummarizer} public and protected methods.
* 
*/
public class RandomGenSummarizerTest {

  ///////////////////////////////////////////////////
  // Test calculation methods with seed value
  ///////////////////////////////////////////////////

  @Test
  public void testChi2() {
    final int iterations = 1000;
    final float tolerance = iterations * ACCEPTABLE_ERROR;

    printOutput("calcChi2 with generator with seed for example case and trivial case");

    RandomGenSummarizer summarizer = buildRandomGenSummarizer(1000, 15);
    assertEquals("Chi-squared", 5.97574f, summarizer.calcChi2(), tolerance);

    RandomGenSummarizer summarizer2 = buildTrivialRandomGenSummarizer(iterations, 19);
    assertEquals("Chi-squared for trivial case", 0.0f, summarizer2.calcChi2(), tolerance);
  }

  @Test
  public void testStandardErrorOfMean() {
    final int iterations = 1000;
    final float tolerance = iterations * ACCEPTABLE_ERROR;

    printOutput(
        "calcStandardErrorOfMean with generator with seed for example case and trivial case");

    RandomGenSummarizer summarizer = buildRandomGenSummarizer(1000, 15);
    assertEquals("StandardErrorOfMean", 0.0039f, summarizer.calcStandardErrorOfMean(), tolerance);

    RandomGenSummarizer summarizer2 = buildTrivialRandomGenSummarizer(iterations, 19);
    assertEquals("StandardErrorOfMean for trivial case", 0.0f,
        summarizer2.calcStandardErrorOfMean(), tolerance);
  }

  @Test
  public void testStdErrorOfMean() {
    final int iterations = 1000;
    final float tolerance = iterations * ACCEPTABLE_ERROR;

    printOutput("calcStdErrorOfMean with given std deviation");

    RandomGenSummarizer summarizer = buildRandomGenSummarizer(1000, 15);
    assertEquals("SEM with  Standard deviation", 0.0039f, summarizer.calcStdErrorOfMean(0.0087f),
        tolerance);

    RandomGenSummarizer summarizer2 = buildTrivialRandomGenSummarizer(iterations, 19);
    assertEquals("SEM with zero Standard deviation", 0.0f, summarizer2.calcStdErrorOfMean(0.0f),
        tolerance);
    assertEquals("SEM with single value", 0.3f, summarizer2.calcStdErrorOfMean(0.3f), tolerance);
  }

  @Test
  public void testCalcMean() {
    RandomGenSummarizer summarizer = buildRandomGenSummarizer(1000, 13);

    printOutput("calcMean ");

    final float[] values1 = { 0.3f, 0.4f, -1.5f, 2.6f };
    assertEquals("Mean of 4 floats", 0.45f, summarizer.calcMean(values1), ACCEPTABLE_ERROR);
    final float[] values2 = new float[0];
    assertEquals("Mean of 0 floats", 0.0f, summarizer.calcMean(values2), ACCEPTABLE_ERROR);
    final float[] values3 = { 0.3f };
    assertEquals("Mean of 1 float", 0.3f, summarizer.calcMean(values3), ACCEPTABLE_ERROR);
  }

  @Test
  public void testFundamentalStdDevCalculation() {
    printOutput("calcFundamentalStdDevCalculation ");
    final int iterations = 1000;
    final float tolerance = iterations * ACCEPTABLE_ERROR;
    RandomGenSummarizer summarizer = buildRandomGenSummarizer(iterations, 14);

    final float[] values1 = { 1.3f, 2.4f, -1.5f, -1.6f };
    final float mean = 0.15f;
    assertEquals("Standard deviation", 2.01412f, summarizer.calcStdDev(values1, mean), tolerance);
    final float[] values2 = new float[0];
    assertEquals("Standard deviation of 0 floats", 0.0f, summarizer.calcStdDev(values2, 0.0f),
        tolerance);
    final float[] values3 = { 0.3f };
    assertEquals("Standard deviation of 1 float", 0.0f, summarizer.calcStdDev(values3, 0.0f),
        tolerance);
  }

  @Test
  public void testCalcStandardDeviation() {
    final int iterations = 1000;
    final float tolerance = iterations * ACCEPTABLE_ERROR;

    printOutput("calculate standard deviation for example and trivial case ");

    RandomGenSummarizer summarizer = buildRandomGenSummarizer(iterations, 15);
    assertEquals("Standard deviation calculation", 0.0087f, summarizer.calcStandardDeviation(),
        tolerance);

    RandomGenSummarizer summarizer2 = buildTrivialRandomGenSummarizer(iterations, 19);
    assertEquals("Standard deviation of single float", 0.0f, summarizer2.calcStandardDeviation(),
        tolerance);

  }

  @Test
  public void testCalcDeviations() {
    final int iterations = 1000;
    final float tolerance = iterations * ACCEPTABLE_ERROR;

    printOutput("calculate array of deviations for example case ");

    RandomGenSummarizer summarizer = buildRandomGenSummarizer(iterations, 15);

    final float[] expected = { 0.002f, 0.022f, 0.01f, 0.017f, 0.003f };
    final float[] deviations = summarizer.calculateDeviations();
    assertEquals("Unexpected size", expected.length, deviations.length);
    for (int i = 0; i < deviations.length; i++) {
      assertEquals("Deviation at index " + i, expected[i], deviations[i], tolerance);
    }
  }

  @Test
  public void testGetSummary() {
    printOutput("Summary string built ");
    RandomGenSummarizer summarizer = buildRandomGenSummarizer(1000, 15);

    final String line = "=========================================================================";
    final String summary = String
        .format("%nFor an array of k=5 integers, after n=1000 attempts: chi squared statistic= "
            + "5.9757, total root squared deviation=0.0540, std error of mean= 0.0039 %n");

    assertEquals("Incorrect summary with no breakdown given", line + summary,
        summarizer.getSummary(false));

    final String nl = String.format("%n");
    final StringBuilder exp = new StringBuilder(line);
    exp.append(nl)
        .append(" Random | Probability | Actual Occurrences | Chi squared        | Deviation ");
    exp.append(nl)
        .append(" Number |  pi         |  Oi                | (pi*n-Oi)^2/(pi*n) |  |pi - Oi/n| ");
    exp.append(nl).append(line);
    exp.append(nl)
        .append(" -1     | 0.0100      |        12 times    | 0.4000             | 0.0020       ");
    exp.append(nl)
        .append(" 0      | 0.3000      |       322 times    | 1.6133             | 0.0220       ");
    exp.append(nl)
        .append(" 1      | 0.5800      |       570 times    | 0.1724             | 0.0100       ");
    exp.append(nl)
        .append(" 2      | 0.1000      |        83 times    | 2.8900             | 0.0170       ");
    exp.append(nl)
        .append(" 3      | 0.0100      |        13 times    | 0.9000             | 0.0030       ");
    exp.append(summary);

    assertEquals("Incorrect summary with full breakdown given", exp.toString(),
        summarizer.getSummary(true));
  }

  /////////////////////////////////
  // Utility methods
  /////////////////////////////////

  protected static void printOutput(final String testName) {
    printOutput(testName, null);
  }

  protected static void printOutput(final String testName, final String output) {
    final String line = "****************************************************************";
    final String title = String.format("%s%nTest %s%n", line, testName);
    System.out.println(title);
    if (output != null) {
      System.out.println(output);
      System.out.println(String.format("%n%s%n", line));
    }
  }

  protected static void printSummary(final String testName, final RandomGenSummarizer summarizer,
      final boolean showBreakdown) {
    printOutput(testName, summarizer.getSummary(showBreakdown));
  }

  protected static RandomGenSummarizer getAndPrintSummary(final String testName,
      final RandomGen gen, final boolean showBreakdown) {
    final RandomGenSummarizer summarizer = new RandomGenSummarizer(gen);
    printSummary(testName, summarizer, showBreakdown);
    return summarizer;
  }

  private RandomGenSummarizer buildRandomGenSummarizer(final int iterations, final long seed) {
    final RandomGen generator = new RandomGen(EXAMPLE_NUM, EXAMPLE_PROB, seed);
    RandomGenTest.runGenerator(iterations, generator);
    return new RandomGenSummarizer(generator);
  }

  private RandomGenSummarizer buildTrivialRandomGenSummarizer(final int iterations,
      final long seed) {
    final int[] single = { 4 };
    final float[] singlep = { 1.0f };
    final RandomGen generator = new RandomGen(single, singlep, seed);
    RandomGenTest.runGenerator(iterations, generator);
    return new RandomGenSummarizer(generator);
  }

}
