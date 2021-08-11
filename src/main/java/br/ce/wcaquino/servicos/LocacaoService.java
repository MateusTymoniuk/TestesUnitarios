package br.ce.wcaquino.servicos;

import br.ce.wcaquino.daos.LocacaoDAO;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;
import br.ce.wcaquino.utils.DataUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static br.ce.wcaquino.utils.DataUtils.adicionarDias;

public class LocacaoService {

    private LocacaoDAO dao;

    private SPCService spc;

    private EmailService email;

    private DataService dataService;

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

        boolean isUsuarioNegativado;
        try {
            isUsuarioNegativado = spc.isUsuarioNegativado(usuario);
        } catch (Exception e) {
            throw new LocadoraException("Não foi possível verificar usuário no SPC");
        }

        if (isUsuarioNegativado) {
            throw new LocadoraException("Usuario negativado");
        }

        Locacao locacao = new Locacao();
        locacao.setFilmes(filmes);
        locacao.setUsuario(usuario);
        locacao.setDataLocacao(dataService.getDataAtual());
        locacao.setValor(calculaValorLocacao(filmes));

        // Entrega no dia seguinte
        Date dataEntrega = dataService.getDataAtual();
        dataEntrega = adicionarDias(dataEntrega, 1);
        boolean isDataEntregaDomingo = DataUtils.verificarDiaSemana(dataEntrega, Calendar.SUNDAY);
        if (isDataEntregaDomingo) {
            dataEntrega = adicionarDias(dataEntrega, 1);
        }
        locacao.setDataRetorno(dataEntrega);

        // Salvando a locacao...
        dao.salvar(locacao);

        return locacao;
    }

    private Double calculaValorLocacao(List<Filme> filmes) {
        Double valorTotal = 0d;

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

            valorTotal += filmes.get(i).getPrecoLocacao() - desconto;
        }
        return valorTotal;
    }

    public void notificarAtrasos() {
        List<Locacao> locacoesPendentes = dao.obterLocacoesPendentes();
        for (Locacao locacao : locacoesPendentes) {
            if (locacao.getDataRetorno().before(dataService.getDataAtual())) {
                email.notificarAtraso(locacao.getUsuario());
            }
        }
    }

    public void prorrogarLocacao(Locacao locacao, int dias) {
        Locacao novaLocacao = new Locacao();
        novaLocacao.setUsuario(locacao.getUsuario());
        novaLocacao.setFilmes(locacao.getFilmes());
        novaLocacao.setDataLocacao(dataService.getDataAtual());
        novaLocacao.setDataRetorno(DataUtils.obterDataComDiferencaDias(dias));
        novaLocacao.setValor(locacao.getValor() * dias);
        dao.salvar(novaLocacao);
    }
}