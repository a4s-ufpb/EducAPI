package br.ufpb.dcx.apps4society.educapi.unit.service;

import br.ufpb.dcx.apps4society.educapi.domain.Challenge;
import br.ufpb.dcx.apps4society.educapi.dto.challenge.ChallengeDTO;
import br.ufpb.dcx.apps4society.educapi.dto.challenge.ChallengeRegisterDTO;
import br.ufpb.dcx.apps4society.educapi.repositories.ChallengeRepository;
import br.ufpb.dcx.apps4society.educapi.services.ChallengeService;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.ChallengeAlreadyExistsException;
import br.ufpb.dcx.apps4society.educapi.unit.domain.builder.ChallengeBuilder;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChallengeServiceTest {

    @Mock
    private ChallengeRepository challengeRepository;
//    @Mock
//    private JWTService jwtService;
    @InjectMocks
    private ChallengeService service;

    private final ChallengeRegisterDTO challengeRegisterDTO = ChallengeBuilder.anChallenge().buildChallengeDTO();
    private final Optional<Challenge> challengeOptional = ChallengeBuilder.anChallenge().buildChallengeDTO();

    @Test
    public void insertAChallengeTest() throws ChallengeAlreadyExistsException{
        ChallengeDTO response = service.insert(this.challengeRegisterDTO);

        assertEquals(response.getWord(), this.challengeRegisterDTO.getWord());
        assertEquals(response.getSoundUrl(), this.challengeRegisterDTO.getSoundUrl());
        assertEquals(response.getImageUrl(), this.challengeRegisterDTO.getImageUrl());
        assertEquals(response.getVideoUrl(), this.challengeRegisterDTO.getVideoUrl());
    }

    @Test
    public void insertAChallengeAlreadyExistTest(){
        Mockito.when()
    }



}
