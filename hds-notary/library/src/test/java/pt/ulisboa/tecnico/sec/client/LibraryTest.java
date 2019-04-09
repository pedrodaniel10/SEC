package pt.ulisboa.tecnico.sec.client;

import org.junit.Assert;
import org.junit.Test;
import pt.ulisboa.tecnico.sec.client.library.HdsProperties;
import pt.ulisboa.tecnico.sec.client.library.data.User;
import pt.ulisboa.tecnico.sec.client.library.exceptions.UserNotFoundException;

/**
 * Library tests
 */
public class LibraryTest {

    @Test
    public void checkUsers() throws UserNotFoundException {
        User alice = HdsProperties.getUser("alice");
        User bob = HdsProperties.getUser("bob");
        User charlie = HdsProperties.getUser("charlie");

        Assert.assertEquals("0", alice.getUserId());
        Assert.assertEquals("1", bob.getUserId());
        Assert.assertEquals("2", charlie.getUserId());

    }
}
