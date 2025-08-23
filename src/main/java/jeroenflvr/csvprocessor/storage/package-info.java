/**
 * Object storage access layer for S3 and Cloud Object Storage.
 * 
 * <p>This package provides a high-level abstraction for accessing files
 * stored in S3-compatible object storage systems. It handles authentication,
 * connection management, and provides convenient interfaces for file reading.
 * 
 * <p>Key classes:
 * <ul>
 *   <li>{@link jeroenflvr.csvprocessor.storage.S3FileReader} - Service for reading files from S3/COS</li>
 * </ul>
 * 
 * <p>Storage capabilities:
 * <ul>
 *   <li><strong>Multi-Provider Support:</strong> Works with AWS S3, IBM COS, and other S3-compatible services</li>
 *   <li><strong>Stream Processing:</strong> Efficient streaming access for large files</li>
 *   <li><strong>Error Handling:</strong> Comprehensive error handling for network and access issues</li>
 *   <li><strong>Resource Management:</strong> Proper cleanup of connections and streams</li>
 * </ul>
 * 
 * <p>The storage layer is designed to be provider-agnostic, allowing the
 * application to work with different S3-compatible storage services through
 * configuration alone.
 * 
 * @since 1.0.0
 */
package jeroenflvr.csvprocessor.storage;
