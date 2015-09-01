package com.soni;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by asoni1 on 8/23/15.
 */
@JsonRootName(value="BalanceTransfer")
@XmlRootElement(name="BalanceTransfer")
@XmlAccessorType(XmlAccessType.FIELD)
public class BalanceTransferDomain {

    @JsonProperty(value="id")
    @XmlElement(name="id")

    private String id;

    @JsonProperty(value="bank_name")
    @XmlElement(name="bank_name")
    private String bankName;

    @JsonProperty(value="initial_balance")
    @XmlElement(name="initial_balance")
    private String initialBalance;

    @JsonProperty(value="balance_left")
    @XmlElement(name="balance_left")
    private String balanceLeft;

    @JsonProperty(value="due_date")
    @XmlElement(name="due_date")
    private String dueDate;

    @JsonProperty(value="time_left")
    @XmlElement(name="time_left")
    private String timeLeft;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(String initialBalance) {
        this.initialBalance = initialBalance;
    }

    public String getBalanceLeft() {
        return balanceLeft;
    }

    public void setBalanceLeft(String balanceLeft) {
        this.balanceLeft = balanceLeft;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getTimeLeft() {
        return timeLeft;
    }

    public void setTimeLeft(String timeLeft) {
        this.timeLeft = timeLeft;
    }

    @Override
    public String toString() {
        return bankName + "," + initialBalance +"," + balanceLeft + "," + dueDate;
    }
}
