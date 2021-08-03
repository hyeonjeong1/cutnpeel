package cutnpeel;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

/**
 * Information about each block found
 */
public class BlockInfo {

    public int[] blockCardinalities;
    private byte[] attributes = null;
    private int[] attVals = null;
    private int size = 0;
    public String diskFilePath = null;
    private boolean useBuffer = false;
    public int minDim = 0;
    public long blockMass = 0;
    public double blockDensity = 0;

    public BlockInfo(int size, int[] blockCardinalities, byte[] attributes, int[] attVals, long mass, double density) {
        this.size = size;
        this.useBuffer = true;
        this.attributes = attributes;
        this.attVals = attVals;
        this.blockCardinalities = blockCardinalities;
        this.blockMass = mass;
        this.blockDensity = density;
    }

    public BlockInfo(int size, int[] blockCardinalities, String diskFilePath, long mass, double density) {
        this.size = size;
        this.useBuffer = false;
        this.diskFilePath = diskFilePath;
        this.blockCardinalities = blockCardinalities;
        this.blockMass = mass;
        this.blockDensity = density;
    }

    public IntOpenHashSet[] getAttributeValues(int dimension) throws IOException {
        IntOpenHashSet[] modeToAttVals = new IntOpenHashSet[dimension];
        for(int mode = 0; mode < dimension; mode++) {
            modeToAttVals[mode] = new IntOpenHashSet();
        }
        if(useBuffer) {
            for (int i = 0; i < size; i++) {
                byte mode = attributes[i];
                modeToAttVals[mode].add(attVals[i]);
            }
        }
        else {
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(diskFilePath), 8388608));
            for(int i = 0; i < size; i++) {
                byte mode = in.readByte();
                modeToAttVals[mode].add(in.readInt());
            }
            in.close();
        }
        return modeToAttVals;
    }
    
    public boolean[][] getBitMask(int dimension, int[] cardinalities) throws IOException {
        int[] attRemoveNum = new int[dimension];
        final boolean[][] modeToIndexToBeingIncluded = new boolean[dimension][];
        for(int mode = 0; mode < dimension; mode++) {
            modeToIndexToBeingIncluded[mode] = new boolean[cardinalities[mode]];
        }

        if(useBuffer) {
            for (int i = 0; i < size; i++) {
                byte mode = attributes[i];
                modeToIndexToBeingIncluded[mode][attVals[i]] = true;
                attRemoveNum[mode] += 1;
            }
        }
        else {
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(diskFilePath), 8388608));
            for(int i = 0; i < size; i++) {
                byte mode = in.readByte();
                modeToIndexToBeingIncluded[mode][in.readInt()] = true;
                attRemoveNum[mode] += 1;
            }
            in.close();
        }

        for(int mode = 0; mode < dimension; mode++){
            if(attRemoveNum[minDim] > attRemoveNum[mode]){
                minDim = mode;
            }
        }
        return modeToIndexToBeingIncluded;
    }

    public String returnFileInfo(String tempLocalFilePath) throws IOException {
        if(useBuffer) {
            String newPath = tempLocalFilePath;
            ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(newPath), 8388608));
            for(int i = 0; i < size; i++) {
                out.writeByte(attributes[i]);
                out.writeInt(attVals[i]);
            }
            out.close();
            return newPath;
        }
        else {
            String newPath = tempLocalFilePath;
            Files.copy(new File(diskFilePath).toPath(), new File(newPath).toPath(), StandardCopyOption.REPLACE_EXISTING);
            return newPath;
        }

    }

    public void clear() {
        if(!useBuffer) {
            if (new File(diskFilePath).exists()) {
                new File(diskFilePath).delete();
            }
        }
    }

    public BlockInfo copy(){
        return new BlockInfo(this.size, this.blockCardinalities, this.attributes, this.attVals, this.blockMass, this.blockDensity);
    }

    public void modifyIndex(int[][] subToOrigin){
        for(int i = 0; i < attVals.length; i++){
            int curAtt = attVals[i];
            attVals[i] = subToOrigin[attributes[i]][curAtt];
        }
    }

}
