package com.blackboxindia.bitcampusfriend.Network.Interfaces;


public interface onCompleteListener<T> {
    void onSuccess(T data);
    void onFailure(Exception e);
}
