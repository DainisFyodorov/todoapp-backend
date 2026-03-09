package lv.dainis.todoapp.responsemodel;

import lombok.Data;

@Data
public class UserInfoResponse {

    private String username;
    private boolean isLoggedIn;
}
