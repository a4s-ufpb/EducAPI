package br.ufpb.dcx.apps4society.educapi.dto.challenge;

import br.ufpb.dcx.apps4society.educapi.domain.Challenge;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotEmpty;
import java.io.Serializable;

public class ChallengeRegisterDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotEmpty(message="Required")
    @Length(min=2, max=72, message="The size must be between 2 and 72 characters")
    private String word;
    
    private String imageUrl;
    private String soundUrl;
    private String videoUrl;    

    public ChallengeRegisterDTO() {
    }

    public ChallengeRegisterDTO(String word, String imageUrl, String soundUrl, String videoUrl) {
        this.word = word;        
        this.imageUrl = imageUrl;
        this.soundUrl = soundUrl;
        this.videoUrl = videoUrl;
    }

    public Challenge challengeRegisterDTOToChallenge(){
        return new Challenge(word, imageUrl, soundUrl, videoUrl);
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getSoundUrl() {
        return soundUrl;
    }

    public void setSoundUrl(String soundUrl) {
        this.soundUrl = soundUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
