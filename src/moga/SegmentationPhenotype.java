package moga;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;

public class SegmentationPhenotype {

    private ArrayList<ArrayList<Integer>> segments;
    private BufferedImage image;
    private int[] segment_alloc;

    public SegmentationPhenotype(BufferedImage img, int[] genotype) {
        this.createSegments(genotype);
        image = img;
    }

    private void createSegments(int[] genotype) {
        int[] segment_allocations = new int[genotype.length];
        Arrays.fill(segment_allocations, -1);

        segments = new ArrayList<>();

        for(int i = 0; i < genotype.length; ++i) {
            if (segment_allocations[i] != -1) continue;

            ArrayList<Integer> current_segment = new ArrayList<>();
            int val = i;
            while (segment_allocations[val] == -1 && !current_segment.contains(val)) {
                current_segment.add(val);
                val = genotype[val];
            }

            if (segment_allocations[val] != -1) {
                int sz = current_segment.size();
                for (int j = 0; j < sz; ++j) {
                    segment_allocations[current_segment.get(j)] = segment_allocations[val];
                }
                segments.get(segment_allocations[val]).addAll(current_segment);
            } else {
                int sz = current_segment.size();
                int seg_id = segments.size();
                for (int j = 0; j < sz; ++j) {
                    segment_allocations[current_segment.get(j)] = seg_id;
                }
                segments.add(current_segment);
            }
        }
        segment_alloc = segment_allocations;
    }

    public boolean checkValidSegmentation() {
        for(int i = 0; i < segment_alloc.length; ++i) {
            if(segment_alloc[i] == -1) return false;
        }

        return true;
    }

    public int overallDeviation() {
        return 0;
    }

    private double dist(int p0, int p1) {
        Color c0 = new Color(image.getRGB(getX(p0), getY(p0)));
        Color c1 = new Color(image.getRGB(getX(p1), getY(p1)));
        int dR = c1.getRed()-c0.getRed();
        int dG = c1.getGreen()-c0.getGreen();
        int dB = c1.getBlue()-c0.getBlue();
        double dist = Math.sqrt(Math.pow(dR, 2.0) + Math.pow(dG, 2.0) + Math.pow(dB, 2.0));
        return dist;
    }

    private int getX(int pxNum) {
        return pxNum % image.getWidth();
    }

    private int getY(int pxNum) {
        return pxNum / image.getWidth();
    }
}
