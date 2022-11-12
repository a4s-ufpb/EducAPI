package br.ufpb.dcx.apps4society.educapi.unit.domain.builder;


import br.ufpb.dcx.apps4society.educapi.domain.Challenge;
import br.ufpb.dcx.apps4society.educapi.domain.User;

import java.util.Optional;

public class ChallengeBuilder {

    private Long id = null;
    private String word = "palavra_test_ChallengeBuilder";
    private User creator;
    private String soundUrl = "soundUrl_StringPadrao_ChallengeBuilder";
    private String videoUrl = "videoUrl_StringPadrao_ChallengeBuilder";
    private String imageUrl = "imageUrl_StringPadrao_ChallengeBuilder";

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
    public Optional<Challenge> buildOptionalUser() {
        return Optional.ofNullable(new Challenge(this.id, this.word, this.creator, this.soundUrl, this.videoUrl, this.imageUrl));
    }
}
