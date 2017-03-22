package moga;

import net.miginfocom.swing.MigLayout;
import org.jzy3d.chart2d.Chart2d;
import org.jzy3d.maths.Coord2d;
import org.jzy3d.plot2d.primitives.ScatterSerie2d;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by hoslo on 22/03/2017.
 */
public class Plotter2d extends JFrame {
    private ArrayList<SegmentationGenotype> population;
    private FitnessCombo combo;
    private Chart2d chart;

    public Plotter2d(ArrayList<SegmentationGenotype> population, FitnessCombo combo) {
        this.population = population;
        this.combo = combo;
    }

    public void init() {
        int sz = population.size();

        double x, y;
        Coord2d[] points = new Coord2d[sz];
        org.jzy3d.colors.Color[] colors = new org.jzy3d.colors.Color[sz];

        int i = 0;
        for(SegmentationGenotype ind: population) {
            if(combo == FitnessCombo.CONNDEV) {
                x = ind.getPhenotype().getConn();
                y = ind.getPhenotype().getDev();
            } else if(combo == FitnessCombo.CONNEDGE) {
                x = ind.getPhenotype().getConn();
                y = ind.getPhenotype().getEdge();
            } else {
                x = ind.getPhenotype().getDev();
                y = ind.getPhenotype().getEdge();
            }

            points[i] = new Coord2d(x,y);
            ++i;
        }

        ScatterSerie2d scatter = new ScatterSerie2d("Pareto Front");
        scatter.setWidth(10);
        float a = 0.75f;
        scatter.setColor(new org.jzy3d.colors.Color(0f,0f,0f,a));
        scatter.add(Arrays.asList(points));
        chart = new Chart2d();
        chart.add(scatter.getDrawable());

        chart.getAxeLayout().setXAxeLabel("Conn");
        chart.getAxeLayout().setYAxeLabel("Edge");
        if(combo == FitnessCombo.CONNDEV) chart.getAxeLayout().setYAxeLabel("Dev");
        else if(combo == FitnessCombo.DEVEDGE) chart.getAxeLayout().setXAxeLabel("Dev");
    }

    public void draw() {
        String lines = "[300px]";
        String columns = "[500px,grow]";
        setLayout(new MigLayout("", columns, lines));

        JPanel chartPanel = new JPanel(new BorderLayout());
        Border b = BorderFactory.createLineBorder(java.awt.Color.black);
        chartPanel.setBorder(b);
        chartPanel.add((java.awt.Component) chart.getCanvas(), BorderLayout.CENTER);

        closeWindowListener();
        this.pack();
    }

    public void closeWindowListener() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Plotter2d.this.dispose();
            }
        });
    }

    public String toString() {
        return "This is a custom plotter for SPEA";
    }
}
