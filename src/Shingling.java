package cutnpeel;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

public class Shingling {

    public int[][] tupleToShingle; // (k, n) -> entry number in original graph of n-th entry(edge) in k-th subtensor(shingle)

    public int[] subTensorMass; // k -> number of edges in k-th subgraph

    public int shingleSize = 0;

    /**
     * Create shingle values with the given properties
     * @param dimension dimension
     * @param cardinalities n -> cardinality of the n-th attribute
     * @param norm axis currently working on
     * @param attributeToValuesToTuples; (n, value) -> list of tuples which has the given value for the n-th attribute
     * @param measureValues    i -> measure attribute value of i-th tuple
     * @param attributes   (i, n) -> the n-th attribute value of the i-th tuple
     */

    public Shingling(int dimension, int[] cardinalities, int norm, int[][][] attributeToValuesToTuples, int[] measureValues, int[][] attributes){
        Hash hashVal = new Hash(dimension, cardinalities, norm);
        final int[] shingle = new int[cardinalities[norm]];
        final int[] shingleTemp = new int[cardinalities[norm]];
        IntOpenHashSet shingleSet = new IntOpenHashSet();
        Int2IntOpenHashMap shingleHash = new Int2IntOpenHashMap();

        int[] dims = new int[dimension-1];
        int k = 0;
        for (int dim = 0; dim < dimension; dim++) {
            if (dim == norm) {
                continue;
            }
            dims[k] = dim;
            k++;
        }

        // find shingle value for each slice
        for (int cardinality = 0; cardinality < cardinalities[norm]; cardinality++) {
            int entries[] = attributeToValuesToTuples[norm][cardinality];
            for (int entry : entries) {
                if (measureValues[entry] == 0) {
                    continue;
                }
                int shingleVal = hashVal.hashFunction(attributes[entry][dims[0]], attributes[entry][dims[1]]);
                // get max value since shingleTemp[] is initialized with zero
                shingleTemp[cardinality] = Math.max(shingleVal, shingleTemp[cardinality]);
            }
            if(shingleSet.contains(shingleTemp[cardinality])){ // there is overlapping shingle value
                if(!shingleHash.containsKey(shingleTemp[cardinality])){
                    shingleHash.put(shingleTemp[cardinality], shingleHash.size());
                }
            }
            else{ // there is not overlapping shingle value yet
                shingleSet.add(shingleTemp[cardinality]);
            }
        }

        if(shingleHash.size() == 0){// case1 : all slice has individual hash value, make sub-tensor with all slices
            for (int cardinality = 0; cardinality < cardinalities[norm]; cardinality++) {
                shingle[cardinality] = cardinality;
            }
            this.shingleSize = cardinalities[norm];
        }
        else{// case2 : some slice has individual hash value, make sub-tensor with slices that have overlapping hash value
            for (int cardinality = 0; cardinality < cardinalities[norm]; cardinality++){
                if(shingleHash.containsKey(shingleTemp[cardinality])){
                    shingle[cardinality] = shingleHash.get(shingleTemp[cardinality]);
                }
                else{
                    shingle[cardinality] = cardinalities[norm];
                }
            }
            this.shingleSize = shingleHash.size();
        }

        this.subTensorMass = new int[this.shingleSize]; // i -> mass of sub-tensor with i-th shingle
        this.tupleToShingle = new int[this.shingleSize][]; // (i, j) -> real tuple number of a tuple in i-th shingle's j-th tuple

        for (int cardinality = 0; cardinality < cardinalities[norm]; cardinality++) {
            int sliceShingle = shingle[cardinality];
            if(sliceShingle == cardinalities[norm]){
                continue;
            }
            subTensorMass[sliceShingle] += attributeToValuesToTuples[norm][cardinality].length;
        }
        for (int i = 0; i < this.shingleSize; i++) {
            tupleToShingle[i] = new int[subTensorMass[i]];
        }
        int[] insertedTupleNum = new int[this.shingleSize];
        for (int cardinality = 0; cardinality < cardinalities[norm]; cardinality++) {
            int entries[] = attributeToValuesToTuples[norm][cardinality];
            int sliceShingleIdx = shingle[cardinality];
            if(sliceShingleIdx == cardinalities[norm]){
                continue;
            }
            for (int entry : entries) {
                tupleToShingle[sliceShingleIdx][insertedTupleNum[sliceShingleIdx]] = entry;
                insertedTupleNum[sliceShingleIdx]++;
            }
        }
    }
}
