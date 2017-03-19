package man;

import java.util.Random;

/**
 * Random number generator, when called multiple times over a long period, it should return the
 * numbers roughly with the initialised probabilities.
 *
 * <p>
 * To generate a random number from the k choices, divide the [0, 1] range into k segments, where
 * the length of each segment is proportional to its corresponding probability. The boundaries of
 * each segment are stored in cumulative probability array, and so already sorted in ascending
 * order. Use binary search to find which segment a random float value falls, (where this is chosen
 * using random.nextFloat(), a uniformly distributed random float between 0 and 1).
 * 
 * @author Sioned Baker
 * @version 1.0
 */
public class RandomGen {
  // Error tolerance
  public static final float ACCEPTABLE_ERROR = 0.00000001f;

  // Values that may be returned by nextNum()
  private final int[] randomNums;

  // Probability of the occurrence of corresponding integer in randomNums
  private final float[] probabilities;

  // Cumulative probabilities
  private final float[] cumProb;

  // Record of how many times each random number has been chosen by nextNum()
  private final int[] occurrences;

  // Instance of uniform random generator
  private final Random random = new Random();

  // if all probabilities are zero except one with probability 1.0,
  // then the choice is trivial
  private Integer trivialCaseIdx = null;

  // The number k of random number choices
  // The 3 arrays, randomNums, probabilities, cumProb should all be of this size
  private final int numChoices;

  // Number of times nextNum() has been called
  private int count = 0;

  /**
   * Constructor for class to generate random numbers according to given distribution.
   * 
   * @param randomNums
   *          Array of integers (positive or negative) that maybe generated
   * @param probabilities
   *          Same size array of float values which are the corresponding probability values between
   *          0 and 1 of the randomNums integer that maybe generated
   */
  public RandomGen(final int[] randomNums, final float[] probabilities) {

    // Check size of inputs
    this.numChoices = getInputSize(randomNums, probabilities);
    this.randomNums = randomNums;

    // Check probabilities and build cumulative probability
    this.cumProb = buildCumProb(probabilities);
    this.probabilities = probabilities;
    this.occurrences = new int[numChoices];
  }

  /**
   * Constructor to generate random numbers according to given distribution with a given seed.
   * 
   * @param randomNums
   *          Array of integers (positive or negative) that maybe generated
   * @param probabilities
   *          Same size array of float values which are the corresponding probability values between
   *          0 and 1 of the randomNums integer that maybe generated
   * @param seed
   *          to create a random number generator using a single long seed.
   */
  public RandomGen(final int[] randomNums, final float[] probabilities, final long seed) {
    this(randomNums, probabilities);
    setSeed(seed);
  }

  /**
   * Create a random number generator using a single long seed (for repeatable testing).
   * 
   * @param seed
   *          long value
   */
  private void setSeed(final long seed) {
    this.random.setSeed(seed);
  }

  /**
   * Returns integer that is one of the randomNums. When this method is called multiple times over a
   * long period, it should return the numbers roughly with the initialised probabilities.
   * 
   * @return int randomNum
   */
  public int nextNum() {
    final int index;

    if (isTrivialCase()) {
      // Trivial case when only one choice of number
      index = trivialCaseIdx;
    } else {
      index = binarySearch(random.nextFloat());
    }

    // Increment occurrence of this random integer and the count
    occurrences[index]++;
    count++;
    return randomNums[index];
  }

  /**
   * For a given key (which is random float value selected from uniform distribution), search the
   * array of cumulative probabilities to find the largest index, <tt>i </tt>, where
   * <tt>key &lt;= cumProb[i]</tt>. If i=0 then<tt> 0 &lt; key &lt;= cumProb[0] </tt>, otherwise
   * <tt>cumProb[i-1] &lt; key &lt;= cumProb[i]</tt>
   * 
   * @param key
   *          the search key as a float
   * @return index for the segment of the distribution where key lies. Note: that index guaranteed
   *         to be 0 or greater and less than length of the array
   */
  private int binarySearch(final float key) {
    int left = 0;
    int right = numChoices - 1;

    while (left < right) {
      int mid = left + (right - left) / 2;
      if (cumProb[mid] < key) {
        left = mid + 1;
      } else {
        right = mid;
      }
    }
    return left;
  }

  /**
   * Gets array of the number occurrences each random number as been selected.
   * 
   * @return an integer array recording number of occurrences of each random number choice.
   */
  public int[] getOccurrences() {
    return occurrences;
  }

  /**
   * Gets number of times nextNum() has been called.
   * 
   * @return number of times nextNum called
   */
  public int getCount() {
    return count;
  }

  /**
   * Gets the given input array of random numbers.
   * 
   * @return The input array of the choice of random integers
   */
  public int[] getRandomNums() {
    return randomNums;
  }

  /**
   * Gets an array of float values corresponding to probability values the randomNums integer.
   * 
   * @return The input array of probabilities
   */
  public float[] getProbabilities() {
    return probabilities;
  }

  /**
   * Gets the number of degrees of freedom, i.e. one less than the number of random numbers to be
   * chosen Since all the probabilities need to add to 1.0, for a choice of k number there are only
   * k-1 degrees of freedom.
   * 
   * @return integer number of degrees of freedom.
   */
  public int getDegreesFreedom() {
    return numChoices - 1;
  }

  /**
   * Get size of the 2 input arrays, expect them to be both of same length, non-null and non-zero
   * length
   * 
   * @param randomNums
   *          Array of integers (positive or negative) that maybe generated
   * @param probabilities
   *          Same size array of float values which are the corresponding probability values between
   *          0 and 1 of the randomNums integer that maybe generated
   * 
   * @return integer length of the input arrays
   */
  private int getInputSize(final int[] nums, final float[] probs) {
    if (nums == null || probs == null || nums.length != probs.length || nums.length == 0) {
      throw new IllegalArgumentException(
          "Expecting arrays to be non-null and of the same non-zero length");
    }
    return nums.length;
  }

  /**
   * Build array of cumulative probabilities and check given probabilities lie between 0.0 and 1.0
   * and sum to 1.0. Identify whether the input probability is trivial, i.e. there's only one
   * probability of 1.0 and all the other probabilities are zero.
   * 
   * @param probabilities
   *          Array of float values which are the corresponding probability values between 0 and 1
   *          of the randomNums integer that maybe generated
   * 
   * @return Same size array of cumulative probabilities as floats
   */
  private float[] buildCumProb(final float[] probs) {

    final float[] cumProb = new float[numChoices];
    int idxPotentialTrivialCase = -1;
    float sum = 0.0f;

    // Check if probabilities are all valid and sum to 1.0
    for (int i = 0; i < numChoices; i++) {
      float prob = probs[i];
      if (checkProbability(i, prob)) {
        idxPotentialTrivialCase = i;
      }
      sum += prob;
      cumProb[i] = sum;
    }

    if (Math.abs(sum - 1.0f) > ACCEPTABLE_ERROR * numChoices) {
      throw new IllegalArgumentException(String
          .format("Expecting probabilities to total to 1.0, however total is %9.8f", sum - 1.0f));
    }

    // Check if trivial case
    setTrivialCase(idxPotentialTrivialCase);
    return cumProb;
  }

  /**
   * Checks whether a probability value is valid, i.e. lies between or equal to 0.0 and 1.0 Returns
   * boolean flag when the probability is 1.0, no checks if probability is 0.0
   * 
   * @param index
   *          integer index of random number in the given array
   * @param prob
   *          probability of random number
   * @return <tt>true</tt> if probability of 1.0, otherwise returns <tt>false</tt>
   */
  private boolean checkProbability(final int idx, final float prob) {
    if (prob < 0.0f || prob > 1.0f || Float.isNaN(prob) || Float.isInfinite(prob)) {
      throw new IllegalArgumentException(
          String.format("Expecting probabilities array to have values between 0 and 1,"
              + "probability at index %d has illegal value: %4.3f", idx, prob));
    }

    // if probability is zero, then we could exclude corresponding number from the array
    // but this seems unnecessary and means resizing both randomNums and probabilities array

    // if probability is 1.0, there should only be one choice,
    // either one choice or all the probabilities are zero apart from one
    // i.e. the generator is trivial - flag this potential case
    if (Math.abs(prob - 1.0f) < ACCEPTABLE_ERROR) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Check whether trivial case when there is only a single choice for random number
   * 
   * @return <tt>true </tt> if only one random number choice with probability 1.0, otherwise
   *         <tt>false</tt>
   */
  private boolean isTrivialCase() {
    return (trivialCaseIdx != null);
  }

  /**
   * If the given probabilities are such that only one is non-zero and has value 1.0, then store the
   * corresponding random integer value as trivialCase as we can always return this value with
   * certainty
   * 
   * @param idxOfNonZeroCase
   *          Index of the only random number in input array with probability of 1.0
   */
  private void setTrivialCase(final int idxOfNonZeroCase) {
    if (idxOfNonZeroCase > -1 && idxOfNonZeroCase < numChoices) {
      this.trivialCaseIdx = new Integer(idxOfNonZeroCase);

      // Some debug
      System.out.println(String.format(
          "INFO: For an array of %d random numbers, all have probability zero "
              + "except one which has probability 1 - this is a trivial case of the generator "
              + "where random value %d will always be returned with certainity.",
          numChoices, randomNums[trivialCaseIdx]));
    }
  }

}
