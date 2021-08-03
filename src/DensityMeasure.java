package cutnpeel;

/**
 * density function for choosing an object to remove
 */

public class DensityMeasure{

    private int dimension;
    private long mass;
    private int[] cardinalities;
    private int sumOfCardinalities;
    private double productOfCardinalities;
    private double sizeOrigin;
    private double L_I;
    private double L_e;

    public double initialize(int dimension, int[] cardinalities, long mass, int[] oriCardinalities) {
        this.dimension = dimension;
        this.mass = mass;
        this.cardinalities = cardinalities.clone();
        sumOfCardinalities = 0;
        productOfCardinalities = 1;
        for(int dim = 0; dim < dimension; dim++) {
            sumOfCardinalities += cardinalities[dim];
            productOfCardinalities *= cardinalities[dim];
        }
        sizeOrigin = productOfCardinalities;
        L_I = Math.log(oriCardinalities[0]+oriCardinalities[1]+oriCardinalities[2])/Math.log(2);
        L_e = (Math.log(oriCardinalities[0])+Math.log(oriCardinalities[1])+Math.log(oriCardinalities[2]))/Math.log(2);
        return density(mass, sumOfCardinalities, productOfCardinalities);
    }

    public double initialize(int dimension, int[] cardinalitiesOfAll, long massOfAll, int[] cardinaltiesOfBlock, long massOfBlock) {
        this.dimension = dimension;
        this.mass = massOfBlock;
        this.cardinalities = cardinaltiesOfBlock.clone();
        sumOfCardinalities = 0;
        productOfCardinalities = 1;
        for(int dim = 0; dim < dimension; dim++) {
            sumOfCardinalities += cardinaltiesOfBlock[dim];
            productOfCardinalities *= cardinaltiesOfBlock[dim];
        }
        sizeOrigin = productOfCardinalities; 
        return density(mass, sumOfCardinalities, productOfCardinalities);
    }

    public double ifRemoved(int attribute, int numValues, long sumOfMasses) {
        if ((productOfCardinalities - this.mass) == 0)
            return sizeOrigin * (-1) * dimension;
        
        return 1-sumOfMasses/(productOfCardinalities / cardinalities[attribute]);
    }

    public double ifInserted(int attribute, int numValues, long sumOfMasses) {
        return density(this.mass + sumOfMasses, sumOfCardinalities + numValues,
                productOfCardinalities / cardinalities[attribute] * (cardinalities[attribute] + numValues));
    }

    public double remove(int attribute, int numValues, long sumOfMasses) {
        this.mass -= sumOfMasses;
        sumOfCardinalities -= numValues;
        cardinalities[attribute] -= numValues;
        productOfCardinalities = productOfCardinalities(cardinalities);
        return density(this.mass, sumOfCardinalities, productOfCardinalities);
    }

    public double insert(int attribute, int numValues, long sumOfMasses) {
        this.mass += sumOfMasses;
        sumOfCardinalities += numValues;
        cardinalities[attribute] += numValues;
        productOfCardinalities = productOfCardinalities(cardinalities);
        return density(this.mass, sumOfCardinalities, productOfCardinalities);
    }

    // Use this density to print Block Density
    public double printDensity(long sumOfPart, int[] cardinalities) {
        double productOfCardinalitiesPart = 1;
        for(int dim = 0; dim < dimension; dim++) {
            productOfCardinalitiesPart *= cardinalities[dim];
        }
        return sumOfPart / productOfCardinalitiesPart;
    }

    public double density(long sumOfPart, int[] cardinalities) {
        int sumOfCardinalitiesPart = 0;
        double productOfCardinalitiesPart = 1;
        for(int dim = 0; dim < dimension; dim++) {
            sumOfCardinalitiesPart += cardinalities[dim];
            productOfCardinalitiesPart *= cardinalities[dim];
        }
        return density(sumOfPart, sumOfCardinalitiesPart, productOfCardinalitiesPart);
    }

    private double density(long sumOfPart, double sumOfCardinalities, double productOfCardinalities) {
        if(sumOfCardinalities == 0 || productOfCardinalities == 0)
            return sizeOrigin * (-1) * dimension;

        return (sumOfPart * L_e - (productOfCardinalities - sumOfPart) * L_e - (sumOfCardinalities + 1) * L_I);
    }

    public long returnMass(){
        return mass;
    }

    public double getProductOfCardinalities(){
        return productOfCardinalities;
    }

    public int[] getCardinalities(){
        return cardinalities;
    }

    private static double productOfCardinalities(int[] cardinalities){
        double productOfCardinalities = 1;
        for(int attribute = 0; attribute < cardinalities.length; attribute++) {
            productOfCardinalities *= cardinalities[attribute];
        }
        return productOfCardinalities;
    }

}

