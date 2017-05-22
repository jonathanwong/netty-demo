package model;

import java.io.Serializable;

/**
 * Created by jon on 5/20/17.
 */
public class Person implements Serializable {

    private static final long serialVersionUID = 1817579102001705452L;

    private String firstName;

    private String lastName;

    public Person(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
