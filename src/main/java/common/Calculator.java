package common;

import static common.Const.*;

public class Calculator {
    public static interface ProgressListener {
        void progessUpdated(int progress, String note);
    }

    public static class ConsoleProgressListener implements ProgressListener {
        public void progessUpdated(int progress, String note) {
            System.out.println(progress + " " + note);
        }
    }

    private static void secureProgressUpdate(ProgressListener listener, int progress,
                                             String note) {
        if (listener == null) {
            return;
        }
        listener.progessUpdated(progress, note);
    }

    public static CalculationResults calculate(double ti, double timeStep, int stepCount,
                                               double nodeStep, int nodeCount, ProgressListener listener) {
        int n = nodeCount;
        double T[][] = new double[stepCount][n];
        double a[][] = new double[stepCount][n];
        double W[][] = new double[stepCount][n];
        double dt = timeStep;
        double dx2 = nodeStep * nodeStep;
        double Q_C = Q / C;

        for (int i = 0; i < n; i++) {
            T[0][i] = T_0;
            a[0][i] = 1;
        }

        for (int time = 1; time < stepCount; time++) {
            secureProgressUpdate(listener, time, String.format(
                    "%d of %d done (%.1f%%)", time, stepCount, 100. * time / stepCount));
            double eq[][] = new double[2 * n][2 * n];
            double vector[] = new double[2 * n];

            double fW, fdW_da, fdW_dT;

            fW = Functions.W(a[time - 1][0], T[time - 1][0]);
            fdW_da = Functions.dW_da(a[time - 1][0], T[time - 1][0]);
            fdW_dT = Functions.dW_dT(a[time - 1][0], T[time - 1][0]);

            eq[0][0] = 1 / dt + 2 * D / dx2 - fdW_da;
            eq[0][2] = -D / dx2;
            eq[0][1] = -fdW_dT;
            vector[0] = a[time - 1][0] / dt + fW - fdW_da * a[time - 1][0] - fdW_dT * T[time - 1][0];

            eq[1][1] = 1 / dt + 2 * KAPPA / dx2 + Q_C * fdW_dT;
            eq[1][3] = -KAPPA / dx2;
            eq[1][0] = Q_C * fdW_da;
            vector[1] = ti * KAPPA / dx2 + T[time - 1][0] / dt +
                    Q_C * (-fW + fdW_da * a[time - 1][0] + fdW_dT * T[time - 1][0]);

            fW = Functions.W(a[time - 1][n - 1], T[time - 1][n - 1]);
            fdW_da = Functions.dW_da(a[time - 1][n - 1], T[time - 1][n - 1]);
            fdW_dT = Functions.dW_dT(a[time - 1][n - 1], T[time - 1][n - 1]);

            for (int x = 1; x < n - 1; x++) {
                fW = Functions.W(a[time - 1][x], T[time - 1][x]);
                fdW_da = Functions.dW_da(a[time - 1][x], T[time - 1][x]);
                fdW_dT = Functions.dW_dT(a[time - 1][x], T[time - 1][x]);

                eq[2 * x][2 * x - 2] = -D / dx2;
                eq[2 * x][2 * x] = 1 / dt + 2 * D / dx2 - fdW_da;
                eq[2 * x][2 * x + 2] = -D / dx2;
                eq[2 * x][2 * x + 1] = -fdW_dT;
                vector[2 * x] = a[time - 1][x] / dt + fW - fdW_da * a[time - 1][x] - fdW_dT * T[time - 1][x];

                eq[2 * x + 1][2 * x] = Q_C * fdW_da;
                eq[2 * x + 1][2 * x - 1] = -KAPPA / dx2;
                eq[2 * x + 1][2 * x + 1] = 1 / dt + 2 * KAPPA / dx2 + Q_C * fdW_dT;
                eq[2 * x + 1][2 * x + 3] = -KAPPA / dx2;
                vector[2 * x + 1] = T[time - 1][x] / dt +
                        Q_C * (-fW + fdW_da * a[time - 1][x] + fdW_dT * T[time - 1][x]);
            }

            eq[2 * n - 2][2 * n - 4] = -D / dx2;
            eq[2 * n - 2][2 * n - 2] = 1 / dt + D / dx2 - fdW_da;
            eq[2 * n - 2][2 * n - 1] = -fdW_dT;
            vector[2 * n - 2] = a[time - 1][n - 1] / dt +
                    fW - fdW_da * a[time - 1][n - 1] - fdW_dT * T[time - 1][n - 1];

            eq[2 * n - 1][2 * n - 3] = -KAPPA / dx2;
            eq[2 * n - 1][2 * n - 1] = 1 / dt + 2 * KAPPA / dx2 + Q_C * fdW_dT;
            eq[2 * n - 1][2 * n - 2] = Q_C * fdW_da;
            vector[2 * n - 1] = KAPPA * T_0 / dx2 + T[time - 1][n - 1] / dt +
                    Q_C * (-fW + fdW_da * a[time - 1][n - 1] + fdW_dT * T[time - 1][n - 1]);

            double result[] = SSolve.solve(eq, vector);
            for (int x = 0; x < n; x++) {
                a[time][x] = result[2 * x];
                T[time][x] = result[2 * x + 1];
            }
        }
        for (int i = 0; i < stepCount; i++) {
            for (int j = 0; j < n; j++) {
                W[i][j] = Math.abs(Functions.W(a[i][j], T[i][j]));
            }
        }
        return new CalculationResults(T, a, W, ti, timeStep, nodeStep);
    }

    public static CalculationResults calculate(double ti, double timeStep, int stepCount, double nodeStep, int nodeCount) {
        return calculate(ti, timeStep, stepCount, nodeStep, nodeCount, null);
    }
}
