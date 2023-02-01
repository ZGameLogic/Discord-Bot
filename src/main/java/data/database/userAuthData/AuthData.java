package data.database.userAuthData;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Random;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "UserAuthData")
public class AuthData {

    @Id
    private long userId;
    private long token;
    private String validationCode;

    public void generateToken(){
        int token = 0;
        for(int i = 0; i < 6; i++){
            token = (token * 10) + new Random().nextInt(10);
        }
        this.token = token;
    }

    public void generateValidationCode(){
        String token = "";
        String allowedChars = "asbcdefghijklmopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123465789";
        for(int i = 0; i < 32; i++){
            token += allowedChars.charAt(new Random().nextInt(allowedChars.length()));
        }
        validationCode = token;
    }
}
