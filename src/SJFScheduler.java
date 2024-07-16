import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SJFScheduler extends JFrame {
    private List<Process> processes = new ArrayList<>();
    private JTextArea processInfoArea;
    private JPanel progressPanel;
    private JLabel avgWaitingTimeLabel;
    private JLabel avgTurnaroundTimeLabel;
    private JLabel totalExecutionTimeLabel;
    private JLabel currentProcessLabel;
    private int processCount = 0;
    private final int MAX_PROCESSES = 10;
    private Thread schedulerThread;
    private boolean running = true;

    public SJFScheduler() {
        setTitle("Shortest Job First Scheduling");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(1, 1));

        JButton addButton = new JButton("Add Process");
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addProcess();
            }
        });
        inputPanel.add(addButton);

        add(inputPanel, BorderLayout.NORTH);

        processInfoArea = new JTextArea();
        processInfoArea.setEditable(false);
        processInfoArea.setFont(new Font("Monospaced", Font.PLAIN, 16));
        add(new JScrollPane(processInfoArea), BorderLayout.CENTER);

        progressPanel = new JPanel();
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));
        add(new JScrollPane(progressPanel), BorderLayout.SOUTH);

        JPanel statsPanel = new JPanel(new GridLayout(4, 1));
        avgWaitingTimeLabel = new JLabel("Average Waiting Time: ");
        avgWaitingTimeLabel.setFont(new Font("Monospaced", Font.PLAIN, 16));
        avgTurnaroundTimeLabel = new JLabel("Average Turnaround Time: ");
        avgTurnaroundTimeLabel.setFont(new Font("Monospaced", Font.PLAIN, 16));
        totalExecutionTimeLabel = new JLabel("Total Execution Time: ");
        totalExecutionTimeLabel.setFont(new Font("Monospaced", Font.PLAIN, 16));
        currentProcessLabel = new JLabel("Current Process: None");
        currentProcessLabel.setFont(new Font("Monospaced", Font.PLAIN, 16));
        statsPanel.add(avgWaitingTimeLabel);
        statsPanel.add(avgTurnaroundTimeLabel);
        statsPanel.add(totalExecutionTimeLabel);
        statsPanel.add(currentProcessLabel);
        add(statsPanel, BorderLayout.WEST);

        JPanel controlPanel = new JPanel(new GridLayout(2, 1));
        JButton startButton = new JButton("Start Scheduling");
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startScheduling();
            }
        });
        controlPanel.add(startButton);

        JButton stopButton = new JButton("Stop Scheduling");
        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stopScheduling();
            }
        });
        controlPanel.add(stopButton);

        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                reset();
            }
        });
        controlPanel.add(resetButton);

        add(controlPanel, BorderLayout.EAST);
    }

    private void addProcess() {
        if (processCount < MAX_PROCESSES) {
            String name = JOptionPane.showInputDialog("Enter process name:");
            int burstTime = Integer.parseInt(JOptionPane.showInputDialog("Enter burst time:"));
            Process process = new Process(name, burstTime);
            processes.add(process);
            updateProcessInfo();
            progressPanel.add(new JLabel("Process " + name));
            progressPanel.add(process.progressBar);
            processCount++;
            revalidate();
            repaint();
        } else {
            JOptionPane.showMessageDialog(this, "Maximum number of processes reached.");
        }
    }

    private void updateProcessInfo() {
        StringBuilder info = new StringBuilder();
        for (Process p : processes) {
            info.append("Process ").append(p.name).append(": Burst Time = ").append(p.burstTime).append("\n");
        }
        processInfoArea.setText(info.toString());
    }

    private void startScheduling() {
        running = true;
        // Sort processes by burst time
        Collections.sort(processes, new Comparator<Process>() {
            public int compare(Process p1, Process p2) {
                return p1.burstTime - p2.burstTime;
            }
        });

        int currentTime = 0;
        int totalWaitingTime = 0;
        int totalTurnaroundTime = 0;

        for (Process p : processes) {
            p.waitingTime = currentTime;
            currentTime += p.burstTime;
            p.turnaroundTime = currentTime;
            totalWaitingTime += p.waitingTime;
            totalTurnaroundTime += p.turnaroundTime;
        }

        int avgWaitingTime = totalWaitingTime / processes.size();
        int avgTurnaroundTime = totalTurnaroundTime / processes.size();

        avgWaitingTimeLabel.setText("Average Waiting Time: " + avgWaitingTime);
        avgTurnaroundTimeLabel.setText("Average Turnaround Time: " + avgTurnaroundTime);
        totalExecutionTimeLabel.setText("Total Execution Time: " + currentTime);

        schedulerThread = new Thread(new Runnable() {
            public void run() {
                for (Process p : processes) {
                    if (!running) break;
                    highlightCurrentProcess(p.name, p.burstTime);
                    p.progressBar.setValue(0);
                    currentProcessLabel.setText("Current Process: " + p.name);
                    int remaining = p.burstTime;
                    while (remaining > 0) {
                        if (!running) break;
                        processInfoArea.append("Processing: " + p.name + "\n");
                        p.progressBar.setValue(p.burstTime - remaining + 1); 
                        try {
                            Thread.sleep(1000); 
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        remaining--;
                    }
                    p.progressBar.setValue(p.burstTime); 
                    processInfoArea.append("Completed: " + p.name + "\n");
                    currentProcessLabel.setText("Current Process: None");
                    try {
                        Thread.sleep(500); 
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                currentProcessLabel.setText("Current Process: None");
            }
        });
        schedulerThread.start();
    }

    private void stopScheduling() {
        running = false;
        if (schedulerThread != null) {
            schedulerThread.interrupt();
        }
    }

    private void reset() {
        running = false;
        if (schedulerThread != null) {
            schedulerThread.interrupt();
        }
        processes.clear();
        processCount = 0;
        progressPanel.removeAll();
        processInfoArea.setText("");
        avgWaitingTimeLabel.setText("Average Waiting Time: ");
        avgTurnaroundTimeLabel.setText("Average Turnaround Time: ");
        totalExecutionTimeLabel.setText("Total Execution Time: ");
        currentProcessLabel.setText("Current Process: None");
        revalidate();
        repaint();
    }

    private void highlightCurrentProcess(String processName, int burstTime) {
        processInfoArea.setText("");
        for (Process p : processes) {
            if (p.name.equals(processName)) {
                processInfoArea.append(">> Process " + p.name + ": Burst Time = " + p.burstTime + "\n");
            } else {
                processInfoArea.append("Process " + p.name + ": Burst Time = " + p.burstTime + "\n");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new SJFScheduler().setVisible(true);
            }
        });
    }
}