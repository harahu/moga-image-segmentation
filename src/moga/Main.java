package moga;

import com.sun.javafx.geom.Edge;
import com.sun.javafx.image.IntPixelGetter;
import org.jzy3d.analysis.AnalysisLauncher;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

enum populationGenerationType {UNIFORM_RANDOM, SPECIFIED_RANDOM}

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

        run(img, br);

    }


    public static void run(BufferedImage img, BufferedReader br) {
        Random randomizer = new Random();
        ArrayList<EdgeCost> mst = generateMST(img.getWidth(), img.getHeight(), img, randomizer);

        int[] dimst = createDiMST(mst, img.getWidth(), img.getHeight());

        Collections.sort(mst);

        int sz = img.getHeight()*img.getWidth();
        ArrayList<int[]> population = createPopulation(dimst, populationGenerationType.SPECIFIED_RANDOM, sz, 100, br, randomizer);

        segment(population, img, 50, 2,false, true, true);
        segment(population, img, 50, 2,true, false, true);
        segment(population, img, 50, 2,true, true, false);
        segment(population, img, 50, 2,true, true, true);
    }

    public static void segment(ArrayList<int[]> population, BufferedImage img, int numGen, int mutRate, boolean dev, boolean edge, boolean con) {
        boolean[] objectives = new boolean[3];
        objectives[0] = dev;
        objectives[1] = edge;
        objectives[2] = con;

        ArrayList<SegmentationGenotype> finalPop = strengthParetoEvolutionaryAlgorithm2(population, img, population.size(), population.size() / 2, numGen, mutRate, objectives);
        //DRAWING
        try {
            AnalysisLauncher.open(new Plotter(finalPop));
        } catch(Exception ex) {
            System.err.println("ERROR PLOTTING");
            ex.printStackTrace();
        }
        //END
    }

    public static ArrayList<int[]> createPopulation(int[] dimst, populationGenerationType type, int genome_size, int pop_sz, BufferedReader br, Random randomizer) {
        if(type == populationGenerationType.SPECIFIED_RANDOM) {
            System.out.println("Enter segmentation focus. Comma separated values.");
            String input;
            try {
                input = br.readLine();
            } catch(IOException ex) {
                System.err.println("Problem reading line during pop creation. Returning uniform random.");
                return generatePopulationUniformRandom(dimst, pop_sz, randomizer);
            }
            String[] vals = input.split("(,| )+");
            int[] ids = new int[vals.length];
            for(int i = 0; i < vals.length; ++i) {
                int id = Integer.parseInt(vals[i]);
                ids[i] = id;
            }

            return generatePopulationSpecRandom(dimst, ids, pop_sz, randomizer);
        }

        if(type == populationGenerationType.UNIFORM_RANDOM) {
            return generatePopulationUniformRandom(dimst, pop_sz, randomizer);
        }

        System.err.println("Incorrect type given. Returning empty population.");
        return new ArrayList<>();
    }

    public static ArrayList<int[]> generatePopulationUniformRandom(int[] dimst, int pop_size, Random randomizer) {
        ArrayList<int[]> population = new ArrayList<>();

        for(int i = 0; i < pop_size; ++i) {
            int[] genotype = generateIndividualFromDiMST(dimst, i, randomizer);
            population.add(genotype);
        }

        return population;
    }

    public static ArrayList<int[]> generatePopulationSpecRandom(int[] dimst, int[] focus_locations, int pop_sz, Random randomizer) {
        ArrayList<int[]> population = new ArrayList<>();

        int cuts_per_focus = pop_sz / focus_locations.length;
        for(int i = 0; i < focus_locations.length; ++i) {
            int start = focus_locations[i] - cuts_per_focus/2;
            int end = focus_locations[i] + cuts_per_focus/2;
            while(start < 0) {
                ++start;
                ++end;
            }

            while(start < end) {
                int[] genotype = generateIndividualFromDiMST(dimst, start, randomizer);
                population.add(genotype);
                ++start;
            }
        }

        for(int sz = population.size(); sz < pop_sz; ++sz) {
            int r = (int)(randomizer.nextDouble()*(double)pop_sz);
            int[] genotype = generateIndividualFromDiMST(dimst, r, randomizer);
            population.add(genotype);
        }

        if(population.size() < pop_sz) System.err.println("Generated pop too small!");

        return population;

    }

    public static int[] generateIndividualFromDiMST(int[] dimst, int cuts, Random randomizer) {
        int[] genotype = new int[dimst.length];
        System.arraycopy(dimst, 0, genotype, 0, dimst.length);
        for(int j = 0; j < cuts; ++j) {
            int id = (int)(randomizer.nextDouble()*(double)dimst.length);

            while(genotype[id] == id) id = (int)(randomizer.nextDouble()*(double)dimst.length);

            genotype[id] = id;
        }
        return genotype;
    }

    public static int[] crossover(int[] p1, int[] p2, double rate) {
        Random randomizer = new Random();
        int[] child = new int[p1.length];
        if (randomizer.nextDouble() >= rate) {
            for (int i = 0; i < p1.length; i++) {
                child[i] = p1[i];
            }
            return child;
        }
        for (int i = 0; i < p1.length; i++) {
            if (randomizer.nextDouble() >= 0.5) {
                child[i] = p1[i];
            }
            else {
                child[i] = p2[i];
            }
        }
        return child;
    }

    public static ArrayList<SegmentationGenotype> reproduce(ArrayList<SegmentationGenotype> selected, int popSize, double pCross, BufferedImage img, boolean[] objectives, double mutRate) {
        ArrayList<SegmentationGenotype> children = new ArrayList<>();
        for (int i = 0; i < selected.size(); i++) {
            SegmentationGenotype p1 = selected.get(i);
            SegmentationGenotype p2;
            if (i == selected.size()-1) {
                p2 = selected.get(0);
            }
            else if (i % 2 == 0) {
                p2 = selected.get(i+1);
            }
            else {
                p2 = selected.get(i-1);
            }
            int[] cGenome = crossover(p1.getGenome(), p2.getGenome(), pCross);
            cGenome = switchMutation(cGenome, new Random(), img.getWidth(), img.getHeight(), mutRate);
            SegmentationGenotype child = new SegmentationGenotype(img, cGenome, objectives);
            children.add(child);
            if (children.size() >= popSize) {
                break;
            }
        }
        return children;
    }

    public static void calculateDominated(ArrayList<SegmentationGenotype> population) {
        for (SegmentationGenotype p: population) {
            ArrayList<SegmentationGenotype> domSet = new ArrayList<>();
            for (SegmentationGenotype q: population) {
                if (p != q && p.dominates(q)) {
                    domSet.add(q);
                }
            }
            p.setDomSet(domSet);
        }
    }

    public static int calculateRawFitness(SegmentationGenotype p, ArrayList<SegmentationGenotype> population) {
        int sum = 0;
        for (SegmentationGenotype q: population) {
            if (q != p && q.dominates(p)) {
                sum += q.getDomSet().size();
            }
        }
        return sum;
    }

    public static double calculateDensity(SegmentationGenotype p, ArrayList<SegmentationGenotype> population) {
        for (SegmentationGenotype q: population) {
            q.setDistance(p);
        }
        ArrayList<SegmentationGenotype> list = (ArrayList<SegmentationGenotype>) population.clone();
        list.sort((p1, p2) -> Double.compare(p1.getDistance(), p2.getDistance()));
        int k = (int) Math.sqrt(population.size());
        return 1.0/(list.get(k).getDistance() + 2.0);
    }

    public static void calculateFitness(ArrayList<SegmentationGenotype> population, ArrayList<SegmentationGenotype> archive) {
        ArrayList<SegmentationGenotype> union = new ArrayList<>();
        union.addAll(population);
        union.addAll(archive);
        calculateDominated(union);
        for (SegmentationGenotype p: union) {
            p.setRawFitness(calculateRawFitness(p, union));
            p.setDensity(calculateDensity(p, union));
            p.setFitness(p.getRawFitness() + p.getDensity());
        }
    }

    public static ArrayList<SegmentationGenotype> environmentalSelection(ArrayList<SegmentationGenotype> population, ArrayList<SegmentationGenotype> archive, int archiveSize) {
        ArrayList<SegmentationGenotype> union = new ArrayList<>();
        union.addAll(population);
        union.addAll(archive);

        ArrayList<SegmentationGenotype> environment = new ArrayList<>();
        for (SegmentationGenotype p: union) {
            if (p.getRawFitness() == 0) {
                environment.add(p);
            }
        }
        if (environment.size() < archiveSize) {
            union.sort((p1, p2) -> Double.compare(p1.getFitness(), p2.getFitness()));
            for (SegmentationGenotype p: union) {
                if (p.getRawFitness() != 0) {
                    environment.add(p);
                }
                if (environment.size() >= archiveSize) {
                    break;
                }
            }
        }
        else if (environment.size() > archiveSize) {
            do {
                int k = (int) Math.sqrt(environment.size());
                for (SegmentationGenotype p1: environment) {
                    for (SegmentationGenotype p2: environment) {
                        p2.setDistance(p1);
                    }
                    ArrayList<SegmentationGenotype> list = (ArrayList<SegmentationGenotype>) environment.clone();
                    list.sort((g1, g2) -> Double.compare(g1.getDistance(), g2.getDistance()));
                    p1.setDensity(list.get(k).getDistance());
                }
                environment.sort((g1, g2) -> Double.compare(g1.getDensity(), g2.getDensity()));
                environment.remove(0);
            } while (environment.size() > archiveSize);
        }
        return environment;
    }

    public static ArrayList<SegmentationGenotype> binaryTournament(ArrayList<SegmentationGenotype> population, int popSize) {
        Random randomizer = new Random();
        ArrayList<SegmentationGenotype> selection = new ArrayList<>();
        while (selection.size() < popSize) {
            int i = randomizer.nextInt(population.size());
            int j = i;
            while (j == i) {
                j = randomizer.nextInt(population.size());
            }
            if (population.get(i).getFitness() < population.get(j).getFitness()) {
                selection.add(population.get(i));
            }
            else {
                selection.add(population.get(j));
            }
        }
        return selection;
    }

    public static ArrayList<SegmentationGenotype> strengthParetoEvolutionaryAlgorithm2(ArrayList<int[]> initialPop, BufferedImage img, int popSize, int archiveSize, int maxGen, int mutRate, boolean[] objectives) {
        System.out.println("Evolving");
        int gen = 0;
        ArrayList<SegmentationGenotype> population = new ArrayList<>();
        ArrayList<SegmentationGenotype> archive = new ArrayList<>();
        //Initialize population
        for (int[] genome: initialPop) {
            population.add(new SegmentationGenotype(img, genome, objectives));
        }

        do {
            System.out.print("\rGeneration ");
            System.out.print(gen);
            calculateFitness(population, archive);
            archive = environmentalSelection(population, archive, archiveSize);
            if (gen >= maxGen) {
                System.out.println();
                System.out.println("-------------");
                break;
            }
            ArrayList<SegmentationGenotype> selected = binaryTournament(archive, popSize);
            double pCross = 1.0;
            population = reproduce(selected, popSize, pCross, img, objectives, mutRate);
            gen++;
        } while (true);
        return archive;
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
        System.out.println("Initializing population");
        while(mst.size() < sz-1 && !edges.isEmpty()) {
            if(count % 1000 == 0) {
                System.out.print("\r");
                System.out.print(((double)count/sz)*100);
                System.out.print("%");
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
        System.out.print("\r100%");
        System.out.println();
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

    public static int[] switchMutation(int[] genotype_in, Random randomizer, int x_sz, int y_sz, double rate) {
        double mutation_rate = rate/(x_sz*y_sz);
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
