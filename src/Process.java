import javax.swing.JProgressBar;

class Process {
    String name;
    int burstTime;
    int waitingTime;
    int turnaroundTime;
    int remainingTime;
    JProgressBar progressBar;

    public Process(String name, int burstTime) {
        this.name = name;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
        this.progressBar = new JProgressBar(0, burstTime);
        this.progressBar.setStringPainted(true);
    }
}