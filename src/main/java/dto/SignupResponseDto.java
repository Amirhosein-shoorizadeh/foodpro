package dto;

public class SignupResponseDto {
    public String message;
    public long user_id;
    public String token;
    public SignupResponseDto() {}
    public SignupResponseDto(String message, long user_id, String token) {
        this.message = message;
        this.user_id = user_id;
        this.token = token;
    }
}
