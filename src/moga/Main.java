package moga;

import javax.imageio.ImageIO;
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
    }

    public static BufferedImage getImage(String pathName) {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(pathName));
        } catch (IOException e) {
            System.out.println("what");
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

    public static int getEdgeCost(int a, int b, int x_sz, BufferedImage img) {
        Color c_0 = new Color(img.getRGB(a%x_sz, a/x_sz));
        Color c_1 = new Color(img.getRGB(b%x_sz, b/x_sz));

        double r_dist = c_0.getRed()-c_1.getRed();
        double g_dist = c_0.getGreen()-c_1.getGreen();
        double b_dist = c_0.getBlue()-c_1.getBlue();

        return (int)Math.pow(r_dist, 2) + (int)Math.pow(g_dist, 2) + (int)Math.pow(b_dist, 2);
    }

    public static ArrayList<EdgeCost> getEdges(int a, int x_sz, int y_sz, BufferedImage img) {
        int sz = x_sz*y_sz;
        ArrayList<EdgeCost> edges = new ArrayList<>();

        if((a%x_sz)-1 >= 0) edges.add(new EdgeCost(a, a-1, getEdgeCost(a, a-1, x_sz, img)));
        if((a%x_sz)+1 < x_sz) edges.add(new EdgeCost(a, a+1, getEdgeCost(a, a+1, x_sz, img)));
        if(a-x_sz >= 0) edges.add(new EdgeCost(a, a-x_sz, getEdgeCost(a, a-x_sz, x_sz, img)));
        if(a+x_sz < sz) edges.add(new EdgeCost(a, a+x_sz, getEdgeCost(a, a+x_sz, x_sz, img)));

        return edges;
    }

    public static ArrayList<EdgeCost> generateMST(int x_sz, int y_sz, BufferedImage img, Random randomizer) {
        int sz = x_sz*y_sz;

        int a;

        a = (int)((double)sz * randomizer.nextDouble());
        ArrayList<EdgeCost> edges = getEdges(a, x_sz, y_sz, img);

        Collections.sort(edges);

        ArrayList<EdgeCost> mst = new ArrayList<>();
        ArrayList<Integer> nodes_in_mst = new ArrayList<>();
        nodes_in_mst.add(a);

        int count = 0;
        while(mst.size() < sz-1 && !edges.isEmpty()) {
            if(count % 1000 == 0) {
                System.out.println(count);
            }
            count++;
            EdgeCost edge = edges.remove(0);
            mst.add(edge);
            nodes_in_mst.add(edge.getB());

            ArrayList<Integer> remove_by_index = new ArrayList<>();

            for(int i = 0; i < edges.size(); ++i) {
                if(edges.get(i).getB() == edge.getB()) {
                    remove_by_index.add(i);
                }
            }

            for(int i = 0; i < remove_by_index.size(); ++i) {
                edges.remove(remove_by_index.get(i).intValue());
                for(int j = i; j < remove_by_index.size(); ++j) {
                    remove_by_index.set(j, remove_by_index.get(j)-1);
                }
            }

            ArrayList<EdgeCost> new_edges = getEdges(edge.getB(), x_sz, y_sz, img);

            for(int i = 0; i < new_edges.size(); ++i) {
                if(!nodes_in_mst.contains(new_edges.get(i).getB())) {
                    edges.add(new_edges.get(i));
                }
            }

            Collections.sort(edges);
        }

        return mst;
    }

    public static int[] nSeg(int x_sz, int y_sz) {
        int sz = x_sz*y_sz;
        int[] genome = new int[sz];
        for (int i = 0; i < sz; i++) {
            genome[i] = i;
        }
        return genome;
    }

    public static int[] oneSeg(int x_sz, int y_sz) {
        int sz = x_sz*y_sz;
        int[] genome = new int[sz];
        for (int i = 0; i < sz-1; i++) {
            genome[i] = i+1;
        }
        for (int i = x_sz-1; i < sz-1; i += x_sz) {
            genome[i] = i+x_sz;
        }
        genome[sz-1] = sz-1;
        return genome;
    }

    public static  int[] twoSeg(int x_sz, int y_sz){
        int[] genome = oneSeg(x_sz, y_sz);
        genome[(x_sz-1)+x_sz*(y_sz/2)] = (x_sz-1)+x_sz*(y_sz/2);
        return genome;
    }
}
