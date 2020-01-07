package com.lombardrisk.ignis.server.util.spark;

import java.io.IOException;

public interface AppSubmitter {

    AppSession submit(final SparkSubmitOption sparkSubmitOption) throws IOException;
}
