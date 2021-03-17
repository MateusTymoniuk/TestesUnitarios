package br.ce.wcaquino.servicos;

import static br.ce.wcaquino.builders.FilmeBuilder.umFilme;
import static br.ce.wcaquino.builders.FilmeBuilder.umFilmeSemEstoque;
import static br.ce.wcaquino.builders.UsuarioBuilder.umUsuario;
import static br.ce.wcaquino.matchers.CustomDateMatchers.caiEm;
import static br.ce.wcaquino.matchers.CustomDateMatchers.caiNumaSegunda;
import static br.ce.wcaquino.matchers.CustomDateMatchers.ehAmanha;
import static br.ce.wcaquino.matchers.CustomDateMatchers.ehHoje;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import br.ce.wcaquino.daos.LocacaoDAO;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;
import br.ce.wcaquino.utils.DataUtils;

public class LocacaoServiceTest {

	private LocacaoService locacaoService;
	
	LocacaoDAO dao;
	
	SPCService spc;

	ErrorCollector error = new ErrorCollector();

	@Before
	public void setUp() {
		locacaoService = new LocacaoService();
		
		dao = Mockito.mock(LocacaoDAO.class);
		spc = Mockito.mock(SPCService.class);

		locacaoService.setLocacaoDAO(dao);
		locacaoService.setSPCService(spc);
	}

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void deveAlugarFilmeComSucesso() throws Exception {
		assumeFalse(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));

		// cenário
		Usuario usuario = umUsuario().construir();
		Filme filme1 = umFilme().comPrecoLocacao(1.0).construir();
		Filme filme2 = umFilme().comPrecoLocacao(2.0).construir();
		List<Filme> listafilmes = Arrays.asList(filme1, filme2);

		// execução
		Locacao resultado = locacaoService.alugarFilme(usuario, listafilmes);

		// validação
		error.checkThat(resultado.getDataLocacao(), ehHoje());
		error.checkThat(resultado.getValor(), is(equalTo(3.0)));
		error.checkThat(resultado.getDataRetorno(), ehAmanha());
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
			throws FilmeSemEstoqueException, LocadoraException {
		assumeTrue(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));

		Usuario usuario = umUsuario().construir();
		List<Filme> filmes = Arrays.asList(umFilme().construir(), umFilme().construir());

		Locacao resultado = locacaoService.alugarFilme(usuario, filmes);

		boolean ehSegundaFeira = DataUtils.verificarDiaSemana(resultado.getDataRetorno(), Calendar.MONDAY);
		assertTrue(ehSegundaFeira);
		assertThat(resultado.getDataRetorno(), caiEm(Calendar.MONDAY));
		assertThat(resultado.getDataRetorno(), caiNumaSegunda());
	}
	
	@Test
	public void naoDeveAlugarFilmeParaNegativadoSPC() throws FilmeSemEstoqueException, LocadoraException {
		Usuario usuario = umUsuario().construir();
		List<Filme> filmes = Arrays.asList(umFilme().construir(), umFilme().construir());
		
		when(spc.isUsuarioNegativado(usuario)).thenReturn(true);

		exception.expect(LocadoraException.class);
		exception.expectMessage("Usuario negativado");
		
		locacaoService.alugarFilme(usuario, filmes);
	}
}
