package cutnpeel;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

/**
 * Data structure to store tensor data
 */
public class SubTensor {

    public int dimension; //number of attributess

    public int[] cardinalities; // n -> cardinality of the n-th attribute

    public int[][][] attributeToValuesToTuples; // (n, value) -> list of tuples which has the given value for the n-th attribute

    public int omega; // number of tuples

    public int[][] attributes; // (i, n) -> the n-th attribute value of the i-th tuple

    public int[] measureValues; // i -> measure attribute value of i-th tuple

    public long mass; // sum of measures attributes values

    public int[][] subToOrigin;

    /**
     * Create a tensor with the given properties
     * @param dimension dimension
     * @param cardinalities n -> cardinality of the n-th attribute
     * @param attributeToValuesToTuples; (n, value) -> list of tuples which has the given value for the n-th attribute
     * @param omega number of tuples
     * @param attributes   (i, n) -> the n-th attribute value of the i-th tuple
     * @param measureValues    i -> measure attribute value of i-th tuple
     * @param mass current number of tuples
     */
    public SubTensor(int dimension, int[] cardinalities, int[][][] attributeToValuesToTuples, int omega, int[][] attributes, int[] measureValues, long mass) {
        this.dimension = dimension;
        this.cardinalities = cardinalities;
        this.attributeToValuesToTuples = attributeToValuesToTuples;
        this.omega = omega;
        this.attributes = attributes;
        this.measureValues = measureValues;
        this.mass = mass;
    }

    public SubTensor(int dimension, int[] oriCardinalities, int[] tupleToShingle, int omega, int[][] oriAttributes, int[] oriMeasureValues) {
        this.dimension = dimension;
        this.omega = omega;
        this.cardinalities = new int[dimension];
        this.attributeToValuesToTuples = new int[dimension][][];
        this.attributes = new int[omega][];
        this.measureValues = new int[omega];
        this.mass = 0;
        // use below to arrays to recover original tensor attribute values
        this.subToOrigin = new int[dimension][];
        // idx = attribute index in sub-tensor, value = attribute index in original tensor
        int[][] originToSub = new int[dimension][];
        // idx = attribute index in original tensor, value = attribute index in sub-tensor
        int[][] insertedTuples = new int[dimension][];

        for (int dim = 0; dim < dimension; dim++) {
            IntOpenHashSet attSet = new IntOpenHashSet();
            originToSub[dim] = new int[oriCardinalities[dim]];
            Int2IntOpenHashMap attributeToValuesToNum = new Int2IntOpenHashMap();
            // counts number of tuples containing certain attribute
            int attIdx = 0;
            for (int j = 0; j < omega; j++) {
                int attribute = oriAttributes[tupleToShingle[j]][dim];
                if (attSet.contains(attribute)) {
                    int val = attributeToValuesToNum.get(originToSub[dim][attribute]);
                    attributeToValuesToNum.put(originToSub[dim][attribute], val + 1);
                } else {
                    attributeToValuesToNum.put(attIdx, 1);
                    attSet.add(attribute);
                    originToSub[dim][attribute] = attIdx;
                    attIdx++;
                }
            }

            this.cardinalities[dim] = attributeToValuesToNum.size();
            subToOrigin[dim] = new int[this.cardinalities[dim]];
            this.attributeToValuesToTuples[dim] = new int[this.cardinalities[dim]][];
            insertedTuples[dim] = new int[this.cardinalities[dim]];

            for (int card = 0; card < this.cardinalities[dim]; card++) {
                this.attributeToValuesToTuples[dim][card] = new int[attributeToValuesToNum.get(card)];
            }
            for (int j = 0; j < omega; j++) {
                int tupleNum = tupleToShingle[j];
                int attribute = oriAttributes[tupleNum][dim];
                int idx = originToSub[dim][attribute];
                subToOrigin[dim][idx] = attribute;
                this.attributeToValuesToTuples[dim][originToSub[dim][attribute]][insertedTuples[dim][originToSub[dim][attribute]]++] = j;
            }
        }
        for (int j = 0; j < omega; j++) {
            int tupleNum = tupleToShingle[j];
            this.measureValues[j] = oriMeasureValues[tupleNum];
            this.mass += this.measureValues[j];
            this.attributes[j] = new int[dimension];
            for (int dim = 0; dim < dimension; dim++) {
                int attribute = originToSub[dim][oriAttributes[tupleNum][dim]];
                this.attributes[j][dim] = attribute;
            }
        }
    }

    /**
     * Copy the given tensor (all fields except measureValues are shared)
     * @param tensor a tensor to copy
     */
    private SubTensor(SubTensor tensor) {
        this.dimension = tensor.dimension;
        this.cardinalities = tensor.cardinalities;
        this.attributeToValuesToTuples = tensor.attributeToValuesToTuples;
        this.omega = tensor.omega;
        this.attributes = tensor.attributes;
        this.measureValues = tensor.measureValues.clone();
        this.mass = tensor.mass;
    }

    /**
     * Copy the given tensor (all fields except measureValues are shared)
     * @return copied tensor
     */
    public SubTensor copy() {
        return new SubTensor(this);
    }
}
