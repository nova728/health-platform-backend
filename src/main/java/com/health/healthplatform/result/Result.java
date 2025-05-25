package com.health.healthplatform.result;

public class Result {
    private int code; //接口状态码
    private String msg; //接口返回消息2、User.java
    private Object data; //响应内容

    public Result(){
        super();
    }
    public Result(int code,String msg,Object data){
        this.code=code;
        this.msg=msg;
        this.data=data;
    }
    public static Result success(Object data){
        Result item=new Result(200,"success",data);
        return item;
    }
    public static Result failure(int errCode,String errorMessage){
        Result item=new Result(errCode,errorMessage,null);
        return item;
    }
    public int getCode(){
        return code;
    }
    public void setCode(int code){
        this.code=code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}