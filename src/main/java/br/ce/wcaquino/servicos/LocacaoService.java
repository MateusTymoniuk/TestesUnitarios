package br.ce.wcaquino.servicos;

import static br.ce.wcaquino.utils.DataUtils.adicionarDias;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;
import br.ce.wcaquino.utils.DataUtils;

public class LocacaoService {

	public Locacao alugarFilme(Usuario usuario, List<Filme> filmes) throws FilmeSemEstoqueException, LocadoraException {
		if (filmes == null) {
			throw new LocadoraException("Filme nulo");
		}

		if (usuario == null) {
			throw new LocadoraException("Usuario nulo");
		}

		for (Filme filme : filmes) {
			if (filme.getEstoque() == 0) {
				throw new FilmeSemEstoqueException("Filme sem estoque");
			}
		}

		Locacao locacao = new Locacao();
		locacao.setFilmes(filmes);
		locacao.setUsuario(usuario);
		locacao.setDataLocacao(new Date());

		Double precoLocacaoTotal = 0d;

		for (int i = 0; i < filmes.size(); i++) {

			Double desconto = 0d;

			switch (i) {
			case 2:
				desconto = filmes.get(i).getPrecoLocacao() * 0.25;
				break;
			case 3:
				desconto = filmes.get(i).getPrecoLocacao() * 0.50;
				break;
			case 4:
				desconto = filmes.get(i).getPrecoLocacao() * 0.75;
				break;
			case 5:
				desconto = filmes.get(i).getPrecoLocacao();
				break;
			default:
				break;
			}

			precoLocacaoTotal += filmes.get(i).getPrecoLocacao() - desconto;
		}

		locacao.setValor(precoLocacaoTotal);

		// Entrega no dia seguinte
		Date dataEntrega = new Date();
		dataEntrega = adicionarDias(dataEntrega, 1);
		boolean isDataEntregaDomingo = DataUtils.verificarDiaSemana(dataEntrega, Calendar.SUNDAY);
		if(isDataEntregaDomingo) {
			dataEntrega = adicionarDias(dataEntrega, 1);
		}
		locacao.setDataRetorno(dataEntrega);

		// Salvando a locacao...
		// TODO adicionar método para salvar

		return locacao;
	}

}