package edu.wcu.cs.agora.FriendFinder;

/**
 * Created by tyler on 11/16/14.
 */
public class Event
{
    /** The title of the image. */
    private String eventName;
    /** The date of the image. */
    private String eventDate;
    private String eventTime;
    private String eventDescription;

    /** Intializes fields. */
    public Event (String eventName, String eventDate, String eventTime, String eventDescription)
    {
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.eventDescription = eventDescription;
    }

    /**
     * Returns title.
     * @return title
     */
    public String getEventName ()
    {
        return eventName;
    }

    public String getEventDate()
    {
        return eventDate;
    }

    public String getEventTime()
    {
        return eventTime;
    }

    /**
     * Returns the date
     * @return date
     */
    public String getDescription ()
    {
        return eventDescription;
    }
}