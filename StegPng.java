//Author Martin Kennelly
//Hide an image within an image
//Must be the same pixel width and height

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class StegPng {

    public static void main(String[] args) {
        try {
            switch (args.length) {
                case 3:
                    encrypt(args);
                    break;
                case 2:
                    decrypt(args);
                    break;
                default:
                    System.out.println("Incorrect arguments");
                    System.out.println("Encrypt: java -jar StehPng.jar [base image] [image to hide (same size as base)] [output location]");
                    System.out.println("Decrypt: java -jar StegPng.jar [encrypted image] [output location]");
            }
        } catch (IOException iox) {
            System.out.println(iox.getMessage());
            iox.printStackTrace(System.out);
        }
    }

    private static int doMask(int input, int cover) {
        int mask = 0xc0c0c0c0;
        cover &= mask;
        cover >>>= 6;
        input &= ~(mask >>> 6);
        return input |= cover;
    }

    private static int extractHiddenPixel(int encrypted) {
        int mask = 0x03030303;
        encrypted &= mask;
        encrypted <<= 6;
        for (int i=0; i<3; i++)
            encrypted |= (encrypted >>> 2);
        return encrypted;
    }

    private static void encrypt(String[] args) throws IOException {
        String base = args[0];
        String hidden = args[1];
        String output = args[2];

        BufferedImage inputImage = ImageIO.read(new File(base));
        BufferedImage hiddenImage = ImageIO.read(new File(hidden));

        if (!((hiddenImage.getHeight() == inputImage.getHeight()) && (hiddenImage.getWidth() == inputImage.getWidth()))) {
            System.out.println("Error: Images are not the same sizes");
            System.exit(1);
        }

        int width = inputImage.getWidth();
        int height = inputImage.getHeight();

        for ( int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int inputPixel = inputImage.getRGB(i,j);
                int hiddenPixel = hiddenImage.getRGB(i,j);
                inputPixel = doMask(inputPixel,hiddenPixel);
                inputImage.setRGB(i,j,inputPixel);
            }
        }

        if (!ImageIO.write(inputImage,"png",new File(output))){
            System.out.println("Failed to write file to output");
        }
    }

    private static void decrypt(String[] args) throws IOException {
        String encrypted = args[0];
        String decrypted = args[1];

        BufferedImage encryptedImage = ImageIO.read(new File(encrypted));

        int width = encryptedImage.getWidth();
        int height = encryptedImage.getHeight();

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int encryptedPixel = encryptedImage.getRGB(i,j);
                int decryptedPixel = extractHiddenPixel(encryptedPixel);
                encryptedImage.setRGB(i,j,decryptedPixel);
            }
        }
        ImageIO.write(encryptedImage,"png", new File(decrypted));
    }

}
