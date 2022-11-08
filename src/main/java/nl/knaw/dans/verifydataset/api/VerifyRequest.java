package nl.knaw.dans.verifydataset.api;

public class VerifyRequest {
    String datasetPid;

    public String getDatasetPid() {
        return datasetPid;
    }

    public void setDatasetPid(String datasetPid) {
        this.datasetPid = datasetPid;
    }

    @Override
    public String toString() {
        return "VerifyRequest{" +
            "datasetPid='" + datasetPid + '\'' +
            '}';
    }
}
