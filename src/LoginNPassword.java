import java.io.Serializable;
import java.util.Objects;

public class LoginNPassword implements Serializable {
    private String login;
    private String password;

    public LoginNPassword(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public LoginNPassword() {
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoginNPassword that = (LoginNPassword) o;
        return login.equalsIgnoreCase(that.login) && password.equals(that.password);
    }


    @Override
    public int hashCode() {
        return Objects.hash(login, password);
    }
}
