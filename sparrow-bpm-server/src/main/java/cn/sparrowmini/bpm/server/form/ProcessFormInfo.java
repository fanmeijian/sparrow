package cn.sparrowmini.bpm.server.form;

/**
 * Projection for {@link ProcessForm}
 */
public interface ProcessFormInfo {
    ProcessFormIdInfo getId();

    /**
     * Projection for {@link ProcessForm.ProcessFormId}
     */
    interface ProcessFormIdInfo {
        String getProcessId();

        String getProcessVersion();

        String getPackageName();

        String getFormId();
    }
}