package cn.sparrowmini.bpm.server.process;

import java.time.OffsetDateTime;

/**
 * Projection for {@link ProcessDesign}
 */
public interface ProcessDesignInfo {
    OffsetDateTime getCreatedDate();

    OffsetDateTime getModifiedDate();

    String getCreatedBy();

    String getModifiedBy();

    String getProcessName();

    ProcessDesignIdInfo getId();

    ContainerIdInfo getContainerId();

    /**
     * Projection for {@link ProcessDesign.ProcessDesignId}
     */
    interface ProcessDesignIdInfo {
        String getProcessId();

        String getProcessVersion();

        String getPackageName();
    }

    /**
     * Projection for {@link Container.ContainerId}
     */
    interface ContainerIdInfo {
        String getGroupId();

        String getArtifactId();

        String getVersion();
    }
}