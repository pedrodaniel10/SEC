package pt.ulisboa.tecnico.sec.library;

import java.io.IOException;
import java.util.Properties;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import pt.ulisboa.tecnico.sec.library.data.User;
import pt.ulisboa.tecnico.sec.library.exceptions.UserNotFoundException;

public final class HdsProperties {

    private static final Logger logger = Logger.getLogger(HdsProperties.class);
    private static final String PROPERTIES_USERS = "users.properties";
    private static final ClassLoader classLoader = HdsProperties.class.getClassLoader();
    private static final Properties properties;

    static {
        properties = new Properties();
        try {
            properties.load(IOUtils.resourceToURL(PROPERTIES_USERS, classLoader).openStream());
        } catch (IOException e) {
            logger.error(e);
        }
    }

    private HdsProperties() {
    }

    public static User getUser(String name) throws UserNotFoundException {
        String id = properties.getProperty(name);

        if (id == null) {
            throw new UserNotFoundException("User with name " + name + " was not found.");
        }

        return new User(name, id);
    }

    public static User getUserById(String userId) throws UserNotFoundException {
        String name = properties.getProperty(userId);

        if (name == null) {
            throw new UserNotFoundException("User with id " + userId + " was not found.");
        }

        return new User(name, userId);
    }

    public static int getServerPort() {
        int registryPort = NumberUtils.toInt(properties.getProperty("server.port"));

        if (registryPort == 0) {
            logger.error("Error getting server port from properties file.");
            System.exit(1);
        }

        return registryPort;
    }

    public static String getServerUri() {
        String host = properties.getProperty("server.host");
        int port = HdsProperties.getServerPort();
        return "//" + host + ":" + port + "/HdsNotaryService";
    }

    public static int getClientPort(String name) throws UserNotFoundException {
        getUser(name);
        int registryPort = NumberUtils.toInt(properties.getProperty(name + ".port"));

        if (registryPort == 0) {
            logger.error("Error getting server port from properties file.");
            System.exit(1);
        }
        return registryPort;
    }

    public static String getClientUri(String uuid) throws UserNotFoundException {
        User user = getUserById(uuid);
        String host = properties.getProperty(user.getName() + ".host");
        int port = HdsProperties.getClientPort(user.getName());
        return "//" + host + ":" + port + "/ClientService";
    }

    public static String getClientPrivateKey(String name) {
        return properties.getProperty(name + ".privKey");
    }

    public static String getServerPublicKey() {
        return properties.getProperty("server.publickey");
    }
}
