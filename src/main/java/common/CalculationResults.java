package common;

public class CalculationResults {
    private double temperatures[][];
    private double densities[][];
    private double energies[][];

    private double ti;
    private double timeStep;
    private double nodeStep;

    public CalculationResults(double[][] temperatures, double[][] densities, double[][] energies, double ti, double timeStep, double nodeStep) {
        this.temperatures = temperatures;
        this.densities = densities;
        this.energies = energies;
        this.ti = ti;
        this.timeStep = timeStep;
        this.nodeStep = nodeStep;
    }

    public double[][] getTemperatures() {
        return temperatures;
    }

    public double[][] getDensities() {
        return densities;
    }

    public double[][] getEnergies() {
        return energies;
    }

    public double getTi() {
        return ti;
    }

    public double getTimeStep() {
        return timeStep;
    }

    public double getNodeStep() {
        return nodeStep;
    }

    public int getStepCount() {
        return temperatures.length;
    }

    public int getNodeCount() {
        return temperatures[0].length;
    }

    public double getLength() {
        return (getNodeCount() + 1) * nodeStep;
    }
}
