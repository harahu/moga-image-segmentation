package moga;

import com.sun.javafx.geom.Edge;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("Which image do you wish to work on?");

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        String filename = "";
        String input = "";

        try {
            input = br.readLine();
        } catch (IOException ex) {
            System.err.println("IOException reading file identifier.");
            ex.printStackTrace();
            input = "test";
        }

        input = input.trim();

        if(input.equals("test") || input.equals("Test") || input.equals("t")) filename = "./simple_image.jpg";
        else {
            filename = "./TestImage/"+input+"/Test image.jpg";
        }

        System.out.println(filename);

        BufferedImage img = getImage(filename);

        Random randomizer = new Random();
        ArrayList<EdgeCost> mst = generateMST(img.getWidth(), img.getHeight(), img, randomizer);

        int[] dimst = createDiMST(mst, img.getWidth(), img.getHeight());

        Collections.sort(mst);
        ArrayList<int[]> population = new ArrayList<>();

        int sz = img.getHeight()*img.getWidth();

        for(int i = 0; i < 100; ++i) {
            int[] genotype = new int[sz];
            System.arraycopy(dimst, 0, genotype, 0, dimst.length);

            for(int j = 0; j < i; ++j) {
                int a = (int)(randomizer.nextDouble() * (double)sz);

                while(genotype[a] == a) a = (int)(randomizer.nextDouble() * (double)sz);

                genotype[a] = a;
            }

            population.add(genotype);
        }

        spea(population, img);

        SegmentationPhenotype seg = new SegmentationPhenotype(img, population.get(99));

        seg.drawSegmentation();
        System.exit(0);
    }

    public static ArrayList<SegmentationGenotype> spea(ArrayList<int[]> initialPop, BufferedImage img) {
        ArrayList<SegmentationGenotype> population = new ArrayList<SegmentationGenotype>();
        ArrayList<SegmentationGenotype> archive = new ArrayList<SegmentationGenotype>();
        //Initialize population
        for (int[] genome: initialPop) {
            population.add(new SegmentationGenotype(img, genome));
        }
        int t = 0;
        //Sort population
        int[] popRanking = fastNonDominatedSort(population);
        for (int rnk: popRanking) {
            System.out.println(rnk);
        }
        return population;
    }

    public static int[] fastNonDominatedSort(ArrayList<SegmentationGenotype> population) {

        int[] ranking = new int[population.size()];
        Arrays.fill(ranking, -1);

        int[] domCounts = new int[population.size()];

        ArrayList<ArrayList<Integer>> dominationLists = new ArrayList<>();

        for (int i = 0; i < population.size(); i++) {
            SegmentationGenotype p = population.get(i);
            ArrayList<Integer> dominated = new ArrayList<>();
            int domCount = 0;
            for (int j = 0; j < population.size(); j++) {
                SegmentationGenotype q = population.get(j);
                if (p.dominates(q)) {
                    dominated.add(j);
                } else if (q.dominates(p)) {
                    domCount++;
                }
            }
            if (domCount == 0) {
                ranking[i] = 0;
            }
            dominationLists.add(dominated);
            domCounts[i] = domCount;
        }
        int i = 0;
        ArrayList<Integer> front = new ArrayList<>();
        for (int j = 0; j < ranking.length; j++) {
            if (ranking[j] == 0) {
                front.add(j);
            }
        }
        while (front.size() > 0) {
            ArrayList<Integer> Q = new ArrayList<>();
            for (int j = 0; j < front.size(); j++) {
                int pIndex = front.get(j);
                for (int qIndex : dominationLists.get(pIndex)) {
                    domCounts[qIndex]--;
                    if (domCounts[qIndex] == 0) {
                        ranking[qIndex] = i + 1;
                        Q.add(qIndex);
                    }
                }
            }
            i++;
            front = Q;
        }
        return ranking;
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

    public static int[][] uniformCrossover(int[] parent_1, int[] parent_2, Random randomizer) {
        int[][] children = new int[2][parent_1.length];

        for(int i = 0; i < parent_1.length; ++i) {
            if(randomizer.nextDouble() <= 0.5) {
                children[0][i] = parent_1[i];
                children[1][i] = parent_2[i];
            } else {
                children[0][i] = parent_2[i];
                children[1][i] = parent_1[i];
            }
        }

        return children;
    }

    public static int[] switchMutation(int[] genotype_in, Random randomizer, double mutation_rate, int x_sz, int y_sz) {
        int[] mutant = new int[genotype_in.length];

        for(int i = 0; i < genotype_in.length; ++i) {
            int[] neighbours;
            if(randomizer.nextDouble() > mutation_rate) {
                mutant[i] = genotype_in[i];
            } else {
                neighbours = SegmentationPhenotype.neighbourhood(i, x_sz, y_sz);
                int count = 0;
                for(int j = 0; j < neighbours.length; ++j) {
                    if(neighbours[j] != -1) ++count;
                }

                int num = (int)(randomizer.nextDouble()*(count+1));

                int second_count = 0;
                for(int j = 0; j < neighbours.length; ++j) {
                    if(neighbours[j] != -1) {
                        if(second_count == num) {
                            mutant[i] = neighbours[j];
                            break;
                        } else {
                            ++second_count;
                        }
                    }
                }
                if(second_count == count) mutant[i] = i;
            }
        }

        return mutant;
    }
}
