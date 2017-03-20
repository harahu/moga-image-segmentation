package moga;

import sun.misc.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        BufferedImage img = getImage("./TestImage/1/Test image.jpg");

        System.out.println("("+Integer.toString(img.getWidth())+","+Integer.toString(img.getHeight())+")");

        int[] mst = generateMST(img.getWidth(), img.getHeight(), img);
    }

    public static BufferedImage getImage(String pathName) {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(pathName));
        } catch (IOException e) {
        }
        return img;
    }

    public static ArrayList<EdgeCost> getEdgeCosts(int x_sz, int y_sz, BufferedImage img) {
        int sz = x_sz*y_sz;
        ArrayList<EdgeCost> edges = new ArrayList<>();

        for(int i = 0; i < sz; ++i) {
            if(i % x_sz != x_sz-1) {
                int a = i;
                int b = i+1;

                int x, y;

                x = i%x_sz;
                y = i/x_sz;
                Color c_0 = new Color(img.getRGB(x,y));

                x = (i+1)%x_sz;
                y = (i+1)/x_sz;
                Color c_1 = new Color(img.getRGB(x,y));

                double r_dist = c_0.getRed()-c_1.getRed();
                double g_dist = c_0.getGreen()-c_1.getGreen();
                double b_dist = c_0.getBlue()-c_1.getBlue();

                int cost = (int)Math.pow(r_dist, 2) + (int)Math.pow(g_dist, 2) + (int)Math.pow(b_dist, 2);

                EdgeCost edge = new EdgeCost(a,b,cost);
                edges.add(edge);
            }

            if(i+x_sz < sz) {
                int a  = i;
                int b = i+x_sz;

                int x, y;

                x = i%x_sz;
                y = i/x_sz;
                Color c_0 = new Color(img.getRGB(x,y));

                x = (i+1)%x_sz;
                y = (i+1)/x_sz;
                Color c_1 = new Color(img.getRGB(x,y));

                double r_dist = c_0.getRed()-c_1.getRed();
                double g_dist = c_0.getGreen()-c_1.getGreen();
                double b_dist = c_0.getBlue()-c_1.getBlue();

                int cost = (int)Math.pow(r_dist, 2) + (int)Math.pow(g_dist, 2) + (int)Math.pow(b_dist, 2);

                EdgeCost edge = new EdgeCost(a,b,cost);
                edges.add(edge);
            }
        }

        return edges;
    }

    public static int[] generateMST(int x_sz, int y_sz, BufferedImage img) {
        int sz = x_sz*y_sz;
        int[] directed_mst = new int[sz];
        Arrays.fill(directed_mst, -1);

        ArrayList<EdgeCost> edges = getEdgeCosts(x_sz, y_sz, img);

        Collections.sort(edges);

        return directed_mst;
    }
}
