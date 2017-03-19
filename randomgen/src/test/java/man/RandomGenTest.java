package man;

import static man.RandomGen.ACCEPTABLE_ERROR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Test class RandomGen
 * 
 * <p>
 * Tests random generator RandomGen. Since output is random, without using seed its difficult to
 * test repeatedly and consistently.
 *
 */
public class RandomGenTest {

  protected static final int[] EXAMPLE_NUM = { -1, 0, 1, 2, 3 };
  protected static final float[] EXAMPLE_PROB = { 0.01f, 0.3f, 0.58f, 0.1f, 0.01f };

  @Test
  public void testExample100() {
    testGenerator("call example 100 times", 100, EXAMPLE_NUM, EXAMPLE_PROB, true, true);
  }

  @Test
  public void testExample10million() {
    testGenerator("call example 10 million times", 10000000, EXAMPLE_NUM, EXAMPLE_PROB, true, true);

  }

  /**
   * Because generator uses Random.float() All 2<sup>24</sup> possible {@code float} values of the
   * form <i>m&nbsp;x&nbsp;</i>2<sup>-24</sup>, where <i>m</i> is a positive integer less than 2
   * <sup>24</sup>, are produced with (approximately) equal probability.
   * 
   * The probability of integers 1 and 2 in this test are the same, but because cumlative
   * probability of 1 lies at 0.5, whereas for 2 it lies at 0.5+1/2<sup>25</sup>, the random number
   * never gets chosen whereas 1 does
   */
  @Test
  public void testVerySmallProbability() {

    final int x = (int) Math.pow(2, 25);
    final float probSmall = 1.0f / x; // 1/2^24> 1/2^25> ACCEPTABLE_ERROR
    final float probBig = 0.5f - probSmall;
    final int[] nums = { 0, 1, 2, 3 };
    final float[] probs = { probBig, probSmall, probSmall, probBig };

    final RandomGen generator = new RandomGen(nums, probs);
    runAndSummarizeGenerator(
        " very small probability for random numbers 1 & 2 but 1 gets chosen and 2 doesn't", 10 * x,
        generator, true, true);

    final int[] occurrences = generator.getOccurrences();
    assertTrue("Zero occurrence of number 1", occurrences[1] > 0);
    assertTrue("Non zero occurrence of number 2", occurrences[2] == 0);

  }

  @Test
  public void testNegligibleProbability() {
    final int[] nums = { 10, 20 };
    final float[] prob = { 0.1f * ACCEPTABLE_ERROR, 0.99999999f };
    testGeneratorTrivialCase(
        "negligible probability for number 10, almost certainity for number 20", 1000, nums, prob,
        true, 1);
  }

  @Test
  public void testValidProbablityEquals1() {
    final int nChoice = 100;
    final int alwaysReturn = nChoice / 2;
    final int[] nums = new int[nChoice];
    final float[] prob = new float[nChoice];
    for (int i = 0; i < nChoice; i++) {
      nums[i] = i;
    }
    prob[alwaysReturn] = 1.0f;
    testGeneratorTrivialCase("100 choices but only one has non-zero probabtility of 1", 1000, nums,
        prob, false, alwaysReturn);
  }

  @Test
  public void testValidSingleProbablityEquals1() {
    final int[] nums = { 6 };
    final float[] prob = { 1.0f };
    testGeneratorTrivialCase("single choice with probability 1", 100, nums, prob, true, 0);
  }

  private void testGeneratorTrivialCase(final String testName, final int num,
      final int[] randomNums, final float[] probabilities, final boolean showBreakdown,
      final int expectedValIdx) {
    final RandomGen gen = testGenerator(testName, num, randomNums, probabilities, showBreakdown,
        true);
    final int[] occurrences = gen.getOccurrences();
    assertEquals(randomNums.length, occurrences.length);
    for (int i = 0; i < randomNums.length; i++) {
      final String message = String.format(
          "For %d attempts, number of occurrences in trivial case for %d", num, randomNums[i]);
      if (i == expectedValIdx) {
        assertEquals(message, num, occurrences[i]);
      } else {
        assertEquals(message, 0, occurrences[i]);
      }

    }
  }

  @Test
  public void testLargeNumberOfDuplicates() {
    final int nNum = 10000;
    final int nChoice = 4;
    final float constp = (float) 1 / nNum;
    final int[] nums = new int[nNum];
    final float[] prob = new float[nNum];
    for (int i = 0; i < nNum; i++) {
      // 3 times more likely to get 1 or 3 than 0 or 2
      nums[i] = i % nChoice;
      prob[i] = (i % 2 == 0 ? constp * 0.5f : constp * 1.5f);
    }

    final RandomGen gen = new RandomGen(nums, prob);
    final int[] occur = new int[nChoice];
    for (int i = 0; i < nNum; i++) {
      final int randomVal = gen.nextNum();
      assertTrue(randomVal < nChoice);
      assertTrue(randomVal > -1);
      occur[randomVal]++;
    }

    final int[] occurrences = gen.getOccurrences();
    assertEquals(occurrences.length, nNum);
    int occurZero = 0;
    int occurOne = 0;
    int idx = 0;
    while (idx < nNum) {
      occurZero += occurrences[idx++];
      occurOne += occurrences[idx++];
      idx = idx + 2; // skip occurrences of 2 and 3
    }

    assertEquals("occurrences of integer 0", occurZero, occur[0]);
    assertEquals("occurrences of integer 1", occurOne, occur[1]);
    final double ratio = (double) occurOne / occurZero;
    assertEquals("Ratio of occurrences of 1 compared to zero is %5.4f", 3, Math.round(ratio));
  }

  ////////////////////////////////////
  // Test with seed
  ///////////////////////////////////

  @Test
  public void testExample10kWithSeed() {
    final int[] expected = { 92, 3025, 5799, 991, 93 };
    testGeneratorWithSeed(25, 10000, EXAMPLE_NUM, EXAMPLE_PROB, true, true, expected);
  }

  @Test
  public void testExampleOrderWithSeed() {
    final int[] expected = { 1, 2, 0, 3, 1, 0, 0, 1, -1, 0, 1, 2, 1, 1, 0, 2, 1, 1, 2, 3, 1, 0, 1,
        1, 0, 0, 1, 2, 1, 0, 1, 1, 0, 1, 2, 2, 1, 1, 0, 1 };
    final int[] expected2 = { 1, 1, 1, 0, 0, 1, 0, 1, 1, 1, 2, 0, 1, 0, 1, 1, 1, 2, 2, 1, 0, 1, 1,
        0, 2, 1, 0, 0, 3, 1, 1, 1, 1, 2, 1, 1, 1, 0, 1, 1 };
    final RandomGen generator = new RandomGen(EXAMPLE_NUM, EXAMPLE_PROB, 25);
    final RandomGen generator2 = new RandomGen(EXAMPLE_NUM, EXAMPLE_PROB, 24);
    for (int i = 0; i < expected.length; i++) {
      assertEquals(String.format("Unexpected random value chosen at iteration %d", i), expected[i],
          generator.nextNum());
      assertEquals(String.format("Unexpected random value chosen at iteration %d", i), expected2[i],
          generator2.nextNum());
    }

    final String testName = "order of number generated (with seed)";
    summarize(testName, true, generator, true);
    summarize(testName, true, generator2, true);
  }

  ///////////////////////////////////////////////////////////////////
  // Utility methods
  ///////////////////////////////////////////////////////////////////
  protected static RandomGen testGenerator(final String testName, final int nNums,
      final int[] randomNums, final float[] probabilities, final boolean showBreakdown,
      final boolean showSummary) {
    RandomGen gen = new RandomGen(randomNums, probabilities);
    runAndSummarizeGenerator(testName, nNums, gen, showBreakdown, showSummary);
    testGetters(gen, nNums, randomNums, probabilities);
    return gen;
  }

  protected static void runAndSummarizeGenerator(final String testName, final int nNums,
      final RandomGen gen, final boolean showBreakdown, final boolean showSummary) {

    // Run generator nNums times
    runGenerator(nNums, gen);

    // Output summary if needed
    summarize(testName, showSummary, gen, showBreakdown);

  }

  protected static void testGetters(final RandomGen generator, final int nNums,
      final int[] randomNums, final float[] probabilities) {
    assertEquals("Degrees of freedom", randomNums.length - 1, generator.getDegreesFreedom());
    assertEquals("Count", nNums, generator.getCount());

    final int[] nums = generator.getRandomNums();
    final float[] probs = generator.getProbabilities();
    for (int i = 0; i < randomNums.length; i++) {
      assertEquals("Random number at index " + i, randomNums[i], nums[i]);
      assertEquals("Probability at index " + i, probabilities[i], probs[i], ACCEPTABLE_ERROR);
    }
  }

  protected static void runGenerator(final int nNums, final RandomGen gen) {
    // Run generator nNums times
    for (int i = 0; i < nNums; i++) {
      gen.nextNum();
    }
  }

  protected static void summarize(final String testName, final boolean showSummary,
      final RandomGen generator, final boolean showBreakdown) {
    if (showSummary) {
      RandomGenSummarizerTest.getAndPrintSummary(testName, generator, showBreakdown);
    }
  }

  protected static void testGeneratorWithSeed(final long seed, final int nNums,
      final int[] randomNums, final float[] probabilities, final boolean showBreakdown,
      final boolean showSummary, final int[] expectedOccur) {

    final RandomGen generator = new RandomGen(randomNums, probabilities, seed);
    runGenerator(nNums, generator);
    final int[] occur = generator.getOccurrences();

    assertEquals("Number of occurrences", randomNums.length, occur.length);
    assertEquals("Number of expected occurrences given is wrong", randomNums.length,
        expectedOccur.length);
    for (int i = 0; i < randomNums.length; i++) {
      assertEquals(String.format("Unexpected number of occurrences for %d", randomNums[i]),
          expectedOccur[i], occur[i]);
    }
  }

}
