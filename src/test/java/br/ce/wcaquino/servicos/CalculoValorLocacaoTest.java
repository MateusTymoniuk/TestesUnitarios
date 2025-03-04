package br.ce.wcaquino.servicos;

import br.ce.wcaquino.daos.LocacaoDAO;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;
import br.ce.wcaquino.utils.DataUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static br.ce.wcaquino.builders.FilmeBuilder.umFilme;
import static br.ce.wcaquino.builders.UsuarioBuilder.umUsuario;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class CalculoValorLocacaoTest {

    @Parameter
    public List<Filme> filmes;

    @Parameter(value = 1)
    public Double valorLocacao;

    @Parameter(value = 2)
    public String cenario;

    @InjectMocks
    private LocacaoService locacaoService;

    @Mock
    LocacaoDAO dao;

    @Mock
    SPCService spc;

    @Mock
    DataService dataService;

    static Filme filme1 = umFilme().construir();
    static Filme filme2 = umFilme().construir();
    static Filme filme3 = umFilme().construir();
    static Filme filme4 = umFilme().construir();
    static Filme filme5 = umFilme().construir();
    static Filme filme6 = umFilme().construir();
    static Filme filme7 = umFilme().construir();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Parameters(name = "{2}")
    public static Collection<Object[]> getParametros() {
        return Arrays.asList(new Object[][]{
                {
                        Arrays.asList(filme1, filme2),
                        8.0,
                        "2 Filmes - sem desconto"
                },
                {
                        Arrays.asList(filme1, filme2, filme3),
                        11.0,
                        "3 Filmes - 25% desconto"
                },
                {
                        Arrays.asList(filme1, filme2, filme3, filme4),
                        13.0,
                        "4 Filmes - 50% desconto"
                },
                {
                        Arrays.asList(filme1, filme2, filme3, filme4, filme5),
                        14.0,
                        "5 Filmes - 75% desconto"
                },
                {
                        Arrays.asList(filme1, filme2, filme3, filme4, filme5, filme6),
                        14.0,
                        "6 Filmes - 100% desconto"
                },
                {
                        Arrays.asList(filme1, filme2, filme3, filme4, filme5, filme6, filme7),
                        18.0,
                        "7 Filmes - sem desconto"
                }
        });
    }

    @Test
    public void deveCalcularValorLocacaoComDevidosDescontos() throws FilmeSemEstoqueException, LocadoraException {
        Usuario usuario = umUsuario().construir();
        when(dataService.getDataAtual()).thenReturn(DataUtils.obterData(28, 4, 2017));

        Locacao resultado = locacaoService.alugarFilme(usuario, filmes);

        assertThat(resultado.getValor(), is(equalTo(valorLocacao)));
    }
}
