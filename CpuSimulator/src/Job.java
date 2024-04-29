
public class Job {

    private int arrTime;
    private int jobNum;
    private int memoryUnit;
    private int deviceNum;
    private int burstTime;
    private int priority;
    private int startTime ;
    private int finishTime ;
    private int remainRT ;
    private int enterCount ;
    private int totalTimeInCPU;
    private int TAT ;
    private String status ;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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
    

    public int getRemainBT() {
        return remainRT;
    }

    public void setRemainBT(int remainTime) {
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

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(int finishTime) {
        this.finishTime = finishTime;
    }

    public int getTAT() {
        if(finishTime - arrTime < 0)
            return 0 ;
        
        return finishTime - arrTime;
    }

    public void setTAT(int TAT) {
        this.TAT = TAT;
    }
    
    
}
