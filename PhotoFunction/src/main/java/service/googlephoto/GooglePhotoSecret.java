package service.googlephoto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GooglePhotoSecret {
    private String clientId;
    private String clientSecret;
    private String accessToken;
    private String refreshToken;
}
