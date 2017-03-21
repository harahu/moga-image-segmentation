package moga;

import com.sun.javafx.geom.Edge;

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
        BufferedImage img = getImage("./TestImage/2/Test Image.jpg");

        Random randomizer = new Random();
        ArrayList<EdgeCost> mst = generateMST(img.getWidth(), img.getHeight(), img, randomizer);

        int[] dimst = createDiMST(mst, img.getWidth(), img.getHeight());

        System.out.println(Arrays.toString(dimst));

        Collections.sort(mst);
        ArrayList<int[]> population = new ArrayList<>();

        int sz = img.getHeight()*img.getWidth();

        /*
        for(int i = 0; i < 60; ++i) {
            int[] genotype = new int[sz];
            System.arraycopy(dimst, 0, genotype, 0, dimst.length);

            for(int j = 0; j < i; ++j) {
                EdgeCost edge = mst.get(mst.size()-1-j);
                int a = edge.getA();
                int b = edge.getB();

                if(genotype[a] == b) genotype[a] = a;
                else if(genotype[b] == a) genotype[b] = b;
                else System.out.println("EDGE PROBLEM IN DIMST");
            }

            population.add(genotype);
        }
        */

        for(int i = 0; i < 60; ++i) {
            int[] genotype = new int[sz];
            System.arraycopy(dimst, 0, genotype, 0, dimst.length);

            for(int j = 0; j < i; ++j) {
                int a = (int)(randomizer.nextDouble() * (double)sz);

                while(genotype[a] == a) a = (int)(randomizer.nextDouble() * (double)sz);

                genotype[a] = a;
            }

            population.add(genotype);
        }

        SegmentationPhenotype seg = new SegmentationPhenotype(img, population.get(59));

        seg.drawSegmentation();
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

    public static int[] createDiMST(ArrayList<EdgeCost> mst, int x_sz, int y_sz) {
        int sz = x_sz*y_sz;

        int[] directed_mst = new int[sz];
        Arrays.fill(directed_mst, -1);

        ArrayList<ArrayList<Integer>> edges = new ArrayList<>();
        for(int i = 0; i < sz; ++i) {
            edges.add(new ArrayList<>());
        }

        int len = mst.size();
        for(int i = 0; i < len; ++i) {
            int a = mst.get(i).getA();
            int b = mst.get(i).getB();

            edges.get(a).add(b);
            edges.get(b).add(a);
        }

        int index_set = 0;
        while(index_set < sz-1) {
            for(int i = 0; i < sz; ++i) {
                if(edges.get(i).size() == 1) {
                    int val = edges.get(i).remove(0);
                    directed_mst[i] = val;

                    for(int j = 0; j < edges.get(val).size(); ++j) {
                        if(edges.get(val).get(j) == i) {
                            edges.get(val).remove(j);
                            break;
                        }
                    }

                    index_set++;
                }
            }
        }

        for(int i = 0; i < sz; ++i) {
            if(directed_mst[i] == -1) {
                directed_mst[i] = i;
                break;
            }
        }

        return directed_mst;
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
