package org.wso2.apk.extractor.fetchers.dto;

import org.wso2.apk.extractor.datatypes.APIDataType;

import java.util.List;

public class APIListDTO {
    private int count;
    private List<APIDataType> list;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<APIDataType> getList() {
        return list;
    }

    public void setList(List<APIDataType> list) {
        this.list = list;
    }
}
