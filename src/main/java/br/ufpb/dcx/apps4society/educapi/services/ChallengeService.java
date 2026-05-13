package br.ufpb.dcx.apps4society.educapi.services;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import br.ufpb.dcx.apps4society.educapi.domain.Challenge;
import br.ufpb.dcx.apps4society.educapi.domain.Context;
import br.ufpb.dcx.apps4society.educapi.domain.User;
import br.ufpb.dcx.apps4society.educapi.dto.challenge.ChallengeRegisterDTO;
import br.ufpb.dcx.apps4society.educapi.repositories.ChallengeRepository;
import br.ufpb.dcx.apps4society.educapi.repositories.ContextRepository;
import br.ufpb.dcx.apps4society.educapi.repositories.UserRepository;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.InvalidUserException;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.ObjectNotFoundException;

@Service
public class ChallengeService {

    private static final Logger logger = LoggerFactory.getLogger(ChallengeService.class);

    @Autowired
    private JWTService jwtService;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContextRepository contextRepository;

    @Autowired
    private UploadImageService uploadImageService;

    public ChallengeService(JWTService jwtService, ChallengeRepository challengeRepository,
            ContextRepository contextRepository, UserRepository userRepository, UploadImageService uploadImageService) {
        this.jwtService = jwtService;
        this.challengeRepository = challengeRepository;
        this.contextRepository = contextRepository;
        this.userRepository = userRepository;
        this.uploadImageService = uploadImageService;
    }

    public Challenge find(String token, Long id) throws ObjectNotFoundException, InvalidUserException {
        Optional<String> userEmail = jwtService.recoverUser(token);
        if (userEmail.isEmpty()) {
            throw new InvalidUserException();
        }

        Optional<Challenge> challengeOptional = challengeRepository.findById(id);
        if (challengeOptional.isEmpty()) {
            throw new ObjectNotFoundException("Object not found! Id: " + id + ", Type: " + Challenge.class.getName());
        }

        return challengeOptional.get();
    }

    @Transactional
    public Challenge insert(String token, ChallengeRegisterDTO obj, Long contextID)
            throws ObjectNotFoundException, InvalidUserException {
        User user = validateUser(token);

        Optional<Context> contextOptional = contextRepository.findById(contextID);
        if (contextOptional.isEmpty()) {
            throw new ObjectNotFoundException();
        }

        Challenge challenge = obj.challengeRegisterDTOToChallenge();
        Context context = contextOptional.get();

        challenge.setCreator(user);

        if (obj.getFile() != null && !obj.getFile().isEmpty()) {
            uploadImage(user, context, challenge, obj.getFile());
        }

        challenge.getContexts().add(context);

        try {
            challengeRepository.save(challenge);
        } catch (RuntimeException e) {
            logger.error(
                    "Erro ao salvar Challenge. userId={}, contextId={}, contextName={}, word={}, imageUrlDefinida={}, imageBackupDefinido={}",
                    user.getId(),
                    context.getId(),
                    context.getName(),
                    challenge.getWord(),
                    challenge.getImageUrl() != null,
                    challenge.getImageBackup() != null,
                    e
            );
            throw e;
        }
        return challenge;

    }

    public List<Challenge> findChallengesByCreator(String token) throws ObjectNotFoundException, InvalidUserException {
        User user = validateUser(token);
        return challengeRepository.findChallengesByCreator(user);
    }

    public Challenge update(String token, ChallengeRegisterDTO obj, Long id) throws ObjectNotFoundException, InvalidUserException {
        User user = validateUser(token);

        Challenge newObj = find(token, id);
        if (!newObj.getCreator().equals(user)) {
            throw new InvalidUserException();
        }

        updateData(newObj, obj.challengeRegisterDTOToChallenge());

        if (obj.getFile() != null && !obj.getFile().isEmpty()) {
            Context context = newObj.getContexts().iterator().next();
            uploadImage(user, context, newObj, obj.getFile());
        }

        challengeRepository.save(newObj);
        return newObj;
    }

    public void delete(String token, Long id) throws ObjectNotFoundException, InvalidUserException {
        User user = validateUser(token);

        Challenge obj = find(token, id);
        if (obj.getCreator().equals(user)) {
            for (Context x : obj.getContexts()) {
                x.getChallenges().remove(obj);
                contextRepository.save(x);
            }
            challengeRepository.deleteById(id);
        } else {
            throw new InvalidUserException();
        }
    }

    public Page<Challenge> findChallengesByParams(String word, Pageable pageable) {
        if (word != null) {
            return challengeRepository.findByWordStartsWithIgnoreCase(word, pageable);
        }

        return challengeRepository.findAll(pageable);
    }

    private User validateUser(String token) throws ObjectNotFoundException, InvalidUserException {
        Optional<String> userEmail = jwtService.recoverUser(token);
        if (userEmail.isEmpty()) {
            throw new InvalidUserException();
        }

        Optional<User> userOptional = userRepository.findByEmail(userEmail.get());
        if (userOptional.isEmpty()) {
            throw new ObjectNotFoundException();
        }

        return userOptional.get();
    }

    private void updateData(Challenge newObj, Challenge obj) {
        newObj.setWord(obj.getWord());
        newObj.setSoundUrl(obj.getSoundUrl());
        newObj.setVideoUrl(obj.getVideoUrl());
        newObj.setImageUrl(obj.getImageUrl());
    }

    private void uploadImage(User user, Context context, Challenge challenge, MultipartFile file) {
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            originalName = "challenge-image";
        }

        String folder
                = "user_" + user.getId()
                + "/context_" + context.getName()
                + "/challenges";

        try {
            byte[] imageBytes = file.getBytes();
            String imageBackup = uploadImageService.generateBase64Thumbnail(imageBytes);

            String imageUrl = uploadImageService.uploadFile(
                    folder,
                    originalName,
                    new ByteArrayInputStream(imageBytes),
                    imageBytes.length,
                    file.getContentType()
            );

            challenge.setImageUrl(imageUrl);
            challenge.setImageBackup(imageBackup);

        } catch (Exception e) {
            logger.error(
                    "Erro ao processar imagem do Challenge. userId={}, contextId={}, contextName={}, folder={}, fileName={}, contentType={}, size={}",
                    user.getId(),
                    context.getId(),
                    context.getName(),
                    folder,
                    originalName,
                    file.getContentType(),
                    file.getSize(),
                    e
            );
            throw new RuntimeException(
                    "Erro ao processar imagem do Challenge: " + e.getMessage(),
                    e
            );
        }
    }

}
