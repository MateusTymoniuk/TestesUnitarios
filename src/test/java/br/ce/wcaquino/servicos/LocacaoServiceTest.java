package br.ce.wcaquino.servicos;

import br.ce.wcaquino.daos.LocacaoDAO;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;
import br.ce.wcaquino.utils.DataUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static br.ce.wcaquino.builders.FilmeBuilder.umFilme;
import static br.ce.wcaquino.builders.FilmeBuilder.umFilmeSemEstoque;
import static br.ce.wcaquino.builders.LocacaoBuilder.umaLocacao;
import static br.ce.wcaquino.builders.UsuarioBuilder.umUsuario;
import static br.ce.wcaquino.matchers.CustomDateMatchers.caiEm;
import static br.ce.wcaquino.matchers.CustomDateMatchers.caiNumaSegunda;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class LocacaoServiceTest {

    @InjectMocks
    private LocacaoService locacaoService;

    @Mock
    LocacaoDAO dao;

    @Mock
    SPCService spc;

    @Mock
    EmailService email;

    @Mock
    DataService dataService;

    @Rule
    public ErrorCollector error = new ErrorCollector();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void deveAlugarFilmeComSucesso() throws Exception {
        // cenário
        Usuario usuario = umUsuario().construir();
        Filme filme1 = umFilme().comPrecoLocacao(1.0).construir();
        Filme filme2 = umFilme().comPrecoLocacao(2.0).construir();
        List<Filme> listafilmes = Arrays.asList(filme1, filme2);

        when(dataService.getDataAtual()).thenReturn(DataUtils.obterData(28, 4, 2017));

        // execução
        Locacao resultado = locacaoService.alugarFilme(usuario, listafilmes);

        // validação
        error.checkThat(resultado.getValor(), is(equalTo(3.0)));
        error.checkThat(DataUtils.isMesmaData(resultado.getDataLocacao(), DataUtils.obterData(28, 4, 2017)), equalTo(true));
        error.checkThat(DataUtils.isMesmaData(resultado.getDataRetorno(), DataUtils.obterData(29, 4, 2017)), equalTo(true));
    }

    @Test(expected = FilmeSemEstoqueException.class)
    public void deveLancarExcecaoAoAlugarFilmeSemEstoque() throws Exception {
        // cenário
        Usuario usuario = umUsuario().construir();
        List<Filme> listafilmes = Arrays.asList(umFilmeSemEstoque().construir());
        LocacaoService locacaoService = new LocacaoService();

        // execução
        locacaoService.alugarFilme(usuario, listafilmes);
    }

    @Test
    public void naoDeveAlugarFilmeSemUsuario() throws FilmeSemEstoqueException {
        // cenário
        List<Filme> listafilmes = Arrays.asList(umFilme().construir());

        // execução
        try {
            locacaoService.alugarFilme(null, listafilmes);
            fail("Deveria ter lançado uma exceção");
        } catch (LocadoraException e) {
            assertThat(e.getMessage(), is(equalTo("Usuario nulo")));
        }
    }

    @Test
    public void naoDeveAlugarFilmeSemFilme() throws FilmeSemEstoqueException, LocadoraException {
        // cenário
        Usuario usuario = umUsuario().construir();

        exception.expect(LocadoraException.class);
        exception.expectMessage("Filme nulo");

        // execução
        locacaoService.alugarFilme(usuario, null);
    }

    @Test
    public void dataDevolucaoDeveSerSegundaFeiraAoAlugarFilmeNoSabado()
            throws Exception {
        Usuario usuario = umUsuario().construir();
        List<Filme> filmes = Arrays.asList(umFilme().construir(), umFilme().construir());

        when(dataService.getDataAtual()).thenReturn(DataUtils.obterData(29, 4, 2017));

        Locacao resultado = locacaoService.alugarFilme(usuario, filmes);

        boolean ehSegundaFeira = DataUtils.verificarDiaSemana(resultado.getDataRetorno(), Calendar.MONDAY);
        assertTrue(ehSegundaFeira);
        assertThat(resultado.getDataRetorno(), caiEm(Calendar.MONDAY));
        assertThat(resultado.getDataRetorno(), caiNumaSegunda());
    }

    @Test
    public void naoDeveAlugarFilmeParaNegativadoSPC() throws Exception {
        Usuario usuario = umUsuario().construir();
        List<Filme> filmes = Arrays.asList(umFilme().construir(), umFilme().construir());

        when(spc.isUsuarioNegativado(usuario)).thenReturn(true);

        try {
            locacaoService.alugarFilme(usuario, filmes);
            fail();
        } catch (LocadoraException e) {
            assertThat(e.getMessage(), equalTo("Usuario negativado"));
        }
        verify(spc).isUsuarioNegativado(usuario);
    }

    @Test
    public void deveEnviarEmailParaLocacoesAtrasadas() {
        Usuario usuarioAtrasado = umUsuario().comNome("Atrasado 1").construir();
        Usuario usuarioEmDia = umUsuario().comNome("Outro").construir();
        Usuario usuarioAtrasado2 = umUsuario().comNome("Atrasado 2").construir();
        List<Locacao> locacoes = Arrays.asList(umaLocacao()
                        .comUsuario(usuarioAtrasado)
                        .comAtraso()
                        .construir(),
                umaLocacao()
                        .comUsuario(usuarioEmDia)
                        .construir(),
                umaLocacao()
                        .comUsuario(usuarioAtrasado2)
                        .comAtraso()
                        .construir());
        when(dao.obterLocacoesPendentes()).thenReturn(locacoes);
        when(dataService.getDataAtual()).thenReturn(new Date());

        locacaoService.notificarAtrasos();

        verify(email).notificarAtraso(usuarioAtrasado);
        verify(email).notificarAtraso(usuarioAtrasado2);
        verify(email, never()).notificarAtraso(usuarioEmDia);
        verifyNoMoreInteractions(email);
    }

    @Test
    public void deveTratarErroNoSPC() throws Exception {
        Usuario usuario = umUsuario().construir();
        List<Filme> filmes = Arrays.asList(umFilme().construir());
        when(spc.isUsuarioNegativado(usuario)).thenThrow(new Exception("Falha no SPC"));
        exception.expect(LocadoraException.class);
        exception.expectMessage("Não foi possível verificar usuário no SPC");

        locacaoService.alugarFilme(usuario, filmes);
    }

    @Test
    public void deveProrrogarUmaLocacao() {
        // cenario
        Locacao locacao = umaLocacao().construir();
        int quantidadeDeDiasProrrogacao = 3;

        // execucao
        locacaoService.prorrogarLocacao(locacao, quantidadeDeDiasProrrogacao);

        // verificacao
        ArgumentCaptor<Locacao> argCapt = ArgumentCaptor.forClass(Locacao.class);
        verify(dao).salvar(argCapt.capture());

        Locacao locacaoRetornada = argCapt.getValue();
        error.checkThat(locacaoRetornada.getValor(), equalTo(12.0));
    }
}
