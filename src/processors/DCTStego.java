package processors;

import org.jtransforms.dct.DoubleDCT_2D;
import filereader.FileReader;
import filereader.Image;
import filereader.FileReader.ImageType;

public class DCTStego implements ImageProcessor {
    private static DCTStego instance = new DCTStego();

    // Blending parameters: alpha near 1 means mostly secret data is embedded.
    public static double alpha = 0.8;
    public static double scale = 1.0;
    
    // Block-based parameters.
    public static int blockSize = 8; // Use 8x8 blocks
    // Selected coefficient within each block (avoid DC at [0][0])
    public static int embedX = 3;
    public static int embedY = 3;

    private DCTStego() {
        DCTStego.instance = this;
    }

    public static DCTStego instance() {
        return DCTStego.instance;
    }

    @Override
    public Image encode(Image cover, Image toEncode) {
        int coverW = cover.width;
        int coverH = cover.height;
        
        // Number of blocks in x and y directions
        int blocksX = coverW / blockSize;
        int blocksY = coverH / blockSize;
        
        // Scale secret image to the block grid size.
        // We assume Image.scale can produce an image with specified width and height.
        Image secretScaled = Image.scale(toEncode, blocksX, blocksY);
        
        // Process each block of the cover image.
        for (int bx = 0; bx < blocksX; bx++) {
            for (int by = 0; by < blocksY; by++) {
                // Create arrays for one block for each channel.
                double[][] blockL = new double[blockSize][blockSize];
                double[][] blockA = new double[blockSize][blockSize];
                double[][] blockB = new double[blockSize][blockSize];
                
                // Copy block data from cover image channels.
                for (int i = 0; i < blockSize; i++) {
                    for (int j = 0; j < blockSize; j++) {
                        int x = bx * blockSize + i;
                        int y = by * blockSize + j;
                        blockL[i][j] = cover.labL[x][y];
                        blockA[i][j] = cover.labA[x][y];
                        blockB[i][j] = cover.labB[x][y];
                    }
                }
                
                // Compute the 2D DCT on this block.
                DoubleDCT_2D dctBlock = new DoubleDCT_2D(blockSize, blockSize);
                dctBlock.forward(blockL, true);
                dctBlock.forward(blockA, true);
                dctBlock.forward(blockB, true);
                
                // Embed the secret data into the chosen mid-frequency coefficient.
                // (Assuming secretScaled channels are stored in the same Lab order and that
                // secretScaled dimensions are [blocksX][blocksY])
                blockL[embedX][embedY] = alpha * (secretScaled.labL[bx][by] * scale)
                        + (1 - alpha) * blockL[embedX][embedY];
                blockA[embedX][embedY] = alpha * (secretScaled.labA[bx][by] * scale)
                        + (1 - alpha) * blockA[embedX][embedY];
                blockB[embedX][embedY] = alpha * (secretScaled.labB[bx][by] * scale)
                        + (1 - alpha) * blockB[embedX][embedY];
                
                // Perform the inverse DCT to get the modified block.
                dctBlock.inverse(blockL, true);
                dctBlock.inverse(blockA, true);
                dctBlock.inverse(blockB, true);
                
                // Write the modified block back into the cover image channels.
                for (int i = 0; i < blockSize; i++) {
                    for (int j = 0; j < blockSize; j++) {
                        int x = bx * blockSize + i;
                        int y = by * blockSize + j;
                        cover.labL[x][y] = blockL[i][j];
                        cover.labA[x][y] = blockA[i][j];
                        cover.labB[x][y] = blockB[i][j];
                    }
                }
            }
        }
        return cover;
    }

    @Override
    public Image decode(Image encoded) {
        int coverW = encoded.width;
        int coverH = encoded.height;
        
        // Number of blocks in x and y directions.
        int blocksX = coverW / blockSize;
        int blocksY = coverH / blockSize;
        
        // Prepare arrays for the recovered secret image.
        double[][] secretL = new double[blocksX][blocksY];
        double[][] secretA = new double[blocksX][blocksY];
        double[][] secretB = new double[blocksX][blocksY];
        
        // Process each block of the encoded image.
        for (int bx = 0; bx < blocksX; bx++) {
            for (int by = 0; by < blocksY; by++) {
                double[][] blockL = new double[blockSize][blockSize];
                double[][] blockA = new double[blockSize][blockSize];
                double[][] blockB = new double[blockSize][blockSize];
                
                // Copy block data from encoded image channels.
                for (int i = 0; i < blockSize; i++) {
                    for (int j = 0; j < blockSize; j++) {
                        int x = bx * blockSize + i;
                        int y = by * blockSize + j;
                        blockL[i][j] = encoded.labL[x][y];
                        blockA[i][j] = encoded.labA[x][y];
                        blockB[i][j] = encoded.labB[x][y];
                    }
                }
                
                // Compute the forward DCT on the block.
                DoubleDCT_2D dctBlock = new DoubleDCT_2D(blockSize, blockSize);
                dctBlock.forward(blockL, true);
                dctBlock.forward(blockA, true);
                dctBlock.forward(blockB, true);
                
                // Extract the secret data from the chosen coefficient.
                secretL[bx][by] = blockL[embedX][embedY] / (alpha * scale);
                secretA[bx][by] = blockA[embedX][embedY] / (alpha * scale);
                secretB[bx][by] = blockB[embedX][embedY] / (alpha * scale);
            }
        }
        
        // Reconstruct and return the secret image.
        // (You can further scale this image to the original secret dimensions if needed.)
        return new Image(secretL, secretA, secretB);
    }
}
