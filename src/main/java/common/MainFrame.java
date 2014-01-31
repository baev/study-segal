package common;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class MainFrame extends JFrame {
    private CalculationResults values;
    private GraphsPanel graphsPanel;
    private LabeledTextField stepCountField;
    private StatusBar statusBar;
    private JSlider slider;
    double ti = 650;
    double timeStep = 0.1;
    double nodeStep = 5e-5;
    int nodeCount = 500;


    private void smartCalculate(
            final double ti, final double timeStep, final int stepCount,
            final double nodeStep, final int nodeCount) {
        new Thread(new Runnable() {
            public void run() {
                values = null;
                graphsPanel.valuesUpdated();
                final ProgressMonitor progressMonitor = new ProgressMonitor(
                        MainFrame.this, "", "", 0, stepCount
                );
                values = Calculator.calculate(
                        ti, timeStep, stepCount, nodeStep, nodeCount,
                        new Calculator.ProgressListener() {
                            public void progessUpdated(int progress, String note) {
                                progressMonitor.setProgress(progress);
                                progressMonitor.setNote(note);
                            }
                        });
                progressMonitor.close();
                graphsPanel.valuesUpdated();
            }
        }).start();
    }

    private class GraphsPanel extends JPanel {
        private static final int MARGIN = 10;
        private static final int DOT_SIZE = 1;

        private final Color TEMPERATURE_COLOR = Color.BLUE;
        private final Color DENSITY_COLOR = Color.BLACK;
        private final Color ENERGY_COLOR = Color.RED;

        private int time = 0;

        private double maxTemperature = 0;
        private double maxDensity = 0;
        private double maxEnergy = 0;

        public GraphsPanel() {
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    if (values == null) {
                        return;
                    }

                    int px = e.getPoint().x;
                    int py = e.getPoint().y;

                    if (px < MARGIN || px > getSize().width - MARGIN
                            || py < MARGIN || py > getSize().height - MARGIN) {
                        statusBar.setText(" ");
                        return;
                    }

                    int w = getSize().width - 2 * MARGIN;
                    int h = getSize().height - 2 * MARGIN;

                    px = px - MARGIN;
                    py = h - py + MARGIN;

                    double timeInSec = time * values.getTimeStep();
                    double x = px * 1. / w * values.getLength();
                    double temperature = py * 1. / h * maxTemperature;
                    double density = py * 1. / h * maxDensity;
                    double energy = py * 1. / h * maxEnergy;

                    statusBar.setText(String.format("t = %.4f, x = %.4f, T = %.4f, a = %.4f, W = %.4f",
                            timeInSec, x, temperature, density, energy));
                }
            });
        }

        public void forceRedraw() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    GraphsPanel.this.invalidate();
                    GraphsPanel.this.repaint();
                }
            });
        }

        public void valuesUpdated() {
            if (values == null) {
                slider.setEnabled(false);
                forceRedraw();
                return;
            }
            setCurrentTime(0);

            slider.setMaximum(values.getStepCount() - 1);
            slider.setValue(0);
            slider.setPaintLabels(true);
            slider.setEnabled(true);

            maxTemperature = values.getTi();
            maxDensity = 0;
            maxEnergy = 0;

            double[][] temperatures = values.getTemperatures();
            double[][] densities = values.getDensities();
            double[][] energies = values.getEnergies();
            for (int time = 0; time < values.getStepCount(); time++) {
                for (int node = 0; node < values.getNodeCount(); node++) {
                    if (temperatures[time][node] > maxTemperature) {
                        maxTemperature = temperatures[time][node];
                    }
                    if (densities[time][node] > maxDensity) {
                        maxDensity = densities[time][node];
                    }
                    if (energies[time][node] > maxEnergy) {
                        maxEnergy = energies[time][node];
                    }
                }
            }

            forceRedraw();
        }

        public void setCurrentTime(int time) {
            this.time = time;
            forceRedraw();
            if (values == null) {
                return;
            }
            statusBar.setText(String.format("Time = %.4f ", time * values.getTimeStep()));
        }

        private void drawGraph(Graphics g, double[] values, double left, double right, double maxValue, Color color) {
            int m = values.length;

            int w = getSize().width - 2 * MARGIN;
            int h = getSize().height - 2 * MARGIN;

            double[] x = new double[m + 2];
            double[] y = new double[m + 2];

            x[0] = 0;
            y[0] = left / maxValue * h;
            x[m + 1] = w;
            y[m + 1] = right / maxValue * h;

            for (int i = 1; i <= m; i++) {
                x[i] = i * 1. / (m + 1) * w;
                y[i] = values[i - 1] / maxValue * h;
            }

            g.setColor(color);
            boolean dotsEnabled = w > DOT_SIZE * 5 * m;
            for (int i = 0; i < m + 2; i++) {
                x[i] = x[i] + MARGIN;
                y[i] = h - y[i] + MARGIN;
                if (dotsEnabled) {
                    g.fillRect((int) (x[i] - DOT_SIZE),
                            (int) (y[i] - DOT_SIZE),
                            DOT_SIZE * 2 + 1, DOT_SIZE * 2 + 1);
                }
                if (i > 0) {
                    g.drawLine((int) x[i - 1], (int) y[i - 1], (int) x[i], (int) y[i]);
                }
            }
        }

        public void paint(Graphics g) {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getSize().width, getSize().height);
            if (values == null) {
                return;
            }

            double[][] temperatures = values.getTemperatures();
            double[][] densities = values.getDensities();
            double[][] energies = values.getEnergies();
            double tLeft = values.getTi();
            double tRight = Const.T_0;
            double dLeft = 0;
            double dRight = densities[time][values.getNodeCount() - 1];
            double eLeft = Functions.W(dLeft, tLeft);
            double eRight = Functions.W(dRight, tRight);

            drawGraph(g, temperatures[time], tLeft, tRight,
                    maxTemperature, TEMPERATURE_COLOR);
            drawGraph(g, densities[time], dLeft, dRight,
                    maxDensity, DENSITY_COLOR);
            drawGraph(g, energies[time], eLeft, eRight, maxEnergy, ENERGY_COLOR);
        }
    }

    private class CountActionListener implements ActionListener {
        private void errorMsg(String msg) {
            JOptionPane.showMessageDialog(MainFrame.this, msg,
                    "error", JOptionPane.ERROR_MESSAGE);
        }

        public void actionPerformed(ActionEvent e) {
            int stepCount;
            try {
                stepCount = Integer.parseInt(stepCountField.getText());
            } catch (NumberFormatException ex) {
                errorMsg("error");
                return;
            }

            smartCalculate(ti, timeStep, stepCount, nodeStep, nodeCount);
        }
    }

    public MainFrame() {
        setTitle("Counter");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);

        JPanel controlsPanel = new JPanel();

        stepCountField = new LabeledTextField("Time steps", "1000");
        JButton countButton = new JButton("Count");
        countButton.addActionListener(new CountActionListener());

        controlsPanel.add(stepCountField);
        controlsPanel.add(countButton);

        getRootPane().setDefaultButton(countButton);

        add(controlsPanel, BorderLayout.NORTH);

        graphsPanel = new GraphsPanel();
        add(graphsPanel, BorderLayout.CENTER);

        statusBar = new StatusBar();
        add(statusBar, BorderLayout.SOUTH);

        slider = new JSlider(JSlider.VERTICAL, 0, 0, 0);
        slider.setEnabled(false);
        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                graphsPanel.setCurrentTime(slider.getValue());
            }
        });
        add(slider, BorderLayout.EAST);

        setSize(800, 600);
        setVisible(true);
    }

    public static void main(String[] args) {
        new MainFrame();
    }
}
