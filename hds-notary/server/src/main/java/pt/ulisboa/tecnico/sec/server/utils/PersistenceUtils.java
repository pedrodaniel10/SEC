package pt.ulisboa.tecnico.sec.server.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import pt.ulisboa.tecnico.sec.library.exceptions.ServerException;
import pt.ulisboa.tecnico.sec.server.services.HdsNotaryState;

/**
 * Class that handles the persistent data.
 */
public final class PersistenceUtils {

    private static final Logger logger = Logger.getLogger(PersistenceUtils.class);

    private static final String DATA_FILE = "dataFile.json";
    private static final String DATA_BACKUP_FILE = "dataBackupFile.json";
    private static final ClassLoader classLoader = PersistenceUtils.class.getClassLoader();

    private static File dataFile;
    private static File dataBackupFile;

    private static HdsNotaryState serverState;

    // Initialize variables
    static {
        try {
            dataFile = new File(IOUtils.resourceToURL(DATA_FILE, classLoader).toURI());
            dataBackupFile = new File(IOUtils.resourceToURL(DATA_BACKUP_FILE, classLoader).toURI());
        } catch (URISyntaxException | IOException e) {
            logger.error(e);
        }

        serverState = getServerState();
    }

    private PersistenceUtils() {
    }

    public static HdsNotaryState getServerState() {
        if (serverState != null) {
            return serverState;
        }

        ObjectMapper mapper = new ObjectMapper();

        try {
            serverState = mapper.readValue(dataFile, HdsNotaryState.class);
        } catch (IOException e) {
            // File might be corrupted, try to load backup file
            logger.error(e);
            try {
                serverState = mapper.readValue(dataBackupFile, HdsNotaryState.class);
            } catch (IOException ex) {
                // Something went terrible wrong.
                logger.error(ex);
                System.exit(1);
            }
        }
        return serverState;
    }

    public static synchronized void save() {
        ObjectMapper mapper = new ObjectMapper();

        try {
            String jsonInString = mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(serverState);

            FileUtils.writeStringToFile(dataFile, jsonInString, (String) null);
            FileUtils.writeStringToFile(dataBackupFile, jsonInString, (String) null);

        } catch (IOException e) {
            logger.error(e);
        }
    }

}
