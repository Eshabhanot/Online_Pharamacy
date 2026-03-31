package in.cg.main.dto;

import in.cg.main.entities.User;

public class InternalUserResponse {

    private Long id;
    private String email;

    public static InternalUserResponse from(User user) {
        InternalUserResponse response = new InternalUserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
