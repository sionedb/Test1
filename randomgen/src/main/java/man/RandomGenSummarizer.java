package man;

import static man.RandomGen.ACCEPTABLE_ERROR;

/**
 * Helper class to analyse and summarise for the {@link RandomGen}.
 * 
 * <p>
 * Provides utility methods for the random generator.
 * <li>Calculates the Standard Error of Mean (SEM), standard error of the sample mean is an estimate
 * of how far the sample mean (of the deviations of the number of occurrences compared to the
 * expected probability)is likely to be from the population mean
 * <li>Also calculates the chi-squared statistic. The chi-squared test is used to determine whether
 * there is a significant difference between the expected frequencies and the observed frequencies
 * in one or more categories.
 * 
 * @author sioned.baker
 * @version 1.0
 */
public class RandomGenSummarizer {

  private final RandomValue[] data;
  private final int count;

  /**
   * Constructor of a summary at a given number (count) of draws from the generator.
   * 
   * @param generator
   *          A random number generator {@link RandomGen} that has generated certain number of
   *          random integers.
   * 
   */
  public RandomGenSummarizer(final RandomGen generator) {
    this.count = generator.getCount();

    // Generator already validated that these array are non-zero size and same length
    final int[] randomNums = generator.getRandomNums();
    final float[] probabilities = generator.getProbabilities();
    final int[] occurrences = generator.getOccurrences();
    final int nNum = generator.getDegreesFreedom() + 1;
    if (nNum < 1) {
      throw new IllegalArgumentException(String.format(
          "Expecting RandomGen to have at least one random number choice, however only %d found ",
          nNum));
    }
    this.data = new RandomValue[nNum];
    for (int i = 0; i < nNum; i++) {
      data[i] = new RandomValue(randomNums[i], probabilities[i], occurrences[i]);
    }
  }

  /**
   * Gets a string summary of the results of the Random Generator.
   * 
   * @param showBreakdown
   *          If <tt>true</tt> then details of each random integer in distribution is given, else
   *          just a summary of totals is given
   * @return String description of results
   */
  public String getSummary(final boolean showBreakdown) {

    // Build a table of results
    final String line = "=========================================================================";
    final StringBuilder tab = new StringBuilder(line);

    if (showBreakdown) {
      // Header
      tab.append(String.format("%n %-6s | %-11s | %-18s | %-18s | %-10s", "Random", "Probability",
          "Actual Occurrences", "Chi squared", "Deviation"));
      tab.append(String.format("%n %-6s | %-11s | %-18s | %-18s | %-10s%n", "Number", " pi ",
          " Oi ", "(pi*n-Oi)^2/(pi*n)", " |pi - Oi/n| "));
      tab.append(line);
    }

    // Calculate the squared error and the chi2 statistic for each number and the total
    float totalDev = 0.0f;
    float totalChi2 = 0.0f;

    for (RandomValue val : data) {
      final float chi2 = val.calcChi2(count);
      final float deviation = val.calcDeviation(count);
      totalChi2 += chi2;
      totalDev += deviation;

      if (showBreakdown) {
        tab.append(String.format("%n %-6d", val.getNumber()));
        tab.append(String.format(" | %-11.4f", val.getProbability()));
        tab.append(String.format(" | %9d times   ", val.getOccurrence()));
        tab.append(String.format(" | %-17.4f ", chi2));
        tab.append(String.format(" | %-12.4f ", deviation));
      }
    }
    double sem = calcStandardErrorOfMean();
    tab.append(String.format(
        "%nFor an array of k=%d integers, after n=%d attempts: chi squared "
            + "statistic= %5.4f, total root squared deviation=%5.4f, std error of mean= %5.4f %n",
        data.length, count, totalChi2, totalDev, sem));

    return tab.toString();
  }

  /**
   * Calculates total chi-square statistic for the current number of iterations.
   * 
   * @return chi squared statistic as float
   */
  public float calcChi2() {
    float total = 0.0f;
    for (RandomValue val : data) {
      total += val.calcChi2(count);
    }
    return total;
  }

  /**
   * Calculate the standard error of mean (SE) For N iterations of the generator, if there are k
   * choices of random number SEM = stdDev/SQRT(k) = SQRT [ 1/(k-1) SUM_i=1^k (xi-x0)^2 ] / SQRT(k)
   * where xi= |pi-oi/N| are the deviations of the occurrences from the expected probability and x0=
   * [SUM_i=1^k xi]/ k
   * 
   * @return SME as a double
   */
  public double calcStandardErrorOfMean() {
    return calcStdErrorOfMean(calcStandardDeviation());
  }

  /**
   * Calculate the standard deviation
   * 
   * @return standard deviation as a double
   */
  protected double calcStandardDeviation() {
    final float[] deviations = calculateDeviations();
    final float mean = calcMean(deviations);
    return calcStdDev(deviations, mean);
  }

  /**
   * Calculate the standard deviation, SQRT [ 1/(k-1) SUM_i=1^k (xi-x0)^2 ] for k values, xi, which
   * have mean value x0
   * 
   * @param values
   *          An array of float values xi
   * @param mean
   *          The mean value of the xi
   * 
   * @return The standard deviation
   */
  protected double calcStdDev(final float[] values, final float mean) {
    float total = 0.0f;
    final double stdDev;
    final int k = values.length;
    for (int i = 0; i < k; i++) {
      final float xi = values[i] - mean;
      total += xi * xi;
    }
    if (k > 1) {
      stdDev = Math.sqrt(total / (k - 1));
    } else {
      stdDev = 0.0;
    }
    return stdDev;
  }

  /**
   * Calculate the mean value of an array floats.
   * 
   * @param values
   *          An array of float values
   * @return mean value as a float
   */
  protected float calcMean(final float[] values) {
    float total = 0.0f;
    for (int i = 0; i < values.length; i++) {
      total += values[i];
    }

    if (values.length == 0) {
      return 0.0f;
    } else {
      return total / values.length;
    }
  }

  /**
   * Calculates the individual absolute difference between the approximation and the expected for
   * each random choice for the current number of iterations.
   * 
   * @return deviation as float[]
   */
  protected float[] calculateDeviations() {
    final float[] deviations = new float[data.length];
    int idx = 0;
    for (RandomValue val : data) {
      deviations[idx++] = val.calcDeviation(count);
    }
    return deviations;
  }

  /**
   * Calculate the standard error of mean value of the deviations, |Oi/N - pi|. SEM = standard
   * deviation/SQRT(k) where k is the number of random integers
   * 
   * @param stdDev
   *          standard deviation as a double
   * @return standard error of mean
   */
  protected double calcStdErrorOfMean(final double stdDev) {
    return stdDev / Math.sqrt(data.length);
  }

  /**
   * Class to hold information about a particular random integer number.
   *
   */
  private class RandomValue {
    private final int number;
    private final float probability;
    private final int occurrence;

    /**
     * Constructor
     * 
     * @param number
     *          random number value as integer
     * @param probability
     *          probability of the number being generated (float value between 0.0f and 1.0f)
     */
    public RandomValue(final int number, final float probability, final int occurrence) {
      this.number = number;
      this.probability = probability;
      this.occurrence = occurrence;
    }

    public int getNumber() {
      return number;
    }

    public float getProbability() {
      return probability;
    }

    public int getOccurrence() {
      return occurrence;
    }

    /**
     * Calculate the absolute value of deviation of the actual number of occurrences compared to the
     * expected value, i.e. |Oi/n - pi| where n is number of iterations (i.e. count)
     * 
     * @param count
     *          integer number of iterations n
     * @return absolute double value of the deviation of number of occurrences compared to expected
     *         probability
     */
    private float calcDeviation(final int count) {
      return count > 0 ? Math.abs((float) occurrence / count - probability) : probability;

    }

    /**
     * After certain number of iterations, n, calculates chi-squared statistic for choosing random
     * number a position index. i.e. calculates (Oi-Ei)^2/Ei where Ei=pi *n is expected occurrences,
     * pi is the probability of this number occurring, Oi actual is actual number of occurrences
     * generated.
     * 
     * @param index
     *          integer number of iterations n
     * @return chi squared statistic as float
     */
    private float calcChi2(final int count) {
      final float expected = probability * count;
      final float chi2;
      if (expected > 0) {
        final float diff = (occurrence - expected);
        chi2 = diff * diff / expected;
      } else {
        chi2 = calcChi2Zero(count);
      }
      return chi2;
    }

    /**
     * Utility method for checking the zero chi-squared case
     * 
     * @param index
     *          integer number of iterations n
     * @return float 0.0f if this random number is valid for the model
     */
    private float calcChi2Zero(final int count) {

      if (probability < ACCEPTABLE_ERROR && occurrence > 0 && count > 0) {
        throw new ArithmeticException(String.format(
            "Random number %d has probability zero, but generator has generated %d occurrences "
                + "- unable to calculate chi squared statistic",
            number, occurrence));
      }
      // If count=0, or probability & occurrences=0 then return 0.0
      return 0.0f;
    }

  }

}
