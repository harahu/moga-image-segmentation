package moga;

import java.util.ArrayList;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
	// write your code here
    }

    public static ArrayList<Integer> generateRandomGenome(int x_sz, int y_sz, Random randomizer) {
        int sz = x_sz*y_sz;
        ArrayList<Integer> genome = new ArrayList<>();
        for(int i = 0; i < sz; ++i) {
            float r = randomizer.nextFloat();

            int val;
            if(r <= 0.2) {
                val = i;
            } else if(r <= 0.4) {
                val = i-1;
            } else if(r <= 0.6) {
                val = i+1;
            } else if(r <= 0.8) {
                val = i-x_sz;
            } else {
                val = i+x_sz;
            }

            if(val < 0 || val >= sz) val = i;

            genome.add(val);
        }

        return genome;
    }
}
