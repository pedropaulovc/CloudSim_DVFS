/*************************************************************************
 *  Compilation:  javac DoublingRatio.java
 *  Execution:    java DoublingRatio
 *  Dependencies: ThreeSum.java Stopwatch.java StdRandom.java StdOut.java
 *
 *
 *  % java DoublingRatio
 *      250   0.0    2.7
 *      500   0.0    4.8
 *     1000   0.1    6.9
 *     2000   0.6    7.7
 *     4000   4.5    8.0
 *     8000  35.7    8.0
 *  ...
 *
 *************************************************************************/

public class DoublingRatio {

    // time ThreeSum.count() for N random 6-digit ints
    public static double timeTrial(int N) {
        int MAX = 1000000;
        int[] a = new int[N];
        for (int i = 0; i < N; i++) {
            a[i] = StdRandom.uniform(-MAX, MAX);
        }
        Stopwatch timer = new Stopwatch();
        int cnt = ThreeSum.count(a);
        return timer.elapsedTime();
    }


    public static void main(String[] args) { 
        double prev = timeTrial(125);
        for (int N = 250; true; N += N) {
            double time = timeTrial(N);
            StdOut.printf("%6d %7.1f %5.1f\n", N, time, time/prev);
            prev = time;
        } 
    } 
} 

