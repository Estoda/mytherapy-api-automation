package models;

public class BookingRequest
{
    private String slotId;

    public BookingRequest(String slotId)
    {
        this.slotId = slotId;
    }

    public String getSlotId()
    {
        return slotId;
    }
}