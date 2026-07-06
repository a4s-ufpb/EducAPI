package br.ufpb.dcx.apps4society.educapi.services;

import java.io.ByteArrayInputStream;
import java.util.Base64;
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

import br.ufpb.dcx.apps4society.educapi.domain.AcaoAuditoria;
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

    @Autowired
    private LogAuditoriaService logAuditoriaService;

    public ChallengeService(JWTService jwtService, ChallengeRepository challengeRepository,
            ContextRepository contextRepository, UserRepository userRepository, UploadImageService uploadImageService,
            LogAuditoriaService logAuditoriaService) {
        this.jwtService = jwtService;
        this.challengeRepository = challengeRepository;
        this.contextRepository = contextRepository;
        this.userRepository = userRepository;
        this.uploadImageService = uploadImageService;
        this.logAuditoriaService = logAuditoriaService;
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
        } else if (challenge.getImageUrl() != null && !challenge.getImageUrl().isBlank()) {
            generateBackupFromExternalUrl(challenge, "Challenge.insert");
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

        logAuditoriaService.registrar(user, AcaoAuditoria.CRIACAO_DESAFIO, "Challenge", challenge.getId(),
                "word=" + challenge.getWord());

        return challenge;

    }

    public List<Challenge> findChallengesByCreator(String token) throws ObjectNotFoundException, InvalidUserException {
        User user = validateUser(token);
        return challengeRepository.findChallengesByCreator(user);
    }

    public Challenge update(String token, ChallengeRegisterDTO obj, Long id) throws ObjectNotFoundException, InvalidUserException {
        User user = validateUser(token);

        Challenge newObj = find(token, id);
        boolean isOwner = newObj.getCreator().equals(user);
        boolean isAdminBypass = !isOwner && user.isAdmin();
        if (!isOwner && !isAdminBypass) {
            throw new InvalidUserException();
        }

        updateData(newObj, obj.challengeRegisterDTOToChallenge());

        String oldImageUrl = newObj.getImageUrl();
        boolean hasNewImage = obj.getFile() != null && !obj.getFile().isEmpty();

        if (hasNewImage) {
            Context context = newObj.getContexts().iterator().next();
            uploadImage(user, context, newObj, obj.getFile());
        } else if (obj.getImageUrl() != null && !obj.getImageUrl().isBlank()) {
            newObj.setImageUrl(obj.getImageUrl());
            generateBackupFromExternalUrl(newObj, "Challenge.update");
        }

        challengeRepository.save(newObj);

        if (hasNewImage) {
            deleteOldChallengeImage(oldImageUrl, newObj.getImageUrl());
        }

        return newObj;
    }

    public void delete(String token, Long id) throws ObjectNotFoundException, InvalidUserException {
        User user = validateUser(token);

        Challenge obj = find(token, id);

        boolean isOwner = obj.getCreator().equals(user);
        boolean isAdminBypass = !isOwner && user.isAdmin();

        if (isOwner || isAdminBypass) {
            String imageUrl = obj.getImageUrl();
            String word = obj.getWord();

            for (Context x : obj.getContexts()) {
                x.getChallenges().remove(obj);
                contextRepository.save(x);
            }
            challengeRepository.deleteById(id);

            deleteChallengeImage(imageUrl);

            String detalhes = "word=" + word + " (" + exclusionMethodDescription(isAdminBypass, user) + ")";
            logAuditoriaService.registrar(user, AcaoAuditoria.EXCLUSAO_DESAFIO, "Challenge", id, detalhes);
        } else {
            throw new InvalidUserException();
        }
    }

    /**
     * Describes, for audit purposes, whether a deletion happened through
     * normal ownership or through administrative bypass power.
     */
    private String exclusionMethodDescription(boolean isAdminBypass, User user) {
        return isAdminBypass
                ? "excluido via poder administrativo, role=" + user.getRole()
                : "excluido via ownership";
    }

    public Page<Challenge> findChallengesByParams(String word, Pageable pageable) {
        if (word != null) {
            return challengeRepository.findByWordStartsWithIgnoreCase(word, pageable);
        }

        return challengeRepository.findAll(pageable);
    }

    public byte[] getChallengeImage(Long idChallenge) throws ObjectNotFoundException {
        Optional<Challenge> challengeOptional = challengeRepository.findById(idChallenge);
        if (challengeOptional.isEmpty()) {
            throw new ObjectNotFoundException("Object not found! Id: " + idChallenge + ", Type: " + Challenge.class.getName());
        }

        Challenge challenge = challengeOptional.get();

        if (challenge.getImageUrl() != null && !challenge.getImageUrl().isBlank()) {
            try {
                if (uploadImageService.isExternalUrl(challenge.getImageUrl())) {
                    return uploadImageService.downloadImageFromUrl(challenge.getImageUrl());
                }
                return uploadImageService.getFileBytesByUrl(challenge.getImageUrl());
            } catch (RuntimeException e) {
                logger.warn(
                        "Nao foi possivel carregar imagem principal do Challenge. challengeId={}, imageUrl={}",
                        idChallenge,
                        challenge.getImageUrl(),
                        e
                );
            }
        }

        if (challenge.getImageBackup() == null || challenge.getImageBackup().isBlank()) {
            throw new ObjectNotFoundException("Imagem do Challenge nao encontrada. Id: " + idChallenge);
        }

        try {
            return Base64.getDecoder().decode(challenge.getImageBackup());
        } catch (IllegalArgumentException e) {
            throw new ObjectNotFoundException("Imagem de backup do Challenge invalida. Id: " + idChallenge, e);
        }
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
        if (obj.getImageUrl() != null && !obj.getImageUrl().isBlank()) {
            newObj.setImageUrl(obj.getImageUrl());
        }
    }

    private void uploadImage(User user, Context context, Challenge challenge, MultipartFile file) {
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            originalName = "challenge-image";
        }

        String folder
                = "user_" + user.getId()
                + "/context_" + context.getId()
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

    private void generateBackupFromExternalUrl(Challenge challenge, String context) {
        try {
            byte[] imageBytes = uploadImageService.downloadImageFromUrl(challenge.getImageUrl());
            String imageBackup = uploadImageService.generateBase64Thumbnail(imageBytes);
            challenge.setImageBackup(imageBackup);
        } catch (Exception e) {
            logger.warn("Nao foi possivel gerar backup da imageUrl externa do Challenge. context={}, url={}",
                    context, challenge.getImageUrl(), e);
        }
    }

    private void deleteOldChallengeImage(String oldImageUrl, String newImageUrl) {
        if (oldImageUrl == null || oldImageUrl.isBlank() || oldImageUrl.equals(newImageUrl)) {
            return;
        }

        try {
            uploadImageService.deleteFileByUrl(oldImageUrl);
        } catch (RuntimeException e) {
            logger.warn("Nao foi possivel remover imagem antiga do Challenge no MinIO. imageUrl={}", oldImageUrl, e);
        }
    }

    private void deleteChallengeImage(String imageUrl) {
        try {
            uploadImageService.deleteFileByUrl(imageUrl);
        } catch (RuntimeException e) {
            logger.warn("Nao foi possivel remover imagem do Challenge no MinIO. imageUrl={}", imageUrl, e);
        }
    }

}
