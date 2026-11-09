package br.ufpb.dcx.apps4society.educapi.resources;

import br.ufpb.dcx.apps4society.educapi.domain.Challenge;
import br.ufpb.dcx.apps4society.educapi.dto.challenge.ChallengeRegisterDTO;
import br.ufpb.dcx.apps4society.educapi.services.ChallengeService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.beans.PropertyEditorSupport;
import java.util.List;

@RestController
@RequestMapping(value="/v1/api/")
@CrossOrigin("*")
public class ChallengeResource {
	@Autowired
	private ChallengeService challengeService;

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(MultipartFile.class, new PropertyEditorSupport() {
			@Override
			public void setAsText(String text) {
				if (text == null || text.isBlank()) {
					setValue(null);
				}
			}
		});
	}

	@Operation(summary = "Returns a Challenge present in the service, if the token and the Challenge ID are valid.")
	@GetMapping("auth/challenges/{idChallenge}")
	public ResponseEntity<Challenge> find(@RequestHeader("Authorization") String token,
										  @PathVariable Long idChallenge) {
		return ResponseEntity.ok(challengeService.find(token, idChallenge));
	}

	@Operation(summary = "Adds a new Challenge to a Context via file upload, if the token and the Context ID are valid.")
	@PostMapping(
			value = "auth/challenges/{idContext}",
			consumes = "multipart/form-data"
	)
	public ResponseEntity<Challenge> insertWithFile(@RequestHeader("Authorization") String token,
											@Valid @ModelAttribute ChallengeRegisterDTO objDto,
											@PathVariable Long idContext){
		return ResponseEntity.status(HttpStatus.CREATED).body(challengeService.insert(token, objDto, idContext));
	}

	@Operation(summary = "Adds a new Challenge to a Context via JSON (imageUrl), if the token and the Context ID are valid.")
	@PostMapping(
			value = "auth/challenges/{idContext}",
			consumes = "application/json"
	)
	public ResponseEntity<Challenge> insertWithJson(@RequestHeader("Authorization") String token,
											@Valid @RequestBody ChallengeRegisterDTO objDto,
											@PathVariable Long idContext){
		return ResponseEntity.status(HttpStatus.CREATED).body(challengeService.insert(token, objDto, idContext));
	}

	@Operation(summary = "Updates a User Challenge via file upload, if the token and the Challenge ID are valid.")
	@PutMapping(
			value = "auth/challenges/{idChallenge}",
			consumes = "multipart/form-data"
	)
	public ResponseEntity<Challenge> updateWithFile(@RequestHeader("Authorization") String token,
											@Valid @ModelAttribute ChallengeRegisterDTO objDto,
											@PathVariable Long idChallenge){
		return ResponseEntity.ok(challengeService.update(token, objDto, idChallenge));
	}

	@Operation(summary = "Updates a User Challenge via JSON (imageUrl), if the token and the Challenge ID are valid.")
	@PutMapping(
			value = "auth/challenges/{idChallenge}",
			consumes = "application/json"
	)
	public ResponseEntity<Challenge> updateWithJson(@RequestHeader("Authorization") String token,
											@Valid @RequestBody ChallengeRegisterDTO objDto,
											@PathVariable Long idChallenge){
		return ResponseEntity.ok(challengeService.update(token, objDto, idChallenge));
	}

	@Operation(summary = "Deletes a User Challenge, if the token and the Challenge ID are valid.")
	@DeleteMapping("auth/challenges/{idChallenge}")
	public ResponseEntity<Void> delete(@RequestHeader("Authorization") String token,
									   @PathVariable Long idChallenge){
		challengeService.delete(token, idChallenge);
		return ResponseEntity.status(HttpStatus.OK).build();
	}

	@Operation(summary = "Returns a list of all Challenges registered by the request User, if the token is valid.")
	@GetMapping("auth/challenges")
	public ResponseEntity<List<Challenge>> findAllByUser(@RequestHeader("Authorization") String token){
		return ResponseEntity.ok(challengeService.findChallengesByCreator(token));
	}

	@Operation(summary = "Returns the Challenge image bytes, using the MinIO image first and imageBackup as fallback.")
	@GetMapping("challenges/{idChallenge}/image")
	public ResponseEntity<byte[]> getImage(@PathVariable Long idChallenge) {
		byte[] imageBytes = challengeService.getChallengeImage(idChallenge);

		return ResponseEntity.ok()
				.contentType(resolveImageContentType(imageBytes))
				.body(imageBytes);
	}

	private MediaType resolveImageContentType(byte[] imageBytes) {
		if (imageBytes.length >= 8
				&& imageBytes[0] == (byte) 0x89
				&& imageBytes[1] == 0x50
				&& imageBytes[2] == 0x4E
				&& imageBytes[3] == 0x47) {
			return MediaType.IMAGE_PNG;
		}

		return MediaType.IMAGE_JPEG;
	}

	@Operation(summary = "Returns a page with Challenges registered in the service.")
	@GetMapping("challenges")
	public ResponseEntity<Page<Challenge>> findAllChallenges(
			@RequestParam(value = "word", required = false) String word,
			@RequestParam(value = "size", defaultValue = "20") Integer size,
			@RequestParam(value = "page", defaultValue = "0") Integer page,
			Pageable pageable){
		return new ResponseEntity<>(challengeService.findChallengesByParams(word, pageable), HttpStatus.OK);
	}

}
