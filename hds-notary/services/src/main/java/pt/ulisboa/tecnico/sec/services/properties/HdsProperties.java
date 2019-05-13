package pt.ulisboa.tecnico.sec.services.properties;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map.Entry;
import java.util.Optional;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import pt.ulisboa.tecnico.sec.services.crypto.CryptoUtils;
import pt.ulisboa.tecnico.sec.services.data.User;
import pt.ulisboa.tecnico.sec.services.exceptions.UserNotFoundException;

public final class HdsProperties {

    private static final Logger logger = Logger.getLogger(HdsProperties.class);
    private static final String INFO_FILE = "info.json";
    private static final ClassLoader classLoader = HdsProperties.class.getClassLoader();
    private static Properties properties = null;

    static {
        try {
            File infoFile = new File(IOUtils.resourceToURL(INFO_FILE, classLoader).toURI());


            properties = new Gson().fromJson(new JsonReader(new FileReader(infoFile)), Properties.class);
        } catch (URISyntaxException | IOException e) {
            logger.error(e);
        }

    }

    private HdsProperties() {
    }

    public static User getUser(String name) throws UserNotFoundException {
        final Optional<Entry<String, UserProperties>> user = properties.getUsers().entrySet()
            .stream()
            .filter(entry -> StringUtils.equals(entry.getValue().getName(), name))
            .findFirst();

        if (!user.isPresent()) {
            throw new UserNotFoundException("User with name " + name + " was not found.");
        }

        return new User(user.get().getValue().getName(), user.get().getValue().getId());
    }

    public static int getServerPort(String id) {
        final ServerProperties serverProperties = properties.getServers().get(id);

        if (serverProperties == null) {
            logger.error("Error getting server port from properties file.");
            System.exit(1);
        }

        return serverProperties.getPort();
    }

    public static String getServerUri(String id) {
        final ServerProperties serverProperties = properties.getServers().get(id);
        int port = HdsProperties.getServerPort(id);
        String host = serverProperties.getHost();
        return "//" + host + ":" + port + "/HdsNotaryService";
    }

    public static int getClientPort(String name) {
        final UserProperties user = getUserByName(name);

        return user.getPort();
    }

    public static String getClientUri(String id) throws UserNotFoundException {
        final UserProperties userProperties = properties.getUsers().get(id);
        if (userProperties == null) {
            throw new UserNotFoundException("User with id " + id + " was not found.");
        }
        return "//" + userProperties.getHost() + ":" + userProperties.getPort() + "/ClientService";
    }

    public static String getClientBonarUri(String id) throws UserNotFoundException {
        final UserProperties userProperties = properties.getUsers().get(id);
        if (userProperties == null) {
            throw new UserNotFoundException("User with id " + id + " was not found.");
        }
        return "//" + userProperties.getHost() + ":" + userProperties.getPort() + "/ReadBonarService";
    }

    public static RSAPrivateKey getClientPrivateKey(String name, String password) {
        final UserProperties user = getUserByName(name);
        return CryptoUtils.getPrivateKey(user.getPrivateKey(), password);
    }

    public static RSAPublicKey getClientPublicKey(String name) {
        final UserProperties user = getUserByName(name);
        return CryptoUtils.getPublicKey(user.getPublicKey());
    }

    public static RSAPrivateKey getServerPrivateKey(String id, String password) {
        final ServerProperties serverProperties = properties.getServers().get(id);
        return CryptoUtils.getPrivateKey(serverProperties.getPrivateKey(), password);
    }

    public static RSAPublicKey getServerPublicKey(String id) {
        final ServerProperties serverProperties = properties.getServers().get(id);
        return CryptoUtils.getPublicKey(serverProperties.getPublicKey());
    }

    public static RSAPrivateKey getNotarySignaturePrivateKey(String id, String password) {
        final ServerProperties serverProperties = properties.getServers().get(id);
        return CryptoUtils.getPrivateKey(serverProperties.getNotarySignaturePrivateKey(), password);
    }

    public static RSAPublicKey getNotarySignaturePublicKey(String id) {
        final ServerProperties serverProperties = properties.getServers().get(id);
        return CryptoUtils.getPublicKey(serverProperties.getNotarySignaturePublicKey());
    }

    private static UserProperties getUserByName(String name) {
        final Optional<Entry<String, UserProperties>> user = properties.getUsers().entrySet()
            .stream()
            .filter(entry -> StringUtils.equals(entry.getValue().getName(), name))
            .findFirst();

        if (!user.isPresent()) {
            logger.error("Error getting server port from properties file.");
            System.exit(1);
        }
        return user.get().getValue();
    }

}


