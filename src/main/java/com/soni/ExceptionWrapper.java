package com.soni;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "Error")
@XmlRootElement(name = "Error")
@XmlAccessorType(XmlAccessType.FIELD)
public class ExceptionWrapper {

    @JsonProperty(value = "StatusCode")
    @XmlElement(name = "StatusCode")
    private String statusCode;

    @JsonProperty(value = "ErrorMessage")
    @XmlElement(name = "ErrorMessage")
    private String errorMessage;

    @JsonProperty(value = "TrackingUid")
    @XmlElement(name = "TrackingUid")
    private String trackingUid;

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getTrackingUid() {
        return trackingUid;
    }

    public void setTrackingUid(String trackingUid) {
        this.trackingUid = trackingUid;
    }

    public ExceptionWrapper(String statusCode, String errorMessage, String trackingUid) {
        this.statusCode = statusCode;
        this.errorMessage = errorMessage;
        this.trackingUid = trackingUid;
    }
}
