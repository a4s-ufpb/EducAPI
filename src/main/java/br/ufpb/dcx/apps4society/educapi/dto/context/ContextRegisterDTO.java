package br.ufpb.dcx.apps4society.educapi.dto.context;

import br.ufpb.dcx.apps4society.educapi.domain.Context;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

public class ContextRegisterDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotEmpty(message="Required")
    @Length(min=2, max=50, message="The size must be between 2 and 50 characters")
    private String name;

    private String imageUrl;
    private String soundUrl;
    private String videoUrl;

    public ContextRegisterDTO(String name, String imageUrl, String soundUrl, String videoUrl){
        this.name = name;
        this.imageUrl = imageUrl;
        this.soundUrl = soundUrl;
        this.videoUrl = videoUrl;
    }

    public Context toContext(){
        return new Context(name, imageUrl, soundUrl, videoUrl);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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

}
