package common;

import static java.lang.Math.min;

public class SSolve {
    public static double[] solve(double[][] matr, double[] vect) {
        int n = vect.length;
        for (int i = 0; i < n - 1; i++) {
            double divider = matr[i][i];
            for (int j = i; j < min(n, i + 3); j++) {
                matr[i][j] /= divider;
            }
            vect[i] /= divider;

            for (int line = i + 1; line < min(n, i + 3); line++) {
                double multiplier = matr[line][i];
                for (int j = i; j < min(n, i + 3); j++) {
                    matr[line][j] -= multiplier * matr[i][j];
                }
                vect[line] -= vect[i] * multiplier;
            }
        }
        vect[n - 1] /= matr[n - 1][n - 1];
        matr[n - 1][n - 1] = 1;
        double[] result = new double[n];

        result[n - 1] = vect[n - 1];
        for (int i = n - 1; i >= 0; i--) {
            result[i] = vect[i];
            for (int j = i + 1; j < min(n, i + 3); j++) {
                result[i] -= result[j] * matr[i][j];
            }
        }
        return result;
    }
}
