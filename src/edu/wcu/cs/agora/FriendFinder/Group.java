package edu.wcu.cs.agora.FriendFinder;

/**
 * Created by tyler on 11/16/14.
 */
public class Group
{
    /** The title of the image. */
    private String group_name;
    /** The date of the image. */
    private String description;

    /** Intializes fields. */
    public Group (String group_name, String description)
    {
        this.group_name = group_name;
        this.description = description;
    }

    /**
     * Returns title.
     * @return title
     */
    public String getGroupName ()
    {
        return group_name;
    }

    /**
     * Returns the date
     * @return date
     */
    public String getDescription ()
    {
        return description;
    }
}
