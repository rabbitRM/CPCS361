
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;

public class DRRproject {

    private static int startTime;
    private static int memorySize;
    private static int serialDevices;
    private static int jobNum;
    private static int availableMemory;
    private static int availableDevices;
    public static int i;
    public static int e;
    public static int quantum;
    public static int SR;
    public static int AR;
    public static PrintWriter output;
    public static Queue<Job> submitQ = new LinkedList();

    // Creating a queue that sort jobs based on its Memory requirments
    public static PriorityQueue<Job> holdQ1 = new PriorityQueue<Job>(new Comparator<Job>() {
        @Override
        public int compare(Job job1, Job job2) {

            // If job1 memory smaller than job2 , make it in the front of the queue
            if (job1.getMemoryUnit() < job2.getMemoryUnit()) {
                return -1;

                // If job1 memory equal job2 , check there arraiving time FIFO   
            } else if (job1.getMemoryUnit() == job2.getMemoryUnit()) {

                // If job1 arraived before job2 , make it in the front of the queue
                if (job1.getArrTime() < job2.getArrTime()) {
                    return -1;

                    // If job1 arraived before job2 
                } else if (job1.getArrTime() == job2.getArrTime()) {
                    return 0;
                }

                // If job1 arraived after job2 , make it in the end of the queue
                return 1;

                // If job1 memory greater than job2 , make it in the end of the queue
            } else {
                return 1;
            }
        }
    });

    // Creating a queue that schedule jobs based on FIFO
    public static Queue<Job> holdQ2 = new LinkedList();
    public static Queue<Job> readyQ = new LinkedList();
    public static Job CPU_Job = new Job();
    public static Queue<Job> completeQ = new LinkedList();
    public static double systemTAT = 0;
    public static int currentTime;

    // list to save all the times in the display command
    public static LinkedList<Integer> SystemStateTimeList = new LinkedList<Integer>();

    public static void main(String[] args) throws FileNotFoundException {
        String command;

        // Create file object
        File inputFile = new File("input3.txt");

        // Check if the file exists ?
        if (!inputFile.exists()) {
            System.out.println("not exists");
            System.exit(0);
        }

        // Creat Scanner object to read from the file 
        Scanner input = new Scanner(inputFile);

        // Create PrintWriter object to write on the file 
        output = new PrintWriter("output13.txt");

        boolean printed = false;
        // Loop to go through the commands
        while (input.hasNext()) {
            command = input.next();

            // If it is System Configuration command
            if (command.equalsIgnoreCase("C")) {
                systemConfiguration(input);
                // If it is a Job Arrival command    
            } else if (command.equalsIgnoreCase("A")) {
                jobArrivel(input);
                // If it is a Displpay command    
            } else if (command.equalsIgnoreCase("D")) {

                int systemStateTime = input.nextInt();
                SystemStateTimeList.add(systemStateTime);

                // Add the display command to the submit queue with unique number 
                submitQ.add(new Job(systemStateTime, -1));

                // Flag to be sure of the execution of the system 
                boolean executed = false;
                
                // Check if it is the last display 
                if (systemStateTime >= 999999 || completeQ.size() == jobNum) {

                    //Start the execution of the system  
                    startExecution();
                    // Print the final state of the system 
                    finalState();
                   
                    executed = true;

                }

                if (!input.hasNext() && !executed) {
                    //Start the execution of the system  
                    startExecution();
                    // Print the final state of the system 
                    finalState();
                }
                
                if (executed) {
                    // Retuning the system to its initial state 
                    prepForNextConfig();
                }
            }
        }

        // Closing rhe input and the output objects
        input.close();
        output.flush();
        output.close();

        // END OF MAIN Program   
    }

    //-----------------------------------------------------------------------------
    // Method to configure the new system 
    public static void systemConfiguration(Scanner input) {

        // Zero jobs so far in the system
        jobNum = 0;

        // Read the system charactaristic 
        startTime = input.nextInt();
        memorySize = Integer.parseInt(input.next().substring(2));
        serialDevices = Integer.parseInt(input.next().substring(2));

        // Initialize the available material to the maximum 
        availableMemory = memorySize;
        availableDevices = serialDevices;
    }

    //-----------------------------------------------------------------------------
    // Method to deal with the arraived jobs
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
            job.setStatus("Submit Queue");
            jobNum++;
        }

    }

    //-----------------------------------------------------------------------------
    // Method to start the system execution 
    public static void startExecution() {

        // Get the fist job in the system
        Job j = submitQ.poll();

        //It sets the system time (currentTime) to the arrival time of the first job
        // and allocates memory and devices based on its requirements.
        // Allocate resources to the first job 
        availableMemory = availableMemory - j.getMemoryUnit();
        availableDevices = serialDevices - j.getDeviceNum();

        CPU_Job = j;

        // Set the current system time with the first job arrival time 
        currentTime = CPU_Job.getArrTime();

        // Set the quantum with the first job Burts time
        quantum = CPU_Job.getBurstTime();

        // execute the first job 
        ExecuteFirstJob();

        // Now we are ready to receive jobs
        // Make the first job enter the CPU directly without the need to go through the ready queue
        // Since we are sure the CPU is idle 
        i = 0;
        e = 0;

        // Execute jobs till they are all in the complete queue 
        while (completeQ.size() != jobNum) {

            // i -> Time on the next input command otherwise, i is infinity
            if (!submitQ.isEmpty()) {
                i = submitQ.peek().getArrTime();
            } else {
                i = 999999;
            }

            //e -> Time of the next internal event otherwise, e is infinity
            if (CPU_Job != null) {
                e = CPU_Job.getFinishTime();
            } else {
                e = 999999;
            }

            // Make the current time the minimum of the i and e 
            currentTime = Math.min(i, e);

            // Process the external event
            if (i < e) {
                externalEvent();

                // Process the internal event    
            } else if (i > e) {
                internalEvent();

                // Process the internal event before the external event     
            } else {
                internalEvent();
                externalEvent();
            }
        }
    }

    //-----------------------------------------------------------------------------
    // Method to execute the first job that enters the CPU  
    public static void ExecuteFirstJob() {

        // Sets its starting time and finishing time 
        CPU_Job.setStartTime(currentTime);
        CPU_Job.setFinishTime(currentTime + quantum);

    }

    // -----------------------------------------------------------------------------
    // Method to execute jobs
    public static void executeJob() {

        // Set the job its new starting and finishing times
        CPU_Job.setStartTime(currentTime);

        // Set its finishing time as the lowest between the reminder of its burts or the quantum 
        if (quantum < CPU_Job.getRemainBT()) {
            CPU_Job.setFinishTime(CPU_Job.getStartTime() + quantum);
        } else {
            CPU_Job.setFinishTime(CPU_Job.getStartTime() + CPU_Job.getRemainBT());
        }

    }

    // -----------------------------------------------------------------------------
    // Method to deal with the job inside the CPU
    public static void internalEvent() {

        // Calculate what has remained from Burts time of the job inside the CPU , and its accumulated time in it 
        CPU_Job.setAccumulatedTime(CPU_Job.getFinishTime() - CPU_Job.getStartTime());
        CPU_Job.setRemainBT(quantum);

        // If there job need the CPU more
        if (CPU_Job.getAccumulatedTime() != CPU_Job.getBurstTime()) {

            // Switch with another job to enter the CPU 
            SwitchJobs();

            // If the job doesnt need the CPU anymore    
        } else {

            // Release its resources and make it go to the complete queue
            TerminateJob();

            // Check if the there is any jobs in the ready queue
            if (!readyQ.isEmpty()) {

                // Get the first job from the ready queue , update the SR and AR
                CPU_Job = readyQ.poll();
                SRAR_Update();

                // Execute the job inside the CPU
                executeJob();

                // Sets its waiting time so far
                CPU_Job.setWaitingTime(CPU_Job.getStartTime() - CPU_Job.getArrTime());

            }
        }
    }

    // -----------------------------------------------------------------------------
    // Method to switch jobs from the CPU to the ready queue
    public static void SwitchJobs() {

        // If there are no other jobs waiting for the CPU
        if (readyQ.isEmpty()) {

            // Make the job stay in the CPU and execute it more 
            executeJob();
            SRAR_Update();

            // There are other jobs waiting for the CPU   
        } else {

            // Return the job in the CPU back to the ready queue 
            readyQ.add(CPU_Job);
            SRAR_Update();

            // Get the first job in the ready queue to be in the CPU
            CPU_Job = readyQ.poll();

            // Update the quantum as the lowest of the remaining burts time or AR
            if (CPU_Job.getRemainBT() < AR) {
                quantum = CPU_Job.getRemainBT();
            } else {
                quantum = AR;
            }

            // Execute the job inside the CPU
            executeJob();

            SRAR_Update();

        }

    }

    //-----------------------------------------------------------------------------
    // Method to deal with jobs in the submit queue 
    public static void externalEvent() {

        // If there is a jobs in the submit queue 
        if (submitQ.size() != 0) {

            // Get the firts job in the submit queue 
            Job j = submitQ.poll();

            // Check if the job number is for normal jobs or for the display jobs
            if (j.getJobNum() == -1) {

                // Check if the job is for current time display or not 
                if (j.getArrTime() < 999999) {

                    // Display the current state of the system 
                    specificState(j.getArrTime());
                }

                // If it is a normal job , check if the resources needed are met 
            } else if (j.getDeviceNum() <= availableDevices && j.getMemoryUnit() <= availableMemory) {

                // Add it to the ready queue , update the SA and AR and gives it the resources needed
                readyQ.add(j);
                SRAR_Update();
                availableDevices -= j.getDeviceNum();
                availableMemory -= j.getMemoryUnit();

                // If the available resources are not enough    
            } else {

                // Make it go to one of the hold queues based on its priority 
                if (j.getPriority() == 1) {
                    holdQ1.add(j);

                } else {
                    holdQ2.add(j);
                }
            }
        }

    }

    // ----------------------------------------------------------------------------------
    // Mehtod to terminate the job in the CPU -> not needed anymore
    public static void TerminateJob() {

        // Realease any allocated resources
        availableMemory = availableMemory + CPU_Job.getMemoryUnit();
        availableDevices = availableDevices + CPU_Job.getDeviceNum();

        // Add it to the complete queue and set its status
        completeQ.add(CPU_Job);

        // The CPU is idle now , no job in it 
        CPU_Job = null;

        // Check the hold queus to move jobs from them to the ready queue if possible
        int holdQ1_size = holdQ1.size();
        for (int i = 0; i < holdQ1_size; i++) {

            // Get the first job in the hold queue 1
            Job j = holdQ1.poll();

            // check if the resources needed are met  
            if (j.getDeviceNum() <= availableDevices && j.getMemoryUnit() <= availableMemory) {

                // Add it to the ready queue , update the SA and AR and gives it the resources needed
                readyQ.add(j);
                SRAR_Update();
                availableDevices -= j.getDeviceNum();
                availableMemory -= j.getMemoryUnit();

                // make it go back to the hold queue 1 
            } else {
                holdQ1.add(j);
            }
        }

        int holdQ2_size = holdQ2.size();
        for (int i = 0; i < holdQ2_size; i++) {

            // Get the first job in the hold queue 2
            Job j = holdQ2.poll();

            // check if the resources needed are met  
            if (j.getDeviceNum() <= availableDevices && j.getMemoryUnit() <= availableMemory) {

                // Add it to the ready queue , update the SA and AR and gives it the resources needed
                readyQ.add(j);
                SRAR_Update();
                availableDevices -= j.getDeviceNum();
                availableMemory -= j.getMemoryUnit();

                // make it go back to the hold queue 2    
            } else {
                holdQ2.add(j);
            }
        }

    }

    //-----------------------------------------------------------------------------
    // Method to update the SA and AR
    public static void SRAR_Update() {

        if (!readyQ.isEmpty()) {
            int totalBurstTime = 0;

            // Go through all the jobs in the ready queue and gets its remainder burst time
            for (Job job : readyQ) {
                totalBurstTime += job.getRemainBT();
            }

            // Calculate the new SA and AR
            SR = totalBurstTime;
            AR = (SR / readyQ.size());
        }
    }

    //-----------------------------------------------------------------------------
    // Sorting the display queue based on the job number for the output format 
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

    //-----------------------------------------------------------------------------
    // Method to display a specific state of the system 
    public static void specificState(int currentTime) {

        output.println("\n<< At time " + currentTime + ": \n"
                + "  Current Available Main Memory = " + availableMemory + "\n"
                + "  Current Devices               = " + availableDevices + " \n\n"
                + "  Completed jobs: \n"
                + "  ----------------\n"
                + "  Job ID   Burst Time  Arrival Time    Finish Time  Turnaround Time  Waiting Time\n"
                + "  =================================================================");

        // Sort the complete queue based on the job numbers
        sortCompleteQueue();

        // Go through the complete queue 
        for (Job job : completeQ) {

            // Check if the job number less than 10 for fromat purposes
            if (job.getJobNum() > 9) {
                output.print("   " + job.getJobNum());
            } else {
                output.printf("    " + job.getJobNum());
            }

            output.printf(" %10d%13d%15d%12d%15d\n", job.getBurstTime(), job.getArrTime(), job.getFinishTime(), job.getTAT(), job.getWaitingTime());
        }

        // Go through the hold queue 1
        output.print("\n\n  Hold Queue 1: \n  ----------------\n   ");
        if (!holdQ1.isEmpty()) {
            for (Job job : holdQ1) {
                output.print(job.getJobNum() + "     ");
            }
        } else {
            output.println();
        }

        // Go through the hold queue 2
        output.println("\n\n\n  Hold Queue 2: \n  ----------------\n   ");
        if (!holdQ2.isEmpty()) {
            for (Job job : holdQ2) {
                output.print(job.getJobNum() + "     ");
            }
        } else {
            output.println();
        }

        // Go through the ready queue
        output.print("\n\n  Ready Queue : \n  ----------------\n   ");
        if (!readyQ.isEmpty()) {
            for (Job job : readyQ) {
                output.print(job.getJobNum() + "     ");
            }
        } else {
            output.println();
        }

        // display the job inside the CPU
        output.println("\n\n\n  Process running on the CPU: \n"
                + "  ----------------------------\n"
                + "  Job ID   Run Time   Time Left");

        if (CPU_Job != null) {
            output.printf("   %d %10d %11d\n", CPU_Job.getJobNum(), CPU_Job.getBurstTime(), CPU_Job.getRemainBT());
        } else {
            output.println();
        }
        output.println();
    }

    //-----------------------------------------------------------------------------
    // Method to display the final state of the system 
    public static void finalState() {

        output.println("<< Final state of system:\n"
                + "  Current Available Main Memory = " + availableMemory + "\n"
                + "  Current Devices               = " + availableDevices + " \n\n"
                + "  Completed jobs: \n"
                + "  ----------------\n"
                + "  Job ID   Burst Time  Arrival Time    Finish Time  Turnaround Time  Waiting Time\n"
                + "  =================================================================");

        // Sort the complete queue based on the job numbers
        sortCompleteQueue();

        // Go through the complete queue 
        for (Job job : completeQ) {

            // Check if the job number less than 10 for fromat purposes
            if (job.getJobNum() > 9) {
                output.print("   " + job.getJobNum());
            } else {
                output.printf("    " + job.getJobNum());
            }

            output.printf(" %10d%13d%15d%12d%15d\n", job.getBurstTime(), job.getArrTime(), job.getFinishTime(), job.getTAT(), job.getWaitingTime());

            // Sum all the jobs TAT
            systemTAT += job.getTAT();
        }

        // Calculate and d isplay the system TAT
        output.printf("\n\n  System Turnaround Time =  %.3f\n\n\n*********************************************************************\n\n", (systemTAT / completeQ.size()));

    }

    //-----------------------------------------------------------------------------
    // Method to return the system to its initial state
    public static void prepForNextConfig() {

        // Cleare all the queues , variables = 0
        submitQ.clear();
        holdQ1.clear();
        holdQ2.clear();
        readyQ.clear();
        completeQ.clear();
        CPU_Job = null;
        startTime = 0;
        memorySize = 0;
        serialDevices = 0;
        jobNum = 0;
        availableMemory = 0;
        availableDevices = 0;
        SR = 0;
        AR = 0;
        systemTAT = 0;
        SystemStateTimeList.clear();
    }

}
