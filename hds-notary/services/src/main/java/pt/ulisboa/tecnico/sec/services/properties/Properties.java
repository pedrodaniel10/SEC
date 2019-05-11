package pt.ulisboa.tecnico.sec.services.properties;

import java.io.Serializable;
import java.util.Map;

public class Properties implements Serializable {

    Map<String, UserProperties> users;
    Map<String, ServerProperties> servers;

    public Properties() {
    }

    public Map<String, UserProperties> getUsers() {
        return users;
    }

    public void setUsers(Map<String, UserProperties> users) {
        this.users = users;
    }

    public Map<String, ServerProperties> getServers() {
        return servers;
    }

    public void setServers(Map<String, ServerProperties> servers) {
        this.servers = servers;
    }
}
