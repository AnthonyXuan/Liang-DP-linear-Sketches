package com.neurips;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        CountMedianSketch a = new CountMedianSketch(1.1,1.0);
        CountMinSketch b = new CountMinSketch(1.1, 1.0);
        DCS c = new DCS(10, 1.1);
        System.out.println( "Hello World!" );
    }
}
