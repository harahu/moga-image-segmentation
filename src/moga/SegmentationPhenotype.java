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

    public void printSegments() {
        int sz = segments.size();
        for (int i = 0; i < sz; ++i) {
            System.out.println("Segment " + Integer.toString(i) + ":");
            System.out.println(segments.get(i).toString());
        }
    }
    public double dev() {
        double dev = 0;
        for (ArrayList<Integer> segment: segments) {
            Color centroidColor = getCentroidColor(segment);
            for (Integer pixel: segment) {
                Color pixelColor = getColor(pixel);
                dev += colorDist(pixelColor, centroidColor);
            }
        }
        return dev;
    }

    public double edge() {
        double edge = 0;
        for(int pixel = 0; pixel < segment_alloc.length; pixel++) {
            Color pixelColor = getColor(pixel);
            for (Integer neighbour: neighbourhood(pixel)) {
                if (neighbour == -1) {
                    continue;
                }
                // if not in same segment
                if (segment_alloc[pixel] != segment_alloc[neighbour]) {
                    Color neighbourColor = getColor(neighbour);
                    edge += colorDist(pixelColor, neighbourColor);
                }
            }
        }
        return -edge;
    }

    public double conn() {
        int conn = 0;
        for(int pixel = 0; pixel < segment_alloc.length; pixel++) {
            int j = 0;
            for (int neighbour: neighbourhood(pixel)) {
                if (neighbour == -1) {
                    continue;
                }
                // if not in same segment
                if (segment_alloc[pixel] != segment_alloc[neighbour]) {
                    j++;
                }
            }
            while (j >= 1) {
                conn += 1/j;
                j--;
            }
        }
        return conn;
    }

    private Integer[] neighbourhood(int pixel) {
        int w = image.getWidth();
        Integer[] nh = new Integer[4];
        nh[0] = pixel - w;
        nh[1] = pixel + 1;
        nh[2] = pixel + w;
        nh[3] = pixel - 1;
        //on top or bottom edge?
        if (pixel < w) {
            nh[0] = -1;
        }
        else if (pixel / w >= image.getHeight() - 1) {
            nh[2] = -1;
        }
        // on left or right edge
        if (pixel % w == 0) {
            nh[3] = -1;
        }
        else if (pixel % w == w - 1) {
            nh[1] = -1;
        }
        return nh;
    }

    private Color getCentroidColor (ArrayList<Integer> segment) {
        int r = 0;
        int g = 0;
        int b = 0;
        for (Integer pixel: segment) {
            Color c = getColor(pixel);
            r += c.getRed();
            g += c.getGreen();
            b += c.getBlue();
        }
        r /= segment.size();
        g /= segment.size();
        b /= segment.size();
        return new Color(r, g, b);
    }

    private double colorDist(Color c0, Color c1) {
        int dR = c1.getRed()-c0.getRed();
        int dG = c1.getGreen()-c0.getGreen();
        int dB = c1.getBlue()-c0.getBlue();
        double dist = Math.sqrt(Math.pow(dR, 2.0) + Math.pow(dG, 2.0) + Math.pow(dB, 2.0));
        return dist;
    }

    private Color getColor(int pixel) {
        return new Color(image.getRGB(getX(pixel), getY(pixel)));
    }

    private int getRGB(int pixel) {
        return image.getRGB(getX(pixel), getY(pixel));
    }

    private int getX(int pixel) {
        return pixel % image.getWidth();
    }

    private int getY(int pixel) {
        return pixel / image.getWidth();
    }
}
