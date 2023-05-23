package br.ufpb.dcx.apps4society.educapi.utils.builder;


import br.ufpb.dcx.apps4society.educapi.domain.Challenge;
import br.ufpb.dcx.apps4society.educapi.domain.User;
import br.ufpb.dcx.apps4society.educapi.dto.challenge.ChallengeDTO;
import br.ufpb.dcx.apps4society.educapi.dto.challenge.ChallengeRegisterDTO;

import java.util.Optional;

public class ChallengeBuilder {

    private Long id = null;
    private String word = "word";
    private User creator;
    
    private String imageUrl = "imageUrl";
    private String soundUrl = "soundUrl";
    private String videoUrl = "videoUrl";

    public static ChallengeBuilder anChallenge(){
        return new ChallengeBuilder();
    }
    public ChallengeBuilder withId(Long id){
        this.id = id;
        return this;
    }
    public ChallengeBuilder withWord(String word){
        this.word = word;
        return this;
    }
    public ChallengeBuilder withCreator(User creator){
        this.creator = creator;
        return this;
    }
    public ChallengeBuilder withImageUrl (String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }
    public ChallengeBuilder withSoundUrl (String soundUrl){
        this.soundUrl = soundUrl;
        return this;
    }
    public ChallengeBuilder withVideoUrl (String videoUrl) {
        this.videoUrl = videoUrl;
        return this;
    }    
    // Optional é utilizado quendo não se sabe que um objeto vai estar no banco de dados(se vai ser utilizado ou nao)
    public Optional<Challenge> buildOptionalChallenge() {
        return Optional.ofNullable(new Challenge(this.id, this.word, this.creator, this.imageUrl, this.soundUrl, this.videoUrl ));
    }

    public ChallengeDTO buildChallengeDTO(){

        ChallengeDTO challengeDTO = new ChallengeDTO();
        challengeDTO.setId(this.id);
        challengeDTO.setWord(this.word);
        challengeDTO.setImageUrl(this.imageUrl);
        challengeDTO.setSoundUrl(this.soundUrl);
        challengeDTO.setVideoUrl(this.videoUrl);

        return challengeDTO;
    }

    public ChallengeRegisterDTO buildChallengeRegisterDTO(){
        return new ChallengeRegisterDTO(this.word, this.imageUrl, this.soundUrl, this.videoUrl);
    }
}
