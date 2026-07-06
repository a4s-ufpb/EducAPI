package br.ufpb.dcx.apps4society.educapi.services;

import br.ufpb.dcx.apps4society.educapi.domain.AcaoAuditoria;
import br.ufpb.dcx.apps4society.educapi.domain.LogAuditoria;
import br.ufpb.dcx.apps4society.educapi.domain.User;
import br.ufpb.dcx.apps4society.educapi.repositories.LogAuditoriaRepository;
import br.ufpb.dcx.apps4society.educapi.utils.builder.UserBuilder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LogAuditoriaServiceTest {

    @Mock
    LogAuditoriaRepository logAuditoriaRepository;

    LogAuditoriaService logAuditoriaService;

    private final User ator = UserBuilder.anUser().withId(1L).buildOptionalUser().get();

    private final Pageable pageable = PageRequest.of(0, 20);

    @BeforeEach
    public void setUp() {
        logAuditoriaService = new LogAuditoriaService(logAuditoriaRepository);
    }

    @Test
    public void registrarSavesLogWithAtorDataTest() {

        logAuditoriaService.registrar(ator, AcaoAuditoria.CRIACAO_TEMA, "Context", 5L, "name=Animais");

        ArgumentCaptor<LogAuditoria> captor = ArgumentCaptor.forClass(LogAuditoria.class);
        verify(logAuditoriaRepository, times(1)).save(captor.capture());

        LogAuditoria saved = captor.getValue();
        assertEquals(ator.getEmail(), saved.getAtorEmail());
        assertEquals(ator.getName(), saved.getAtorNome());
        assertEquals(AcaoAuditoria.CRIACAO_TEMA, saved.getAcao());
        assertEquals("Context", saved.getTipoEntidade());
        assertEquals(5L, saved.getEntidadeId());
        assertEquals("name=Animais", saved.getDetalhes());
    }

    @Test
    public void registrarWithNullAtorUsesSistemaLabelTest() {

        logAuditoriaService.registrar(null, AcaoAuditoria.EXCLUSAO_USUARIO, "User", 9L, "seed inicial");

        ArgumentCaptor<LogAuditoria> captor = ArgumentCaptor.forClass(LogAuditoria.class);
        verify(logAuditoriaRepository).save(captor.capture());

        assertEquals("sistema", captor.getValue().getAtorEmail());
        assertEquals("Sistema", captor.getValue().getAtorNome());
    }

    @Test
    public void registrarNeverThrowsWhenRepositoryFailsTest() {

        when(logAuditoriaRepository.save(any(LogAuditoria.class))).thenThrow(new RuntimeException("db down"));

        assertDoesNotThrow(() ->
                logAuditoriaService.registrar(ator, AcaoAuditoria.EXCLUSAO_TEMA, "Context", 1L, "falha simulada"));
    }

    @Test
    public void buscarWithNoFiltersCallsFindAllTest() {

        Page<LogAuditoria> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(logAuditoriaRepository.findAll(pageable)).thenReturn(emptyPage);

        Page<LogAuditoria> result = logAuditoriaService.buscar(null, null, pageable);

        assertEquals(emptyPage, result);
        verify(logAuditoriaRepository).findAll(pageable);
    }

    @Test
    public void buscarWithOnlyEmailFiltersByEmailTest() {

        Page<LogAuditoria> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(logAuditoriaRepository.findAllByAtorEmailContainingIgnoreCase("user@educapi.com", pageable))
                .thenReturn(emptyPage);

        Page<LogAuditoria> result = logAuditoriaService.buscar("user@educapi.com", null, pageable);

        assertEquals(emptyPage, result);
        verify(logAuditoriaRepository).findAllByAtorEmailContainingIgnoreCase("user@educapi.com", pageable);
    }

    @Test
    public void buscarWithOnlyAcaoFiltersByAcaoTest() {

        Page<LogAuditoria> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(logAuditoriaRepository.findAllByAcao(AcaoAuditoria.PROMOCAO_ADMIN, pageable)).thenReturn(emptyPage);

        Page<LogAuditoria> result = logAuditoriaService.buscar(null, AcaoAuditoria.PROMOCAO_ADMIN, pageable);

        assertEquals(emptyPage, result);
        verify(logAuditoriaRepository).findAllByAcao(AcaoAuditoria.PROMOCAO_ADMIN, pageable);
    }

    @Test
    public void buscarWithEmailAndAcaoFiltersByBothTest() {

        Page<LogAuditoria> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(logAuditoriaRepository.findAllByAtorEmailContainingIgnoreCaseAndAcao(
                "user@educapi.com", AcaoAuditoria.EXCLUSAO_DESAFIO, pageable)).thenReturn(emptyPage);

        Page<LogAuditoria> result = logAuditoriaService.buscar("user@educapi.com", AcaoAuditoria.EXCLUSAO_DESAFIO, pageable);

        assertEquals(emptyPage, result);
        verify(logAuditoriaRepository).findAllByAtorEmailContainingIgnoreCaseAndAcao(
                "user@educapi.com", AcaoAuditoria.EXCLUSAO_DESAFIO, pageable);
    }

    @Test
    public void buscarWithBlankEmailIsTreatedAsNoFilterTest() {

        Page<LogAuditoria> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(logAuditoriaRepository.findAll(pageable)).thenReturn(emptyPage);

        Page<LogAuditoria> result = logAuditoriaService.buscar("   ", null, pageable);

        assertEquals(emptyPage, result);
        verify(logAuditoriaRepository).findAll(pageable);
    }
}
