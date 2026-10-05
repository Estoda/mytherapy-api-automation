package models;

public class AvailabilityRequest 
{
    private String startTime;
    private String endTime;

    public AvailabilityRequest(String startTime, String endTime)
    {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getStartTime()
    {
        return startTime;
    }

    public String getEndTime()
    {
        return endTime;
    }
}
