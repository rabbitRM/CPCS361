
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;

// status 0 -> running , 1 -> in readyQ , 2 -> in holdQ , 3 -> finished (completeQ) 
public class Osproject {

    private static int startTime;
    private static int memorySize;
    private static int serialDevices;
    private static int jobNum;
    private static int availableMemory;
    private static int availableDevices;
    public static double i;
    public static double e;
    public static double quantum;
    public static double SR;
    public static double AR;
    public static PrintWriter output;

    public static Queue<Job> submitQ = new LinkedList();
    public static PriorityQueue<Job> HoldQ1 = new PriorityQueue<Job>(new Comparator<Job>() {
        @Override
        public int compare(Job w1, Job w2) {
            if (w1.getMemoryUnit() < w2.getMemoryUnit()) {
                return -1;
            } else if (w1.getMemoryUnit() == w2.getMemoryUnit()) {
                if (w1.getArrTime() < w2.getArrTime()) {
                    return -1;
                } else if (w1.getArrTime() > w2.getArrTime()) {
                    return 1;
                }
                return 0;

            } else {
                return 1;
            }
        }
    });
    public static Queue<Job> holdQ2 = new LinkedList();
    public static Queue<Job> ReadyQ = new LinkedList();
    public static Job CPU_Process = new Job();
    public static Queue<Job> completeQ = new LinkedList();

    public static double currentTime;

    public static void main(String[] args) throws FileNotFoundException {
        int systemStateTime;
        String command;
        //create file object
        File inputFile = new File("input1.txt");
        if (!inputFile.exists()) {
            System.out.println("not exists");
            System.exit(0);
        }

        // check if file exists or not .
        // read from input file , write in outputFile .  
        Scanner input = new Scanner(inputFile);
        output = new PrintWriter("outputFile.txt");

        while (input.hasNext()) {
            command = input.next();

            if (command.equalsIgnoreCase("C")) {
                systemConfiguration(input);
            } else if (command.equalsIgnoreCase("A")) {
                jobArrivel(input);
            } else if (command.equalsIgnoreCase("D")) {
                systemStateTime = input.nextInt();
                if (systemStateTime != 999999) {
                    //  it's not the final state of the system.
                    // The code adds a new process (job) to the queue (allJobsQ)
                    // representing the system state at that time.
                    Job job = new Job(systemStateTime, -1);
                    submitQ.add(job);
                } else {
                    // it indicates the final state of the system
                    // execution of jobs
                    Job j = submitQ.poll();

                    //It sets the system time (currentTime) to the arrival time of the first job
                    // and allocates memory and devices based on its requirements.
                    availableMemory = availableMemory - j.getMemoryUnit();
                    availableDevices = serialDevices - j.getDeviceNum();
                    currentTime = j.getArrTime();
                    quantum = j.getBurstTime();
                    CPU_Process = j;
//                    CPU_Process.setStartTime(currentTime);
//                    CPU_Process.setFinishTime(currentTime + quantum);
                    cpu_execution();
                    i = 0;
                    e = 0;

                    while (completeQ.size() != jobNum) {
                        if (submitQ.isEmpty()) {
                            i = 999999;
                        } else {
                            i = submitQ.peek().getArrTime();
                        }

                        if (CPU_Process == null) {
                            e = 999999;
                        } else {
                            e = CPU_Process.getFinishTime();
                        }
                        currentTime = Math.min(i, e);

                        if (i < e) {
                            externalEvent();
                        } else if (i > e) {
                            internalEvent();
                        } else {
                            internalEvent();
                            externalEvent();
                        }
                    }

                    finalState();
                    prepForNextConfig();
                    systemStateTime = 0;

                }

            }
        }

        input.close();
        // END OF MAIN :)---------------------------------------------------      

    }

    public static void prepForNextConfig() {
        submitQ.clear();
        HoldQ1.clear();
        holdQ2.clear();
        ReadyQ.clear();
        completeQ.clear();
        CPU_Process = null;
        startTime = 0;
        memorySize = 0;
        serialDevices = 0;
        jobNum = 0;
        availableMemory = 0;
        availableDevices = 0;
        SR = 0;
        AR = 0;
    }

    public static void systemConfiguration(Scanner input) {
        jobNum = 0;
        startTime = input.nextInt();
        memorySize = Integer.parseInt(input.next().substring(2));
        serialDevices = Integer.parseInt(input.next().substring(2));
        availableMemory = memorySize;
        availableDevices = serialDevices;
    }

    // TASK (0)
    public static void jobArrivel(Scanner input) {
        int numJ, arrJ, memoJ, devJ, burstJ, prioJ;
        arrJ = Integer.parseInt(input.next());
        numJ = Integer.parseInt(input.next().substring(2));
        memoJ = Integer.parseInt(input.next().substring(2));
        devJ = Integer.parseInt(input.next().substring(2));
        burstJ = Integer.parseInt(input.next().substring(2));
        prioJ = Integer.parseInt(input.next().substring(2));
        Job job = new Job(arrJ, numJ, memoJ, devJ, burstJ, prioJ);
        // If there is not enough total main memory or total number of 
        //devices in the system for the job, the job is rejected never gets to one of the Hold Queues
        if (memoJ <= memorySize && devJ <= serialDevices) {
            submitQ.add(job);
            jobNum++;
        }
    }

    // 
    public static void internalEvent() {
        if (CPU_Process.getRemainBT() > 0) 
            dynamicRoundRobin();
        else {
            TerminateJob();
            if (!ReadyQ.isEmpty()) {
                CPU_Process = ReadyQ.poll();
                if (AR < CPU_Process.getRemainBT()) {
                    quantum = AR;
                } else {
                    quantum = CPU_Process.getRemainBT();
                    cpu_execution();
                    SRAR_Update();
                }
            }
        }
    }

    public static void dynamicRoundRobin() {
        if (ReadyQ.isEmpty()) {
            quantum = CPU_Process.getRemainBT();
            cpu_execution();
            SRAR_Update();
        } else {
            ReadyQ.add(CPU_Process);
            SRAR_Update();
            CPU_Process = ReadyQ.poll();
            if (AR < CPU_Process.getRemainBT()) {
                quantum = AR;
            } else {
                quantum = CPU_Process.getRemainBT();
            }
            cpu_execution();
            SRAR_Update();

        }

    }

    public static void cpu_execution() {

        CPU_Process.setStartTime(currentTime);
        if (quantum < CPU_Process.getRemainBT()) {
            CPU_Process.setFinishTime(CPU_Process.getStartTime() + quantum);
            CPU_Process.setRemainBT(CPU_Process.getRemainBT() - quantum);
        } else {
            CPU_Process.setFinishTime(CPU_Process.getStartTime() + CPU_Process.getRemainBT());
            CPU_Process.setRemainBT(0);
        }

    }

    //If new Process arrives
    //P Enter ready queue
    //Update SR and AR
    //End If
    public static void externalEvent() {
        if (submitQ.size() != 0) {
            Job j = submitQ.poll();

            //case of "D" job
            if (j.getJobNum() == -1) {
                // d 22
                specificState(j.getArrTime());
                // . If there is enough main memory and devices for the job
                // process is put in the Ready Queue. 
            } else if (j.getDeviceNum() <= availableDevices && j.getMemoryUnit() <= availableMemory) {
                ReadyQ.add(j);
                SRAR_Update();
                availableDevices -= j.getDeviceNum();
                availableMemory -= j.getMemoryUnit();
            } else {
                if (j.getPriority() == 1) {
                    HoldQ1.add(j);
                } else {
                    holdQ2.add(j);
                }
            }
        }

    }

    public static void specificState(int currentTime) {
        System.out.println("<< At time " + currentTime + " :\n"
                + "  Current Available Main Memory = " + availableMemory + "\n"
                + "Current Devices               = " + availableDevices + "\n"
                + "  Completed jobs: \n  ----------------");
        sortCompleteQueue();
        System.out.println("  Job ID   Arrival Time    Finish Time  Turnaround Time \n"
                + "  =================================================================");
        while (!completeQ.isEmpty()) {
            Job j = completeQ.poll();
            System.out.println("   " + j.getJobNum() + "          " + j.getArrTime() + "                " + j.getFinishTime() + "              " + j.getTAT());

        }
    }

    public static void finalState() {

    }


    public static void SRAR_Update() {
        int totalBurstTime = 0;

        for (Job job : ReadyQ) {
            totalBurstTime += job.getRemainBT();
        }
        SR = totalBurstTime;
        AR = SR / ReadyQ.size();
    }

    // ----------------------------------------------------------------------------------
    //TERMINATING THE JOB IN THE CPU
    public static void TerminateJob() {

        // CALCULATION needed to know which way the process will go ( readyQ OR completeQ)
        // process has finished, not needed anymore 
        // RELEASES ANY MAIN MEMORY
        availableMemory = availableMemory + CPU_Process.getMemoryUnit();
        availableDevices = availableDevices + CPU_Process.getDeviceNum();

        // release resources and go to the completedQ
        CPU_Process.setStatus(3);
        completeQ.add(CPU_Process);
        CPU_Process = null;
        // process need the CPU again ! go back to the readyQ :)
//            else {
//                CPU_Process.setStatus(1);
//                ReadyQ.add(CPU_Process);
//                SRAR_Update();
//            }

        // LEAVE ONE OF THE HOLD QUEUES AND MOVE TO THE READY QUEUE.
        while (!HoldQ1.isEmpty()) {
            Job j = HoldQ1.poll();
            j.setStatus(1);
            ReadyQ.add(j);
            SRAR_Update();
        }

        while (!holdQ2.isEmpty()) {
            Job j = holdQ2.poll();
            j.setStatus(1);
            ReadyQ.add(j);
            SRAR_Update();
        }

        // INDICATING NO PROCESS IN THE CPU 
    }

    // SORTING THE COMPLETE QUEUE FOR THE OUPPUT FORMAT 
    public static void sortCompleteQueue() {

        // Creating a list from the queue
        List<Job> list = new ArrayList<>(completeQ);

        // Sort based on arrival time
        list.sort(Comparator.comparing(Job::getJobNum));

        // Clearing the original queue
        completeQ.clear();

        // Adding sorted processes back to the queue
        completeQ.addAll(list);
    }

}
