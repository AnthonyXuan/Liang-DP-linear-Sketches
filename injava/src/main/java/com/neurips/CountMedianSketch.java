package com.neurips;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.rank.Median;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CountMedianSketch {
    private final int t;
    private final int d;
    private final List<Double[]> C;
    private final Double sigma;

    public CountMedianSketch(double gamma, double beta) {
        this(gamma, beta, null);
    }

    public CountMedianSketch(double gamma, double beta, Double rho) {
        this.t = (int) Math.ceil(1.0 / gamma);
        this.d = (int) Math.ceil(Math.log(1.0 / beta));
        this.C = new ArrayList<>();
        this.sigma = rho != null ? Math.sqrt(Math.log(2.0 / beta) / rho) : null;

        for (int i = 0; i < this.d; i++) {
            if (this.sigma != null) {
                NormalDistribution normalDistribution = new NormalDistribution(0, this.sigma);
                double[] noises = IntStream.range(0, this.t)
                        .mapToDouble(j -> normalDistribution.sample())
                        .mapToObj(sample -> (double) sample)
                        .collect(Collectors.toList())
                        .stream().mapToDouble(Double::doubleValue).toArray();
                Double[] boxedNoises = Arrays.stream(noises).boxed().toArray(Double[]::new);
                this.C.add(boxedNoises);
            } else {
                double[] table = new double[this.t];
                Double[] boxedTable = Arrays.stream(table).boxed().toArray(Double[]::new);
                this.C.add(boxedTable);
            }
        }
    }

    private List<Integer> h(Object x) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(String.valueOf(x.hashCode()).getBytes(StandardCharsets.UTF_8));
            return IntStream.range(0, this.d).map(i -> {
                md5.update(String.valueOf(i).getBytes(StandardCharsets.UTF_8));
                return new BigInteger(1, md5.digest()).mod(BigInteger.valueOf(this.t)).intValue();
            }).boxed().collect(Collectors.toList());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found");
        }
    }

    private List<Integer> g(Object x) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            sha.update(String.valueOf(x.hashCode()).getBytes(StandardCharsets.UTF_8));
            return IntStream.range(0, this.d).map(i -> {
                sha.update(String.valueOf(i).getBytes(StandardCharsets.UTF_8));
                int val = new BigInteger(1, sha.digest()).mod(BigInteger.valueOf(2)).intValue();
                return val == 0 ? -1 : 1;
            }).boxed().collect(Collectors.toList());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found");
        }
    }

    public void update(Object x, int value) {
        List<Integer> hValues = h(x);
        List<Integer> gValues = g(x);
        for (int i = 0; i < this.d; i++) {
            this.C.get(i)[hValues.get(i)] += gValues.get(i) * value;
        }
    }

    public double query(Object x) {
        List<Integer> hValues = h(x);
        List<Integer> gValues = g(x);
        Median median = new Median();
        double[] values = IntStream.range(0, this.d)
                .mapToDouble(i -> this.C.get(i)[hValues.get(i)] * gValues.get(i))
                .toArray();
        return median.evaluate(values);
    }
}