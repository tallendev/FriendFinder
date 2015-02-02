package edu.wcu.cs.agora.FriendFinder;

/**
 * @author Tyler Allen
 * @created 11/16/14
 * @version 12/7/2014
 *
 * A representation for an event. Created for use in a ListView.
 */
public class Event
{
    /**
     * The title of the event.
     */
    private String eventName;
    /**
     * The date of the event.
     */
    private String eventDate;
    /**
     * The time of the event.
     */
    private String eventTime;
    /**
     * A description of the event.
     */
    private String eventDescription;
    /**
     * The owner of the event.
     */
    private String creator;
    /**
     * The id of the event.
     */
    private String id;

    /**
     * Initializes fields.
     *
     * @param eventName The name of the event.
     * @param eventDate The date of the event.
     * @param eventTime The time of the event.
     * @param eventDescription The event's description as specified by the user.
     */
    public Event (String eventName, String eventDate, String eventTime, String eventDescription,
                  String creator, String id)
    {
        this.creator = creator;
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.eventDescription = eventDescription;
        this.id = id;
    }

    /**
     * Returns the name of the event.
     *
     * @return name
     */
    public String getEventName ()
    {
        return eventName;
    }

    /**
     * Returns the date of the event.
     *
     * @return date
     */
    public String getEventDate ()
    {
        return eventDate;
    }

    /**
     * Returns the time of the event.
     *
     * @return time
     */
    public String getEventTime ()
    {
        return eventTime;
    }

    /**
     * Returns the date
     *
     * @return date
     */
    public String getDescription ()
    {
        return eventDescription;
    }

    /**
     * Returns the owner of this event.
     */
    public String getCreator ()
    {
        return creator;
    }

    /**
     * Returns the id of this event.
     */
    public String getId ()
    {
        return id;
    }
}