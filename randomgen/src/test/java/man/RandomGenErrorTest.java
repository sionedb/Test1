package man;

import static man.RandomGen.ACCEPTABLE_ERROR;
import static man.RandomGenTest.EXAMPLE_NUM;
import static man.RandomGenTest.EXAMPLE_PROB;

import org.junit.Test;

/*
* Tests the error case for class {@link RandomGen} public
* 
*/
public class RandomGenErrorTest {

  @Test(expected = IllegalArgumentException.class)
  public void testNullInputs() {
    new RandomGen(null, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullRandomNumInputs() {
    new RandomGen(null, EXAMPLE_PROB);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullProbInputs() {
    new RandomGen(EXAMPLE_NUM, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testZeroLengthInputs() {
    new RandomGen(new int[0], new float[0]);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testZeroLengthNumInputs() {
    new RandomGen(new int[0], EXAMPLE_PROB);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testZeroLengthProbInputs() {
    new RandomGen(EXAMPLE_NUM, new float[0]);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDifferentLengthInputs() {
    final float[] prob = { 0.5f, 0.5f };
    new RandomGen(EXAMPLE_NUM, prob);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidProbabilities() {
    final int[] nums = { 1, 2 };
    final float[] prob = { -0.5f, 1.5f };
    new RandomGen(nums, prob);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeProbability() {
    final int[] nums = { 1, 2 };
    final float[] prob = { -0.1f * ACCEPTABLE_ERROR, 1.0f - 0.1f * ACCEPTABLE_ERROR };
    new RandomGen(nums, prob);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNaNProbability() {
    final int[] nums = { 1, 2 };
    final float[] prob = { Float.NaN, 0.99f };
    new RandomGen(nums, prob);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInfiniteProbability() {
    final int[] nums = { 1, 2, 3, 4 };
    final float[] prob = { Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, 0.5f, 0.5f };
    new RandomGen(nums, prob);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testProbabilityGreaterThan1() {
    final int[] nums = { 1, 2 };
    final float[] prob = { ACCEPTABLE_ERROR, 1.0f + 6.0f * ACCEPTABLE_ERROR };
    new RandomGen(nums, prob);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testProbabilitySumGreaterThan1() {
    final int[] nums = { 1, 2 };
    final float[] prob = { 0.6f, 0.5f };
    new RandomGen(nums, prob);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testProbabilitySumLessThan1() {
    final int[] nums = { 1, 2 };
    final float[] prob = { 0.7f - 3.0f * ACCEPTABLE_ERROR, 0.3f - ACCEPTABLE_ERROR };
    new RandomGen(nums, prob);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidProbablityEquals1() {
    final int[] nums = { 1, 2, 3 };
    final float[] prob = { 6.0f * ACCEPTABLE_ERROR, 1.0f, 6.0f * ACCEPTABLE_ERROR };
    new RandomGen(nums, prob);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidSingleProbablity() {
    final int[] nums = { 1 };
    final float[] prob = { 0.99999f };
    new RandomGen(nums, prob);
  }

}
