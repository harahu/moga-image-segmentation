package moga;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        BufferedImage img = getImage("./Test Image/1/Test image.jpg");
        System.out.println(img.getRGB(0, 0));
        System.out.println(img.getWidth());
        JFrame frame = new JFrame();
        frame.getContentPane().setLayout(new FlowLayout());
        frame.getContentPane().add(new JLabel(new ImageIcon(img)));
        frame.pack();
        frame.setVisible(true);
    }

    public static BufferedImage getImage(String pathName) {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(pathName));
        } catch (IOException e) {
        }
        return img;
    }

    public static int[] generateRandomGenome(int x_sz, int y_sz, Random randomizer) {
        int sz = x_sz*y_sz;
        int[] genome = new int[sz];
        for(int i = 0; i < sz; ++i) {
            float r = randomizer.nextFloat();

            int val;
            if(r <= 0.2) {
                val = i;
            } else if(r <= 0.4) {
                val = i-1;
            } else if(r <= 0.6) {
                val = i+1;
            } else if(r <= 0.8) {
                val = i-x_sz;
            } else {
                val = i+x_sz;
            }

            if(val < 0 || val >= sz) val = i;

            genome[i] = val;
        }

        return genome;
    }
}
