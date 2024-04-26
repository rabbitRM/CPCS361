
public class Job {

    private int arrTime;
    private int jobNum;
    private int memoryUnit;
    private int deviceNum;
    private int burstTime;
    private int priority;
    private double startTime ;
    private double finishTime ;
    private double remainRT ;
    private int enterCount ;
    private int totalTimeInCPU;
    private double TAT ;
    private int status ;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Job(int arrTime, int jobNum) {
        this.arrTime = arrTime;
        this.jobNum = jobNum;
    }

    
    public int getTotalTimeInCPU() {
        return totalTimeInCPU;
    }

    public void setTotalTimeInCPU(int totalTimeInCPU) {
        this.totalTimeInCPU = totalTimeInCPU;
    }

    public int getEnterCount() {
        return enterCount;
    }

    public void setEnterCount(int enterCount) {
        this.enterCount = enterCount;
    }
    

    public double getRemainBT() {
        return remainRT;
    }

    public void setRemainBT(double remainTime) {
        this.remainRT = remainTime;
    }

    public Job() {
    }

    public Job(int arrTime) {
        this.arrTime = arrTime;
        jobNum= 0;
    }

    public Job(int arrTime, int jobNum, int memoryUnit, int deviceNum, int burstTime, int priority) {
        this.arrTime = arrTime;
        this.jobNum = jobNum;
        this.memoryUnit = memoryUnit;
        this.deviceNum = deviceNum;
        this.burstTime = burstTime;
        this.priority = priority;
        this.remainRT = burstTime ;
    }

    
    public int getArrTime() {
        return arrTime;
    }

    public void setArrTime(int arrTime) {
        this.arrTime = arrTime;
    }

    public int getJobNum() {
        return jobNum;
    }

    public void setJobNum(int jobNum) {
        this.jobNum = jobNum;
    }

    public int getMemoryUnit() {
        return memoryUnit;
    }

    public void setMemoryUnit(int memoryUnit) {
        this.memoryUnit = memoryUnit;
    }

    public int getDeviceNum() {
        return deviceNum;
    }

    public void setDeviceNum(int deviceNum) {
        this.deviceNum = deviceNum;
    }

    public int getBurstTime() {
        return burstTime;
    }

    public void setBurstTime(int burstTime) {
        this.burstTime = burstTime;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public double getStartTime() {
        return startTime;
    }

    public void setStartTime(double startTime) {
        this.startTime = startTime;
    }

    public double getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(double finishTime) {
        this.finishTime = finishTime;
    }

    public double getTAT() {
        return finishTime - arrTime;
    }

    public void setTAT(double TAT) {
        this.TAT = TAT;
    }
    
    
}
