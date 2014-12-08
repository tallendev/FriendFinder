package edu.wcu.cs.agora.FriendFinder;

/**
 * @author Tyler Allen
 * @created 11/28/2014
 * @version 12/7/2014
 *
 * This class represents a like. Originally created to act as a holder object for a ListView.
 */
public class Like
{
    /**
     * Contains the name of a Like.
     */
    private String like;

    /**
     * Initializes fields.
     *
     * @param like Name of the like.
     */
    public Like (String like)
    {
        this.like = like;
    }

    /**
     * Getter method for like.
     *
     * @return The name of the like.
     */
    public String getLike ()
    {
        return like;
    }
}
