package com.lombardrisk.ignis.server.batch;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AppId {

    private Long clusterTimestamp;
    private Integer id;

    public static AppId valueOf(final long clusterTimestamp, final int id) {
        AppId appId = new AppId();

        appId.setClusterTimestamp(clusterTimestamp);
        appId.setId(id);

        return appId;
    }

    public boolean isNotEmpty() {
        return clusterTimestamp != null
                && id != null;
    }
}
