package moga;

import java.awt.image.BufferedImage;

public class SegmentationGenotype {

    private int[] genome;
    private SegmentationPhenotype phenotype;

    public SegmentationGenotype(BufferedImage img, int[] genotype) {
        genome = genotype;
        phenotype = new SegmentationPhenotype(img, genome);
    }

    public int[] getGenome() {
        return genome;
    }

    public SegmentationPhenotype getPhenotype() {
        return phenotype;
    }

    public boolean dominates(SegmentationGenotype q) {
        SegmentationPhenotype qPhen = q.getPhenotype();
        boolean dominates = false;
        if (phenotype.getConn() < qPhen.getConn()) {
            dominates = true;
        }
        else if (phenotype.getConn() > qPhen.getConn()) {
            return false;
        }
        if (phenotype.getEdge() < qPhen.getEdge()) {
            dominates = true;
        }
        else if (phenotype.getEdge() > qPhen.getEdge()) {
            return false;
        }
        if (phenotype.getDev() < qPhen.getDev()) {
            dominates = true;
        }
        else if (phenotype.getDev() > qPhen.getDev()) {
            return false;
        }
        return dominates;
    }
}
