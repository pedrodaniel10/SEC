package pt.ulisboa.tecnico.sec;

import org.junit.Assert;
import org.junit.Test;
import pt.ulisboa.tecnico.sec.library.HdsProperties;
import pt.ulisboa.tecnico.sec.library.data.User;
import pt.ulisboa.tecnico.sec.library.exceptions.UserNotFoundException;

/**
 * Library tests
 */
public class LibraryTest {

    @Test
    public void checkUsers() throws UserNotFoundException {
        User alice = HdsProperties.getUser("alice");
        User bob = HdsProperties.getUser("bob");
        User charlie = HdsProperties.getUser("charlie");

        Assert.assertEquals(alice.getUserId(), 0);
        Assert.assertEquals(bob.getUserId(), 1);
        Assert.assertEquals(charlie.getUserId(), 2);

    }
}
