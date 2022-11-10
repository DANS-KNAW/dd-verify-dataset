package nl.knaw.dans.verifydataset.core.config;

public class AuthorNameFormatOkConfig {
    String regexp;

    public String getRegexp() {
        return regexp;
    }

    public void setRegexp(String regexp) {
        this.regexp = regexp;
    }

    @Override
    public String toString() {
        return "AuthorNameFormatOkConfig{" +
            "regexp='" + regexp + '\'' +
            '}';
    }
}
