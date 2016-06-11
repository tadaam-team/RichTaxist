package tt.richCabman.util;

/**
 * Created by TAU on 10.05.2016.
 */
public class SharedPrefEntry {
    private final String name;
    private final String password;

    public SharedPrefEntry(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }
}
