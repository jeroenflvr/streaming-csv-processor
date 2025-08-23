package jeroenfl.csvprocessor.config;

/**
 * Application configuration holder containing all topic names and application settings.
 */
public class ApplicationConfig {
    private final String inputTopic;
    private final String outputTopic;
    private final String updateTopic;
    private final String stateTopic;
    private final String applicationId;
    private final boolean emitSnapshotOnBootstrap;

    public ApplicationConfig(String inputTopic, String outputTopic, String updateTopic, 
                           String stateTopic, String applicationId, boolean emitSnapshotOnBootstrap) {
        this.inputTopic = inputTopic;
        this.outputTopic = outputTopic;
        this.updateTopic = updateTopic;
        this.stateTopic = stateTopic;
        this.applicationId = applicationId;
        this.emitSnapshotOnBootstrap = emitSnapshotOnBootstrap;
    }

    public static ApplicationConfig fromEnvironment() {
        return new ApplicationConfig(
            EnvironmentUtils.envOrProp("INPUT_TOPIC", "local-input-topic"),
            EnvironmentUtils.envOrProp("OUTPUT_TOPIC", "local-stream-topic"),
            EnvironmentUtils.envOrProp("UPDATE_TOPIC", "local-updates-only-topic"),
            EnvironmentUtils.envOrProp("STATE_TOPIC", "local-state-topic"),
            EnvironmentUtils.envOrProp("APP_ID", "cos-csv-expander-app"),
            Boolean.parseBoolean("true")
        );
    }

    public String getInputTopic() { return inputTopic; }
    public String getOutputTopic() { return outputTopic; }
    public String getUpdateTopic() { return updateTopic; }
    public String getStateTopic() { return stateTopic; }
    public String getApplicationId() { return applicationId; }
    public boolean isEmitSnapshotOnBootstrap() { return emitSnapshotOnBootstrap; }
}
