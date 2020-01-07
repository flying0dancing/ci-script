package com.lombardrisk.ignis.izpack.core;

import lombok.experimental.UtilityClass;

@UtilityClass
public class InstallerProps {

    public static final String INSTALLER_ROOT_PATH_PROP = "INSTALLER_PATH";
    public static final String DIST_PROP_PREFIX = "izpack.dist.";

    public static final String SERVER_HTTP_PORT_PROP = "server.http.port";
    public static final String SERVER_HTTPS_PORT_PROP = "server.https.port";
    public static final String SERVER_CONTEXT_PATH_PROP = "server.servlet.contextPath";

    public static final String DATASET_SOURCE_S3_BUCKET_PROP = "dataset.source.location.s3.bucket";
    public static final String DATASET_SOURCE_S3_PREFIX_PROP = "dataset.source.location.s3.prefix";
    public static final String DATASET_ERROR_S3_BUCKET_PROP = "dataset.error.location.s3.bucket";
    public static final String DATASET_ERROR_S3_PREFIX_PROP = "dataset.error.location.s3.prefix";

    public static final String S3_PROTOCOL_PROP = "s3.protocol";

    public static final String DATASET_SOURCE_LOCAL_PATH_PROP = "dataset.source.location.localPath";

    public static final String PHOENIX_QUERY_HOST_PROP = "phoenix.query.server.host";
    public static final String PHOENIX_QUERY_PORT_PROP = "phoenix.query.server.port";

    public static final String ZOOKEEPER_HOSTS_PROP = "zookeeper.hosts";
    public static final String ZOOKEEPER_CLIENT_PORT_PROP = "zookeeper.client.port";
    public static final String HADOOP_USER = "hadoop.user";
    public static final String HADOOP_FS_PROP = "fs.defaultFS";
    public static final String YARN_RESOURCE_MANAGER_HOST_PROP = "yarn.resourcemanager.hostname";
}
