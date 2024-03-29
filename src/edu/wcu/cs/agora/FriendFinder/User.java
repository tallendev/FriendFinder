package edu.wcu.cs.agora.FriendFinder;

/**
 * @author Tyler Allen
 * @created 11/28/14
 * @version 3/24/2015
 */
public class User
{
    /**
     * User's email address. Unique Identifier.
     */
    private String  email;
    /**
     * User birth date.
     */
    private String  birthday;
    /**
     * User's gender.
     */
    private String  gender;
    /**
     * User's full name.
     */
    private String  name;
    /**
     * Determines if user is busy or not.
     */
    private boolean busy;

    /**
     * Initializes fields.
     */
    public User (String email, String birthday, String gender, String name, boolean busy)
    {
        this.email = email;
        this.birthday = birthday;
        this.gender = gender;
        this.name = name;
        this.busy = busy;
    }

    /**
     * Returns the user's birthday as a string.
     *
     * @return birthday
     */
    public String getBirthday ()
    {
        return birthday;
    }

    /**
     * Returns the user's gender as a string.
     *
     * @return gender
     */
    public String getGender ()
    {
        return gender;
    }

    /**
     * Returns the user's full name as a string.
     *
     * @return name
     */
    public String getName ()
    {
        return name;
    }

    /**
     * Return the user's email address as a string.
     *
     * @return email
     */
    public String getEmail ()
    {
        return email;
    }

    /**
     * Returns if the user is busy or not.
     *
     * @return busy
     */
    public boolean isBusy ()
    {
        return busy;
    }
}
