package cutnpeel;

import java.util.Collections;
import it.unimi.dsi.fastutil.ints.IntArrayList;

/**
 * Hash value for min-hash
 */
public class Hash {
    // ((a * input + b) mod prime) mod size form
    private int[] size; // objects # of dimensions except V
    private IntArrayList permutation1; // permutation of dimension 1
    private IntArrayList permutation2; // permutation of dimension 2

    /**
     * Generate hash function which is random permutation
     * This is permutation for 2D matrix; size1(row) X size2(column)
     * The hash value is permutation[i] * J + permutation2[j] for (i, j) element
     * @param dimension dimension of current graph, i.e., 3
     * @param cardinalities i -> number of objects in i-th dimension
     * @param norm dimension of current set V; src : 0, dst : 1, time : 2
     * @return
     */
    public Hash(int dimension, int[] cardinalities, int norm){
        int j=0;
        this.size = new int[dimension-1];
        for(int dim=0; dim < dimension; dim++){
            if(dim == norm){ continue; }
            this.size[j] = cardinalities[dim];
            j++;
        }

        permutation1 = new IntArrayList(this.size[0]);
        permutation2 = new IntArrayList(this.size[1]);

        for(int i=0; i<this.size[0]; i++){
            permutation1.add(i);
        }
        for(int i=0; i<this.size[1]; i++){
            permutation2.add(i);
        }
        Collections.shuffle(permutation1);
        Collections.shuffle(permutation2);
    }

    public int hashFunction(int idxI, int idxJ){
        return permutation1.get(idxI) * this.size[1] + permutation2.get(idxJ);
    }

}
