package br.ufpb.dcx.apps4society.educapi.unit.service;

import br.ufpb.dcx.apps4society.educapi.domain.Challenge;
import br.ufpb.dcx.apps4society.educapi.domain.Context;
import br.ufpb.dcx.apps4society.educapi.domain.User;
import br.ufpb.dcx.apps4society.educapi.dto.challenge.ChallengeDTO;
import br.ufpb.dcx.apps4society.educapi.dto.challenge.ChallengeRegisterDTO;
import br.ufpb.dcx.apps4society.educapi.dto.context.ContextRegisterDTO;
import br.ufpb.dcx.apps4society.educapi.dto.user.UserLoginDTO;
import br.ufpb.dcx.apps4society.educapi.repositories.ChallengeRepository;
import br.ufpb.dcx.apps4society.educapi.services.ChallengeService;
import br.ufpb.dcx.apps4society.educapi.services.JWTService;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.ChallengeAlreadyExistsException;
import br.ufpb.dcx.apps4society.educapi.unit.domain.builder.ChallengeBuilder;
import br.ufpb.dcx.apps4society.educapi.unit.domain.builder.ContextBuilder;
import br.ufpb.dcx.apps4society.educapi.unit.domain.builder.UserBuilder;
import br.ufpb.dcx.apps4society.educapi.util.Messages;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
@ExtendWith(MockitoExtension.class)
public class ChallengeServiceTest {

    @Mock
    private ChallengeRepository challengeRepository;
    @Mock
    private JWTService jwtService;
    @InjectMocks
    private ChallengeService service;

    private final ChallengeRegisterDTO challengeRegisterDTO = ChallengeBuilder.anChallenge().buildChallengeRegisterDTO();
    private final Optional<Challenge> challengeOptional = ChallengeBuilder.anChallenge().buildOptionalChallenge();

    private final Optional<User> userOptional = UserBuilder.anUser().buildOptionalUser();
    private final UserLoginDTO userLoginDTO = UserBuilder.anUser().buildUserLoginDTO();

    private final Optional<Context> contextRegisterDTO = ContextBuilder.anContext().buildOptionalContext();
    @Test
    public void insertAChallengeTest() throws ChallengeAlreadyExistsException{

        // OBS: construtor insert(String token, ChallengeRegisterDTO, Long contextId)
        // OBS2: os tokens são de User, Challenge e context?
        // OBS3: como eu puxo o token pro construtor insert?
        ChallengeDTO response = service.insert(jwtService.authenticate(), this.challengeRegisterDTO);

        assertEquals(response.getWord(), this.challengeRegisterDTO.getWord());
        assertEquals(response.getSoundUrl(), this.challengeRegisterDTO.getSoundUrl());
        assertEquals(response.getImageUrl(), this.challengeRegisterDTO.getImageUrl());
        assertEquals(response.getVideoUrl(), this.challengeRegisterDTO.getVideoUrl());
    }

    @Test
    public void insertAChallengeAlreadyExistTest(){
        Mockito.when(this.challengeRepository.findByWord(this.challengeRegisterDTO.getWord())).thenReturn(this.challengeOptional);
        Exception exception = assertThrows(ChallengeAlreadyExistsException.class, () -> {

            //OBS: Construtor precisa de (Token e contextRegisterDTO)
            //OBS2: os tokens são de User, Challenge e context?
            //OBS3: como eu puxo o token pro construtor insert?
            service.insert(jwtService.authenticate(userLoginDTO), this.challengeRegisterDTO, this.challengeRegisterDTO.getContext());
        });
        assertEquals(Messages.CHALLENGE_ALREADY_EXISTS, exception.getMessage());
    }
}
