package br.ufpb.dcx.apps4society.educapi.dto.challenge;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotEmpty;

import br.ufpb.dcx.apps4society.educapi.domain.Context;
import br.ufpb.dcx.apps4society.educapi.domain.User;
import org.hibernate.validator.constraints.Length;

import br.ufpb.dcx.apps4society.educapi.domain.Challenge;

public class ChallengeDTO implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private Long id;
	@NotEmpty(message="Required")
	@Length(min=2, max=72, message="The size must be between 2 and 72 characters")
	private String word;
	
	private String soundUrl;
	private String videoUrl;
	private String imageUrl;

	private User creator;

	private List<Context> contexts = new ArrayList<>();

	public Challenge challengeDTOToChallenge(){
		return new Challenge(id, word, creator, imageUrl, soundUrl, videoUrl, contexts);
	}
	
	public ChallengeDTO() {}
	
	public ChallengeDTO(Challenge obj) {
		this.word = obj.getWord();
		this.soundUrl = obj.getSoundUrl();
		this.videoUrl = obj.getVideoUrl();
		this.imageUrl = obj.getVideoUrl();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public List<Context> getContexts() {
		return contexts;
	}

	public void setContexts(List<Context> contexts) {
		this.contexts = contexts;
	}
}
