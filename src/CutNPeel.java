// cutnpeel
package cutnpeel;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

import static java.util.Objects.isNull;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2BooleanMap;
import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;

/**
 * CutNPeel Implementation
 */
public class CutNPeel {
    // outputs
    public static List<IntOpenHashSet> listOfBlockAttributes = new LinkedList<>(); // list of bi-cliques
    public static List<int[]> CMinusList; // list of |R|
    public static List<int[]> CPlusList; // list of |M|
    public static long attributeSum = 0; // sum of objects in each bi-cliques
    public static int[] attributeNum; // src, dst, time object sum
    

    /**
     * Main function
     * @param args  input_path, num_of_attributes, density_measure, num_of_blocks, lower_bound
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        if(args.length < 1) {
            printError();
            System.exit(-1);
        }

        int dimension = 3;

        final String input = args[0];
        System.out.println("input_path: " + input);

        final double thresholdBasic = Double.valueOf(args[1]);
        System.out.println("threshold(Basic): " + thresholdBasic);

        final int basicIter = Integer.valueOf(args[2]);
        System.out.println("basic model iteration: " + basicIter);

        final String output = args[3];
        System.out.println("output_path: " + output);

        System.out.println();

        System.out.println("importing the input dynamic graph...");
        Tensor tensor = TensorMethods.importTensor(input, dimension);
        System.out.println();
        System.out.println("running the algorithm...");
        System.out.println();

        new CutNPeel().run(tensor, thresholdBasic, basicIter, output);
    }

    private static void printError() {
        System.err.println("Usage: run_cutnpeel.sh input_path dimension threshold_parameter min_cost");
    }

    public void run(Tensor tensor, double thresholdBasic, int basicIter, final String path) throws IOException {
        final long start = System.currentTimeMillis();
        final Tensor oriTensor = tensor;
        tensor = oriTensor.copy();
        final int dimension = tensor.dimension;
        final double minCost = 0;
        final int[] cardOrder = getOrder(tensor.dimension, tensor.cardinalities);
        DensityMeasure measure = new DensityMeasure();
        List<BlockInfo> blockLists = new LinkedList<BlockInfo>();
        List<IntOpenHashSet[]> listOfAttributes = new LinkedList<>();
        List<Long> massList = new LinkedList<Long>();
        int num = 0;
        double maxOfCurLoop = Integer.MAX_VALUE / 100;
        double threshold = maxOfCurLoop;

        for(int repNum = 0; repNum < basicIter; repNum++) {
            if(threshold <= minCost){
                System.out.println("Stopped at "+(repNum+1)+"-th Loop. Running Time : " + (System.currentTimeMillis() - start + 0.0)/1000 + " seconds.");
                System.out.println();
                break;
            }
            threshold = (int) (maxOfCurLoop * thresholdBasic);
            maxOfCurLoop = 0;
            for (int order = 0; order < tensor.dimension; order++) {
                if(threshold <= minCost){break;}
                final int norm = cardOrder[order];
                Shingling shingling = new Shingling(tensor.dimension, tensor.cardinalities, norm, tensor.attributeToValuesToTuples, tensor.measureValues, tensor.attributes);
                for (int i = 0; i < shingling.shingleSize; i++) {
                    final SubTensor subOriTensor = new SubTensor(dimension, tensor.cardinalities, shingling.tupleToShingle[i], shingling.subTensorMass[i], tensor.attributes, tensor.measureValues);
                    SubTensor copyTensor = subOriTensor.copy();
                    measure.initialize(copyTensor.dimension, copyTensor.cardinalities, copyTensor.mass, oriTensor.cardinalities);

                    while (true) {
                        BlockInfo blockInfo = findOneBlock(copyTensor, oriTensor.cardinalities);
                        if (isNull(blockInfo.blockCardinalities)) {
                            break;
                        }
                        double tempCost = blockInfo.blockDensity;
                        num += 1;
                        if (num == 1) {
                            threshold = tempCost;
                        }
                        if (tempCost < threshold || tempCost < minCost) {
                            num -= 1;
                            maxOfCurLoop = Math.max(maxOfCurLoop, tempCost);
                            break;
                        }
                        else{
                            maxOfCurLoop = threshold;
                        }
                        long mass = removeAndGetMass(copyTensor, blockInfo, subOriTensor, measure, shingling.tupleToShingle[i], tensor);
                        blockInfo.modifyIndex(subOriTensor.subToOrigin);
                        listOfAttributes.add(blockInfo.getAttributeValues(copyTensor.dimension));
                        blockLists.add(blockInfo);
                        massList.add(mass);
                    }
                }
            }
            if(repNum % 10 == 9){
                System.out.println((repNum+1) + "th iteration, threshold : " + threshold + ", block number : " + blockLists.size());
            }
        }
        // check result
        final double CR = getResult(oriTensor, listOfAttributes);
        final double L_E = (Math.log(tensor.cardinalities[0])+Math.log(tensor.cardinalities[1])+Math.log(tensor.cardinalities[2]))/Math.log(2);
        final double tensorCost = tensor.omega * L_E;
        final double blockCost = (attributeSum+blockLists.size()) * Math.log(oriTensor.cardinalities[0]+oriTensor.cardinalities[1]+oriTensor.cardinalities[2])/Math.log(2);
        System.out.println();
        System.out.println("[Graph Info]     Edge # : " + tensor.omega + ", Graph Cost : " + String.format("%.4f", tensorCost));
        System.out.println("[Bi-clique Info] Bi-clique # : " + blockLists.size() + ", |R| : " + CPlusList.size() + ", |M| : " + CMinusList.size());
        System.out.println("[Cost Info]      Preciseness : " + String.format("%.4f", CMinusList.size() * L_E) +"(" + String.format("%.4f", CMinusList.size()*L_E/tensorCost*100)+ "%), "
                            + "Exhaustiveness : " + String.format("%.4f", CPlusList.size() * L_E) + "("+String.format("%.4f", CPlusList.size()*L_E/tensorCost*100)+"%), "
                            + "Conciseness : " + String.format("%.4f", blockCost) + "(" + String.format("%.4f", blockCost/tensorCost*100) +"%)");
        System.out.println("Compress Rate : " + String.format("%.4f", CR) + "%, Total Running Time : " + (System.currentTimeMillis() - start + 0.0)/1000 + " seconds.");

        System.out.println("\nWriting Outputs..");
        writeOutput(path, oriTensor, massList);
    }

    /**
     * find one dense block from a given tensor
     * @param tensor
     * @return
     * @throws IOException
     */
    protected BlockInfo findOneBlock(SubTensor tensor, int[] oriCardinalities) throws IOException {
        final int dimension = tensor.dimension;
        final int[] measureValues = tensor.measureValues.clone(); // clone values
        final int[][][] attributeToValuesToTuples = tensor.attributeToValuesToTuples;
        final int[][] attributes = tensor.attributes;
        final MinHeap[] heaps = createHeaps(tensor);
        final int sumOfCardinalities = sumOfCardinalities(tensor);
        final int LB = 4;
        DensityMeasure measure = new DensityMeasure();
        BlockIterInfo iterInfo = new BlockIterInfo(tensor.cardinalities);
        int maxIters = 0;
        double maxDensityAmongIters = measure.initialize(tensor.dimension, tensor.cardinalities, tensor.mass, oriCardinalities);
        maxDensityAmongIters = (sumOfCardinalities >= LB) ? maxDensityAmongIters : -Double.MAX_VALUE;

        if (sumOfCardinalities < LB || measure.getProductOfCardinalities() == 0.0)
            return iterInfo.returnEmptyBlock();

        for (int i = 0; i < sumOfCardinalities; i++) {
            byte maxAttribute = 0;
            double maxDensityAmongAttributes = -Double.MAX_VALUE;

            for (byte attribute = 0; attribute < dimension; attribute++) {
                final Pair<Integer, Integer> pair = heaps[attribute].peek();
                if (pair != null) {
                    double tempDensity = measure.ifRemoved(attribute, 1, pair.getValue());
                    if (tempDensity > maxDensityAmongAttributes) {
                        maxAttribute = attribute;
                        maxDensityAmongAttributes = tempDensity;
                    }
                }
            }

            Pair<Integer, Integer> pair = heaps[maxAttribute].poll();

            // key : which attribute has minimum pair.element (mass) in maxAttribute-th dimension
            // valueToRemove is removing attribute in dimension 'maxAttribute'
            int valueToRemove = pair.getKey();
            double density = measure.remove(maxAttribute, 1, pair.getValue());
            if (sumOfCardinalities-i-1 >= LB && ((density > maxDensityAmongIters) && density > 0.0)) {
                maxDensityAmongIters = density;
                iterInfo.setMass(measure.returnMass());
                maxIters = i + 1;
            }
            iterInfo.addIterInfo((byte)maxAttribute, valueToRemove);

            //update degress
            // entries are list of tuples in dimension of 'matAttribute-th', attribute of 'valueToRemove'
            int[] entries = attributeToValuesToTuples[maxAttribute][valueToRemove];
            for (int entry : entries) {
                // entry is tuple; measureValue is mass of the tuple
                int measureValue = measureValues[entry];
                if (measureValue > 0) {
                    for (int dim = 0; dim < dimension; dim++) {
                        if(dim != maxAttribute) {
                            // entry is one of removing tuple. we have to remove this mass from other dimensions
                            // attributevalue is attribute of entry's dim-th dimension (ex. t1 in t-th dimension)
                            int attributeValue = attributes[entry][dim];
                            // update attributevalue-th attribute's mass in dim-th dimension heap
                            heaps[dim].updatePriority(attributeValue, heaps[dim].getPriority(attributeValue) - measureValue);
                        }
                    }
                }
                measureValues[entry] = 0;
            }
        }

        if (maxDensityAmongIters < 0.0) {
            return iterInfo.returnEmptyBlock();
        }

        iterInfo.setDensity(maxDensityAmongIters);
        return iterInfo.returnBlock(maxIters, null);
    }

    // index of dimensions in increasing cardinality
    public static int[] getOrder(int dimension, int[] cardinalities){
        int[] order = new int[dimension];
        
        for(int i=0; i < dimension; i++){
            order[i] = i;
        }
        if(cardinalities[0] > cardinalities[1]){
            order[0] = 1;
            order[1] = 0;
        }
        if(cardinalities[order[1]] > cardinalities[order[2]]) {
            int temp = order[1];
            order[1] = order[2];
            order[2] = temp;
        }
        if(cardinalities[order[0]] > cardinalities[order[1]]) {
            int temp = order[0];
            order[0] = order[1];
            order[1] = temp;
        }
        
        return order;
    }

    /**
     * compute the sum of the cardinalities of the attributes of the given tensor
     * @param SubTensor    tensor
     * @return  sume of the ca
     */
    private static int sumOfCardinalities(SubTensor tensor){
        int sumOfCardinalities = 0;
        for(int dim = 0; dim < tensor.dimension; dim++) {
            sumOfCardinalities += tensor.cardinalities[dim];
        }
        return sumOfCardinalities;
    }

    /**
     * create heaps for each attribute
     * @param SubTensor tensor
     * @return
     */
    private static MinHeap[] createHeaps(final SubTensor tensor) {
        int[][] mass = TensorMethods.attributeValueMasses(tensor);
        MinHeap[] heaps = new MinHeap[tensor.dimension];
        for(int dim = 0; dim < tensor.dimension; dim++) {
            MinHeap heap = new MinHeap(tensor.cardinalities[dim]);
            int[] attributeMass = mass[dim];
            for(int index = 0; index < tensor.cardinalities[dim]; index++) {
                heap.insert(index, attributeMass[index]);
            }
            heaps[dim] = heap;
        }
        return heaps;
    }

    private static long removeAndGetMass(final SubTensor curTensor, final BlockInfo blockInfo, final SubTensor oriTensor, final DensityMeasure measure, final int[] curTupleToShingle, Tensor initTensor) throws IOException {

        final boolean[][] attributeToValuesToRemove = blockInfo.getBitMask(oriTensor.dimension, oriTensor.cardinalities);
        final int[][] attributes = curTensor.attributes;
        final int[] measureValues = curTensor.measureValues;
        final int[] oriValues = oriTensor.measureValues;
        int[] initValues = initTensor.measureValues;
        long mass = 0;
        final int minDim = blockInfo.minDim;

        for(int i=0; i<attributeToValuesToRemove[minDim].length; i++){
            if(attributeToValuesToRemove[minDim][i]){ // this is removing attribute value
                int[] entries = curTensor.attributeToValuesToTuples[minDim][i];
                for(int entry : entries){
                    int[] attributeValues = attributes[entry];
                    boolean removed = true;
                    for(int dim = 0; dim < curTensor.dimension; dim++) {
                        if(dim == minDim){
                            continue;
                        }
                        if(!attributeToValuesToRemove[dim][attributeValues[dim]]) {
                            removed = false;
                            break;
                        }
                    }
                    if(removed) {
                        mass += measureValues[entry];
                        curTensor.mass -= measureValues[entry];
                        initTensor.mass -= measureValues[entry];
                        measureValues[entry] = 0; //remove tuple (removing i-th tuple value)
                        initValues[curTupleToShingle[entry]] = 0;
                    }
                }
            }
        }
        return mass;
    }

    private double getResult(Tensor tensor, List<IntOpenHashSet[]> listOfAttributeToValues) throws IOException {
        final int blockNum = listOfAttributeToValues.size();
        final int dimension = tensor.dimension;
        double compress_ratio = 0;
        CMinusList = new LinkedList<int[]>();
        CPlusList = new LinkedList<int[]>();
        final Int2ObjectMap[] maps = tensor.convert();
        attributeNum = new int[dimension];
        int sumOfCardinalities = 0;

        int[] weight = new int[dimension];
        for(int i=0; i < tensor.dimension; i++){
            sumOfCardinalities += tensor.cardinalities[i];
            for(int j=i-1; j>=0; j--){
                weight[i] += tensor.cardinalities[j];
            }
        }

        for(int blockIndex = 0; blockIndex < blockNum; blockIndex++) {
            final IntOpenHashSet[] blockAttributes = listOfAttributeToValues.get(blockIndex);
            final IntOpenHashSet aggregateAtt = new IntOpenHashSet();
            
            for(int i=0; i < dimension; i++){
                for(int t : blockAttributes[i]){
                    aggregateAtt.add(t+weight[i]);
                }
            }
            listOfBlockAttributes.add(aggregateAtt);

            for(int i=0; i < tensor.dimension; i++){
                attributeSum += blockAttributes[i].size();
                attributeNum[i] += blockAttributes[i].size();
            }
            // negative exception
            for(int t : blockAttributes[2]){
                final Int2ObjectMap nodeToNeighbors = maps[t];
                int[] tempCorrection = new int[dimension];
                tempCorrection[2] = t + weight[2];
                if(nodeToNeighbors.size()==0){
                    for(int src : blockAttributes[0]){
                        tempCorrection[0] = src;
                        for(int dst : blockAttributes[1]){
                            tempCorrection[1] = dst + weight[1];
                            CMinusList.add(tempCorrection.clone());
                        }
                    }
                    continue;
                }
                for(int src : blockAttributes[0]){
                    tempCorrection[0] = src;
                    if(!nodeToNeighbors.containsKey(src)){
                        for(int dst : blockAttributes[1]){
                            tempCorrection[1] = dst + weight[1];
                            CMinusList.add(tempCorrection.clone());
                        }
                        continue;
                    }

                    Int2BooleanOpenHashMap neighbors = (Int2BooleanOpenHashMap) nodeToNeighbors.get(src);
                    for(int dst : blockAttributes[1]){
                        if(!neighbors.containsKey(dst)){
                            tempCorrection[1] = dst + weight[1];
                            CMinusList.add(tempCorrection.clone());
                        }
                        else{
                            neighbors.replace(dst, true);
                        }
                    }
                }
            }
        }

        for(int t=0; t < tensor.cardinalities[2]; t++){
            int[] tempCorrection = new int[dimension];
            tempCorrection[2] = t + weight[2];
            final Int2ObjectMap nodeToNeighbors = maps[t];
            for(int src : nodeToNeighbors.keySet()){
                tempCorrection[0] = src;
                final Int2BooleanOpenHashMap neighbors = (Int2BooleanOpenHashMap) nodeToNeighbors.get(src);
                for(int dst : neighbors.keySet()){
                    if(neighbors.get(dst) == false){                
                        tempCorrection[1] = dst + weight[1];
                        CPlusList.add(tempCorrection.clone());
                    }
                }
            }
        }
        final double L_I = Math.log(sumOfCardinalities)/Math.log(2);
        final double L_e = (Math.log(tensor.cardinalities[0])+Math.log(tensor.cardinalities[1])+Math.log(tensor.cardinalities[2]))/Math.log(2);
        compress_ratio = ((attributeSum+blockNum) * L_I + CPlusList.size() * L_e + CMinusList.size() * L_e + 0.0) / (tensor.omega * L_e) * 100;
        return compress_ratio;
    }

    private static void writeOutput(String output, Tensor tensor, List<Long> massList) throws IOException{
        File dir = new File(output);
        try{
            dir.mkdir();
        }
        catch(Exception e){
        }
        
        String[][] intToStrValue = tensor.intToStrValue;
        int blockNum = listOfBlockAttributes.size();
        int[] weight = new int[tensor.dimension];
        for(int i=0; i < 3; i++){
            for(int j=i-1; j>=0; j--){
                weight[i] += tensor.cardinalities[j];
            }
        }

        BufferedWriter bw = new BufferedWriter(new FileWriter(output + File.separator + "bicliques.txt"));

        for(int blockIndex = 0; blockIndex < blockNum; blockIndex++){
            final IntOpenHashSet attributeToValues = listOfBlockAttributes.get(blockIndex);
            List<Integer> dim0 = new LinkedList();
            List<Integer> dim1 = new LinkedList();
            List<Integer> dim2 = new LinkedList();
            for(int value : attributeToValues){
                if(value >= weight[2]){
                    dim2.add(value-weight[2]);
                }
                else if(value >= weight[1]){
                    dim1.add(value-weight[1]);
                }
                else{
                    dim0.add(value);
                }
            }
            bw.write("#"+(1+blockIndex)+" size : "+dim0.size()+" X "+dim1.size()+" X "+dim2.size()+" = "+dim0.size()*dim1.size()*dim2.size()+", mass : "+massList.get(blockIndex)+"\n");
            for(int value : dim0){
                bw.write(intToStrValue[0][value]+" ");
            }
            bw.newLine();
            for(int value : dim1){
                bw.write(intToStrValue[1][value]+" ");
            }
            bw.newLine();
            for(int value : dim2){
                bw.write(intToStrValue[2][value]+" ");
            }
            bw.newLine();
        }
        bw.close();
        
        BufferedWriter miss = new BufferedWriter(new FileWriter(output + File.separator + "missingE.txt"));
        for(int i = 0; i < CMinusList.size(); i++){
            final int[] attributes = CMinusList.get(i);
            miss.write(intToStrValue[0][attributes[0]]+" "+intToStrValue[1][attributes[1]-weight[1]]+" "+intToStrValue[2][attributes[2]-weight[2]]);
            miss.newLine();
        }
        miss.close();

        BufferedWriter remain = new BufferedWriter(new FileWriter(output + File.separator + "remainingE.txt"));
        for(int i = 0; i < CPlusList.size(); i++){
            final int[] attributes = CPlusList.get(i);
            remain.write(intToStrValue[0][attributes[0]]+" "+intToStrValue[1][attributes[1]-weight[1]]+" "+intToStrValue[2][attributes[2]-weight[2]]);
            remain.newLine();
        }
        remain.close();
    }
}
