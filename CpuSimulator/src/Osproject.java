
import java.io.*;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;

public class Osproject {

    private static int startTime;
    private static int memorySize;
    private static int serialDevices;
    private static int jobNum;
    private static int availableMemory;
    private static int availableDevices;
    public static int i;
    public static int e;
    public static int quantum;
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
                    int currentTime = j.getArrTime();
                    quantum = j.getBurstTime();
                    enterCPU(j, currentTime);
                    i = 0;
                    e = 0;
                    
                    while (completeQ.size() != jobNum){
                        if(submitQ.isEmpty()){
                            i = 999999;
                        }else {
                            i = submitQ.peek().getArrTime();
                        }
                        
                        if (CPU_Process == null){
                            e = 999999;
                        }else {
                            e = CPU_Process.getFinishTime();
                        }
                        currentTime = Math.min(i, e);
                        
                        if( i < e){
                            externalEvent();
                        }else if (i > e){
                            internalEvent();
                        }else {
                            internalEvent();
                            externalEvent();
                        }
                    }
                }
            }

        }
    }

     public static void systemConfiguration(Scanner input) {
        jobNum = 0;
        startTime = input.nextInt();
        memorySize = Integer.parseInt(input.next().substring(2));
        serialDevices = Integer.parseInt(input.next().substring(2));
        availableMemory = memorySize;
        availableDevices = serialDevices;
    }
     
     
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
        if (memoJ <= memorySize && devJ < serialDevices) {
            submitQ.add(job);
            jobNum++;
        }
    }

    public static void internalEvent(){
        
    }

    public static void externalEvent(){
        if (submitQ.size() != 0){
            Job j = submitQ.poll();
            
            //case of "D" job
            if (j.getJobNum() == -1 ){
                
            }
        }
    }
    
    
    public static void enterCPU(Job job, int currentTime) {
        CPU_Process = job;
        if (job.getEnterCount() == 0) {
            CPU_Process.setStartTime(currentTime);
        }

        CPU_Process.setRemainTime(CPU_Process.getBurstTime() - CPU_Process.getTotalTimeInCPU());
        if (CPU_Process.getRemainTime() < quantum) {
            CPU_Process.setFinishTime(currentTime + CPU_Process.getRemainTime());
        } else {
            CPU_Process.setFinishTime(currentTime + quantum);
        }

    }

    public static void exitCPU() {
        //check if there is process in the CPU
        if (CPU_Process != null) {
            // CALCULATION needed to know which way the process will go ( readyQ OR completeQ)
            // process has finished, not needed anymore 
            if (CPU_Process.getTotalTimeInCPU() == CPU_Process.getBurstTime()) {
                // release resources and go to the completedQ
                availableMemory = availableMemory + CPU_Process.getMemoryUnit();
                availableDevices = availableDevices + CPU_Process.getDeviceNum();
                completeQ.add(CPU_Process);
            } // process need the CPU again ! go back to the readyQ :)
            else {
                ReadyQ.add(CPU_Process);
            }
            CPU_Process = null;
        }
    }

    public static void SRAR_Update() {
        int totalBurstTime = 0;
        for (Job job : ReadyQ) {
            totalBurstTime += job.getBurstTime();
        }
        SR = totalBurstTime;
        AR = SR / ReadyQ.size();
    }

    public static void dynamicRoundRobin() {
//        while(!ReadyQ.isEmpty()){
//            if(!ReadyQ.isEmpty()){
//               ReadyQ.add(job)
//            }
//        }
        while (!ReadyQ.isEmpty()) {
            Job currentProcess = ReadyQ.poll();

            int TQ;
            if (ReadyQ.isEmpty()) {
                TQ = currentProcess.getBurstTime();
            } else {
                calculateAverage(totalBurstTime, readyQueue.size());
                TQ = AR;
            }

            if (currentProcess.getBurstTime() > timeQuantum) {
                currentProcess.setBurstTime(currentProcess.getBurstTime() - timeQuantum);
                readyQueue.offer(currentProcess);
                System.out.println("Returning process " + currentProcess.getName() + " to the ready queue");
            } else {
                System.out.println("Process " + currentProcess.getName() + " terminated");
            }

            updateSR(totalBurstTime, readyQueue);
        }
    }
}
