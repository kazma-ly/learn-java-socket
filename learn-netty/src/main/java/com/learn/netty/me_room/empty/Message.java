package com.learn.netty.me_room.empty;

/**
 * 传输的消息
 * Created by mac_zly on 2017/5/30.
 */

public class Message {

    // 谁发送的消息
    private String from;
    // 消息是啥
    private String message;
    // 发给谁
    private String to;
    // 发送时间
    private Long date;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    @Override
    public String toString() {
        return "Message{" +
                "from='" + from + '\'' +
                ", message='" + message + '\'' +
                ", date=" + date +
                ", to='" + to + '\'' +
                '}';
    }
}
