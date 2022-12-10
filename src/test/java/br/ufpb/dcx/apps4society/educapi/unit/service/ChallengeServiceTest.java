package br.ufpb.dcx.apps4society.educapi.unit.service;

import br.ufpb.dcx.apps4society.educapi.domain.Challenge;
import br.ufpb.dcx.apps4society.educapi.dto.challenge.ChallengeDTO;
import br.ufpb.dcx.apps4society.educapi.dto.challenge.ChallengeRegisterDTO;
import br.ufpb.dcx.apps4society.educapi.repositories.ChallengeRepository;
import br.ufpb.dcx.apps4society.educapi.services.ChallengeService;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.ChallengeAlreadyExistsException;
import br.ufpb.dcx.apps4society.educapi.unit.domain.builder.ChallengeBuilder;
import br.ufpb.dcx.apps4society.educapi.util.Messages;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ChallengeServiceTest {

    @Mock
    private ChallengeRepository challengeRepository;
//    @Mock
//    private JWTService jwtService;
    @InjectMocks
    private ChallengeService service;

    private final ChallengeRegisterDTO challengeRegisterDTO = ChallengeBuilder.anChallenge().buildChallengeRegisterDTO();
    private final Optional<Challenge> challengeOptional = ChallengeBuilder.anChallenge().buildOptionalChallenge();

    @Test
    public void insertAChallengeTest() throws ChallengeAlreadyExistsException{
        // OBS: construtor insert(String token, ChallengeRegisterDTO, Long contextId)
        ChallengeDTO response = service.insert(this.challengeRegisterDTO);

        assertEquals(response.getWord(), this.challengeRegisterDTO.getWord());
        assertEquals(response.getSoundUrl(), this.challengeRegisterDTO.getSoundUrl());
        assertEquals(response.getImageUrl(), this.challengeRegisterDTO.getImageUrl());
        assertEquals(response.getVideoUrl(), this.challengeRegisterDTO.getVideoUrl());
    }

    @Test
    public void insertAChallengeAlreadyExistTest(){
        Mockito.when(this.challengeRepository.findByWord(this.challengeRegisterDTO.getWord())).thenReturn(this.challengeOptional);
        Exception exception = assertThrows(ChallengeAlreadyExistsException.class, () -> {

            //OBS: na classe "ChallengeRegisterDTO" não há autor nem context
            //OBS2: Somente a classe "ChallengeDTO" possui autores e id
            service.insert(this.challengeRegisterDTO.getToken(), this.challengeRegisterDTO, this.challengeRegisterDTO.getContext());
        });
        assertEquals(Messages.USER_ALREADY_EXISTS, exception.getMessage());
    }
}
