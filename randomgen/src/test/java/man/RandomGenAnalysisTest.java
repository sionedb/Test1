package man;

import static man.RandomGenTest.EXAMPLE_NUM;
import static man.RandomGenTest.EXAMPLE_PROB;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * 
 * Further tests on class {@link RandomGen}.
 * 
 * <p>
 * These tests are repeatable but not absolute certainity of passing every time.
 * 
 * Two types of test are performed:
 * 
 * <li>ensure that the standard error of mean is improving as the number of calls to nextNum()
 * increases.
 * <li>using chi-squared statistic to ensure that 99% or 95% of the time the generator behaves as
 * expected.
 * <p>
 * Note: For the chi-squared test, the expected frequency count of at least 5 in each population and
 * each categorical variable is needed.
 * 
 * 
 */
public class RandomGenAnalysisTest {
  private final static Map<Integer, Float> CHI_01 = createChi2MapSignificance1pc();
  private final static Map<Integer, Float> CHI_05 = createChi2MapSignificance5pc();

  /**
   * Lookup for chi-square, X2, statistic having k degrees of freedom and significance level P=0.01
   * E.g. for 4 degrees of freedom, find probability(Χ2 > cv) = 0.01, where cv = 13.3
   * 
   * @return Map of k -> critical value for X2
   */
  private static Map<Integer, Float> createChi2MapSignificance1pc() {
    Map<Integer, Float> result = new HashMap<>();
    result.put(new Integer(4), new Float(13.3));
    result.put(new Integer(9), new Float(21.7));
    result.put(new Integer(50), new Float(76.2));
    return Collections.unmodifiableMap(result);
  }

  /**
   * Lookup for chi-square, X2, statistic having k degrees of freedom and significance level P=0.05
   * E.g. for 4 degrees of freedom, find probability(Χ2 > cv) = 0.05, where cv = 13.3
   * 
   * @return Map of k -> critical value for X2
   */
  private static Map<Integer, Float> createChi2MapSignificance5pc() {
    Map<Integer, Float> result = new HashMap<>();
    result.put(new Integer(4), new Float(9.49));
    result.put(new Integer(9), new Float(16.9));
    result.put(new Integer(50), new Float(67.5));
    return Collections.unmodifiableMap(result);
  }

  @Test
  public void testExampleChi2() {
    // To ensure frequency count was at least 5 in each population at each level
    final int iterations = 1000;
    testChi2("chi-squared test for 1000 iterations of example", 10, iterations, EXAMPLE_NUM,
        EXAMPLE_PROB);
  }

  @Test
  public void testExampleChi2LargeIteration() {
    final int iterations = 10000000;
    testChi2("chi-squared test for 10million iterations of example", 10, iterations, EXAMPLE_NUM,
        EXAMPLE_PROB);
  }

  @Test
  public void testUniformDistribution() {
    final int nChoice = 51;
    final int[] nums = new int[nChoice];
    final float[] prob = new float[nChoice];
    final float p0 = 1.0f / nChoice;
    for (int i = 0; i < nChoice; i++) {
      nums[i] = i;
      prob[i] = p0;
    }
    final int iterations = 1000000;
    testChi2(
        "chi-squared test where equal probability of one of 50 numbers after 1million iterations ",
        1, iterations, nums, prob, 1);
  }

  @Test
  public void testLargeNumberOfChoices() {
    final int nChoice = 1000000;
    final int[] nums = new int[nChoice];
    final float[] prob = new float[nChoice];
    final float p0 = 0.5f / nChoice;
    final float p1 = 2 * p0;
    final float p2 = 3 * p0;
    for (int i = 0; i < nChoice; i++) {
      nums[i] = i;
      // if i divisible by 3 then probability=1/(2k) where k is number of choices for i
      // if remainder is 1 or 2 then probability 1/k or 3/(2k) respectively
      // e.g. twice as likely to chose 1 compared to 0, and 3 times as likely to choose 2 compared
      // to 0
      int mod3 = i % 3;
      if (mod3 == 2) {
        prob[i] = p2;
      } else if (mod3 == 1) {
        prob[i] = p1;
      } else {
        prob[i] = p0;
      }
    }

    // for million choices, and 10million iterations the expected occurrence of each is at least 10
    final int iterations = 10 * nChoice;
    testChi2("chi-squared for 10million iterations with large number of choices", 10, iterations,
        EXAMPLE_NUM, EXAMPLE_PROB, 1);
  }

  @Test
  public void showImprovementWithMoreIterations() {

    final RandomGen generator = new RandomGen(EXAMPLE_NUM, EXAMPLE_PROB);

    // Calculate SEM after 100, 10 000, 1 000 000, 100 000 0000 iterations
    int previousCount = 0;
    double previousSEM = 1;
    StringBuilder sb = new StringBuilder();
    for (int i = 1; i < 5; i++) {

      int newCount = (int) Math.pow(10, 2 * i);
      int iterations = newCount - previousCount;
      previousCount = newCount;

      RandomGenTest.runGenerator(iterations, generator);
      RandomGenSummarizer summarizer = new RandomGenSummarizer(generator);
      double sem = summarizer.calcStandardErrorOfMean();
      assertTrue(" Error reducing as number of iterations increases", previousSEM > sem);
      previousSEM = sem;
      sb.append(String.format("After %-9d iterations, SEM=%9.8f %n", newCount, sem));
    }

    RandomGenSummarizerTest.printOutput(
        "ensure that error reduces as number of iterations of nextNum increases", sb.toString());

  }

  /////////////////////////////////
  // Chi2 test helper methods
  /////////////////////////////////

  private void testChi2(final String testName, final int ntimes, final int iterations,
      final int[] randomNums, final float[] probabilities) {
    // Run test ntimes for significance level 1%
    testChi2(testName, ntimes, iterations, randomNums, probabilities, 1);

    // Run test ntimes for significance level 5%
    testChi2(testName, ntimes, iterations, randomNums, probabilities, 5);

  }

  /**
   * The chi-squared test is used to determine whether there is a significant difference between the
   * expected frequencies and the observed frequencies in one or more categories.
   * 
   * Null Hypothesis is that 'data consistent with specified distribution'.
   * 
   * At a significance level P=1%, null Hypothesis will be rejected if chi2 value> cv_0.01
   * 
   * @param testName
   *          Test description
   * @param ntimes
   *          Number of times new generator is created, called and Chi2 is calculated
   * @param iterations
   *          Number of times nextNum() is called
   * @param randomNums
   *          Array of integers (positive or negative) that maybe generated
   * @param probabilities
   *          Array of corresponding probabilities
   */
  private void testChi2(final String testName, final int ntimes, final int iterations,
      final int[] randomNums, final float[] probabilities, final int significanceLevel) {

    // Find the critical value such that for given significance level,
    // that a chi-square statistic, having (k-1) degrees of freedom,
    // is more extreme than critical value.
    final Integer degreesFreedom = new Integer(randomNums.length - 1); // k-1
    final float criticalVal = getCriticalValue(degreesFreedom, significanceLevel);

    // Create and run new generator ntimes.
    final List<Float> extremeChi2 = new ArrayList<>();
    final float chi2Total = runGeneratorsCalcChi2(testName, ntimes, iterations, randomNums,
        probabilities, criticalVal, extremeChi2);
    final float meanChi2 = chi2Total / ntimes;

    // Log some info
    final String info = getChi2Info(testName, ntimes, iterations, degreesFreedom, significanceLevel,
        criticalVal, meanChi2, extremeChi2);
    RandomGenSummarizerTest.printOutput(testName, info);

    // For each simulation it is only in extreme cases that chi2> criticalVal
    // Therefore to make unit test repeatable, it is only in very rare cases
    // that the mean could exceed the critical value.
    assertTrue(info, (meanChi2 <= criticalVal));

  }

  /**
   * Get critical value for chi-squared test for a given number of degrees of freedom and
   * significance level
   * 
   * @param degreesFreedom
   *          integer zero or greater
   * @param significance
   *          identifier for the significance level 1: P=0.01 (1%), 5: P=0.05 (5%)
   * 
   * @return critical value for the chi-squared test
   */
  private float getCriticalValue(final int degreesFreedom, final int significance) {
    final Float criticalVal;
    switch (significance) {
    case 1:
      criticalVal = CHI_01.get(degreesFreedom);
      break;
    case 5:
      criticalVal = CHI_05.get(degreesFreedom);
      break;
    default:
      throw new IllegalArgumentException(String.format(
          "Unable to find chi-squared critical value for significance level %3.1f", significance));
    }
    if (criticalVal == null) {
      throw new IllegalArgumentException(String.format(
          "Unable to find chi-squared critical value for %d degrees freedom ", degreesFreedom));
    }
    return criticalVal;
  }

  /**
   * Get descriptive information about chi-squared test
   * 
   * @param testName
   * @param ntimes
   * @param iterations
   * @param df
   * @param significance
   * @param cv
   * @param meanChi2
   * @param extremeChi2
   * @return
   */
  private String getChi2Info(final String testName, final int ntimes, final int iterations,
      final int df, final int significance, final float cv, final float meanChi2,
      final List<Float> extremeChi2) {

    return String.format(
        "Chi-squared test where Null Hypothesis is that "
            + "'data consistent with specified distribution'.%n"
            + "For %d degrees of freedom and significance level P=%3.2f, "
            + "if chi2 > %3.1f then we should reject null hypothesis. %n"
            + "For %d simulations where a new generator is called %d times, "
            + " average value for chi2 is %4.2f.%n"
            + "For individual simulations, extreme cases where chi2> %3.1f are %s %n",
        df, (double) significance / 100, cv, ntimes, iterations, meanChi2, cv,
        extremeChi2.toString());
  }

  /**
   * Create ntimes generators, and for each call nextNum() repeatedly for given number of iterations
   * Calculate the chi-squared statistic, and keep track of those that are greater than critical
   * value
   * 
   * @param testName
   * @param ntimes
   * @param iterations
   * @param randomNums
   * @param probabilities
   * @param criticalVal
   * @param extremeChi2
   * @return
   */
  private float runGeneratorsCalcChi2(final String testName, final int ntimes, final int iterations,
      final int[] randomNums, final float[] probabilities, final float criticalVal,
      final List<Float> extremeChi2) {

    float chi2Total = 0.0f;
    for (int j = 0; j < ntimes; j++) {
      final RandomGen generator = RandomGenTest.testGenerator(testName, iterations, randomNums,
          probabilities, false, false);

      final RandomGenSummarizer summarizer = new RandomGenSummarizer(generator);
      final float chi2 = summarizer.calcChi2();

      if (chi2 > criticalVal) {
        extremeChi2.add(chi2);
      }
      chi2Total += chi2;
    }

    return chi2Total;

  }
}
