package eif.viko.lt.predictionappserver.Controllers;


import eif.viko.lt.predictionappserver.Dto.LoginResponseDto;
import eif.viko.lt.predictionappserver.Dto.LoginRequestDto;
import eif.viko.lt.predictionappserver.Dto.RegisterRequestDto;
import eif.viko.lt.predictionappserver.Entities.ChatUser;
import eif.viko.lt.predictionappserver.Services.AuthService;
import eif.viko.lt.predictionappserver.Services.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/auth")
@RestController
public class AuthController {
    private final JwtService jwtService;

    private final AuthService authenticationService;

    public AuthController(JwtService jwtService, AuthService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/signup")
    public ResponseEntity<ChatUser> register(@RequestBody RegisterRequestDto registerUserDto) {
        ChatUser registeredUser = authenticationService.signup(registerUserDto);
        return ResponseEntity.ok(registeredUser);
    }

//    {
//        "email": "eif@viko.lt",
//            "password": "Kolegija1@",
//            "username": "Marius"
//    }





    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> authenticate(@RequestBody LoginRequestDto loginUserDto) {
        ChatUser authenticatedUser = authenticationService.authenticate(loginUserDto);

        String jwtToken = jwtService.generateToken(authenticatedUser);

        LoginResponseDto loginResponse = new LoginResponseDto(
                jwtService.getExpirationTime(), jwtToken
        );

        return ResponseEntity.ok(loginResponse);
    }

    //    {
//        "email": "eif@viko.lt",
//            "password": "Kolegija1@"
//    }


}