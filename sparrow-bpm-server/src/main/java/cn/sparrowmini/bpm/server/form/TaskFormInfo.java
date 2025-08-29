package cn.sparrowmini.bpm.server.form;

/**
 * Projection for {@link TaskForm}
 */
public interface TaskFormInfo {
    TaskFormIdInfo getId();

    /**
     * Projection for {@link TaskForm.TaskFormId}
     */
    interface TaskFormIdInfo {
        String getProcessId();

        String getProcessVersion();

        String getPackageName();

        String getFormId();

        String getTaskName();
    }
}