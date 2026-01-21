package com.databasepreservation.common.client.models.status.collection;

import java.io.Serializable;
import java.util.List;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class VirtualReferenceStatus implements Serializable {
    private String referencedTableUUID;
    private List<String> referencedColumnsIds;
    private TemplateStatus referencedTemplateStatus;

    public String getReferencedTableUUID() {
        return referencedTableUUID;
    }

    public void setReferencedTableUUID(String referencedTableUUID) {
        this.referencedTableUUID = referencedTableUUID;
    }

    public List<String> getReferencedColumnsIds() {
        return referencedColumnsIds;
    }

    public void setReferencedColumnsIds(List<String> referencedColumnsIds) {
        this.referencedColumnsIds = referencedColumnsIds;
    }

    public TemplateStatus getReferencedTemplateStatus() {
        return referencedTemplateStatus;
    }

    public void setReferencedTemplateStatus(TemplateStatus referencedTemplateStatus) {
        this.referencedTemplateStatus = referencedTemplateStatus;
    }
}
