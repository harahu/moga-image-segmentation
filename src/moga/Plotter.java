package moga;

import org.jzy3d.analysis.AbstractAnalysis;
import org.jzy3d.chart.Chart;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.Scatter;
import org.jzy3d.plot3d.rendering.canvas.Quality;

import java.util.ArrayList;

/**
 * Created by hoslo on 22/03/2017.
 */
public class Plotter extends AbstractAnalysis {

    private ArrayList<SegmentationGenotype> population;

    public Plotter(ArrayList<SegmentationGenotype> population) {
        this.population = population;
    }

    public void init() {
        int sz = population.size();

        float x, y, z, a;
        Coord3d[] points = new Coord3d[sz];
        org.jzy3d.colors.Color[] colors = new org.jzy3d.colors.Color[sz];

        int i = 0;
        for(SegmentationGenotype ind: population) {
            x = (float)ind.getPhenotype().getConn();
            y = (float)ind.getPhenotype().getDev();
            z = (float)ind.getPhenotype().getEdge();
            a = 0.75f;
            points[i] = new Coord3d(x,y,z);
            colors[i] = new org.jzy3d.colors.Color(0f,0f,0f,a);
            ++i;
        }

        Scatter scatter = new Scatter(points, colors);
        scatter.setWidth(10);
        chart = AWTChartComponentFactory.chart(Quality.Advanced, "newt");
        chart.getScene().add(scatter);
        chart.getAxeLayout().setXAxeLabel("Conn");
        chart.getAxeLayout().setYAxeLabel("Dev");
        chart.getAxeLayout().setZAxeLabel("Edge");
    }

    public String toString() {
        return "This is a custom plotter for SPEA";
    }
}
