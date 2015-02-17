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
     * Owner of the group.
     */
    private String owner;
    /**
     * If user is a member of this group.
     */
    private boolean member;

    /**
     * Initializes fields.
     */
    public Group (String group_name, String description, String owner, boolean member)
    {
        this.group_name = group_name;
        this.description = description;
        this.owner = owner;
        this.member = member;
    }

    /**
     * Returns the name of this group.
     *
     * @return group_name
     */
    public String getGroupName ()
    {
        return group_name;
    }

    /**
     * Returns the description of this group.
     *
     * @return description
     */
    public String getDescription ()
    {
        return description;
    }

    /**
     * Returns the owner of this group.
     *
     * @return owner
     */
    public String getOwner ()
    {
        return owner;
    }

    /**
     * True if user is a member of this group
     *
     * @return member
     */
    public boolean isMember ()
    {
        return member;
    }
}
