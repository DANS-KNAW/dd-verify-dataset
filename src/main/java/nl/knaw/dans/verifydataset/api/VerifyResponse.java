package nl.knaw.dans.verifydataset.api;

import java.util.HashMap;
import java.util.List;

public class VerifyResponse {
    private HashMap<String, List<String>> errors;

    public VerifyResponse() {
    }

    public VerifyResponse(HashMap<String, List<String>> errors) {
        this.errors = errors;
    }

    public void setErrors(HashMap<String, List<String>> errors) {
        this.errors = errors;
    }

    public HashMap<String, List<String>> getErrors() {
        return errors;
    }
}
