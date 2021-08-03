package cutnpeel;

import java.io.*;

/**
 * Order by which attribute values are removed
 */
public class BlockIterInfo {

    private int dimension = 0;
    private byte[] attributes = null;
    private int[] attVals = null;
    private boolean useBuffer = true;
    private int cardinalitySum = 0;
    private int curIndex = 0;
    private ObjectOutputStream out = null;
    private String orderingFilePath = null;
    private long mass = 0;
    private double density = 0;

    public BlockIterInfo(int[] modeLengths) throws IOException {
        this.useBuffer = true;
        this.dimension = modeLengths.length;
        for(int mode = 0; mode < dimension; mode++) {
            cardinalitySum += modeLengths[mode];
        }
        this.attributes = new byte[cardinalitySum];
        this.attVals = new int[cardinalitySum];
    }

    public void setMass(long mass) throws IOException {
        this.mass = mass;
    }

    public void setDensity(double density) throws IOException {
        this.density = density;
    }

    public void addIterInfo(byte mode, int index) throws IOException {
        if(useBuffer) {
            attributes[curIndex] = mode;
            attVals[curIndex++] = index;
        }
        else {
            out.writeByte(mode);
            out.writeInt(index);
        }
    }

    public BlockInfo returnBlock(int maxIter, String blockInfoPath) throws IOException {
        if (out != null) {
            out.close();
        }

        if(useBuffer) { // write block info in memory
            int[] modeLengths = new int[dimension];
            int newLength = cardinalitySum - maxIter;
            byte[] newModes = new byte[newLength];
            for(int i = 0; i < newLength; i++) {
                newModes[i] = attributes[i+maxIter];
                modeLengths[attributes[i+maxIter]]++;
            }
            int[] newIndices = new int[newLength];
            for(int i = 0; i < newLength; i++) {
                newIndices[i] = attVals[i+maxIter];
            }
            return new BlockInfo(newLength, modeLengths, newModes, newIndices, mass, density);
        }
        else { //write block info in disk

            int[] modeLengths = new int[dimension];
            int newLength = cardinalitySum - maxIter;
            ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(blockInfoPath), 8388608));
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(orderingFilePath), 8388608));

            for(int i = 0; i < maxIter; i++) { //throw away
                in.readByte();
                in.readInt();
            }
            for(int i = 0; i < newLength; i++) {
                byte mode = in.readByte();
                out.writeByte(mode);
                modeLengths[mode]++;
                out.writeInt(in.readInt());
            }
            in.close();
            out.close();

            if(new File(orderingFilePath).exists()) {
                new File(orderingFilePath).delete();
            }

            return new BlockInfo(newLength, modeLengths, blockInfoPath, mass, density);
        }
    }

    public BlockInfo returnEmptyBlock() throws IOException {
        return new BlockInfo(0, null, null, null, 0, 0);
    }
}
