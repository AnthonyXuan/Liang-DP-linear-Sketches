package com.neurips;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

public class CountMinSketch {
    private int t;
    private int d;
    private float[][] C;
    private Double sigma;
    private Double E;

    public CountMinSketch(double gamma, double beta) {
        this(gamma, beta, null);
    }

    public CountMinSketch(double gamma, double beta, Double rho) {
        this.t = (int) Math.ceil(1.0 / gamma);
        this.d = (int) Math.ceil(Math.log(1.0 / beta));
        this.C = new float[this.d][this.t];
        if (rho == null) {
            for (float[] row : C) {
                Arrays.fill(row, 0.0f);
            }
        } else {
            this.sigma = Math.sqrt(Math.log(2.0 / beta) / rho);
            this.E = Math.sqrt(2.0 * Math.log(2.0 / beta) / rho) * Math.sqrt(Math.log(Math.log(2.0 / beta) * 4.0 / gamma) / beta);
            Random rand = new Random();
            for (float[] row : C) {
                for (int i = 0; i < row.length; i++) {
                    row[i] = (float) (rand.nextGaussian() * this.sigma + this.E);
                }
            }
        }
    }

    private Iterable<Integer> h(Object x) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(String.valueOf(x.hashCode()).getBytes());
            return () -> new java.util.Iterator<Integer>() {
                private int currentIndex = 0;

                @Override
                public boolean hasNext() {
                    return currentIndex < d;
                }

                @Override
                public Integer next() {
                    md5.update(String.valueOf(currentIndex).getBytes());
                    currentIndex++;
                    return new BigInteger(1, md5.digest()).mod(BigInteger.valueOf(t)).intValue();
                }
            };
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 not supported", e);
        }
    }

    public void update(Object x, float value) {
        int index = 0;
        for (Integer i : h(x)) {
            C[index][i] += value;
            index++;
        }
    }

    public float query(Object x) {
        float minValue = Float.MAX_VALUE;
        int index = 0;
        for (Integer i : h(x)) {
            minValue = Math.min(minValue, C[index][i]);
            index++;
        }
        return minValue;
    }
}