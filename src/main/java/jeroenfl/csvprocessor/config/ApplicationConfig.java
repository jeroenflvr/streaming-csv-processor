package jeroenfl.csvprocessor.config;

/**
 * Immutable configuration holder for the CSV processor application.
 * 
 * <p>This class encapsulates all application-specific configuration including
 * Kafka topic names, application identifiers, and processing behavior settings.
 * It provides a type-safe way to access configuration values throughout the application.
 * 
 * <p>Configuration values are typically loaded from environment variables
 * through the {@link #fromEnvironment()} factory method, which provides
 * sensible defaults for local development while allowing production customization.
 * 
 * @author JeroenFL
 * @version 1.0.0
 * @since 1.0.0
 * @see EnvironmentUtils
 */
public class ApplicationConfig {
    private final String inputTopic;
    private final String outputTopic;
    private final String updateTopic;
    private final String stateTopic;
    private final String applicationId;
    private final boolean emitSnapshotOnBootstrap;

    /**
     * Constructs a new ApplicationConfig with the specified parameters.
     * 
     * @param inputTopic the Kafka topic to read file paths from
     * @param outputTopic the Kafka topic to write all processed records to
     * @param updateTopic the Kafka topic to write only changed records to
     * @param stateTopic the Kafka topic used for state management
     * @param applicationId the unique identifier for this Kafka Streams application
     * @param emitSnapshotOnBootstrap whether to emit all records on application startup
     */
    public ApplicationConfig(String inputTopic, String outputTopic, String updateTopic, 
                           String stateTopic, String applicationId, boolean emitSnapshotOnBootstrap) {
        this.inputTopic = inputTopic;
        this.outputTopic = outputTopic;
        this.updateTopic = updateTopic;
        this.stateTopic = stateTopic;
        this.applicationId = applicationId;
        this.emitSnapshotOnBootstrap = emitSnapshotOnBootstrap;
    }

    /**
     * Creates an ApplicationConfig instance from environment variables.
     * 
     * <p>This factory method reads configuration from environment variables
     * and provides sensible defaults for local development. The following
     * environment variables are consulted:
     * 
     * <ul>
     *   <li>{@code INPUT_TOPIC} - defaults to "local-input-topic"</li>
     *   <li>{@code OUTPUT_TOPIC} - defaults to "local-stream-topic"</li>
     *   <li>{@code UPDATE_TOPIC} - defaults to "local-updates-only-topic"</li>
     *   <li>{@code STATE_TOPIC} - defaults to "local-state-topic"</li>
     *   <li>{@code APP_ID} - defaults to "cos-csv-expander-app"</li>
     * </ul>
     * 
     * @return a new ApplicationConfig instance with values from environment or defaults
     * @see EnvironmentUtils#envOrProp(String, String)
     */
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

    /**
     * Gets the Kafka topic name for reading input file paths.
     * 
     * @return the input topic name
     */
    public String getInputTopic() { return inputTopic; }
    
    /**
     * Gets the Kafka topic name for writing all processed records.
     * 
     * @return the output topic name
     */
    public String getOutputTopic() { return outputTopic; }
    
    /**
     * Gets the Kafka topic name for writing only changed/updated records.
     * 
     * @return the update topic name
     */
    public String getUpdateTopic() { return updateTopic; }
    
    /**
     * Gets the Kafka topic name used for state management and change detection.
     * 
     * @return the state topic name
     */
    public String getStateTopic() { return stateTopic; }
    
    /**
     * Gets the unique identifier for this Kafka Streams application.
     * 
     * @return the application ID
     */
    public String getApplicationId() { return applicationId; }
    
    /**
     * Determines whether to emit all records on application bootstrap.
     * 
     * @return true if snapshot emission is enabled on bootstrap
     */
    public boolean isEmitSnapshotOnBootstrap() { return emitSnapshotOnBootstrap; }
}
