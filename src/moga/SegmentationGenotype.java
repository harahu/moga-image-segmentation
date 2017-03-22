package moga;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class SegmentationGenotype {

    private int[] genome;
    private SegmentationPhenotype phenotype;
    private ArrayList<SegmentationGenotype> domSet;
    private boolean[] objectives;
    private double distance;
    private double rawFitness;
    private double density;
    private double fitness;

    public SegmentationGenotype(BufferedImage img, int[] genotype, boolean[] activeObjectives) {
        objectives = activeObjectives; //dev edge conn
        genome = genotype;
        phenotype = new SegmentationPhenotype(img, genome);
    }

    public int[] getGenome() {
        return genome;
    }

    public SegmentationPhenotype getPhenotype() {
        return phenotype;
    }

    public ArrayList<SegmentationGenotype> getDomSet() {
        return domSet;
    }

    public void setDomSet(ArrayList<SegmentationGenotype> domSet) {
        this.domSet = domSet;
    }

    public void setDistance(SegmentationGenotype p) {
        double dDev = 0.0;
        double dEdge = 0.0;
        double dConn = 0.0;

        if (objectives[0]) {
            dDev = phenotype.getDev() - p.getPhenotype().getDev();
        }
        if (objectives[1]) {
            dEdge = phenotype.getEdge() - p.getPhenotype().getEdge();
        }
        if (objectives[2]) {
            dConn = phenotype.getConn() - p.getPhenotype().getConn();
        }

        distance = Math.sqrt(Math.pow(dDev, 2.0) + Math.pow(dEdge, 2.0) + Math.pow(dConn, 2.0));
    }

    public double getDistance() {
        return distance;
    }

    public double getRawFitness() {
        return rawFitness;
    }

    public void setRawFitness(double rawFitness) {
        this.rawFitness = rawFitness;
    }

    public double getDensity() {
        return density;
    }

    public void setDensity(double density) {
        this.density = density;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public boolean dominates(SegmentationGenotype q) {
        SegmentationPhenotype qPhen = q.getPhenotype();

        if (objectives[0]) {
            if (phenotype.getDev() > qPhen.getDev()) {
                return false;
            }
        }

        if (objectives[1]) {
            if (phenotype.getEdge() > qPhen.getEdge()) {
                return false;
            }
        }

        if (objectives[2]) {
            if (phenotype.getConn() > qPhen.getConn()) {
                return false;
            }
        }
        return true;
    }
}
