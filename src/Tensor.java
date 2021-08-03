package cutnpeel;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2BooleanMap;
import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;

/**
 * Data structure to store tensor data
 */
public class Tensor {

	public int dimension; //number of attributess

	public int[] cardinalities; // n -> cardinality of the n-th attribute

    public int[][][] attributeToValuesToTuples; // (n, value) -> list of tuples which has the given value for the n-th attribute

	public int omega; // number of tuples

	public int[][] attributes; // (i, n) -> the n-th attribute value of the i-th tuple

	public int[] measureValues; // i -> measure attribute value of i-th tuple

    public long mass; // sum of measures attributes values

    public String[][] intToStrValue; // (n, value) -> str attribute value mapped to the given integer value of the n-th attribute

	/**
	 * Create a tensor with the given properties
	 * @param dimension dimension
	 * @param cardinalities n -> cardinality of the n-th attribute
     * @param attributeToValuesToTuples; (n, value) -> list of tuples which has the given value for the n-th attribute
	 * @param omega number of tuples
	 * @param attributes   (i, n) -> the n-th attribute value of the i-th tuple
	 * @param measureValues    i -> measure attribute value of i-th tuple
     * @param mass current number of tuples
     * @param intToStrValue (n, value) -> str attribute value mapped to the given integer value of the n-th attribute
	 */
	public Tensor(int dimension, int[] cardinalities, int[][][] attributeToValuesToTuples, int omega, int[][] attributes, int[] measureValues, long mass, String[][] intToStrValue) {
        this.dimension = dimension;
        this.cardinalities = cardinalities;
        this.attributeToValuesToTuples = attributeToValuesToTuples;
        this.omega = omega;
        this.attributes = attributes;
        this.measureValues = measureValues;
        this.mass = mass;
        this.intToStrValue = intToStrValue;
    }

    /**
     * Copy the given tensor (all fields except measureValues are shared)
     * @param tensor a tensor to copy
     */
    private Tensor(Tensor tensor) {
        this.dimension = tensor.dimension;
        this.cardinalities = tensor.cardinalities;
        this.attributeToValuesToTuples = tensor.attributeToValuesToTuples;
        this.omega = tensor.omega;
        this.attributes = tensor.attributes;
        this.measureValues = tensor.measureValues.clone();
        this.mass = tensor.mass;
        this.intToStrValue = tensor.intToStrValue;
    }

    /**
     * Copy the given tensor (all fields except measureValues are shared)
     * @return copied tensor
     */
    public Tensor copy() {
        return new Tensor(this);
    }

    /**
     * re-indexing objects in each dimension; all dimensions have integrated index
     * @return re-indexed objects
     */
    public Int2ObjectMap[] convert(){
        Int2ObjectMap<Int2BooleanOpenHashMap>[] map = new Int2ObjectOpenHashMap[cardinalities[2]];
        for(int i=0; i < cardinalities[2]; i++){
            map[i] = new Int2ObjectOpenHashMap<Int2BooleanOpenHashMap>();
        }
        for(int i=0; i < omega; i++){
            int[] tupleAttributes = attributes[i];
            if(!map[tupleAttributes[2]].containsKey(tupleAttributes[0])){
                Int2BooleanOpenHashMap temp = new Int2BooleanOpenHashMap();
                map[tupleAttributes[2]].put(tupleAttributes[0], temp);
            }
            map[tupleAttributes[2]].get(tupleAttributes[0]).put(tupleAttributes[1], false);
            
        }
        return map;
    }


}
