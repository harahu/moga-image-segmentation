package moga;

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
}
