package br.ufpb.dcx.apps4society.educapi.unit.domain.builder;


import br.ufpb.dcx.apps4society.educapi.domain.Challenge;
import br.ufpb.dcx.apps4society.educapi.domain.User;
import br.ufpb.dcx.apps4society.educapi.dto.challenge.ChallengeRegisterDTO;

import java.util.Optional;

public class ChallengeBuilder {

    private Long id = null;
    private String word = "word";
    private User creator;
    private String soundUrl = "soundUrl";
    private String videoUrl = "videoUrl";
    private String imageUrl = "imageUrl";

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
    public ChallengeBuilder withUser(User creator){
        this.creator = creator;
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
    public ChallengeBuilder withImageUrl (String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }
    // Optional é utilizado quendo não se sabe que um objeto vai estar no banco de dados(se vai ser utilizado ou nao)
    public Optional<Challenge> buildOptionalChallenge() {
        return Optional.ofNullable(new Challenge(this.id, this.word, this.creator, this.soundUrl, this.videoUrl, this.imageUrl));
    }
    public ChallengeRegisterDTO buildChallengeRegisterDTO(){
        return new ChallengeRegisterDTO(this.word, this.soundUrl, this.videoUrl, this.imageUrl);
    }
}
