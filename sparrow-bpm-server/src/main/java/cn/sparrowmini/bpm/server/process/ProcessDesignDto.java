package cn.sparrowmini.bpm.server.process;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * DTO for {@link ProcessDesign}
 */
@Getter
@NoArgsConstructor
public final class ProcessDesignDto implements Serializable {
    OffsetDateTime modifiedDate;
    String modifiedBy;
    ProcessDesignIdDto id;
    String processName;
    ContainerIdDto containerId;

    public ProcessDesignDto(OffsetDateTime modifiedDate, String modifiedBy, ProcessDesignIdDto id, String processName, ContainerIdDto containerId) {
        this.modifiedDate = modifiedDate;
        this.modifiedBy = modifiedBy;
        this.id = id;
        this.processName = processName;
        this.containerId = containerId;
    }

    /**
     * DTO for {@link ProcessDesign.ProcessDesignId}
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcessDesignIdDto implements Serializable {
        String processId;
        String processVersion;
        String packageName;
    }

    /**
     * DTO for {@link Container.ContainerId}
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContainerIdDto implements Serializable {
        String groupId;
        String artifactId;
        String version;
    }
}