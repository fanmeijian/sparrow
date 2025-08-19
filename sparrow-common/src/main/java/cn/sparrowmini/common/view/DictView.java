package cn.sparrowmini.common.view;

/**
 * Projection for {@link cn.sparrowmini.pem.model.Dict}
 */
public interface DictView extends BaseIdLogView {
    String getCode();

    String getName();

    String getParentId();

    String getPreviousNodeId();

    String getNextNodeId();

    String getCatalogId();

    DictCatalogView getCatalog();
}