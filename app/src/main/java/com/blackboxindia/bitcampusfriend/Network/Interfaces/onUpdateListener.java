package com.blackboxindia.bitcampusfriend.Network.Interfaces;

import com.blackboxindia.bitcampusfriend.dataModels.UserInfo;


public interface onUpdateListener {

    void onSuccess(UserInfo userInfo);

    void onFailure(Exception e);

}
