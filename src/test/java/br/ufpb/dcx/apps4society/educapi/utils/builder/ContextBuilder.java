package br.ufpb.dcx.apps4society.educapi.utils.builder;

import br.ufpb.dcx.apps4society.educapi.domain.Context;
import br.ufpb.dcx.apps4society.educapi.domain.User;
import br.ufpb.dcx.apps4society.educapi.dto.context.ContextDTO;
import br.ufpb.dcx.apps4society.educapi.dto.context.ContextRegisterDTO;

import java.util.Optional;

public class ContextBuilder {

    private Long id = null;
    private String name = "Context";

    private String imageUrl = "imageUrl";
    private String soundUrl = "soundUrl";
    private String videoUrl = "videoUrl";
    private User creator;

    public static ContextBuilder anContext(){
        return new ContextBuilder();
    }

    public ContextBuilder withId(Long id){
        this.id = id;
        return this;
    }

    public ContextBuilder withName(String name){
        this.name = name;
        return this;
    }

    public ContextBuilder withImage(String imageUrl){
        this.imageUrl = imageUrl;
        return this;
    }

    public ContextBuilder withSound(String soundUrl){
        this.soundUrl = soundUrl;
        return this;
    }

    public ContextBuilder withVideo(String videoUrl){
        this.videoUrl = videoUrl;
        return this;
    }

    public ContextBuilder withCreator(User creator) {
        this.creator = creator;
        return this;
    }
    // Optional é utilizado quando não se sabe que um objeto vai estar no banco de dados(se vai ser utilizado ou nao)
    public Optional<Context> buildOptionalContext(){ return Optional.ofNullable(new Context(this.id, this.name, this.creator, this.imageUrl, this.soundUrl, this.videoUrl));
    }

    public ContextRegisterDTO buildContextRegisterDTO(){ return new ContextRegisterDTO(this.name, this.imageUrl, this.soundUrl, this.videoUrl);
    }

    public Context buildContext(){
        return new Context(this.id, this.name, this.imageUrl, this.soundUrl, this.videoUrl, this.creator);
    }

//    public ContextDTO buildContextDTO(){
//        return new ContextDTO(this.id, this.name, this.imageUrl, this.soundUrl, this.videoUrl);
//    }

    public ContextDTO buildContextDTO() {

        ContextDTO contexDTO = new ContextDTO();
        contexDTO.setId(this.id);
        contexDTO.setName(this.name);
        contexDTO.setImageUrl(this.imageUrl);
        contexDTO.setSoundUrl(this.soundUrl);
        contexDTO.setVideoUrl(this.videoUrl);

        return contexDTO;

    }

}
