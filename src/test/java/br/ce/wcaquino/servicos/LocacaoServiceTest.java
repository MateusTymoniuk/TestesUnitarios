package br.ce.wcaquino.servicos;

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

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;

import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;
import br.ce.wcaquino.utils.DataUtils;

public class LocacaoServiceTest {

	private LocacaoService locacaoService;
	
	ErrorCollector error = new ErrorCollector();

	@Before
	public void setUp() {
		locacaoService = new LocacaoService();
	}

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void deveAlugarFilmeComSucesso() throws Exception {
		assumeFalse(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));
		
		// cenário
		Usuario usuario = new Usuario("Mateus");
		Filme filme1 = new Filme("Filme 1", 1, 10.0);
		Filme filme2 = new Filme("Filme 2", 2, 20.0);
		List<Filme> listafilmes = Arrays.asList(filme1, filme2);

		// execução
		Locacao resultado = locacaoService.alugarFilme(usuario, listafilmes);

		// validação
		error.checkThat(resultado.getDataLocacao(), ehHoje());
		error.checkThat(resultado.getValor(), is(equalTo(30.0)));
		error.checkThat(resultado.getDataRetorno(), ehAmanha());
	}

	@Test(expected = FilmeSemEstoqueException.class)
	public void deveLancarExcecaoAoAlugarFilmeSemEstoque() throws Exception {
		// cenário
		Usuario usuario = new Usuario("Mateus");
		Filme filme1 = new Filme("Filme 1", 1, 10.0);
		Filme filme2 = new Filme("Filme 2", 0, 20.0);
		List<Filme> listafilmes = Arrays.asList(filme1, filme2);
		LocacaoService locacaoService = new LocacaoService();

		// execução
		locacaoService.alugarFilme(usuario, listafilmes);
	}

	@Test
	public void naoDeveAlugarFilmeSemUsuario() throws FilmeSemEstoqueException {
		// cenário
		Filme filme1 = new Filme("Filme 1", 1, 10.0);
		Filme filme2 = new Filme("Filme 2", 2, 20.0);
		List<Filme> listafilmes = Arrays.asList(filme1, filme2);

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
		Usuario usuario = new Usuario("Mateus");

		exception.expect(LocadoraException.class);
		exception.expectMessage("Filme nulo");

		// execução
		locacaoService.alugarFilme(usuario, null);
	}
	
	@Test
	public void dataDevolucaoDeveSerSegundaFeiraAoAlugarFilmeNoSabado() throws FilmeSemEstoqueException, LocadoraException {
		assumeTrue(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));
		
		Usuario usuario = new Usuario("Mateus");
		List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 1, 10.0), new Filme("Filme 2", 2, 20.0));
		
		Locacao resultado = locacaoService.alugarFilme(usuario, filmes);
		
		boolean ehSegundaFeira = DataUtils.verificarDiaSemana(resultado.getDataRetorno(), Calendar.MONDAY);
		assertTrue(ehSegundaFeira);
		assertThat(resultado.getDataRetorno(), caiEm(Calendar.MONDAY));
		assertThat(resultado.getDataRetorno(), caiNumaSegunda());
	}
}
