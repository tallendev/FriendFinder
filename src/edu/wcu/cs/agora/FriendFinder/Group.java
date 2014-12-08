package edu.wcu.cs.agora.FriendFinder;

/**
 * @author Tyler Allen
 * @created 11/16/2014
 * @version 12/7/2014
 *
 * This class represents a Group. It holds a group's name and description. Originally created
 * to be a data holder object for a ListView.
 *
 */
public class Group
{
    /**
     * The title of the image.
     */
    private String group_name;
    /**
     * The date of the image.
     */
    private String description;

    /**
     * Initializes fields.
     */
    public Group (String group_name, String description)
    {
        this.group_name = group_name;
        this.description = description;
    }

    /**
     * Returns title.
     *
     * @return title
     */
    public String getGroupName ()
    {
        return group_name;
    }

    /**
     * Returns the date.
     *
     * @return date
     */
    public String getDescription ()
    {
        return description;
    }
}
