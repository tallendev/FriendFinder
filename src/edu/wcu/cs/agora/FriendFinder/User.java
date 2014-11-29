package edu.wcu.cs.agora.FriendFinder;

/**
 * Created by tyler on 11/28/14.
 */
public class User
{
    /** User birthdate. */
    private String birthday;
    private String gender;
    private String name;

    /** Intializes fields. */
    public User (String birthday, String gender, String name)
    {
        this.birthday = birthday;
        this.gender = gender;
        this.name = name;
    }

    /**
     * Returns title.
     * @return title
     */
    public String getBirthday ()
    {
        return birthday;
    }

    /**
     * Returns the date
     * @return date
     */
    public String getGender ()
    {
        return gender;
    }

    public String getName()
    {
        return name;
    }
}
