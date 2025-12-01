package com.org.app.dcas.dto;

import java.util.List;

public class RematchResult {
    private List<Long> successIds;
    private List<Long> failedIds;

    public RematchResult(List<Long> successIds, List<Long> failedIds) {
        this.successIds = successIds;
        this.failedIds = failedIds;
    }

    public List<Long> getSuccessIds() {
        return successIds;
    }

    public void setSuccessIds(List<Long> successIds) {
        this.successIds = successIds;
    }

    public List<Long> getFailedIds() {
        return failedIds;
    }

    public void setFailedIds(List<Long> failedIds) {
        this.failedIds = failedIds;
    }
}
