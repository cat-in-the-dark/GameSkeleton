package org.catinthedark.network;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include= JsonTypeInfo.As.PROPERTY, property="@class")
public class CommonMessage {
    private String typeName;
    
    public String getTypeName() {
        return typeName;
    }
    
    public void setTypeName(String value) {
        typeName = value;
    }
}
