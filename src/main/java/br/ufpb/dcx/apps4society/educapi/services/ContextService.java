package br.ufpb.dcx.apps4society.educapi.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
import br.ufpb.dcx.apps4society.educapi.dto.context.ContextDTO;
import br.ufpb.dcx.apps4society.educapi.dto.context.ContextRegisterDTO;
import br.ufpb.dcx.apps4society.educapi.repositories.ChallengeRepository;
import br.ufpb.dcx.apps4society.educapi.repositories.ContextRepository;
import br.ufpb.dcx.apps4society.educapi.repositories.UserRepository;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.InvalidUserException;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.ObjectNotFoundException;
import java.io.IOException;

@Service
public class ContextService {

    private static final Logger logger = LoggerFactory.getLogger(ContextService.class);

    @Autowired
    private JWTService jwtService;

    @Autowired
    private ContextRepository contextRepository;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UploadImageService uploadImageService;

    public ContextService(JWTService jwtService, ContextRepository contextRepository, UserRepository userRepository, UploadImageService uploadImageService) {
        this.jwtService = jwtService;
        this.contextRepository = contextRepository;
        this.userRepository = userRepository;
        this.uploadImageService = uploadImageService;
    }

    public Context find(Long id) throws ObjectNotFoundException {

        Optional<Context> obgOptional = contextRepository.findById(id);
        if (obgOptional.isEmpty()) {
            throw new ObjectNotFoundException("Object not found! Id: " + id + ", Type: " + Context.class.getName());
        }
        return obgOptional.get();
    }

    @Transactional
    public ContextDTO insert(
            String token,
            ContextRegisterDTO contextRegisterDTO
    ) throws IOException {

        User user = validateUser(token);

        Context context = contextRegisterDTO.contextRegisterDTOToContext();

        context.setCreator(user);

        if (contextRegisterDTO.getFile() == null
                || contextRegisterDTO.getFile().isEmpty()) {
            contextRepository.save(context);
            return new ContextDTO(context);
        }

        String uploadedImageUrl = null;

        try {

            MultipartFile file = contextRegisterDTO.getFile();

            String folder
                    = "user_" + user.getId()
                    + "/context_" + context.getName();

            uploadedImageUrl = uploadImageService.uploadFile(
                    folder,
                    file.getOriginalFilename(),
                    file.getInputStream(),
                    file.getSize(),
                    file.getContentType()
            );

            String imageBackup
                    = uploadImageService.generateBase64Thumbnail(file);

            context.setImageUrl(uploadedImageUrl);
            context.setImageBackup(imageBackup);

            contextRepository.save(context);
            contextRepository.flush();

        } catch (RuntimeException | IOException e) {
            deleteUploadedContextImageAfterFailure(uploadedImageUrl, "insert");
            logger.error(
                    "Erro ao inserir Context apos upload de imagem. userId={}, contextName={}, uploadedImageUrl={}",
                    user.getId(),
                    context.getName(),
                    uploadedImageUrl,
                    e
            );
            throw e;
        }

        return new ContextDTO(context);
    }

    public ContextDTO update(
            String token,
            ContextRegisterDTO contextRegisterDTO,
            Long id
    ) throws ObjectNotFoundException,
            InvalidUserException,
            IOException {

        User user = validateUser(token);

        Optional<Context> contextOptional = contextRepository.findById(id);

        if (!contextOptional.isPresent()) {
            throw new ObjectNotFoundException();
        }

        Context newObj = find(id);

        if (!newObj.getCreator().equals(user)) {
            throw new InvalidUserException(
                    "User: " + user.getName()
                    + " is not the owner of the context: "
                    + newObj.getName() + "."
            );
        }

        String oldImageUrl = newObj.getImageUrl();

        updateData(newObj, contextRegisterDTO.contextRegisterDTOToContext());

        boolean hasNewImage = contextRegisterDTO.getFile() != null
                && !contextRegisterDTO.getFile().isEmpty();

        if (!hasNewImage) {
            contextRepository.save(newObj);
            return new ContextDTO(newObj);
        }

        String uploadedImageUrl = null;

        try {

            MultipartFile file = contextRegisterDTO.getFile();

            String folder
                    = "user_" + user.getId()
                    + "/context_" + newObj.getName();

            uploadedImageUrl = uploadImageService.uploadFile(
                    folder,
                    file.getOriginalFilename(),
                    file.getInputStream(),
                    file.getSize(),
                    file.getContentType()
            );

            String imageBackup
                    = uploadImageService.generateBase64Thumbnail(file);

            newObj.setImageUrl(uploadedImageUrl);
            newObj.setImageBackup(imageBackup);

            contextRepository.save(newObj);
            contextRepository.flush();

        } catch (RuntimeException | IOException e) {
            deleteUploadedContextImageAfterFailure(uploadedImageUrl, "update");
            logger.error(
                    "Erro ao atualizar Context apos upload de imagem. userId={}, contextId={}, contextName={}, oldImageUrl={}, uploadedImageUrl={}",
                    user.getId(),
                    newObj.getId(),
                    newObj.getName(),
                    oldImageUrl,
                    uploadedImageUrl,
                    e
            );
            throw e;
        }

        deleteOldContextImage(oldImageUrl, newObj.getImageUrl());

        return new ContextDTO(newObj);
    }

    @Transactional
    public ContextDTO delete(String token, Long id) throws ObjectNotFoundException, InvalidUserException {
        User user = validateUser(token);

        Optional<Context> contextOptional = contextRepository.findById(id);

        if (!contextOptional.isPresent()) {
            throw new ObjectNotFoundException();
        }

        Context context = contextOptional.get();
        if (!context.getCreator().equals(user)) {
            throw new InvalidUserException("User: " + user.getName() + " is not the owner of the context: "
                    + context.getName() + ".");
        }

        ContextDTO deletedContextDTO = new ContextDTO(context);
        List<Challenge> challenges = new ArrayList<>(context.getChallenges());

        for (Challenge challenge : challenges) {
            challenge.getContexts().remove(context);
            context.getChallenges().remove(challenge);
            deleteImageFromMinio(challenge.getImageUrl(), "Challenge", challenge.getId());
            challengeRepository.delete(challenge);
        }

        deleteImageFromMinio(context.getImageUrl(), "Context", context.getId());
        contextRepository.delete(context);

        return deletedContextDTO;
    }

    public Page<Context> findContextsByParams(String email, String name, Pageable pageable) {
        if (email != null && name != null) {
            return contextRepository.findAllByCreatorEmailLikeAndNameStartsWithIgnoreCase(email, name, pageable);
        } else if (email != null) {
            return contextRepository.findAllByCreatorEmailEqualsIgnoreCase(email, pageable);
        } else if (name != null) {
            return contextRepository.findAllByNameStartsWithIgnoreCase(name, pageable);
        } else {
            return contextRepository.findAll(pageable);
        }
    }

    public List<ContextDTO> findContextsByCreator(String token) throws ObjectNotFoundException, InvalidUserException {
        User user = validateUser(token);

        List<Context> contextListByCreator = contextRepository.findContextsByCreator(user);
        if (contextListByCreator.isEmpty()) {
            throw new ObjectNotFoundException();
        }

        return contextListByCreator.stream().map(ContextDTO::new).collect(Collectors.toList());
    }

    private void updateData(Context newObj, Context obj) {
        newObj.setName(obj.getName());
        if (obj.getImageUrl() != null && !obj.getImageUrl().isBlank()) {
            newObj.setImageUrl(obj.getImageUrl());
        }
        newObj.setSoundUrl(obj.getSoundUrl());
        newObj.setVideoUrl(obj.getVideoUrl());
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

    private void deleteOldContextImage(String oldImageUrl, String newImageUrl) {
        if (oldImageUrl == null || oldImageUrl.isBlank() || oldImageUrl.equals(newImageUrl)) {
            return;
        }

        try {
            uploadImageService.deleteFileByUrl(oldImageUrl);
        } catch (RuntimeException e) {
            logger.warn("Nao foi possivel remover imagem antiga do Context no MinIO. imageUrl={}", oldImageUrl, e);
        }
    }

    private void deleteUploadedContextImageAfterFailure(String imageUrl, String operation) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        try {
            uploadImageService.deleteFileByUrl(imageUrl);
        } catch (RuntimeException e) {
            logger.warn(
                    "Nao foi possivel remover imagem recem-enviada do Context no MinIO apos falha no {}. imageUrl={}",
                    operation,
                    imageUrl,
                    e
            );
        }
    }

    private void deleteImageFromMinio(String imageUrl, String entityName, Long entityId) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        try {
            uploadImageService.deleteFileByUrl(imageUrl);
        } catch (RuntimeException e) {
            logger.warn(
                    "Nao foi possivel remover imagem do {} no MinIO. id={}, imageUrl={}",
                    entityName,
                    entityId,
                    imageUrl,
                    e
            );
        }
    }

}
