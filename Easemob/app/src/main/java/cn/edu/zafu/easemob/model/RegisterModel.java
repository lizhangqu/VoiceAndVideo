package cn.edu.zafu.easemob.model;

import java.io.Serializable;

/**
 * User:lizhangqu(513163535@qq.com)
 * Date:2015-09-18
 * Time: 11:05
 */
public class RegisterModel implements Serializable{
    private int status;
    private String message;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Bean{" +
                "status=" + status +
                ", message='" + message + '\'' +
                '}';
    }
}
