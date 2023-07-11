package com.neurips;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

public class DCS {
    private int totalsize;
    private int U;
    private double gamma;
    private int columns;
    private int rows;
    private int total_levels;
    private List<CountMinSketch> subdomains;

    public DCS(int universe, double gamma) {
        this(universe, gamma, null);
    }

    public DCS(int universe, double gamma, Double rho) {
        this.totalsize = 0;
        this.U = universe;
        this.gamma = gamma;

        this.columns = (int) Math.ceil((1.0 / this.gamma) * Math.sqrt(Math.log(this.U) * Math.log(Math.log(this.U) / this.gamma)));
        this.rows = (int) Math.ceil(Math.log(Math.log(this.U) / this.gamma));

        this.total_levels = (int) Math.ceil(Math.log10(universe) / Math.log10(2));
        this.subdomains = new ArrayList<>();
        for (int i = 0; i < this.total_levels; i++) {
            if (rho != null) {
                this.subdomains.add(new CountMinSketch(1.0 / this.columns, 1.0 / this.rows, 1.0 * rho / this.total_levels));
            } else {
                this.subdomains.add(new CountMinSketch(1.0 / this.columns, 1.0 / this.rows));
            }
            this.totalsize += this.rows * this.columns;
        }
    }

    public void update(int item, int weight) {
        for (int j = 0; j < this.total_levels; j++) {
            this.subdomains.get(j).update(item, weight);
            item = (int) Math.floor(item / 2);
        }
    }

    public double rank(int x) {
        double result = 0;
        for (int i = 0; i < this.total_levels; i++) {
            if (x % 2 == 1) {
                result += this.subdomains.get(i).query(x - 1);
            }
            x = (int) Math.floor(x / 2);
        }
        return result;
    }

    public double query(int x) {
        return this.rank(x);
    }

    public int memoryBudget() {
        return this.totalsize;
    }
}